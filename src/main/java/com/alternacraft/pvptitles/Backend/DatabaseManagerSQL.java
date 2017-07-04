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
import com.alternacraft.pvptitles.Misc.UtilsFile;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class DatabaseManagerSQL implements DatabaseManager {

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
                        try (ResultSet rss = sql.query("select max(id) from PlayerServer")) {
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
    public void savePlayedTime(UUID playerUUID, long playedTime) throws DBException {
        if (playerUUID == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_TIME_SAVING);
        }

        OfflinePlayer oplayer = plugin.getServer().getOfflinePlayer(playerUUID);
        
        int psid = playerID(playerUUID.toString(), null);
        if (psid == -1) {
            psid = checkPlayerExists(oplayer, null);
        }

        try {
            try (PreparedStatement pTime = sql.getConnection()
                    .prepareStatement(UPDATE_PLAYERMETA_PLAYEDTIME)) {
                pTime.setLong(1, playedTime);
                pTime.setInt(2, psid);
                PERFORMANCE.start("Saving playedtime");
                pTime.executeUpdate();
                PERFORMANCE.recordValue("Saving playedtime");
                CustomLogger.logDebugInfo("Save played time: " + pTime.toString());
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
        String query;

        if (plugin.getManager().params.isMw_enabled()) {
            query = TOPMWPLAYERS;
        } else {
            query = TOPPLAYERS;
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
                query += " where";
                for (Short serverID : servidores.keySet()) {
                    query += " (serverID = " + serverID;

                    if (plugin.getManager().params.isMw_enabled() && !servidores.get(serverID).isEmpty()) {
                        query += " AND (";
                        for (String mundoElegido : servidores.get(serverID)) {
                            query += "worldName like '" + mundoElegido + "' OR ";
                        }
                        query = query.substring(0, query.length() - 4) + ')';
                    }

                    query += ") OR";
                }
                query = query.substring(0, query.length() - 3);
            }
        } else {
            query += " where serverID=" + plugin.getManager().params.getMultiS();

            if (plugin.getManager().params.isMw_enabled() && servidores != null && servidores.get(plugin.getManager().params.getMultiS()) != null) {
                query += " AND (";
                for (String mundoElegido : servidores.get(plugin.getManager().params.getMultiS())) {
                    query += "worldName like '" + mundoElegido + "' OR ";
                }
                query = query.substring(0, query.length() - 4) + ')';
            }
        }

        query += mundos;
        
        if (plugin.getManager().params.isMw_enabled()) {
            query += " order by PlayerWorld.points";
        } else {
            query += " order by points";
        }
        
        query += " DESC limit " + cant;
        // </editor-fold>
        CustomLogger.logDebugInfo("Top players: " + query);

        try {
            PlayerFame pf;

            PERFORMANCE.start("TOP");
            try (ResultSet rs = this.sql.query(query)) {
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

        String data = "select id, playerUUID, points, lastLogin from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String purge = "delete from PlayerServer where id=?";

        PluginLog l = new PluginLog(plugin, "user_changes.txt");

        try {
            try (ResultSet rs = sql.query(data)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String strUUID = rs.getString("playerUUID");
                    
                    Date lastLogin;
                    if (sql instanceof MySQLConnection) {
                        lastLogin = rs.getDate("lastLogin");
                    } else {
                        String date = rs.getString("lastLogin");
                        try {
                            lastLogin = new Date(Integer.parseInt(date));
                        } catch (NumberFormatException | NullPointerException e) {
                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                lastLogin = format.parse(date);
                            } catch (ParseException ex) {
                                lastLogin = new GregorianCalendar(1978, Calendar.JANUARY, 1).getTime();
                            }
                        }
                    }
                    
                    if (!plugin.getManager().params.getNoPurge().contains(strUUID)) {
                        Calendar lastLoginDate = new GregorianCalendar();
                        lastLoginDate.setTime(lastLogin);
                        lastLoginDate.add(6, q);

                        Calendar actualDate = new GregorianCalendar();
                        actualDate.setTime(new Date());

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
                            int points = rs.getInt("points");
                            int time = (int) ((actualDate.getTimeInMillis() - lastLoginDate.getTimeInMillis()) / 1000);
                            l.addMessage("Player " + Bukkit.getOfflinePlayer(uuid).getName()
                                    + " has been removed. Points: " + points 
                                    + "; AFK time: " + StrUtils.splitToComponentTimes(time));
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

        boolean sqlite = this.sql instanceof SQLiteConnection;
        String sql_to_export = "-- " + ((this.sql instanceof SQLiteConnection) 
                ? "MySQL":"SQLite") + "\n";

        String servers = "select * from Servers";

        String playerserver = "select * from PlayerServer";
        String playermeta = "select points, playedTime, lastLogin from PlayerMeta where psid=?";
        String playerworld = "select worldName, points from PlayerWorld where psid=?";

        String signs = "select * from Signs";

        try {
            try (ResultSet sv = this.sql.query(servers)) {
                while (sv.next()) {
                    if (sqlite) {
                        sql_to_export += "insert into Servers(id, name) values (" + sv.getInt("id")
                                + ", '" + sv.getString("name") + "') ON DUPLICATE KEY UPDATE name=VALUES(name);\n";
                    } else {
                        sql_to_export += "insert or ignore into Servers(id, name) values (" + sv.getInt("id")
                                + ", '" + sv.getString("name") + "');\n";
                        sql_to_export += "update Servers set name='" + sv.getString("name") 
                                + "' where id=" + sv.getInt("id") + ";\n";
                    }
                }
            }

            try (ResultSet ps = this.sql.query(playerserver)) {
                while (ps.next()) {
                    if (sqlite) {
                        sql_to_export += "insert into PlayerServer(id, playerUUID, serverID) select "
                                + "max(id)+1, '" + ps.getString("playerUUID") + "', "
                                + ps.getShort("serverID") + " from PlayerServer ON DUPLICATE KEY UPDATE id = VALUES(id);\n";
                    } else {
                        sql_to_export += "insert or replace into PlayerServer(id, playerUUID, serverID) select "
                                + "max(id)+1, '" + ps.getString("playerUUID") + "', "
                                + ps.getShort("serverID") + " from PlayerServer;\n";
                    }
                    
                    int id = ps.getInt("id");
                    
                    PreparedStatement statement = this.sql.getConnection().prepareStatement(playermeta);
                    statement.setInt(1, id);                    
                    try (ResultSet pm = statement.executeQuery()) {
                        if (pm.next()) {
                            if (sqlite) {
                                DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                String str_date = pm.getString("lastLogin");
                                try {
                                    str_date = format.format(new Date(Integer.parseInt(str_date)));
                                } catch (NumberFormatException | NullPointerException e) {
                                    try {
                                        format.parse(str_date);
                                    } catch (ParseException ex) {
                                        str_date = format.format(new Date());
                                    }
                                }
                                sql_to_export += "insert into PlayerMeta(psid, points, playedTime, lastLogin) select "
                                        + "max(id), " + pm.getInt("points") + ", " + pm.getInt("playedTime")
                                        + ", '" + str_date + "' from PlayerServer "
                                        + "ON DUPLICATE KEY UPDATE points=VALUES(points),playedTime=VALUES(playedTime),"
                                        + "lastLogin=VALUES(lastLogin);\n";
                            } else {                                
                                sql_to_export += "insert or replace into PlayerMeta(psid, points, playedTime, lastLogin) select "
                                        + "max(id), " + pm.getInt("points") + ", " + pm.getInt("playedTime")
                                        + ", '" + pm.getDate("lastLogin") + "' from PlayerServer;\n";
                            }
                        }
                    }

                    statement = this.sql.getConnection().prepareStatement(playerworld);
                    statement.setInt(1, id);
                    try (ResultSet pw = statement.executeQuery()) {
                        while (pw.next()) {
                            if (sqlite) {
                                sql_to_export += "insert into PlayerWorld(psid, worldName, points) select "
                                        + "max(id), '" + pw.getString("worldName") + "', "
                                        + pw.getInt("points") + " from PlayerServer "
                                        + "ON DUPLICATE KEY UPDATE worldName=VALUES"
                                        + "(worldName),points=VALUES(points);\n";
                            } else {
                                sql_to_export += "insert or replace into PlayerWorld(psid, worldName, points) select "
                                        + "max(id), '" + pw.getString("worldName") + "', "
                                        + pw.getInt("points") + " from PlayerServer;\n";
                            }
                        }
                    }
                }
            }

            try (ResultSet s = this.sql.query(signs)) {
                if (s.next()) {
                    if (sqlite) {
                        sql_to_export += "insert into Signs values";
                    } else {
                        sql_to_export += "insert or replace into Signs values";
                    }
                    do {
                        sql_to_export += "('" + s.getString("name") + "', '" + s.getString("signModel")
                                + "', '" + s.getString("dataModel") + "', '" + s.getString("orientation")
                                + "', " + s.getShort("blockface") + ", " + s.getShort("serverID")
                                + ", '" + s.getString("world") + "', " + s.getInt("x")
                                + ", " + s.getInt("y") + ", " + s.getInt("z") + "),";
                    } 
                    while (s.next());
                    sql_to_export = sql_to_export.substring(0, sql_to_export.length() - 1); // Quito la coma sobrante                    
                    if (sqlite) {
                        sql_to_export += " ON DUPLICATE KEY UPDATE name=VALUES(name),signModel=VALUES(signModel),"
                                + "dataModel=VALUES(dataModel),orientation=VALUES(orientation),"
                                + "blockface=VALUES(blockface);";
                    } else {
                        sql_to_export += ";";
                    }
                }
            }
        } catch (final SQLException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.DB_EXPORT, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }

        UtilsFile.writeFile(ruta, sql_to_export);
    }

    @Override
    public boolean DBImport(String filename) throws DBException {
        String ruta = new StringBuilder(PvpTitles.PLUGIN_DIR)
                .append(filename).toString();

        if (!UtilsFile.exists(ruta)) {
            return false;
        }

        List<String> sqlfile = UtilsFile.getFileLines(ruta);
        boolean sqlite = this.sql instanceof SQLiteConnection;
        Pattern r = Pattern.compile("values \\((-?\\d+),\\s'(.*)'\\)");
        
        if (sqlfile.get(0).contains("--")) {
            if (sqlfile.get(0).matches("-- (MySQL|SQLite)")) {
                boolean compile_by_mysql = false;
                if (sqlfile.get(0).contains("MySQL")) {
                    compile_by_mysql = true;
                }
                if (sqlite && compile_by_mysql || !sqlite && !compile_by_mysql) {
                    HashMap data = new HashMap();
                    data.put("Actual database", DBLoader.tipo.name());
                    data.put("SQL syntax", (compile_by_mysql) ? "MYSQL":"SQLITE"); 
                    throw new DBException(DBException.BAD_SQL_SYNTAX, 
                            DBException.DB_METHOD.DB_IMPORT, data);
                }
                sqlfile.remove(0);
            }
        }
        
        for (String consulta : sqlfile) {
            // Minor fix for old queries
            if (sqlite && consulta.contains("create table"))
                continue;
            if (sqlite && consulta.contains("ON DUPLICATE")) {
                if (consulta.contains("Servers")) {
                    Matcher m = r.matcher(Pattern.quote(consulta));
                    consulta = "insert or ignore " + consulta.substring(consulta.indexOf("into"), 
                            consulta.indexOf(" ON DUPLICATE KEY")) + ";";
                    if (m.find()) {
                        String id = m.group(1), name = m.group(2);
                        consulta += "update Servers set name='" + name + "' where id=" + id;
                    }
                } else {
                    consulta = "insert or replace " + consulta.substring(consulta.indexOf("into"), 
                            consulta.indexOf(" ON DUPLICATE KEY")) + ";";
                }
            }
            
            try {
                this.sql.update(consulta);
            } catch (SQLException ex) {
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
    public int repair() throws DBException {
        int q = 0;
        boolean repaired = false;

        String data = "select id, playerUUID, points, playedTime, lastLogin from PlayerServer "
                + "inner join PlayerMeta on id=psid";
        String update = "update PlayerMeta set points=?,playedTime=?,lastLogin=? where psid=?";

        PluginLog l = new PluginLog(plugin, "db_changes.txt");

        try {
            try (ResultSet rs = sql.query(data)) {
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
