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
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoardData;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Managers.Timer.TimedPlayer;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.TagsClass;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DatabaseManagerEbean implements DatabaseManager {

    private static final String FILENAME_IMPORT = "database.json";
    private static final String FILENAME_EXPORT = "database.sql";

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private final PvpTitles plugin;
    private Ebean ebeanServer = null;

    public DatabaseManagerEbean(PvpTitles plugin, Ebean ebeanServer) {
        this.plugin = plugin;
        this.ebeanServer = ebeanServer;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    @Override
    public void playerConnection(Player player) throws DBException {
        PlayerPT plClass = null;

        if (player == null) {
            HashMap data = new HashMap();
            data.put("Null player?", (player == null));
            throw new DBException(DBException.PLAYER_CONNECTION_ERROR,
                    DBException.DB_METHOD.PLAYER_CONNECTION);
        }

        UUID playerUUID = player.getUniqueId();

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
    }

    /* TABLA PLAYERS */
    @Override
    public void savePlayerFame(UUID playerUUID, int fame, String w) throws DBException {
        // Multiworld + mundo permitido
        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);

        PlayerPT plClass = null;

        // Base
        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .select("playerUUID")
                .where()
                .ieq("playerUUID", playerUUID.toString())
                .findUnique();

        if (plClass == null) {
            plClass = new PlayerPT();

            plClass.setPlayerUUID(playerUUID.toString());
            plClass.setLastLogin(new Date());

            ebeanServer.getDatabase().save(plClass);
        }

        WorldPlayerPT plwClass = null;

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

            plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                    .select("playerUUID, world")
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .ieq("world", world)
                    .findUnique();

            if (plwClass == null) {
                plwClass = new WorldPlayerPT();

                plwClass.setPlayerUUID(playerUUID.toString());
                plwClass.setWorld(world);
            }

            plwClass.setPoints(fame);
            ebeanServer.getDatabase().save(plwClass);
        } else {
            // Base
            plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                    .select("playerUUID")
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .findUnique();

            plClass.setPoints(fame);
            ebeanServer.getDatabase().save(plClass);
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String w) throws DBException {
        int points = 0;

        OfflinePlayer pl = plugin.getServer().getOfflinePlayer(playerUUID);

        if (pl == null) {
            throw new DBException("Player is null", DBException.DB_METHOD.PLAYER_FAME_LOADING);
        }

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

            WorldPlayerPT plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                    .select("playerUUID, world, points")
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .ieq("world", world)
                    .findUnique();

            if (plwClass != null) {
                points = plwClass.getPoints();
            }
        } else {
            PlayerPT plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                    .select("playerUUID, points")
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .findUnique();

            if (plClass != null) {
                points = plClass.getPoints();
            }
        }

        return points;
    }

    @Override
    public void savePlayedTime(TimedPlayer tPlayer) throws DBException {
        PlayerPT plClass = null;

        if (tPlayer == null) {
            HashMap<String, Object> data = new HashMap();
            data.put("Null Player?", tPlayer == null);
            
            throw new DBException(DBException.PLAYER_TIME_ERROR, 
                    DBException.DB_METHOD.PLAYER_TIME_SAVING, data);
        }

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .select("playerUUID")
                .where()
                .ieq("playerUUID", tPlayer.getUniqueId().toString())
                .findUnique();

        if (plClass == null) {
            plClass = new PlayerPT();
            plClass.setPlayerUUID(tPlayer.getUniqueId().toString());
            plClass.setPlayedTime(tPlayer.getTotalOnline());
        } else {
            plClass.setPlayedTime(tPlayer.getTotalOnline() + plClass.getPlayedTime());
        }

        ebeanServer.getDatabase().save(plClass);
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) throws DBException {
        PlayerPT plClass = null;
        int time = 0;

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .select("playerUUID, playedTime")
                .where()
                .ieq("playerUUID", playerUUID.toString())
                .findUnique();

        if (plClass != null) {
            time = plClass.getPlayedTime();
        }

        return time;
    }

    /* OTROS */
    @Override
    public ArrayList<PlayerFame> getTopPlayers(short cant, String server) throws DBException {
        List<WorldPlayerPT> allPlayersW;
        List<PlayerPT> allPlayers;

        ArrayList<PlayerFame> rankedPlayers = new ArrayList();

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
                            allPlayersW.get(i).getPoints(), time.getPlayedTime(),
                            this.plugin);

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
                            allPlayers.get(i).getPoints(), allPlayers.get(i).getPlayedTime(),
                            this.plugin);
                    rankedPlayers.add(pf);
                }
            }
        } catch (Exception ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.PLAYERS_TOP, ex.getMessage());
        }

        return rankedPlayers;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="BOARDS...">
    /* TABLA CARTELES */
    @Override
    public void registraBoard(SignBoard sb) throws DBException {
        try {
            SignPT st = new SignPT();
            st.setName(sb.getData().getNombre());
            st.setModel(sb.getData().getModelo());
            st.setLocation(sb.getData().getLocation());
            st.setOrientation(sb.getData().getOrientacion());
            st.setBlockface(sb.getData().getPrimitiveBlockface());

            ebeanServer.getDatabase().save(st);
        } catch (Exception ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_SAVING, ex.getMessage());
        }
    }

    @Override
    public void modificaBoard(Location l) {
        // Nothing yet
    }

    @Override
    public void borraBoard(Location l) throws DBException {
        try {
            SignPT st = ebeanServer.getDatabase().find(SignPT.class)
                    .where()
                    .eq("x", l.getX())
                    .eq("y", l.getY())
                    .eq("z", l.getZ())
                    .ieq("world", l.getWorld().getName())
                    .findUnique();

            ebeanServer.getDatabase().delete(st);
        } catch (Exception ex) {
            throw new DBException(UNKNOWN_ERROR, DBException.DB_METHOD.BOARD_REMOVING, ex.getMessage());
        }
    }

    @Override
    public ArrayList<SignBoardData> buscaBoards() {
        List<SignPT> plClass = null;

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
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="OTHERS...">
    @Override
    public String getServerName(short id) {
        return this.plugin.getManager().params.getNameS();
    }

    @Override
    public int purgeData(int q) {
        int contador = 0;

        List<PlayerPT> allDates = ebeanServer.getDatabase().find(PlayerPT.class)
                .select("playerUUID, lastLogin")
                .where()
                .lt("lastLogin", new Date())
                .findList();

        for (PlayerPT player : allDates) {
            if (plugin.getManager().params.getNoPurge().contains(player.getPlayerUUID())) {
                continue;
            }

            Date fechaFile = player.getLastLogin();
            Calendar cFile = new GregorianCalendar();
            cFile.setTime(fechaFile);

            // Tiempo en config
            cFile.add(GregorianCalendar.DAY_OF_YEAR, q);

            Date hoy = new Date();
            Calendar cHoy = new GregorianCalendar();
            cHoy.setTime(hoy);

            // cFile + timePurga < hoy
            if (cFile.before(cHoy)) {
                if (plugin.getManager().params.isMw_enabled()) {
                    List<WorldPlayerPT> allPlayers = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                            .where()
                            .lt("playerUUID", player.getPlayerUUID())
                            .findList();

                    for (WorldPlayerPT allPlayer : allPlayers) {
                        ebeanServer.getDatabase().delete(allPlayer);
                    }
                }

                ebeanServer.getDatabase().delete(player);

                contador++;
            }
        }

        return contador;
    }

    @Override
    public void DBExport(String filename) {
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

        List<PlayerPT> plClass = (List<PlayerPT>) ebeanServer.getDatabase().
                find(PlayerPT.class).findList();

        List<WorldPlayerPT> pwClass = (List<WorldPlayerPT>) ebeanServer.getDatabase().
                find(WorldPlayerPT.class).findList();

        List<SignPT> stClass = (List<SignPT>) ebeanServer.getDatabase().
                find(SignPT.class).findList();

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
    public boolean DBImport(String filename) {
        String ruta = new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append( // Separador
                        filename).toString();

        if (!UtilsFile.exists(ruta)) {
            return false;
        }

        JSONParser parser = new JSONParser();

        try {
            Object obj = parser.parse(UtilsFile.readFile(ruta));
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

        } catch (org.json.simple.parser.ParseException ex) {
            LoggerManager.logError(ex.getMessage(), ex);
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

    @Override
    public void updateConnection(Object connection) {
        this.ebeanServer = (Ebean) connection;
    }
    // </editor-fold>
}
