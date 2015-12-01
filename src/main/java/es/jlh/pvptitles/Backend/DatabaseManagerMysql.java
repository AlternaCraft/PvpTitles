package es.jlh.pvptitles.Backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.TagsClass;
import es.jlh.pvptitles.Misc.UtilFile;
import es.jlh.pvptitles.Backend.EbeanTables.PlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.SignPT;
import es.jlh.pvptitles.Objects.LBSigns.LBData;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Objects.TimedPlayer;
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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AlternaCraft
 */
public class DatabaseManagerMysql implements DatabaseManager {

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private PvpTitles pt;
    private Connection mysql;

    public DatabaseManagerMysql(PvpTitles mpt, Connection mysql) {
        this.pt = mpt;
        this.mysql = mysql;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    private short checkPlayerExists(Player pl) {
        String uuid = pl.getUniqueId().toString();

        String playerExists = "select id from PlayerServer where playerUUID like '"
                + uuid + "' AND (serverID=" + pt.cm.params.getMultiS() + " OR serverID=-1)";

        short psid = -1;

        try {
            ResultSet rs = mysql.createStatement().executeQuery(playerExists);

            // No existe
            if (!rs.next()) {
                String creaPlayer = "insert into PlayerServer(playerUUID, serverID) values (?,?)";
                PreparedStatement registraPlayer = mysql.prepareStatement(creaPlayer);
                registraPlayer.setString(1, uuid);
                registraPlayer.setInt(2, pt.cm.params.getMultiS());
                registraPlayer.executeUpdate();

                rs = mysql.createStatement().executeQuery(playerExists);
                if (rs.next()) {
                    psid = rs.getShort("id");
                    String creaMeta = "insert into PlayerMeta(psid) values (" + psid + ")";
                    mysql.createStatement().executeUpdate(creaMeta);
                }
            } else {
                psid = rs.getShort("id");

                // Fix
                String updateServerID = "update PlayerServer set serverID=? "
                        + "where playerUUID like ? AND serverID=-1";
                PreparedStatement modID = mysql.prepareStatement(updateServerID);
                modID.setInt(1, pt.cm.params.getMultiS());
                modID.setString(2, uuid);
                modID.executeUpdate();
            }

            if (pt.cm.params.isMw_enabled()) {
                String world = pl.getWorld().getName();
                String MWPlayerExists = "select psid from PlayerWorld where psid=" + psid;
                rs = mysql.createStatement().executeQuery(MWPlayerExists);
                if (!rs.next()) {
                    String creaWPlayer = "insert into PlayerWorld(psid, worldName) "
                            + "values (" + psid + ", '" + world + "')";
                    mysql.createStatement().executeUpdate(creaWPlayer);
                }
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        return psid;
    }

    @Override
    public void PlayerConnection(Player player) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        short psid = checkPlayerExists(player);

        try {
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            String upDate = "update PlayerMeta set lastLogin=? where psid=" + psid;
            PreparedStatement modFecha = mysql.prepareStatement(upDate);
            modFecha.setDate(1, sqlDate);
            modFecha.executeUpdate();
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
    }

    @Override
    public void savePlayerFame(UUID playerUUID, int fame) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        short id = checkPlayerExists(Bukkit.getPlayer(playerUUID));

        try {
            Player pl = Bukkit.getServer().getPlayer(playerUUID);

            if (pt.cm.params.isMw_enabled()) {
                String setPoints = "update PlayerWorld set points=? where psid="
                        + id + " AND worldName=?";

                PreparedStatement updateFame = mysql.prepareStatement(setPoints);
                updateFame.setInt(1, fame);
                updateFame.setString(2, pl.getWorld().getName());
                updateFame.executeUpdate();
            } else {
                String setPoints = "update PlayerMeta set points=? where psid=" + id;

                PreparedStatement updateFame = mysql.prepareStatement(setPoints);
                updateFame.setInt(1, fame);
                updateFame.executeUpdate();
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String world) {
        int fama = 0;

        if (!MySQLConnection.isConnected()) {
            return fama;
        }

        short id = checkPlayerExists(Bukkit.getPlayer(playerUUID));

        try {
            Player pl = Bukkit.getServer().getPlayer(playerUUID);

            if (pt.cm.params.isMw_enabled()) {
                String selectedWorld = (world == null) ? pl.getWorld().getName() : world;

                String points = "select points from PlayerWorld where psid=" + id
                        + " AND worldName like '" + selectedWorld + "'";

                ResultSet rs = mysql.createStatement().executeQuery(points);

                if (rs.next()) {
                    fama = rs.getInt("points");
                }
            } else {
                String points = "select points from PlayerMeta where psid=" + id;

                ResultSet rs = mysql.createStatement().executeQuery(points);
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
    public void savePlayedTime(TimedPlayer player) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        short psid = checkPlayerExists(Bukkit.getPlayer(player.getUniqueId()));

        try {
            int time = player.getTotalOnline();
            String update = "update PlayerMeta set playedTime = playedTime + " + time
                    + " where psid=" + psid;
            mysql.createStatement().executeUpdate(update);
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) {
        int time = 0;

        if (!MySQLConnection.isConnected()) {
            return time;
        }

        short psid = checkPlayerExists(Bukkit.getPlayer(playerUUID));

        String consulta = "select playedTime from PlayerMeta where psid=" + psid;

        try {
            ResultSet rs = mysql.createStatement().executeQuery(consulta);
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

        HashMap<Short, List<String>> servidores = pt.cm.servers.get(server);
        String sql;

        if (pt.cm.params.isMw_enabled()) {
            sql = "select serverID, playerUUID, points, worldName from PlayerServer inner join PlayerWorld on id=psid";
        } else {
            sql = "select serverID, playerUUID, points from PlayerServer inner join PlayerMeta on id=psid";
        }

        if (!"".equals(server) && servidores != null && pt.cm.servers.containsKey(server)) {
            // Si hay un '-1' recojo los jugadores de todos los servers
            if (servidores.size() > 0 && !servidores.containsKey(-1)) {
                sql += " where";
                for (Iterator<Short> iterator = servidores.keySet().iterator(); iterator.hasNext();) {
                    Short next = iterator.next();
                    sql += " (serverID = " + next;

                    if (pt.cm.params.isMw_enabled() && !servidores.get(next).isEmpty()) {
                        sql += " AND";
                        for (Iterator<String> mundo = servidores.get(next).iterator(); mundo.hasNext();) {
                            String mundoElegido = mundo.next();
                            sql += " worldName like '" + mundoElegido + "' OR";
                        }
                        sql = sql.substring(0, sql.length() - 3);
                    }

                    sql += ") OR";
                }
                sql = sql.substring(0, sql.length() - 3);
            }
        } else {
            sql += " where serverID=" + pt.cm.params.getMultiS();

            if (pt.cm.params.isMw_enabled() && servidores != null && servidores.get(pt.cm.params.getMultiS()) != null) {
                sql += " AND";
                for (Iterator<String> mundo = servidores.get(pt.cm.params.getMultiS()).iterator(); mundo.hasNext();) {
                    String mundoElegido = mundo.next();
                    sql += " worldName like '" + mundoElegido + "' OR";
                }
                sql = sql.substring(0, sql.length() - 3);
            }
        }

        sql += " order by points DESC limit " + cant;

        try {
            List<String> worlds_disabled = pt.cm.params.getAffectedWorlds();
            PlayerFame pf;

            for (ResultSet rs = mysql.createStatement().executeQuery(sql); rs.next(); rankedPlayers.add(pf)) {
                pf = new PlayerFame(rs.getString("playerUUID"), rs.getInt("points"), this.pt);
                pf.setServer(rs.getShort("serverID"));

                if (pt.cm.params.isMw_enabled()) {
                    if (!pt.cm.params.showOnLeaderBoard() && worlds_disabled.contains(rs.getString("worldName"))) {
                        continue;
                    }

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
    public void registraCartel(String nombre, String modelo, String server,
            Location l, String orientacion, short blockface) {

        if (!MySQLConnection.isConnected()) {
            return;
        }

        String cartel = "insert into Signs(name, signModel, dataModel, orientation, "
                + "blockface, serverID, world, x, y, z) values (?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement regCartel = mysql.prepareStatement(cartel);
            regCartel.setString(1, nombre);
            regCartel.setString(2, modelo);
            regCartel.setString(3, server);
            regCartel.setString(4, orientacion);
            regCartel.setShort(5, blockface);
            regCartel.setShort(6, pt.cm.params.getMultiS());
            regCartel.setString(7, l.getWorld().getName());
            regCartel.setInt(8, l.getBlockX());
            regCartel.setInt(9, l.getBlockY());
            regCartel.setInt(10, l.getBlockZ());

            regCartel.executeUpdate();
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
    }

    @Override
    public void modificaCartel(Location l) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String updateServerID = "update Signs set serverID=? where serverID=-1 "
                + "AND world=? AND x=? AND y=? AND z=?";
        try {
            PreparedStatement actCartel = mysql.prepareStatement(updateServerID);
            actCartel.setInt(1, pt.cm.params.getMultiS());
            actCartel.setString(2, l.getWorld().getName());
            actCartel.setInt(3, l.getBlockX());
            actCartel.setInt(4, l.getBlockY());
            actCartel.setInt(5, l.getBlockZ());
            actCartel.executeUpdate();
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
    }

    @Override
    public void borraCartel(Location l) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String cartel = "delete from Signs where serverID=? AND world=? "
                + "AND x=? AND y=? AND z=?";
        try {
            PreparedStatement regCartel = mysql.prepareStatement(cartel);
            regCartel.setInt(1, pt.cm.params.getMultiS());
            regCartel.setString(2, l.getWorld().getName());
            regCartel.setInt(3, l.getBlockX());
            regCartel.setInt(4, l.getBlockY());
            regCartel.setInt(5, l.getBlockZ());
            regCartel.executeUpdate();
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
    }

    @Override
    public ArrayList buscaCarteles() {
        String sql = "select * from Signs where serverID = " + pt.cm.params.getMultiS();
        ArrayList sd = new ArrayList();

        if (!MySQLConnection.isConnected()) {
            return sd;
        }

        try {
            LBData sdc;
            for (ResultSet rs = mysql.createStatement().executeQuery(sql); rs.next(); sd.add(sdc)) {
                String nombre = rs.getString("name");
                String modelo = rs.getString("signModel");
                String server = rs.getString("dataModel");
                String orientacion = rs.getString("orientation");
                short blockface = rs.getShort("blockface");
                String world = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                Location l = new Location(pt.getServer().getWorld(world), x, y, z);
                sdc = new LBData(nombre, modelo, server, l);
                sdc.setOrientacion(orientacion);
                sdc.setBlockface(blockface);
            }

        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }
        return sd;
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
            String sql = "select name from Servers where id=" + id;

            ResultSet rs = mysql.createStatement().executeQuery(sql);
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

                if (!pt.cm.params.getNoPurge().contains(nombre)) {
                    Calendar cFile = new GregorianCalendar();
                    cFile.setTime(fechaMod);
                    cFile.add(6, pt.cm.params.getTimeP());

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
    public void DBExport() {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "database.json").toString();

        short serverID = pt.cm.params.getMultiS();

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
                World w = Bukkit.getWorld(rs.getString("world"));
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
        UtilFile.writeFile(ruta, gson.toJson(el));
    }

    @Override
    public boolean DBImport() {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "database.sql").toString();

        if (!UtilFile.exists(ruta)) {
            return false;
        }

        String[] sql = UtilFile.readFile(ruta).split("\n");
        
        for (String consulta : sql) {
            try {
                mysql.createStatement().executeUpdate(consulta);                
            } catch (SQLException ex) {
                PvpTitles.logError(ex.getMessage(), ex);
                return false;
            }
        }
        
        return true;
    }
    //</editor-fold>
}
