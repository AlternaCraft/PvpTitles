package es.jlh.pvptitles.Managers.DB;

import es.jlh.pvptitles.Libraries.Ebean;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.MySQLConnection;
import es.jlh.pvptitles.Misc.UtilFile;
import es.jlh.pvptitles.Objects.LBSigns.LBData;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Objects.TimedPlayer;
import es.jlh.pvptitles.Objects.DB.PlayerPT;
import es.jlh.pvptitles.Objects.DB.WorldPlayerPT;
import es.jlh.pvptitles.Objects.DB.SignPT;
import java.io.File;
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
import org.bukkit.entity.Player;

/**
 *
 * @author julito
 */
public class DatabaseManagerEbean implements DatabaseManager {

    private PvpTitles pt = null;
    private Ebean ebeanServer = null;

    public DatabaseManagerEbean(PvpTitles pt, Ebean ebeanServer) {
        this.pt = pt;
        this.ebeanServer = ebeanServer;
    }

    @Override
    public void PlayerConnection(Player player) {
        PlayerPT plClass = null;

        UUID playerUUID = player.getUniqueId();

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
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
        if (pt.cm.params.isMw_enabled()) {
            WorldPlayerPT plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
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
    public void savePlayerFame(UUID playerUUID, int fame) {
        PlayerPT plClass = null;

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .where()
                .ieq("playerUUID", playerUUID.toString())
                .findUnique();

        if (plClass == null) {
            plClass = new PlayerPT();
            plClass.setPlayerUUID(playerUUID.toString());
        }

        // Multiworld + mundo permitido
        Player pl = Bukkit.getPlayer(playerUUID);

        WorldPlayerPT plwClass = null;

        if (pt.cm.params.isMw_enabled()) {
            plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .ieq("world", pl.getWorld().getName())
                    .findUnique();

            if (plwClass == null) {
                plwClass = new WorldPlayerPT();

                plwClass.setPlayerUUID(playerUUID.toString());
                plwClass.setWorld(pl.getWorld().getName());
            }
        }

        if (plwClass != null) {
            plwClass.setPoints(fame);
            ebeanServer.getDatabase().save(plwClass);
        } else {
            plClass.setPoints(fame);
            ebeanServer.getDatabase().save(plClass);
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String world) {
        PlayerPT plClass = null;
        int points = 0;

        Player pl = Bukkit.getServer().getPlayer(playerUUID);

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .where()
                .ieq("playerUUID", playerUUID.toString())
                .findUnique();

        if (pt.cm.params.isMw_enabled()) {
            String world_selected = (world == null) ? pl.getWorld().getName() : world;

            WorldPlayerPT plwClass = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                    .where()
                    .ieq("playerUUID", playerUUID.toString())
                    .ieq("world", world_selected)
                    .findUnique();

            if (plwClass != null) {
                points = plwClass.getPoints();
            }
        } else if (plClass != null) {
            points = plClass.getPoints();
        }

        return points;
    }

    @Override
    public void savePlayedTime(TimedPlayer tPlayer) {
        PlayerPT plClass = null;

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
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
    public int loadPlayedTime(UUID playerUUID) {
        PlayerPT plClass = null;
        int time = 0;

        plClass = ebeanServer.getDatabase().find(PlayerPT.class)
                .where()
                .ieq("playerUUID", playerUUID.toString())
                .findUnique();

        if (plClass != null) {
            time = plClass.getPlayedTime();
        }

        return time;
    }

    /* TABLA CARTELES */
    @Override
    public void registraCartel(String nombre, String modelo, String server,
            Location l, String orientacion, short blockface) {
        SignPT st = new SignPT();

        st.setName(nombre);
        st.setModel(modelo);
        st.setLocation(l);
        st.setOrientation(orientacion);
        st.setBlockface(blockface);

        ebeanServer.getDatabase().save(st);
    }

    @Override
    public void modificaCartel(Location l) {
        // Nothing yet
    }

    @Override
    public void borraCartel(Location l) {
        SignPT st = ebeanServer.getDatabase().find(SignPT.class)
                .where()
                .eq("x", l.getX())
                .eq("y", l.getY())
                .eq("z", l.getZ())
                .ieq("world", l.getWorld().getName())
                .findUnique();

        ebeanServer.getDatabase().delete(st);
    }

    @Override
    public ArrayList<LBData> buscaCarteles() {
        List<SignPT> plClass = ebeanServer.getDatabase().find(SignPT.class).findList();
        ArrayList<LBData> sd = new ArrayList();

        for (Iterator<SignPT> it = plClass.iterator(); it.hasNext();) {
            SignPT st = it.next();

            Location l = new Location(pt.getServer().getWorld(st.getWorld()), st.getX(), st.getY(), st.getZ());
            LBData sdc = new LBData(st.getName(), st.getModel(), "", l);
            sdc.setOrientacion(st.getOrientation());
            sdc.setBlockface(st.getBlockface());

            sd.add(sdc);
        }

        return sd;
    }

    /* OTROS */
    @Override
    public ArrayList<PlayerFame> getTopPlayers(short cant, String server) {
        List<WorldPlayerPT> allPlayersW = new ArrayList();
        List<PlayerPT> allPlayers = new ArrayList();

        if (pt.cm.params.isMw_enabled()) {
            String mundos = "";

            for (HashMap<Short, List<String>> allServers : pt.cm.servers.values()) {
                if (allServers.containsKey(pt.cm.params.getMultiS())) {
                    List<String> worlds = allServers.get(pt.cm.params.getMultiS());

                    for (String world : worlds) {
                        mundos += "world = '" + world + "' OR ";
                    }

                    if (!worlds.isEmpty()) {
                        mundos = mundos.substring(0, mundos.length() - 4);
                    }

                    break;
                }
            }

            allPlayersW = ebeanServer.getDatabase().find(WorldPlayerPT.class)
                    .select("playerUUID, points, world")
                    .where(mundos)
                    .orderBy("points desc")
                    .setMaxRows(cant)
                    .findList();

        } else {
            allPlayers = ebeanServer.getDatabase().find(PlayerPT.class)
                    .select("playerUUID, points")
                    .orderBy("points desc")
                    .setMaxRows(cant)
                    .findList();
        }

        ArrayList<PlayerFame> rankedPlayers = new ArrayList();

        if (pt.cm.params.isMw_enabled()) {
            List<String> worlds_disabled = pt.cm.params.getAffectedWorlds();

            for (int i = 0; i < allPlayersW.size(); i++) {
                if (!pt.cm.params.showOnLeaderBoard() && worlds_disabled.contains(allPlayersW.get(i).getWorld())) {
                    continue;
                }

                PlayerFame pf = new PlayerFame(allPlayersW.get(i).getPlayerUUID(),
                        allPlayersW.get(i).getPoints(), this.pt);
                pf.setWorld(allPlayersW.get(i).getWorld());
                rankedPlayers.add(pf);
            }
        } else {
            for (int i = 0; i < allPlayers.size(); i++) {
                PlayerFame pf = new PlayerFame(allPlayers.get(i).getPlayerUUID(),
                        allPlayers.get(i).getPoints(), this.pt);
                rankedPlayers.add(pf);
            }
        }

        return rankedPlayers;
    }

    @Override
    public String getServerName(short id) {
        return this.pt.cm.params.getNameS();
    }

    @Override
    public int purgeData() {
        int contador = 0;

        List<PlayerPT> allDates = ebeanServer.getDatabase().find(PlayerPT.class)
                .select("playerUUID, lastLogin")
                .where()
                .lt("lastLogin", new Date())
                .findList();

        for (PlayerPT player : allDates) {
            if (pt.cm.params.getNoPurge().contains(player.getPlayerUUID())) {
                continue;
            }

            Date fechaFile = player.getLastLogin();
            Calendar cFile = new GregorianCalendar();
            cFile.setTime(fechaFile);

            // Tiempo en config
            cFile.add(GregorianCalendar.DAY_OF_YEAR, pt.cm.params.getTimeP());

            Date hoy = new Date();
            Calendar cHoy = new GregorianCalendar();
            cHoy.setTime(hoy);

            // cFile + timePurga < hoy
            if (cFile.before(cHoy)) {
                if (pt.cm.params.isMw_enabled()) {
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
    public void DBExport() {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "database.sql").toString();

        short serverID = pt.cm.params.getMultiS();

        String sql = "";
        sql += MySQLConnection.getTablePlayerServer() + "\n";
        sql += MySQLConnection.getTablePlayerMeta() + "\n";
        sql += MySQLConnection.getTablePlayerWorld() + "\n";
        sql += MySQLConnection.getTableServers() + "\n";
        sql += MySQLConnection.getTableSigns() + "\n";

        List<PlayerPT> plClass = (List<PlayerPT>) ebeanServer.getDatabase().
                find(PlayerPT.class).findList();

        List<WorldPlayerPT> pwClass = (List<WorldPlayerPT>) ebeanServer.getDatabase().
                find(WorldPlayerPT.class).findList();

        List<SignPT> stClass = (List<SignPT>) ebeanServer.getDatabase().
                find(SignPT.class).findList();

        int id = 0;
        boolean mw = pwClass != null && pwClass.size() > 0;

        sql += "insert into Servers values (" + serverID + ", '" + pt.cm.params.getNameS() + "');\n";

        if (plClass != null && plClass.size() > 0) {
            String sql1 = "insert into PlayerServer(id, playerUUID, serverID) values ";
            String sql2 = "insert into PlayerMeta(psid, points, playedTime, lastLogin) values ";
            String sql3 = "";

            if (mw) {
                sql3 = "insert into PlayerWorld(psid, worldName, points) values ";
            }

            for (int j = 0; j < plClass.size(); j++) {
                PlayerPT next = plClass.get(j);

                String fecha = new java.sql.Date(next.getLastLogin().getTime()).toString();

                sql1 += "(" + id + ", '" + next.getPlayerUUID() + "', " + serverID + "),";
                sql2 += "(" + id + ", " + next.getPoints() + ", " + next.getPlayedTime() + ", '" + fecha + "'),";

                if (mw) {
                    for (int k = 0; k < pwClass.size(); k++) {
                        WorldPlayerPT nextW = pwClass.get(k);
                        if (nextW.getPlayerUUID().equals(next.getPlayerUUID())) {
                            sql3 += "(" + id + ", '" + nextW.getWorld() + "', " + nextW.getPoints() + "),";
                            // optimizacion
                            pwClass.remove(k);
                            k--;
                        }
                    }
                }

                id++;
            }

            sql1 = sql1.substring(0, sql1.length() - 1) + ";\n"; // Quito la coma sobrante
            sql2 = sql2.substring(0, sql2.length() - 1) + ";\n"; // Quito la coma sobrante
            if (mw) {
                sql3 = sql3.substring(0, sql3.length() - 1) + ";\n"; // Quito la coma sobrante
            }

            sql += sql1 + sql2 + sql3;
        }

        if (stClass != null && stClass.size() > 0) {
            sql += "insert into Signs values";

            for (Iterator<SignPT> iterator = stClass.iterator(); iterator.hasNext();) {
                SignPT next = iterator.next();
                sql += "('" + next.getName() + "', '" + next.getModel() + "', "
                        + "'', " + serverID + ", '" + next.getOrientation() + "', "
                        + next.getBlockface() + ", '" + next.getWorld() + "', "
                        + next.getX() + ", " + next.getY() + ", " + next.getZ() + "),";
            }

            sql = sql.substring(0, sql.length() - 1); // Quito la coma sobrante
            sql += ";";
        }

        UtilFile.writeFile(ruta, sql);
    }
}
