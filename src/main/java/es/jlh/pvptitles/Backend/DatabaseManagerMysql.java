package es.jlh.pvptitles.Backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.TagsClass;
import es.jlh.pvptitles.Misc.UtilsFile;
import es.jlh.pvptitles.Backend.EbeanTables.PlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.SignPT;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoard;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Managers.Timer.TimedPlayer;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AlternaCraft
 */
public class DatabaseManagerMysql implements DatabaseManager {

    private static final String FILENAME_IMPORT = "database.sql";
    private static final String FILENAME_EXPORT = "database.json";

    // Players
    private static final String PLAYER_EXISTS = "select id from PlayerServer "
            + "where playerUUID like ? AND (serverID=? OR serverID=-1)";
    private static final String MWPLAYER_EXISTS = "select psid from PlayerWorld where psid=?";

    private static final String PLAYER_POINTS = "select points from PlayerMeta where psid=?";
    private static final String MWPLAYER_POINTS = "select points from PlayerWorld "
            + "where psid=? AND worldName like ?";

    private static final String PLAYEDTIME = "select playedTime from PlayerMeta where psid=?";

    private static final String TOPPLAYERS = "select serverID, playerUUID, points, playedTime "
            + "from PlayerServer inner join PlayerMeta on id=psid";
    private static final String TOPMWPLAYERS = "select serverID, playerUUID, PlayerWorld.points, worldName, playedTime "
            + "from PlayerServer inner join PlayerWorld on id=PlayerWorld.psid inner join PlayerMeta on id=PlayerMeta.psid";

    private static final String CREATE_PLAYER = "insert into PlayerServer(playerUUID, serverID) values (?,?)";
    private static final String CREATE_MWPLAYER = "insert into PlayerWorld(psid, worldName) values (?,?)";
    private static final String CREATE_PLAYERMETA = "insert into PlayerMeta(psid) values (?)";

    private static final String UPDATE_PLAYER_SERVERID = "update PlayerServer set serverID=? "
            + "where playerUUID like ? AND serverID=-1";
    private static final String UPDATE_PLAYERMETA_PLAYEDTIME = "update PlayerMeta set "
            + "playedTime = playedTime+? where psid=?";
    private static final String UPDATE_PLAYERMETA_LASTLOGIN = "update PlayerMeta set "
            + "lastLogin=? where psid=?";
    private static final String UPDATE_PLAYERMETA_POINTS = "update PlayerMeta set "
            + "points=? where psid=?";
    private static final String UPDATE_MWPLAYER_POINTS = "update PlayerWorld set "
            + "points=? where psid=? AND worldName=?";

    // Signs
    private static final String CREATE_BOARD = "insert into Signs(name, signModel, dataModel, orientation, "
            + "blockface, serverID, world, x, y, z) values (?,?,?,?,?,?,?,?,?,?)";
    private static final String SAVE_BOARD = "update Signs set serverID=? where serverID=-1 "
            + "AND world=? AND x=? AND y=? AND z=?";
    private static final String DELETE_BOARD = "delete from Signs where serverID=? AND world=? "
            + "AND x=? AND y=? AND z=?";
    private static final String SEARCH_BOARDS = "select * from Signs where serverID=?";

    // Others
    private static final String SERVER_NAME = "select name from Servers where id=?";

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private final PvpTitles plugin;
    private final Connection mysql;

    /**
     * Constructor de la clase
     *
     * @param plugin Plugin
     * @param mysql
     */
    public DatabaseManagerMysql(PvpTitles plugin, Connection mysql) {
        this.plugin = plugin;
        this.mysql = mysql;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    private short checkPlayerExists(OfflinePlayer pl, String w) {
        short psid = -1;
        if (pl == null || !MySQLConnection.isConnected()) {
            return psid;
        }
        String uuid = pl.getUniqueId().toString();

        try {
            PreparedStatement playerExists = mysql.prepareStatement(PLAYER_EXISTS);
            playerExists.setString(1, uuid);
            playerExists.setInt(2, plugin.manager.params.getMultiS());
            ResultSet rs = playerExists.executeQuery();

            if (!rs.next()) { // No existe
                PreparedStatement registraPlayer = mysql.prepareStatement(CREATE_PLAYER);
                registraPlayer.setString(1, uuid);
                registraPlayer.setInt(2, plugin.manager.params.getMultiS());
                registraPlayer.executeUpdate();

                rs = playerExists.executeQuery();
                if (rs.next()) {
                    psid = rs.getShort("id");

                    PreparedStatement registraPlayerMeta = mysql.prepareStatement(CREATE_PLAYERMETA);
                    registraPlayerMeta.setInt(1, psid);
                    registraPlayerMeta.executeUpdate();
                }
            } else {
                psid = rs.getShort("id");

                // Fix
                PreparedStatement modID = mysql.prepareStatement(UPDATE_PLAYER_SERVERID);
                modID.setInt(1, plugin.manager.params.getMultiS());
                modID.setString(2, uuid);
                modID.executeUpdate();
            }

            if (plugin.manager.params.isMw_enabled()) {
                if (w == null && !pl.isOnline()) {
                    return -1;
                }
                String world = (w == null) ? ((Player) pl).getWorld().getName() : w;

                PreparedStatement mwplayerExists = mysql.prepareStatement(MWPLAYER_EXISTS);
                mwplayerExists.setInt(1, psid);
                rs = mwplayerExists.executeQuery();

                if (!rs.next()) {
                    PreparedStatement registraMWPlayer = mysql.prepareStatement(CREATE_MWPLAYER);
                    registraMWPlayer.setInt(1, psid);
                    registraMWPlayer.setString(2, world);
                    registraMWPlayer.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return psid;
    }

    @Override
    public boolean playerConnection(Player player) {
        short psid = checkPlayerExists(player, player.getWorld().getName());
        if (psid == -1) {
            return false;
        }

        try {
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            PreparedStatement modFecha = mysql.prepareStatement(UPDATE_PLAYERMETA_LASTLOGIN);
            modFecha.setDate(1, sqlDate);
            modFecha.setInt(2, psid);
            modFecha.executeUpdate();
            PvpTitles.logDebugInfo("Update last login: " + modFecha.toString());
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean savePlayerFame(UUID playerUUID, int fame, String w) {
        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);

        short psid = checkPlayerExists(pl, w);
        if (psid == -1) {
            return false;
        }

        try {
            if (plugin.manager.params.isMw_enabled()) {
                if (w == null && !pl.isOnline()) {
                    return false;
                }
                String world = (w == null) ? ((Player) pl).getWorld().getName() : w;

                PreparedStatement updateFame = mysql.prepareStatement(UPDATE_MWPLAYER_POINTS);
                updateFame.setInt(1, fame);
                updateFame.setInt(2, psid);
                updateFame.setString(3, world);
                updateFame.executeUpdate();
                PvpTitles.logDebugInfo("Update mwplayer points: " + updateFame.toString());
            } else {
                PreparedStatement updateFame = mysql.prepareStatement(UPDATE_PLAYERMETA_POINTS);
                updateFame.setInt(1, fame);
                updateFame.setInt(2, psid);
                updateFame.executeUpdate();
                PvpTitles.logDebugInfo("Update player points: " + updateFame.toString());
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String w) {
        int fama = 0;
        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);
        short psid = checkPlayerExists(pl, w);
        if (psid == -1) {
            return fama;
        }

        try {
            if (plugin.manager.params.isMw_enabled()) {
                if (w == null && !pl.isOnline()) {
                    return fama;
                }
                String world = (w == null) ? ((Player) pl).getWorld().getName() : w;

                PreparedStatement getFame = mysql.prepareStatement(MWPLAYER_POINTS);
                getFame.setInt(1, psid);
                getFame.setString(2, world);
                ResultSet rs = getFame.executeQuery();
                PvpTitles.logDebugInfo("Get mwplayer fame: " + getFame.toString());

                if (rs.next()) {
                    fama = rs.getInt("points");
                }
            } else {
                PreparedStatement getFame = mysql.prepareStatement(PLAYER_POINTS);
                getFame.setInt(1, psid);
                ResultSet rs = getFame.executeQuery();
                PvpTitles.logDebugInfo("Get player fame: " + getFame.toString());

                if (rs.next()) {
                    fama = rs.getInt("points");
                }
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return fama;
    }

    @Override
    public boolean savePlayedTime(TimedPlayer player) {
        short psid = checkPlayerExists(plugin.getServer().getPlayer(player.getUniqueId()), null);
        if (psid == -1) {
            return false;
        }

        try {
            int time = player.getTotalOnline();
            PreparedStatement playedTime = mysql.prepareStatement(UPDATE_PLAYERMETA_PLAYEDTIME);
            playedTime.setInt(1, time);
            playedTime.setInt(2, psid);
            playedTime.executeUpdate();
            PvpTitles.logDebugInfo("Save played time: " + playedTime.toString());

        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) {
        int time = 0;

        short psid = checkPlayerExists(plugin.getServer().getOfflinePlayer(playerUUID), null);
        if (psid == -1) {
            return time;
        }

        try {
            PreparedStatement playedTime = mysql.prepareStatement(PLAYEDTIME);
            playedTime.setInt(1, psid);
            ResultSet rs = playedTime.executeQuery();
            PvpTitles.logDebugInfo("Load played time: " + playedTime.toString());

            if (rs.next()) {
                time = rs.getInt("playedTime");
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return time;
    }

    @Override
    public ArrayList getTopPlayers(short cant, String server) {
        ArrayList rankedPlayers = new ArrayList();

        if (!MySQLConnection.isConnected()) {
            return rankedPlayers;
        }

        HashMap<Short, List<String>> servidores = plugin.manager.servers.get(server);
        String sql;

        if (plugin.manager.params.isMw_enabled()) {
            sql = TOPMWPLAYERS;
        } else {
            sql = TOPPLAYERS;
        }

        // <editor-fold defaultstate="collapsed" desc="QUERY MAKER">
        // Checker mw-filter        
        String mundos = "";        
        if (plugin.manager.params.isMw_enabled() && !plugin.manager.params.showOnLeaderBoard()) {
            List<String> worlds_disabled = plugin.manager.params.getAffectedWorlds();

            StringBuilder buf = new StringBuilder();
            for (String world : worlds_disabled) {
                buf.append(" AND ").append("worldName != '").append(world).append('\'');
            }
            if (!worlds_disabled.isEmpty()) {
                mundos = buf.toString();
            }
        }

        if (!server.equals("") && servidores != null && plugin.manager.servers.containsKey(server)) {
            // Si hay un '-1' recojo los jugadores de todos los servers
            if (servidores.size() > 0 && !servidores.containsKey(-1)) {
                sql += " where";
                for (Short serverID : servidores.keySet()) {
                    sql += " (serverID = " + serverID;

                    if (plugin.manager.params.isMw_enabled() && !servidores.get(serverID).isEmpty()) {
                        sql += " AND (";
                        for (String mundoElegido : servidores.get(serverID)) {
                            sql += "worldName like '" + mundoElegido + "' OR ";
                        }
                        sql = sql.substring(0, sql.length() - 4) + ')';
                    }

                    sql += ") OR";
                }
                sql = sql.substring(0, sql.length() - 3);
            }
        } else {
            sql += " where serverID=" + plugin.manager.params.getMultiS();

            if (plugin.manager.params.isMw_enabled() && servidores != null && servidores.get(plugin.manager.params.getMultiS()) != null) {
                sql += " AND (";
                for (String mundoElegido : servidores.get(plugin.manager.params.getMultiS())) {
                    sql += "worldName like '" + mundoElegido + "' OR ";
                }
                sql = sql.substring(0, sql.length() - 4) + ')';
            }
        }

        sql += mundos + " order by points DESC limit " + cant;
        // </editor-fold>
        PvpTitles.logDebugInfo("Top players: " + sql);

        try {
            PlayerFame pf;

            for (ResultSet rs = mysql.createStatement().executeQuery(sql); rs.next(); rankedPlayers.add(pf)) {
                if (plugin.manager.params.isMw_enabled()) {
                    pf = new PlayerFame(rs.getString("playerUUID"), rs.getInt("PlayerWorld.points"),
                            rs.getInt("playedTime"), this.plugin);
                } else {
                    pf = new PlayerFame(rs.getString("playerUUID"), rs.getInt("points"),
                            rs.getInt("playedTime"), this.plugin);
                }

                pf.setServer(rs.getShort("serverID"));

                if (plugin.manager.params.isMw_enabled()) {
                    pf.setWorld(rs.getString("worldName"));
                }
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return rankedPlayers;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="SIGNS...">  
    @Override
    public boolean registraBoard(SignBoard sb) {

        if (!MySQLConnection.isConnected()) {
            return false;
        }

        Location l = sb.getData().getLocation();

        try {
            PreparedStatement saveBoard = mysql.prepareStatement(CREATE_BOARD);
            saveBoard.setString(1, sb.getData().getNombre());
            saveBoard.setString(2, sb.getData().getModelo());
            saveBoard.setString(3, sb.getData().getServer());
            saveBoard.setString(4, sb.getData().getOrientacion());
            saveBoard.setShort(5, sb.getData().getPrimitiveBlockface());
            saveBoard.setShort(6, plugin.manager.params.getMultiS());
            saveBoard.setString(7, l.getWorld().getName());
            saveBoard.setInt(8, l.getBlockX());
            saveBoard.setInt(9, l.getBlockY());
            saveBoard.setInt(10, l.getBlockZ());

            saveBoard.executeUpdate();
            PvpTitles.logDebugInfo("Save board: " + saveBoard.toString());
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean modificaBoard(Location l) {
        if (!MySQLConnection.isConnected()) {
            return false;
        }

        try {
            PreparedStatement updBoard = mysql.prepareStatement(SAVE_BOARD);
            updBoard.setInt(1, plugin.manager.params.getMultiS());
            updBoard.setString(2, l.getWorld().getName());
            updBoard.setInt(3, l.getBlockX());
            updBoard.setInt(4, l.getBlockY());
            updBoard.setInt(5, l.getBlockZ());
            updBoard.executeUpdate();
            PvpTitles.logDebugInfo("Update board: " + updBoard.toString());
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean borraBoard(Location l) {
        if (!MySQLConnection.isConnected()) {
            return false;
        }

        try {
            PreparedStatement delBoard = mysql.prepareStatement(DELETE_BOARD);
            delBoard.setInt(1, plugin.manager.params.getMultiS());
            delBoard.setString(2, l.getWorld().getName());
            delBoard.setInt(3, l.getBlockX());
            delBoard.setInt(4, l.getBlockY());
            delBoard.setInt(5, l.getBlockZ());
            delBoard.executeUpdate();
            PvpTitles.logDebugInfo("Delete board: " + delBoard.toString());
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
            return false;
        }

        return true;
    }

    @Override
    public ArrayList<SignBoardData> buscaBoards() {
        ArrayList<SignBoardData> sbd = new ArrayList();

        if (!MySQLConnection.isConnected()) {
            return sbd;
        }

        try {
            PreparedStatement searchBoards = mysql.prepareStatement(SEARCH_BOARDS);
            searchBoards.setInt(1, plugin.manager.params.getMultiS());
            ResultSet rs = searchBoards.executeQuery();
            PvpTitles.logDebugInfo("Search boards: " + searchBoards.toString());

            SignBoardData sdc;

            for (; rs.next(); sbd.add(sdc)) {
                String nombre = rs.getString("name");
                String modelo = rs.getString("signModel");
                String server = rs.getString("dataModel");
                String orientacion = rs.getString("orientation");
                short blockface = rs.getShort("blockface");

                String world = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                Location l = new Location(plugin.getServer().getWorld(world), x, y, z);

                sdc = new SignBoardData(nombre, modelo, server, l);
                sdc.setOrientacion(orientacion);
                sdc.setBlockface(blockface);
            }

        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return sbd;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="OTHERS...">  
    @Override
    public String getServerName(short id) {
        String nombre = "";

        if (!MySQLConnection.isConnected()) {
            return nombre;
        }

        try {
            PreparedStatement serverName = mysql.prepareStatement(SERVER_NAME);
            serverName.setInt(1, id);
            ResultSet rs = serverName.executeQuery();
            PvpTitles.logDebugInfo("Server name: " + serverName.toString());

            while (rs.next()) {
                nombre = rs.getString("nombreS");
                break;
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return nombre;
    }

    @Override
    public int purgeData() {
        int contador = 0;

        if (!MySQLConnection.isConnected()) {
            return contador;
        }

        String data = "select id, playerUUID, lastLogin from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String purge = "delete from PlayerServer where id=?";

        try {
            ResultSet rs = mysql.createStatement().executeQuery(data);
            rs.next();
            do {
                short id = rs.getShort("id");
                String nombre = rs.getString("playerUUID");
                Date fechaMod = rs.getDate("lastLogin");

                if (!plugin.manager.params.getNoPurge().contains(nombre)) {
                    Calendar cFile = new GregorianCalendar();
                    cFile.setTime(fechaMod);
                    cFile.add(6, plugin.manager.params.getTimeP());

                    Date hoy = new Date();
                    Calendar cHoy = new GregorianCalendar();
                    cHoy.setTime(hoy);

                    if (cFile.before(cHoy)) {
                        PreparedStatement borraUsr = mysql.prepareStatement(purge);
                        borraUsr.setShort(1, id);
                        borraUsr.executeUpdate();
                        contador++;
                    }
                }
            } while (rs.next());
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
        return contador;
    }

    @Override
    public void DBExport(String filename) {
        String ruta = new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append( // Separador
                        filename).toString();

        short serverID = plugin.manager.params.getMultiS();

        List<PlayerPT> plClass = new ArrayList();
        List<WorldPlayerPT> plwClass = new ArrayList();
        List<SignPT> signClass = new ArrayList();

        String players = "select * from PlayerServer inner join PlayerMeta "
                + "on id=psid where serverID=" + serverID;
        String playersPerWorld = "select * from PlayerServer inner join PlayerWorld "
                + "on id=psid where serverID=" + serverID;
        String signs = "select * from Signs where serverID=" + serverID;
        try {
            ResultSet rs = mysql.createStatement().executeQuery(players);

            while (rs.next()) {
                PlayerPT pl = new PlayerPT();
                pl.setPlayerUUID(rs.getString("playerUUID"));
                pl.setPoints(rs.getInt("points"));
                pl.setPlayedTime(rs.getInt("playedTime"));
                pl.setLastLogin(rs.getDate("lastLogin"));
                plClass.add(pl);
            }

            rs = mysql.createStatement().executeQuery(playersPerWorld);

            while (rs.next()) {
                WorldPlayerPT plWorld = new WorldPlayerPT();
                plWorld.setPlayerUUID(rs.getString("playerUUID"));
                plWorld.setPoints(rs.getInt("points"));
                plWorld.setWorld(rs.getString("worldName"));
                plwClass.add(plWorld);
            }

            rs = mysql.createStatement().executeQuery(signs);

            while (rs.next()) {
                World w = plugin.getServer().getWorld(rs.getString("world"));
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");

                SignPT sg = new SignPT();
                sg.setName(rs.getString("name"));
                sg.setModel(rs.getString("signModel"));
                sg.setOrientation(rs.getString("orientation"));
                sg.setBlockface(rs.getShort("blockface"));
                sg.setLocation(new Location(w, x, y, z));
                signClass.add(sg);
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        // Estilo
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JSONObject jo = new JSONObject();

        JSONArray jsPlayers = new JSONArray();
        JSONArray jsSigns = new JSONArray();
        JSONArray jsWorldPlayers = new JSONArray();

        for (PlayerPT next : plClass) {
            jsPlayers.add(TagsClass.createPlayer(next));
        }
        jo.put("Players", jsPlayers);

        for (SignPT next : signClass) {
            jsSigns.add(TagsClass.createSign(next));
        }
        jo.put("Signs", jsSigns);

        for (WorldPlayerPT next : plwClass) {
            jsWorldPlayers.add(TagsClass.createPlayerW(next));
        }
        jo.put("PlayersPerWorld", jsWorldPlayers);

        // Escribo el fichero
        JsonElement el = parser.parse(jo.toJSONString());
        UtilsFile.writeFile(ruta, gson.toJson(el));
    }

    @Override
    public boolean DBImport(String filename) {
        String ruta = new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append( // Separador
                        filename).toString();

        if (!UtilsFile.exists(ruta)) {
            return false;
        }

        String[] sql = UtilsFile.readFile(ruta).split("\n");

        for (String consulta : sql) {
            try {
                PreparedStatement ps = mysql.prepareStatement(consulta);
                ps.executeUpdate();
            } catch (SQLException ex) {
                PvpTitles.logError(ex.getMessage(), ex);
                return false;
            }
        }

        return true;
    }

    @Override
    public String getDefaultFImport() {
        return this.FILENAME_IMPORT;
    }

    @Override
    public String getDefaultFExport() {
        return this.FILENAME_EXPORT;
    }
    //</editor-fold>
}
