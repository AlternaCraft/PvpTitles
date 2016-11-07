/*
 * Copyright (C) 2016 AlternaCraft
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Backend.EbeanTables.PlayerPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.SignPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import com.alternacraft.pvptitles.Exceptions.DBException;
import static com.alternacraft.pvptitles.Exceptions.DBException.UNKNOWN_ERROR;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoardData;
import com.alternacraft.pvptitles.Misc.PluginLogs;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.StrUtils;
import com.alternacraft.pvptitles.Misc.TagsClass;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
    private static final String CREATE_PLAYERMETA = "insert into PlayerMeta(psid, lastLogin) values (?,?)";

    private static final String UPDATE_PLAYER_SERVERID = "update PlayerServer set serverID=? "
            + "where playerUUID like ? AND serverID=-1";
    private static final String UPDATE_PLAYERMETA_PLAYEDTIME = "update PlayerMeta set "
            + "playedTime=playedTime+? where psid=?";
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
    private Connection mysql;

    /**
     * Constructor de la clase
     *
     * @param plugin Plugin
     * @param mysql Connection
     */
    public DatabaseManagerMysql(PvpTitles plugin, Connection mysql) {
        this.plugin = plugin;
        this.mysql = mysql;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    private short checkPlayerExists(OfflinePlayer pl, String w) throws DBException {
        short psid = -1;

        if (pl == null || !MySQLConnection.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("Null player?", (pl == null));
            data.put("MySQL connection?", MySQLConnection.isConnected(false));

            throw new DBException("Error checking if player is registered",
                    DBException.DB_METHOD.PLAYER_CONNECTION, data);
        }

        String uuid = pl.getUniqueId().toString();

        try {
            PreparedStatement playerExists = mysql.prepareStatement(PLAYER_EXISTS);
            playerExists.setString(1, uuid);
            playerExists.setInt(2, plugin.getManager().params.getMultiS());
            ResultSet rs = playerExists.executeQuery();

            if (!rs.next()) { // No existe
                PreparedStatement registraPlayer = mysql.prepareStatement(CREATE_PLAYER);
                registraPlayer.setString(1, uuid);
                registraPlayer.setInt(2, plugin.getManager().params.getMultiS());
                registraPlayer.executeUpdate();

                rs = playerExists.executeQuery();
                if (rs.next()) {
                    psid = rs.getShort("id");
                    java.util.Date utilDate = new java.util.Date();
                    java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                    PreparedStatement registraPlayerMeta = mysql.prepareStatement(CREATE_PLAYERMETA);
                    registraPlayerMeta.setInt(1, psid);
                    registraPlayerMeta.setDate(2, sqlDate);
                    registraPlayerMeta.executeUpdate();
                }
            } else {
                psid = rs.getShort("id");

                // Fix
                PreparedStatement modID = mysql.prepareStatement(UPDATE_PLAYER_SERVERID);
                modID.setInt(1, plugin.getManager().params.getMultiS());
                modID.setString(2, uuid);
                modID.executeUpdate();
            }

            if (plugin.getManager().params.isMw_enabled()) {
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
            throw new DBException("Error checking if player exists",
                    DBException.DB_METHOD.PLAYER_CONNECTION, ex.getMessage());
        }

        return psid;
    }

    @Override
    public void playerConnection(Player player) throws DBException {
        short psid = checkPlayerExists(player, player.getWorld().getName());

        try {
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            PreparedStatement modFecha = mysql.prepareStatement(UPDATE_PLAYERMETA_LASTLOGIN);
            modFecha.setDate(1, sqlDate);
            modFecha.setInt(2, psid);
            modFecha.executeUpdate();

            LoggerManager.logDebugInfo("Update last login: " + modFecha.toString());
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_CONNECTION, ex.getMessage());
        }
    }

    @Override
    public void savePlayerFame(UUID playerUUID, int fame, String w) throws DBException {
        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);

        short psid = checkPlayerExists(pl, w);

        try {
            if (plugin.getManager().params.isMw_enabled()) {
                if (w == null && !pl.isOnline()) {
                    HashMap<String, Object> data = new HashMap();
                    data.put("Player", pl.getName());
                    data.put("Player Online", pl.isOnline());
                    data.put("Method world", w);
                    data.put("Multiworld enabled", plugin.getManager().params.isMw_enabled());

                    throw new DBException(DBException.MULTIWORLD_ERROR,
                            DBException.DB_METHOD.PLAYER_FAME_SAVING, data);
                }
                String world = (w == null) ? ((Player) pl).getWorld().getName() : w;

                PreparedStatement updateFame = mysql.prepareStatement(UPDATE_MWPLAYER_POINTS);
                updateFame.setInt(1, fame);
                updateFame.setInt(2, psid);
                updateFame.setString(3, world);
                updateFame.executeUpdate();
                LoggerManager.logDebugInfo("Update mwplayer points: " + updateFame.toString());
            } else {
                PreparedStatement updateFame = mysql.prepareStatement(UPDATE_PLAYERMETA_POINTS);
                updateFame.setInt(1, fame);
                updateFame.setInt(2, psid);
                updateFame.executeUpdate();
                LoggerManager.logDebugInfo("Update player points: " + updateFame.toString());
            }
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_FAME_SAVING, ex.getMessage());
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String w) throws DBException {
        int fama = 0;

        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);
        short psid = checkPlayerExists(pl, w);

        try {
            if (plugin.getManager().params.isMw_enabled()) {
                if (w == null && !pl.isOnline()) {
                    HashMap<String, Object> data = new HashMap();
                    data.put("Player", pl.getName());
                    data.put("Player Online", pl.isOnline());
                    data.put("Method world", w);
                    data.put("Multiworld enabled", plugin.getManager().params.isMw_enabled());

                    throw new DBException(DBException.MULTIWORLD_ERROR,
                            DBException.DB_METHOD.PLAYER_FAME_LOADING, data);
                }

                String world = (w == null) ? ((Player) pl).getWorld().getName() : w;

                PreparedStatement getFame = mysql.prepareStatement(MWPLAYER_POINTS);
                getFame.setInt(1, psid);
                getFame.setString(2, world);
                ResultSet rs = getFame.executeQuery();
                LoggerManager.logDebugInfo("Get mwplayer fame: " + getFame.toString());

                if (rs.next()) {
                    fama = rs.getInt("points");
                }
            } else {
                PreparedStatement getFame = mysql.prepareStatement(PLAYER_POINTS);
                getFame.setInt(1, psid);
                ResultSet rs = getFame.executeQuery();
                LoggerManager.logDebugInfo("Get player fame: " + getFame.toString());

                if (rs.next()) {
                    fama = rs.getInt("points");
                }
            }
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_FAME_LOADING, ex.getMessage());
        }

        return fama;
    }

    @Override
    public void savePlayedTime(TimedPlayer tPlayer) throws DBException {
        short psid = checkPlayerExists(tPlayer.getOfflinePlayer(), null);

        try {
            int time = tPlayer.getTotalOnline();
            PreparedStatement playedTime = mysql.prepareStatement(UPDATE_PLAYERMETA_PLAYEDTIME);
            playedTime.setInt(1, time);
            playedTime.setInt(2, psid);
            playedTime.executeUpdate();
            LoggerManager.logDebugInfo("Save played time: " + playedTime.toString());

        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_TIME_SAVING, ex.getMessage());
        }
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) throws DBException {
        int time = 0;

        short psid = checkPlayerExists(plugin.getServer().getOfflinePlayer(playerUUID), null);

        try {
            PreparedStatement playedTime = mysql.prepareStatement(PLAYEDTIME);
            playedTime.setInt(1, psid);
            ResultSet rs = playedTime.executeQuery();
            LoggerManager.logDebugInfo("Load played time: " + playedTime.toString());

            if (rs.next()) {
                time = rs.getInt("playedTime");
            }
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_TIME_LOADING, ex.getMessage());
        }

        return time;
    }

    @Override
    public ArrayList getTopPlayers(short cant, String server) throws DBException {
        ArrayList rankedPlayers = new ArrayList();

        if (!MySQLConnection.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("MySQL connection?", false);

            throw new DBException(DBException.TOP_PLAYERS_ERROR,
                    DBException.DB_METHOD.PLAYERS_TOP, data);
        }

        HashMap<Short, List<String>> servidores = plugin.getManager().servers.get(server);
        String sql;

        if (plugin.getManager().params.isMw_enabled()) {
            sql = TOPMWPLAYERS;
        } else {
            sql = TOPPLAYERS;
        }

        // <editor-fold defaultstate="collapsed" desc="QUERY MAKER">
        // Checker mw-filter        
        String mundos = "";
        if (plugin.getManager().params.isMw_enabled() && !plugin.getManager().params.showOnLeaderBoard()) {
            List<String> worlds_disabled = plugin.getManager().params.getAffectedWorlds();

            StringBuilder buf = new StringBuilder();
            for (String world : worlds_disabled) {
                buf.append(" AND ").append("worldName != '").append(world).append('\'');
            }
            if (!worlds_disabled.isEmpty()) {
                mundos = buf.toString();
            }
        }

        if (!server.equals("") && servidores != null && plugin.getManager().servers.containsKey(server)) {
            // Si hay un '-1' recojo los jugadores de todos los servers
            if (servidores.size() > 0 && !servidores.containsKey(-1)) {
                sql += " where";
                for (Short serverID : servidores.keySet()) {
                    sql += " (serverID = " + serverID;

                    if (plugin.getManager().params.isMw_enabled() && !servidores.get(serverID).isEmpty()) {
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
            sql += " where serverID=" + plugin.getManager().params.getMultiS();

            if (plugin.getManager().params.isMw_enabled() && servidores != null && servidores.get(plugin.getManager().params.getMultiS()) != null) {
                sql += " AND (";
                for (String mundoElegido : servidores.get(plugin.getManager().params.getMultiS())) {
                    sql += "worldName like '" + mundoElegido + "' OR ";
                }
                sql = sql.substring(0, sql.length() - 4) + ')';
            }
        }

        sql += mundos + " order by points DESC limit " + cant;
        // </editor-fold>
        LoggerManager.logDebugInfo("Top players: " + sql);

        try {
            PlayerFame pf;

            for (ResultSet rs = mysql.createStatement().executeQuery(sql); rs.next(); rankedPlayers.add(pf)) {
                pf = new PlayerFame(rs.getString("playerUUID"), rs.getInt("points"),
                        rs.getInt("playedTime"), this.plugin);
                pf.setServer(rs.getShort("serverID"));

                if (plugin.getManager().params.isMw_enabled()) {
                    pf.setFame(rs.getInt("PlayerWorld.points"));
                    pf.setWorld(rs.getString("worldName"));
                }
            }
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYERS_TOP, ex.getMessage());
        }

        return rankedPlayers;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="BOARDS...">  
    @Override
    public void registraBoard(SignBoard sb) throws DBException {

        if (!MySQLConnection.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("MySQL connection?", false);

            throw new DBException(DBException.SAVING_BOARD_ERROR,
                    DBException.DB_METHOD.BOARD_SAVING, data);
        }

        Location l = sb.getData().getLocation();

        try {
            PreparedStatement saveBoard = mysql.prepareStatement(CREATE_BOARD);
            saveBoard.setString(1, sb.getData().getNombre());
            saveBoard.setString(2, sb.getData().getModelo());
            saveBoard.setString(3, sb.getData().getServer());
            saveBoard.setString(4, sb.getData().getOrientacion());
            saveBoard.setShort(5, sb.getData().getPrimitiveBlockface());
            saveBoard.setShort(6, plugin.getManager().params.getMultiS());
            saveBoard.setString(7, l.getWorld().getName());
            saveBoard.setInt(8, l.getBlockX());
            saveBoard.setInt(9, l.getBlockY());
            saveBoard.setInt(10, l.getBlockZ());

            saveBoard.executeUpdate();
            LoggerManager.logDebugInfo("Save board: " + saveBoard.toString());
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_SAVING, ex.getMessage());
        }
    }

    @Override
    public void modificaBoard(Location l) throws DBException {
        if (!MySQLConnection.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("MySQL connection?", false);

            throw new DBException(DBException.UPDATING_BOARD_ERROR,
                    DBException.DB_METHOD.BOARD_UPDATING, data);
        }

        try {
            PreparedStatement updBoard = mysql.prepareStatement(SAVE_BOARD);
            updBoard.setInt(1, plugin.getManager().params.getMultiS());
            updBoard.setString(2, l.getWorld().getName());
            updBoard.setInt(3, l.getBlockX());
            updBoard.setInt(4, l.getBlockY());
            updBoard.setInt(5, l.getBlockZ());
            updBoard.executeUpdate();
            LoggerManager.logDebugInfo("Update board: " + updBoard.toString());
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_UPDATING, ex.getMessage());
        }
    }

    @Override
    public void borraBoard(Location l) throws DBException {
        if (!MySQLConnection.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("MySQL connection?", false);

            throw new DBException(DBException.REMOVING_BOARD_ERROR,
                    DBException.DB_METHOD.BOARD_REMOVING, data);
        }

        try {
            PreparedStatement delBoard = mysql.prepareStatement(DELETE_BOARD);
            delBoard.setInt(1, plugin.getManager().params.getMultiS());
            delBoard.setString(2, l.getWorld().getName());
            delBoard.setInt(3, l.getBlockX());
            delBoard.setInt(4, l.getBlockY());
            delBoard.setInt(5, l.getBlockZ());
            delBoard.executeUpdate();
            LoggerManager.logDebugInfo("Delete board: " + delBoard.toString());
        } catch (SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_REMOVING, ex.getMessage());
        }
    }

    @Override
    public ArrayList<SignBoardData> buscaBoards() throws DBException {
        ArrayList<SignBoardData> sbd = new ArrayList();

        if (!MySQLConnection.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("MySQL connection?", false);

            throw new DBException(DBException.SEARCHING_BOARD_ERROR,
                    DBException.DB_METHOD.BOARD_SEARCHING, data);
        }

        try {
            PreparedStatement searchBoards = mysql.prepareStatement(SEARCH_BOARDS);
            searchBoards.setInt(1, plugin.getManager().params.getMultiS());
            ResultSet rs = searchBoards.executeQuery();
            LoggerManager.logDebugInfo("Search boards: " + searchBoards.toString());

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
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_SEARCHING, ex.getMessage());
        }

        return sbd;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="OTHERS...">  
    @Override
    public String getServerName(short id) {
        String nombre = "";

        if (!MySQLConnection.isConnected(true)) {
            return nombre;
        }

        try {
            PreparedStatement serverName = mysql.prepareStatement(SERVER_NAME);
            serverName.setInt(1, id);
            ResultSet rs = serverName.executeQuery();
            LoggerManager.logDebugInfo("Server name: " + serverName.toString());

            while (rs.next()) {
                nombre = rs.getString("nombreS");
                break;
            }
        } catch (SQLException ex) {
            LoggerManager.logError(ex.getMessage(), ex);
        }

        return nombre;
    }

    @Override
    public int purgeData(int q) {
        int contador = 0;

        if (!MySQLConnection.isConnected(true)) {
            return contador;
        }

        String data = "select id, playerUUID, lastLogin from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String purge = "delete from PlayerServer where id=?";

        PluginLogs l = new PluginLogs(plugin, "user_changes.txt");

        try {
            ResultSet rs = mysql.createStatement().executeQuery(data);
            rs.next();
            do {
                short id = rs.getShort("id");
                String strUUID = rs.getString("playerUUID");
                Date lastLogin = rs.getDate("lastLogin");

                Calendar date = new GregorianCalendar(1978, Calendar.JANUARY, 1);
                lastLogin = (lastLogin == null) ? date.getTime() : lastLogin;

                if (!plugin.getManager().params.getNoPurge().contains(strUUID)) {
                    Calendar lastLoginDate = new GregorianCalendar();
                    lastLoginDate.setTime(lastLogin);
                    lastLoginDate.add(6, q);

                    Date today = new Date();
                    Calendar actualDate = new GregorianCalendar();
                    actualDate.setTime(today);

                    if (lastLoginDate.before(actualDate)) {
                        lastLoginDate.setTime(lastLogin); // Remove q time
                        PreparedStatement delUser = mysql.prepareStatement(purge);
                        delUser.setShort(1, id);
                        delUser.executeUpdate();
                        contador++;

                        // Log settings
                        UUID uuid = UUID.fromString(strUUID);
                        int time = (int) ((actualDate.getTimeInMillis() - lastLoginDate.getTimeInMillis()) / 1000);
                        l.addMessage("Player " + Bukkit.getOfflinePlayer(uuid).getName()
                                + " has been removed. AFK time: "
                                + StrUtils.splitToComponentTimes(time));
                    }
                }
            } while (rs.next());
        } catch (SQLException ex) {
            LoggerManager.logError(ex.getMessage(), ex);
        }

        if (contador > 0) {
            l.export(true);
        }

        return contador;
    }

    @Override
    public void DBExport(String filename) {
        String ruta = new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append( // Separador
                        filename).toString();

        short serverID = plugin.getManager().params.getMultiS();

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
            LoggerManager.logError(ex.getMessage(), ex);
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

        List<String> sql = UtilsFile.getFileLines(ruta);

        for (String consulta : sql) {
            try {
                PreparedStatement ps = mysql.prepareStatement(consulta);
                ps.executeUpdate();
            } catch (SQLException ex) {
                LoggerManager.logError(ex.getMessage(), ex);
                return false;
            }
        }

        return true;
    }

    @Override
    public String getDefaultFImport() {
        return FILENAME_IMPORT;
    }

    @Override
    public String getDefaultFExport() {
        return FILENAME_EXPORT;
    }

    @Override
    public int repair() {
        int q = 0;
        boolean repaired = false;

        if (!MySQLConnection.isConnected(true)) {
            return q;
        }

        String data = "select id, playerUUID, points, playedTime from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String update = "update PlayerMeta set points=?,playedTime=? where psid=?";

        PluginLogs l = new PluginLogs(plugin, "db_changes.txt");

        try {
            ResultSet rs = mysql.createStatement().executeQuery(data);
            rs.next();
            do {
                short id = rs.getShort("id");

                String strUUID = rs.getString("playerUUID");
                int points = rs.getInt("points");
                int playedTime = rs.getInt("playedTime");

                if (points < 0 || playedTime < 0) {
                    UUID uuid = UUID.fromString(strUUID);
                    String name = Bukkit.getOfflinePlayer(uuid).getName();

                    if (points < 0) {
                        l.addMessage("[BAD FAME] Player \"" + name + "\" had "
                                + points + " and it was changed to 0");
                        points = 0;
                    } 
                    
                    if (playedTime < 0) {
                        l.addMessage("[BAD PLAYED TIME] Player \"" + name + "\" had "
                                + playedTime + " and it was changed to 0");
                        playedTime = 0;
                    }

                    PreparedStatement fix = mysql.prepareStatement(update);
                    fix.setInt(1, points);
                    fix.setInt(2, playedTime);
                    fix.setInt(3, id);
                    fix.executeUpdate();
                    
                    repaired = true;
                    q++;
                }
            } while (rs.next());
        } catch (SQLException ex) {
            LoggerManager.logError(ex.getMessage(), ex);
        }

        if (repaired) {            
            l.export(true);
        }
        
        return q;
    }

    @Override
    public void updateConnection(Object mysql) {
        this.mysql = (Connection) mysql;
    }
    //</editor-fold>
}
