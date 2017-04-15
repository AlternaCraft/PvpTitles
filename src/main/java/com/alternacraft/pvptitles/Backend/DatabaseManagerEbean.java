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
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.PERFORMANCE;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoardData;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.PluginLog;
import com.alternacraft.pvptitles.Misc.StrUtils;
import com.alternacraft.pvptitles.Misc.TagsClass;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.persistence.OptimisticLockException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DatabaseManagerEbean implements DatabaseManager {

    private static final String FILENAME_IMPORT = "database.json";
    private static final String FILENAME_EXPORT = "database.sql";

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private final PvpTitles plugin;
    private EbeanConnection ebeanServer = null;

    public DatabaseManagerEbean(PvpTitles plugin, EbeanConnection ebeanServer) {
        this.plugin = plugin;
        this.ebeanServer = ebeanServer;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    @Override
    public void playerConnection(Player player) throws DBException {
        if (player == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_CONNECTION);
        }

        PlayerPT plClass = null;
        UUID playerUUID = player.getUniqueId();

        PERFORMANCE.start("Player connection");

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .select("playerUUID")
                .where()
                .ieq("playerUUID", playerUUID.toString())
                .findUnique();

        if (plClass == null) {
            plClass = new PlayerPT();
            plClass.setPlayerUUID(playerUUID.toString());
        }

        plClass.setLastLogin(new Date());

        ebeanServer.getDatabase().save(plClass);

        // MultiWorld   
        if (plugin.getManager().params.isMw_enabled()) {
            WorldPlayerPT plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                    .select("playerUUID, world")
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .ieq("world", player.getWorld().getName())
                    .findUnique();

            if (plwClass == null) {
                plwClass = new WorldPlayerPT();

                plwClass.setPlayerUUID(playerUUID.toString());
                plwClass.setWorld(player.getWorld().getName());

                ebeanServer.getDatabase().save(plwClass);
            }
        }

        PERFORMANCE.recordValue("Player connection");
    }

    /* TABLA PLAYERS */
    @Override
    public void savePlayerFame(UUID playerUUID, int fame, String w) throws DBException {
        if (playerUUID == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_FAME_SAVING);
        }

        try {
            if (plugin.getManager().params.isMw_enabled()) {
                // Multiworld + mundo permitido
                OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);

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

                PERFORMANCE.start("Saving playerfame MW");
                WorldPlayerPT plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                        .select("playerUUID, world")
                        .where()
                        .ieq("playerUUID", playerUUID.toString())
                        .ieq("world", world)
                        .findUnique();
                PERFORMANCE.recordValue("Saving playerfame MW");

                if (plwClass == null) {
                    plwClass = new WorldPlayerPT();

                    plwClass.setPlayerUUID(playerUUID.toString());
                    plwClass.setWorld(world);
                }

                plwClass.setPoints(fame);
                ebeanServer.getDatabase().save(plwClass);
            } else {
                PERFORMANCE.start("Saving playerfame");
                PlayerPT plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                        .select("playerUUID")
                        .where()
                        .ieq("playerUUID", playerUUID.toString())
                        .findUnique();
                PERFORMANCE.recordValue("Saving playerfame");
                plClass.setPoints(fame);
                ebeanServer.getDatabase().save(plClass);
            }
        } catch (NullPointerException | OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_FAME_SAVING, ex.getMessage());
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String w) throws DBException {
        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);

        if (pl == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_FAME_LOADING);
        }

        int points = 0;

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

                PERFORMANCE.start("Loading playerfame MW");
                WorldPlayerPT plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                        .select("playerUUID, world, points")
                        .where()
                        .ieq("playerUUID", playerUUID.toString())
                        .ieq("world", world)
                        .findUnique();
                PERFORMANCE.recordValue("Loading playerfame MW");

                if (plwClass != null) {
                    points = plwClass.getPoints();
                }
            } else {
                PERFORMANCE.start("Loading playerfame");
                PlayerPT plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                        .select("playerUUID, points")
                        .where()
                        .ieq("playerUUID", playerUUID.toString())
                        .findUnique();
                PERFORMANCE.recordValue("Loading playerfame");

                if (plClass != null) {
                    points = plClass.getPoints();
                }
            }
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_FAME_LOADING, ex.getMessage());
        }

        return points;
    }

    @Override
    public void savePlayedTime(TimedPlayer tPlayer) throws DBException {
        if (tPlayer == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_TIME_SAVING);
        }

        PlayerPT plClass = null;

        try {
            PERFORMANCE.start("Saving playedtime");
            plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                    .select("playerUUID")
                    .where()
                    .ieq("playerUUID", tPlayer.getUniqueId().toString())
                    .findUnique();
            PERFORMANCE.recordValue("Saving playedtime");

            if (plClass == null) {
                plClass = new PlayerPT();
                plClass.setPlayerUUID(tPlayer.getUniqueId().toString());
                plClass.setPlayedTime(tPlayer.getTotalOnline());
                plClass.setLastLogin(new Date());
            } else {
                plClass.setPlayedTime(tPlayer.getTotalOnline() + plClass.getPlayedTime());
            }

            ebeanServer.getDatabase().save(plClass);
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_TIME_SAVING, ex.getMessage());
        }
    }

    @Override
    public long loadPlayedTime(UUID playerUUID) throws DBException {
        if (playerUUID == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_TIME_LOADING);
        }

        PlayerPT plClass = null;
        long time = 0;

        try {
            PERFORMANCE.start("Loading playedtime");
            plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                    .select("playerUUID, playedTime")
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .findUnique();
            PERFORMANCE.recordValue("Loading playedtime");

            if (plClass != null) {
                time = plClass.getPlayedTime();
            }
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYER_TIME_LOADING, ex.getMessage());
        }

        return time;
    }

    /* OTROS */
    @Override
    public ArrayList<PlayerFame> getTopPlayers(short cant, String server) throws DBException {
        List<WorldPlayerPT> allPlayersW;
        List<PlayerPT> allPlayers;

        ArrayList<PlayerFame> rankedPlayers = new ArrayList();

        PERFORMANCE.start("TOP");

        try {
            if (plugin.getManager().params.isMw_enabled()) {
                String mundos = "";

                if (!plugin.getManager().params.showOnLeaderBoard()) {
                    List<String> worlds_disabled = plugin.getManager().params.getAffectedWorlds();

                    StringBuilder buf = new StringBuilder();
                    for (String world : worlds_disabled) {
                        buf.append("world != '").append(world).append("' AND ");
                    }
                    if (!worlds_disabled.isEmpty()) {
                        mundos = buf.toString();
                        mundos = mundos.substring(0, mundos.length() - 5);
                    }
                }

                allPlayersW = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                        .select("playerUUID, points, world")
                        .where(mundos)
                        .orderBy("points desc")
                        .setMaxRows(cant)
                        .findList();

                for (int i = 0; i < allPlayersW.size(); i++) {
                    PlayerPT time = ebeanServer.getDatabase().find(PlayerPT.class)
                            .select("playerUUID, playedTime")
                            .where()
                            .ieq("playerUUID", allPlayersW.get(i).getPlayerUUID())
                            .findUnique();

                    PlayerFame pf = new PlayerFame(allPlayersW.get(i).getPlayerUUID(),
                            allPlayersW.get(i).getPoints(), time.getPlayedTime());

                    pf.setWorld(allPlayersW.get(i).getWorld());
                    rankedPlayers.add(pf);
                }
            } else {
                allPlayers = ebeanServer.getDatabase().find(PlayerPT.class)
                        .select("playerUUID, points, playedTime")
                        .orderBy("points desc")
                        .setMaxRows(cant)
                        .findList();

                for (int i = 0; i < allPlayers.size(); i++) {
                    PlayerFame pf = new PlayerFame(allPlayers.get(i).getPlayerUUID(),
                            allPlayers.get(i).getPoints(), allPlayers.get(i).getPlayedTime());
                    rankedPlayers.add(pf);
                }
            }
        } catch (Exception ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYERS_TOP, ex.getMessage());
        }

        PERFORMANCE.recordValue("TOP");

        return rankedPlayers;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="BOARDS...">
    /* TABLA CARTELES */
    @Override
    public void saveBoard(SignBoard sb) throws DBException {
        if (sb == null) {
            throw new DBException("Board is null", DBException.DB_METHOD.BOARD_SAVING);
        }

        try {
            SignPT st = new SignPT();
            st.setName(sb.getData().getNombre());
            st.setModel(sb.getData().getModelo());
            st.setLocation(sb.getData().getLocation());
            st.setOrientation(sb.getData().getOrientacion());
            st.setBlockface(sb.getData().getPrimitiveBlockface());

            ebeanServer.getDatabase().save(st);
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_SAVING, ex.getMessage());
        }
    }

    @Override
    public void updateBoard(Location l) throws DBException {
        if (l == null) {
            throw new DBException("Location is null", DBException.DB_METHOD.BOARD_UPDATING);
        }
    }

    @Override
    public void deleteBoard(Location l) throws DBException {
        if (l == null) {
            throw new DBException("Location is null", DBException.DB_METHOD.BOARD_REMOVING);
        }

        try {
            SignPT st = ebeanServer.getDatabase().find(SignPT.class)
                    .where()
                    .eq("x", l.getX())
                    .eq("y", l.getY())
                    .eq("z", l.getZ())
                    .ieq("world", l.getWorld().getName())
                    .findUnique();

            ebeanServer.getDatabase().delete(st);
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_REMOVING, ex.getMessage());
        }
    }

    @Override
    public ArrayList<SignBoardData> findBoards() throws DBException {
        List<SignPT> plClass = null;

        try {
            plClass = ebeanServer.getDatabase().find(SignPT.class).findList();

            ArrayList<SignBoardData> sbd = new ArrayList();

            for (SignPT signPT : plClass) {
                Location l = new Location(plugin.getServer().getWorld(signPT.getWorld()), signPT.getX(), signPT.getY(), signPT.getZ());
                SignBoardData bds = new SignBoardData(signPT.getName(), signPT.getModel(), "", l);
                bds.setOrientacion(signPT.getOrientation());
                bds.setBlockface(signPT.getBlockface());

                sbd.add(bds);
            }

            return sbd;

        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_REMOVING, ex.getMessage());
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="OTHERS...">
    @Override
    public String getServerName(short id) throws DBException {
        return this.plugin.getManager().params.getNameS();
    }

    @Override
    public int purgeData(int q) throws DBException {
        return checkUsers(q, ebeanServer.getDatabase().find(WorldPlayerPT.class).findList().size() > 0);
    }

    private int checkUsers(int q, boolean complete) throws DBException {
        int contador = 0;

        try {
            List<PlayerPT> allDates = ebeanServer.getDatabase().find(PlayerPT.class)
                    .select("playerUUID, lastLogin")
                    .where()
                    .lt("lastLogin", new Date())
                    .findList();

            PluginLog l = new PluginLog(plugin, "user_changes.txt");

            for (PlayerPT player : allDates) {
                if (plugin.getManager().params.getNoPurge().contains(player.getPlayerUUID())) {
                    continue;
                }

                Date lastLogin = player.getLastLogin();
                Calendar lastLoginDate = new GregorianCalendar();
                lastLoginDate.setTime(lastLogin);

                // Tiempo en config
                lastLoginDate.add(GregorianCalendar.DAY_OF_YEAR, q);

                Date today = new Date();
                Calendar actualDate = new GregorianCalendar();
                actualDate.setTime(today);

                // cFile + timePurga < hoy
                if (lastLoginDate.before(actualDate)) {
                    if (complete) {
                        List<WorldPlayerPT> mwplayers = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                                .where()
                                .ieq("playerUUID", player.getPlayerUUID())
                                .findList();

                        if (mwplayers != null) {
                            ebeanServer.getDatabase().delete(mwplayers);
                        }
                    } else {
                        ebeanServer.getDatabase().delete(player);

                        lastLoginDate.setTime(lastLogin); // Remove q time

                        // Log settings
                        UUID uuid = UUID.fromString(player.getPlayerUUID());
                        int time = (int) ((actualDate.getTimeInMillis() - lastLoginDate.getTimeInMillis()) / 1000);
                        l.addMessage("Player " + Bukkit.getOfflinePlayer(uuid).getName()
                                + " has been removed. AFK time: "
                                + StrUtils.splitToComponentTimes(time));

                        contador++;
                    }
                }
            }

            if (complete) {
                contador = checkUsers(q, false);
            } else {
                if (contador > 0) {
                    l.export(true);
                }
            }
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PURGE_DATA, ex.getMessage());
        }

        return contador;
    }

    @Override
    public void DBExport(String filename) throws DBException {
        String ruta = new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append( // Separador
                        filename).toString();

        short serverID = plugin.getManager().params.getMultiS();

        String sql = "";
        sql += MySQLConnection.getTableServers() + "\n";
        sql += MySQLConnection.getTablePlayerServer() + "\n";
        sql += MySQLConnection.getTablePlayerMeta() + "\n";
        sql += MySQLConnection.getTablePlayerWorld() + "\n";
        sql += MySQLConnection.getTableSigns() + "\n";

        List<PlayerPT> plClass = null;
        List<WorldPlayerPT> pwClass = null;
        List<SignPT> stClass = null;
                
        try {
            plClass = (List<PlayerPT>) ebeanServer.getDatabase().
                    find(PlayerPT.class).findList();
            pwClass = (List<WorldPlayerPT>) ebeanServer.getDatabase().
                    find(WorldPlayerPT.class).findList();
            stClass = (List<SignPT>) ebeanServer.getDatabase().
                    find(SignPT.class).findList();
        } 
        catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.DB_EXPORT, ex.getMessage());
        }
        
        boolean mw = pwClass != null && pwClass.size() > 0;

        sql += "insert into Servers values (" + serverID + ", '" + plugin.getManager().params.getNameS() + "')"
                + " ON DUPLICATE KEY UPDATE name=VALUES(name);\n";

        if (plClass != null && plClass.size() > 0) {
            for (int j = 0; j < plClass.size(); j++) {
                PlayerPT next = plClass.get(j);

                String fecha = new java.sql.Date(next.getLastLogin().getTime()).toString();

                sql += "insert into PlayerServer(id, playerUUID, serverID) select "
                        + "max(id)+1, '" + next.getPlayerUUID() + "', "
                        + serverID + " from PlayerServer ON DUPLICATE KEY UPDATE id = VALUES(id);\n";
                sql += "insert into PlayerMeta(psid, points, playedTime, lastLogin) select "
                        + "max(id), " + next.getPoints() + ", "
                        + next.getPlayedTime() + ", '" + fecha + "' from PlayerServer "
                        + "ON DUPLICATE KEY UPDATE points=VALUES(points),playedTime=VALUES(playedTime),"
                        + "lastLogin=VALUES(lastLogin);\n";

                if (mw) {
                    for (int k = 0; k < pwClass.size(); k++) {
                        WorldPlayerPT nextW = pwClass.get(k);
                        if (nextW.getPlayerUUID().equals(next.getPlayerUUID())) {
                            sql += "insert into PlayerWorld(psid, worldName, points) select "
                                    + "max(id), '" + nextW.getWorld() + "', "
                                    + nextW.getPoints() + " from PlayerServer "
                                    + "ON DUPLICATE KEY UPDATE worldName=VALUES"
                                    + "(worldName),points=VALUES(points);\n";
                            // optimizacion
                            pwClass.remove(k);
                            k--;
                        }
                    }
                }
            }
        }

        if (stClass != null && stClass.size() > 0) {
            sql += "insert into Signs values";

            for (SignPT sPT : stClass) {
                sql += "('" + sPT.getName() + "', '" + sPT.getModel() + "', "
                        + "'', '" + sPT.getOrientation() + "', " + sPT.getBlockface()
                        + ", " + serverID + ", '" + sPT.getWorld() + "', "
                        + sPT.getX() + ", " + sPT.getY() + ", " + sPT.getZ() + "),";
            }

            sql = sql.substring(0, sql.length() - 1); // Quito la coma sobrante
            sql += " ON DUPLICATE KEY UPDATE name=VALUES(name),signModel=VALUES(signModel),"
                    + "orientation=VALUES(orientation),blockface=VALUES(blockface);";
        }

        UtilsFile.writeFile(ruta, sql);
    }

    @Override
    public boolean DBImport(String filename) throws DBException {
        String ruta = new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append( // Separador
                        filename).toString();

        if (!UtilsFile.exists(ruta)) {
            return false;
        }

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(UtilsFile.getFileAsString(ruta));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray players = (JSONArray) jsonObject.get("Players");
            JSONArray signs = (JSONArray) jsonObject.get("Signs");
            JSONArray pworlds = (JSONArray) jsonObject.get("PlayersPerWorld");

            List<PlayerPT> playersJSON = TagsClass.getPlayers(players);
            for (PlayerPT playersPT : playersJSON) {
                PlayerPT ppt = ebeanServer.getDatabase().find(PlayerPT.class)
                        .where()
                        .ieq("playerUUID", playersPT.getPlayerUUID())
                        .findUnique();

                if (ppt == null) {
                    ppt = new PlayerPT();
                    ppt.setPlayerUUID(playersPT.getPlayerUUID());
                }

                ppt.setPoints(playersPT.getPoints());
                ppt.setLastLogin(playersPT.getLastLogin());
                ppt.setPlayedTime(playersPT.getPlayedTime());

                ebeanServer.getDatabase().save(ppt);
            }

            List<SignPT> signsJSON = TagsClass.getSigns(signs);
            for (SignPT signsPT : signsJSON) {
                SignPT spt = ebeanServer.getDatabase().find(SignPT.class)
                        .where()
                        .eq("x", signsPT.getX())
                        .eq("y", signsPT.getY())
                        .eq("z", signsPT.getZ())
                        .ieq("world", signsPT.getWorld())
                        .findUnique();

                if (spt == null) {
                    spt = new SignPT();
                    spt.setLocation(new Location(plugin.getServer().getWorld(signsPT.getWorld()),
                            signsPT.getX(), signsPT.getY(), signsPT.getZ()));
                }

                spt.setName(signsPT.getName());
                spt.setModel(signsPT.getModel());
                spt.setBlockface(signsPT.getBlockface());
                spt.setOrientation(signsPT.getOrientation());

                ebeanServer.getDatabase().save(spt);
            }

            List<WorldPlayerPT> worldPlayerJSON = TagsClass.getPWorlds(pworlds);
            for (WorldPlayerPT wordPlayerPT : worldPlayerJSON) {
                WorldPlayerPT wppt = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                        .where()
                        .ieq("playerUUID", wordPlayerPT.getPlayerUUID())
                        .findUnique();

                if (wppt == null) {
                    wppt = new WorldPlayerPT();
                    wppt.setPlayerUUID(wordPlayerPT.getPlayerUUID());
                }

                wppt.setPoints(wordPlayerPT.getPoints());
                wppt.setWorld(wordPlayerPT.getWorld());

                ebeanServer.getDatabase().save(wppt);
            }
        } catch (ParseException | OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.DB_IMPORT, ex.getMessage());
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

        try {
            List<PlayerPT> players = ebeanServer.getDatabase().find(PlayerPT.class).findList();
            PluginLog l = new PluginLog(plugin, "db_changes.txt");

            for (PlayerPT player : players) {
                if (player.getPoints() < 0 || player.getPlayedTime() < 0 || player.getLastLogin() == null) {
                    UUID uuid = UUID.fromString(player.getPlayerUUID());
                    String name = Bukkit.getOfflinePlayer(uuid).getName();

                    if (player.getPoints() < 0) {
                        l.addMessage("[BAD FAME] Player \"" + name + "\" had "
                                + player.getPoints() + " and it was changed to 0");
                        player.setPoints(0);
                    }

                    if (player.getPlayedTime() < 0 || player.getPlayedTime() >= 3153600000L) {
                        l.addMessage("[BAD PLAYED TIME] Player \"" + name + "\" had "
                                + player.getPlayedTime() + " and it was changed to 0");
                        player.setPlayedTime(0);
                    }

                    if (player.getLastLogin() == null) {
                        l.addMessage("[BAD LAST LOGIN] Player \"" + name + "\" had "
                                + "an invalid login date");
                        player.setLastLogin(new Date());
                    }

                    ebeanServer.getDatabase().save(player);
                    repaired = true;
                    q++;
                }
            }

            if (repaired) {
                l.export(true);
            }
        } catch (OptimisticLockException ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.REPAIR, ex.getMessage());
        }

        return q;
    }

    @Override
    public void updateConnection(Object connection) {
        this.ebeanServer = (EbeanConnection) connection;
    }
    // </editor-fold>
}
