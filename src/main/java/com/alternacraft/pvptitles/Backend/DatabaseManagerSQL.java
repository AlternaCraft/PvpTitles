/*
 * Copyright (C) 2017 AlternaCraft
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
import com.alternacraft.pvptitles.Backend.SQLConnection.STATUS_AVAILABLE;
import com.alternacraft.pvptitles.Exceptions.DBException;
import static com.alternacraft.pvptitles.Exceptions.DBException.UNKNOWN_ERROR;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.DBLoader;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.PERFORMANCE;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoardData;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.PluginLog;
import com.alternacraft.pvptitles.Misc.StrUtils;
import com.alternacraft.pvptitles.Misc.TagsClass;
import static com.alternacraft.pvptitles.Misc.TagsClass.PlayerWorld.world;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class DatabaseManagerSQL implements DatabaseManager {

    private static final String FILENAME_IMPORT = "database.sql";
    private static final String FILENAME_EXPORT = "database.json";

    // Players
    private static final String PLAYER_EXISTS = "select id from PlayerServer "
            + "where playerUUID like ? AND (serverID=? OR serverID=-1)";
    private static final String MWPLAYER_EXISTS = "select psid from PlayerWorld "
            + "where psid=? AND worldName like ?";

    private static final String PLAYER_POINTS = "select points from PlayerMeta where psid=?";
    private static final String MWPLAYER_POINTS = "select points from PlayerWorld "
            + "where psid=? AND worldName like ?";

    private static final String PLAYEDTIME = "select playedTime from PlayerMeta where psid=?";

    private static final String TOPPLAYERS = "select serverID, playerUUID, points, playedTime "
            + "from PlayerServer inner join PlayerMeta on id=psid";
    private static final String TOPMWPLAYERS = "select serverID, playerUUID, PlayerWorld.points, worldName, playedTime "
            + "from PlayerServer inner join PlayerWorld on id=PlayerWorld.psid inner join PlayerMeta on id=PlayerMeta.psid";

    private static final String CREATE_PLAYER = "insert into PlayerServer(id, playerUUID, serverID) values (?,?,?)";
    private static final String CREATE_MWPLAYER = "insert into PlayerWorld(psid, worldName) values (?,?)";

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
    // Optimization
    private final Map<String, Integer> playerids = new HashMap();
    private final Map<String, List<String>> playerworlds = new HashMap();

    private final PvpTitles plugin;
    private SQLConnection sql;

    /**
     * Constructor de la clase
     *
     * @param plugin Plugin
     * @param sql Connection
     */
    public DatabaseManagerSQL(PvpTitles plugin, SQLConnection sql) {
        this.plugin = plugin;
        this.sql = sql;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    private int checkPlayerExists(OfflinePlayer pl, String w) throws DBException {
        int psid = -1;

        if (pl == null || !sql.isConnected(true)) {
            HashMap data = new HashMap();
            data.put("Null player", (pl == null));
            data.put(DBLoader.tipo.name() + " connection", 
                    sql.status == STATUS_AVAILABLE.CONNECTED);

            throw new DBException("Error checking if player is registered",
                    DBException.DB_METHOD.PLAYER_CONNECTION, data);
        }

        String uuid = pl.getUniqueId().toString();

        try {            
            try (PreparedStatement playerExists = sql.getConnection()
                    .prepareStatement(PLAYER_EXISTS)) {
                playerExists.setString(1, uuid);
                playerExists.setInt(2, plugin.getManager().params.getMultiS());                

                try (ResultSet rs = playerExists.executeQuery()) {
                    if (!rs.next()) {
                        try (ResultSet rss = sql.getConnection().createStatement()
                                .executeQuery("select max(id) from PlayerServer")) {
                            psid = (rss.next()) ? rss.getInt("max(id)") + 1 : 0;
                        }                        
                        try (PreparedStatement registraPlayer = sql.getConnection()
                                .prepareStatement(CREATE_PLAYER)) {
                            registraPlayer.setInt(1, psid);
                            registraPlayer.setString(2, uuid);
                            registraPlayer.setInt(3, plugin.getManager().params.getMultiS());
                            registraPlayer.executeUpdate();
                        }
                    } else {
                        psid = rs.getInt("id");

                        // Fix
                        try (PreparedStatement modID = sql.getConnection()
                                .prepareStatement(UPDATE_PLAYER_SERVERID)) {
                            modID.setInt(1, plugin.getManager().params.getMultiS());
                            modID.setString(2, uuid);
                            modID.executeUpdate();
                        }
                    }
                }
            }

            if (plugin.getManager().params.isMw_enabled()) {
                if (w == null && !pl.isOnline()) {
                    return -1;
                }
                final String world = (w == null) ? ((Player) pl).getWorld().getName() : w;

                try (PreparedStatement mwplayerExists = sql.getConnection()
                        .prepareStatement(MWPLAYER_EXISTS)) {
                    mwplayerExists.setInt(1, psid);
                    mwplayerExists.setString(2, world);
                    try (ResultSet rs = mwplayerExists.executeQuery()) {
                        if (!rs.next()) {
                            try (PreparedStatement registraMWPlayer = sql.getConnection()
                                    .prepareStatement(CREATE_MWPLAYER)) {
                                registraMWPlayer.setInt(1, psid);
                                registraMWPlayer.setString(2, world);
                                registraMWPlayer.executeUpdate();
                            }
                        }
                    }
                }
                
                // Controlar que mundos han sido comprobados
                if (this.playerworlds.containsKey(uuid) 
                        && !this.playerworlds.get(uuid).contains(world)) {
                    this.playerworlds.get(uuid).add(world);
                } else {
                    this.playerworlds.put(uuid, new ArrayList() {
                        {
                            this.add(world);
                        }
                    });
                }
            }
        } catch (final SQLException ex) {
            throw new DBException("Error checking if player exists",
                    DBException.DB_METHOD.PLAYER_CONNECTION, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        this.playerids.put(uuid, psid);

        return psid;
    }

    private int playerID(String uuid, String world) throws DBException {
        int id = (this.playerids.containsKey(uuid)) ? this.playerids.get(uuid) : -1;
        if (plugin.getManager().params.isMw_enabled() && world != null) {
            if (!this.playerworlds.containsKey(uuid) 
                    || !this.playerworlds.get(uuid).contains(world)) {
                id = -1;
            }
        }
        return id;                
    }

    @Override
    public void playerConnection(Player player) throws DBException {
        PERFORMANCE.start("Player connection");

        int psid = checkPlayerExists(player, player.getWorld().getName());
        try {
            try (PreparedStatement modFecha = sql.getConnection()
                    .prepareStatement(UPDATE_PLAYERMETA_LASTLOGIN)) {
                modFecha.setDate(1, new java.sql.Date(System.currentTimeMillis()));
                modFecha.setInt(2, psid);
                modFecha.executeUpdate();
                CustomLogger.logDebugInfo("Update last login: " + modFecha.toString());
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_CONNECTION, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        PERFORMANCE.recordValue("Player connection");
    }

    @Override
    public void savePlayerFame(UUID playerUUID, int fame, String w) throws DBException {
        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);
        
        if (pl == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_FAME_SAVING);
        }

        int psid = playerID(playerUUID.toString(), w);
        if (psid == -1) {
            psid = checkPlayerExists(pl, w);
        }

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
                try (PreparedStatement updateFame = sql.getConnection()
                        .prepareStatement(UPDATE_MWPLAYER_POINTS)) {
                    updateFame.setInt(1, fame);
                    updateFame.setInt(2, psid);
                    updateFame.setString(3, world);
                    PERFORMANCE.start("Saving playerfame MW");
                    updateFame.executeUpdate();
                    PERFORMANCE.recordValue("Saving playerfame MW");
                    CustomLogger.logDebugInfo("Update mwplayer points: " + updateFame.toString());
                }
            } else {
                try (PreparedStatement updateFame = sql.getConnection()
                        .prepareStatement(UPDATE_PLAYERMETA_POINTS)) {
                    updateFame.setInt(1, fame);
                    updateFame.setInt(2, psid);
                    PERFORMANCE.start("Saving playerfame");
                    updateFame.executeUpdate();
                    PERFORMANCE.recordValue("Saving playerfame");
                    CustomLogger.logDebugInfo("Update player points: " + updateFame.toString());
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_FAME_SAVING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String w) throws DBException {
        if (playerUUID == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_FAME_LOADING);
        }

        int fama = 0;

        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);
        int psid = playerID(playerUUID.toString(), w);
        if (psid == -1) {
            psid = checkPlayerExists(pl, w);
        }

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

                try (PreparedStatement getFame = sql.getConnection()
                        .prepareStatement(MWPLAYER_POINTS)) {
                    getFame.setInt(1, psid);
                    getFame.setString(2, world);
                    PERFORMANCE.start("Loading playerfame MW");
                    try (ResultSet rs = getFame.executeQuery()) {
                        PERFORMANCE.recordValue("Loading playerfame MW");
                        if (rs.next()) {
                            fama = rs.getInt("points");
                        }
                    }
                    CustomLogger.logDebugInfo("Get mwplayer fame: " + getFame.toString());
                }
            } else {
                try (PreparedStatement getFame = sql.getConnection()
                        .prepareStatement(PLAYER_POINTS)) {
                    getFame.setInt(1, psid);
                    PERFORMANCE.start("Loading playerfame");
                    try (ResultSet rs = getFame.executeQuery()) {
                        PERFORMANCE.recordValue("Loading playerfame");
                        if (rs != null && rs.next()) {
                            fama = rs.getInt("points");
                        }
                    }
                    CustomLogger.logDebugInfo("Get player fame: " + getFame.toString());
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_FAME_LOADING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        return fama;
    }

    @Override
    public void savePlayedTime(TimedPlayer tPlayer) throws DBException {
        if (tPlayer == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_TIME_SAVING);
        }

        int psid = playerID(tPlayer.getOfflinePlayer().getUniqueId().toString(), null);
        if (psid == -1) {
            psid = checkPlayerExists(tPlayer.getOfflinePlayer(), null);
        }

        try {
            long time = tPlayer.getTotalOnline();

            try (PreparedStatement playedTime = sql.getConnection()
                    .prepareStatement(UPDATE_PLAYERMETA_PLAYEDTIME)) {
                playedTime.setLong(1, time);
                playedTime.setInt(2, psid);
                PERFORMANCE.start("Saving playedtime");
                playedTime.executeUpdate();
                PERFORMANCE.recordValue("Saving playedtime");
                CustomLogger.logDebugInfo("Save played time: " + playedTime.toString());
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_TIME_SAVING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }

    @Override
    public long loadPlayedTime(UUID playerUUID) throws DBException {
        if (playerUUID == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_TIME_LOADING);
        }

        long time = 0;

        int psid = playerID(playerUUID.toString(), null);
        if (psid == -1) {
            psid = checkPlayerExists(plugin.getServer().getOfflinePlayer(playerUUID), null);
        }

        try {
            PreparedStatement playedTime = sql.getConnection()
                    .prepareStatement(PLAYEDTIME);
            playedTime.setInt(1, psid);
            PERFORMANCE.start("Loading playedtime");
            try (ResultSet rs = playedTime.executeQuery()) {
                PERFORMANCE.recordValue("Loading playedtime");
                CustomLogger.logDebugInfo("Load played time: " + playedTime.toString());
                if (rs.next()) {
                    time = rs.getLong("playedTime");
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_TIME_LOADING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        return time;
    }

    @Override
    public ArrayList getTopPlayers(short cant, String server) throws DBException {
        ArrayList rankedPlayers = new ArrayList();

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

        sql += mundos;
        
        if (plugin.getManager().params.isMw_enabled()) {
            sql += " order by PlayerWorld.points";
        } else {
            sql += " order by points";
        }
        
        sql += " DESC limit " + cant;
        // </editor-fold>
        CustomLogger.logDebugInfo("Top players: " + sql);

        try {
            PlayerFame pf;

            PERFORMANCE.start("TOP");
            try (ResultSet rs = this.sql.getConnection().createStatement().executeQuery(sql)) {
                PERFORMANCE.recordValue("TOP");

                while (rs.next()) {
                    pf = new PlayerFame(rs.getString("playerUUID"), rs.getInt("points"),
                            rs.getLong("playedTime"));
                    pf.setServer(rs.getShort("serverID"));

                    if (plugin.getManager().params.isMw_enabled()) {
                        pf.setFame(rs.getInt("points"));
                        pf.setWorld(rs.getString("worldName"));
                    }

                    rankedPlayers.add(pf);
                }
            }
        } catch (final SQLException ex) {            
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYERS_TOP, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        return rankedPlayers;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="BOARDS...">  
    @Override
    public void saveBoard(SignBoard sb) throws DBException {
        if (sb == null) {
            throw new DBException("Board is null", DBException.DB_METHOD.BOARD_SAVING);
        }

        Location l = sb.getData().getLocation();

        try {
            try (PreparedStatement saveBoard = sql.getConnection().prepareStatement(CREATE_BOARD)) {
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

                CustomLogger.logDebugInfo("Save board: " + saveBoard.toString());
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_SAVING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }

    @Override
    public void updateBoard(Location l) throws DBException {
        if (l == null) {
            throw new DBException("Location is null", DBException.DB_METHOD.BOARD_UPDATING);
        }

        try {
            try (PreparedStatement updBoard = sql.getConnection().prepareStatement(SAVE_BOARD)) {
                updBoard.setInt(1, plugin.getManager().params.getMultiS());
                updBoard.setString(2, l.getWorld().getName());
                updBoard.setInt(3, l.getBlockX());
                updBoard.setInt(4, l.getBlockY());
                updBoard.setInt(5, l.getBlockZ());
                updBoard.executeUpdate();
                CustomLogger.logDebugInfo("Update board: " + updBoard.toString());
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_UPDATING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }

    @Override
    public void deleteBoard(Location l) throws DBException {
        if (l == null) {
            throw new DBException("Location is null", DBException.DB_METHOD.BOARD_REMOVING);
        }

        try {
            try (PreparedStatement delBoard = sql.getConnection().prepareStatement(DELETE_BOARD)) {
                delBoard.setInt(1, plugin.getManager().params.getMultiS());
                delBoard.setString(2, l.getWorld().getName());
                delBoard.setInt(3, l.getBlockX());
                delBoard.setInt(4, l.getBlockY());
                delBoard.setInt(5, l.getBlockZ());
                delBoard.executeUpdate();
                CustomLogger.logDebugInfo("Delete board: " + delBoard.toString());
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_REMOVING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }

    @Override
    public ArrayList<SignBoardData> findBoards() throws DBException {
        ArrayList<SignBoardData> sbd = new ArrayList();

        try {
            try (PreparedStatement searchBoards = sql.getConnection()
                    .prepareStatement(SEARCH_BOARDS)) {
                searchBoards.setInt(1, plugin.getManager().params.getMultiS());
                try (ResultSet rs = searchBoards.executeQuery()) {
                    CustomLogger.logDebugInfo("Search boards: " + searchBoards.toString());

                    while (rs.next()) {
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

                        SignBoardData sdc = new SignBoardData(nombre, modelo, server, l);
                        sdc.setOrientacion(orientacion);
                        sdc.setBlockface(blockface);

                        sbd.add(sdc);
                    }
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_SEARCHING, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        return sbd;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="OTHERS...">  
    @Override
    public String getServerName(short id) throws DBException {
        String nombre = "";

        try {
            try (PreparedStatement serverName = sql.getConnection()
                    .prepareStatement(SERVER_NAME)) {
                serverName.setInt(1, id);
                try (ResultSet rs = serverName.executeQuery()) {
                    while (rs.next()) {
                        nombre = rs.getString("nombreS");
                        break;
                    }
                }
                CustomLogger.logDebugInfo("Server name: " + serverName.toString());
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.SERVER_NAME, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        return nombre;
    }

    @Override
    public int purgeData(int q) throws DBException {
        int contador = 0;

        String data = "select id, playerUUID, lastLogin from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String purge = "delete from PlayerServer where id=?";

        PluginLog l = new PluginLog(plugin, "user_changes.txt");

        try {
            try (ResultSet rs = sql.getConnection().createStatement().executeQuery(data)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String strUUID = rs.getString("playerUUID");
                    
                    Date lastLogin;
                    if (sql instanceof MySQLConnection) {
                        lastLogin = rs.getDate("lastLogin");
                    } else {
                        lastLogin = new Date(rs.getInt("lastLogin"));
                    }

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
                            PreparedStatement delUser = sql.getConnection()
                                    .prepareStatement(purge);
                            delUser.setInt(1, id);
                            delUser.executeUpdate();
                            contador++;

                            this.playerids.remove(strUUID);
                            this.playerworlds.remove(strUUID);

                            // Log settings
                            UUID uuid = UUID.fromString(strUUID);
                            int time = (int) ((actualDate.getTimeInMillis() - lastLoginDate.getTimeInMillis()) / 1000);
                            l.addMessage("Player " + Bukkit.getOfflinePlayer(uuid).getName()
                                    + " has been removed. AFK time: "
                                    + StrUtils.splitToComponentTimes(time));
                        }
                    }
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PURGE_DATA, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        if (contador > 0) {
            l.export(true);
        }

        return contador;
    }

    @Override
    public void DBExport(String filename) throws DBException {
        String ruta = new StringBuilder(PvpTitles.PLUGIN_DIR)
                .append(filename).toString();

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
            try (ResultSet prs = sql.getConnection().createStatement().executeQuery(players)) {
                while (prs.next()) {
                    PlayerPT pl = new PlayerPT();
                    pl.setPlayerUUID(prs.getString("playerUUID"));
                    pl.setPoints(prs.getInt("points"));
                    pl.setPlayedTime(prs.getLong("playedTime"));
                    // Int to date
                    if (sql instanceof MySQLConnection) {
                        pl.setLastLogin(prs.getDate("lastLogin"));
                    } else {
                        pl.setLastLogin(new Date(prs.getInt("lastLogin")));
                    }                                        
                    plClass.add(pl);
                }
            }

            try (ResultSet pwrs = sql.getConnection().createStatement().executeQuery(playersPerWorld)) {
                while (pwrs.next()) {
                    WorldPlayerPT plWorld = new WorldPlayerPT();
                    plWorld.setPlayerUUID(pwrs.getString("playerUUID"));
                    plWorld.setPoints(pwrs.getInt("points"));
                    plWorld.setWorld(pwrs.getString("worldName"));
                    plwClass.add(plWorld);
                }
            }

            try (ResultSet srs = sql.getConnection().createStatement().executeQuery(signs)) {
                while (srs.next()) {
                    World w = plugin.getServer().getWorld(srs.getString("world"));
                    int x = srs.getInt("x");
                    int y = srs.getInt("y");
                    int z = srs.getInt("z");

                    SignPT sg = new SignPT();
                    sg.setName(srs.getString("name"));
                    sg.setModel(srs.getString("signModel"));
                    sg.setOrientation(srs.getString("orientation"));
                    sg.setBlockface(srs.getShort("blockface"));
                    sg.setLocation(new Location(w, x, y, z));
                    signClass.add(sg);
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.DB_EXPORT, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
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
    public boolean DBImport(String filename) throws DBException {
        String ruta = new StringBuilder(PvpTitles.PLUGIN_DIR)
                .append(filename).toString();

        if (!UtilsFile.exists(ruta)) {
            return false;
        }

        List<String> sql = UtilsFile.getFileLines(ruta);

        for (String consulta : sql) {
            try {
                try (PreparedStatement ps = this.sql.getConnection().prepareStatement(consulta)) {
                    ps.executeUpdate();
                }
            } catch (final SQLException ex) {
                throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.DB_IMPORT, ex.getMessage()) {
                    {
                        this.setStackTrace(ex.getStackTrace());
                    }
                };
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
    public int repair() throws DBException {
        int q = 0;
        boolean repaired = false;

        String data = "select id, playerUUID, points, playedTime, lastLogin from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String update = "update PlayerMeta set points=?,playedTime=?,lastLogin=? where psid=?";

        PluginLog l = new PluginLog(plugin, "db_changes.txt");

        try {
            try (ResultSet rs = sql.getConnection().createStatement().executeQuery(data)) {
                while(rs.next()) {
                    int id = rs.getInt("id");

                    String strUUID = rs.getString("playerUUID");
                    int points = -1;
                    long playedTime = -1;
                    java.sql.Date lastLogin = null;

                    try {
                        points = rs.getInt("points");
                    } catch (SQLException e) {
                    }

                    try {
                        playedTime = rs.getLong("playedTime");
                    } catch (SQLException e) {
                    }

                    // Int to date
                    if (sql instanceof MySQLConnection) {
                        lastLogin = rs.getDate("lastLogin");
                    } else {
                        lastLogin = new java.sql.Date(rs.getInt("lastLogin"));
                    }  

                    if (points < 0 || playedTime < 0 || playedTime >= 3153600000L
                            || lastLogin == null) {
                        UUID uuid = UUID.fromString(strUUID);
                        String name = Bukkit.getOfflinePlayer(uuid).getName();

                        if (points < 0) {
                            l.addMessage("[BAD FAME] Player \"" + name + "\" had "
                                    + points + " and it was changed to 0");
                            points = 0;
                        }

                        if (playedTime < 0 || playedTime >= 3153600000L) {
                            l.addMessage("[BAD PLAYED TIME] Player \"" + name + "\" had "
                                    + playedTime + " and it was changed to 0");
                            playedTime = 0;
                        }

                        if (lastLogin == null) {
                            l.addMessage("[BAD LAST LOGIN] Player \"" + name + "\" had "
                                    + "an invalid login date");
                            lastLogin = new java.sql.Date(new Date().getTime());
                        }

                        PreparedStatement fix = sql.getConnection().prepareStatement(update);
                        fix.setInt(1, points);
                        fix.setLong(2, playedTime);
                        fix.setDate(3, lastLogin);
                        fix.setInt(4, id);
                        fix.executeUpdate();

                        repaired = true;
                        q++;
                    }
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.REPAIR, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        if (repaired) {
            l.export(true);
        }

        return q;
    }

    @Override
    public void updateConnection(Object sql) {
        this.sql = (SQLConnection) sql;
    }
    //</editor-fold>
}
