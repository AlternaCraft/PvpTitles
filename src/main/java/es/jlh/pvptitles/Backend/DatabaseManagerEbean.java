package es.jlh.pvptitles.Backend;

import es.jlh.pvptitles.Libraries.Ebean;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.TagsClass;
import es.jlh.pvptitles.Misc.UtilsFile;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Managers.Timer.TimedPlayer;
import es.jlh.pvptitles.Backend.EbeanTables.PlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.SignPT;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoard;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author AlternaCraft
 */
public class DatabaseManagerEbean implements DatabaseManager {

    private static final String FILENAME_IMPORT = "database.json";
    private static final String FILENAME_EXPORT = "database.sql";

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private PvpTitles pt = null;
    private Ebean ebeanServer = null;

    public DatabaseManagerEbean(PvpTitles pt, Ebean ebeanServer) {
        this.pt = pt;
        this.ebeanServer = ebeanServer;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="PLAYERS...">
    @Override
    public boolean playerConnection(Player player) {
        PlayerPT plClass = null;

        if (player == null) {
            return false;
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
        if (pt.cm.params.isMw_enabled()) {
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

        return true;
    }

    /* TABLA PLAYERS */
    @Override
    public boolean savePlayerFame(UUID playerUUID, int fame, String w) {
        // Multiworld + mundo permitido
        OfflinePlayer pl = pt.getServer().getOfflinePlayer(playerUUID);

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

        if (pt.cm.params.isMw_enabled()) {
            if (w == null && !pl.isOnline()) {
                return false;
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

        return true;
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String w) {
        int points = 0;

        OfflinePlayer pl = pt.getServer().getOfflinePlayer(playerUUID);

        if (pl == null) {
            return points;
        }

        if (pt.cm.params.isMw_enabled()) {
            if (w == null && !pl.isOnline()) {
                return points;
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
    public boolean savePlayedTime(TimedPlayer tPlayer) {
        PlayerPT plClass = null;

        if (tPlayer == null) {
            return false;
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

        return true;
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) {
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
    public ArrayList<PlayerFame> getTopPlayers(short cant, String server) {
        List<WorldPlayerPT> allPlayersW;
        List<PlayerPT> allPlayers;

        ArrayList<PlayerFame> rankedPlayers = new ArrayList();

        if (pt.cm.params.isMw_enabled()) {
            String mundos = "";

            if (!pt.cm.params.showOnLeaderBoard()) {
                List<String> worlds_disabled = pt.cm.params.getAffectedWorlds();
                
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
                        this.pt);

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
                        this.pt);
                rankedPlayers.add(pf);
            }
        }

        return rankedPlayers;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="SIGNS...">
    /* TABLA CARTELES */
    @Override
    public boolean registraBoard(SignBoard sb) {
        SignPT st = new SignPT();
        st.setName(sb.getData().getNombre());
        st.setModel(sb.getData().getModelo());
        st.setLocation(sb.getData().getLocation());
        st.setOrientation(sb.getData().getOrientacion());
        st.setBlockface(sb.getData().getPrimitiveBlockface());

        ebeanServer.getDatabase().save(st);
        return true;
    }

    @Override
    public boolean modificaBoard(Location l) {
        // Nothing yet
        return false;
    }

    @Override
    public boolean borraBoard(Location l) {
        SignPT st = ebeanServer.getDatabase().find(SignPT.class)
                .where()
                .eq("x", l.getX())
                .eq("y", l.getY())
                .eq("z", l.getZ())
                .ieq("world", l.getWorld().getName())
                .findUnique();

        ebeanServer.getDatabase().delete(st);
        return true;
    }

    @Override
    public ArrayList<SignBoardData> buscaBoards() {
        List<SignPT> plClass = ebeanServer.getDatabase().find(SignPT.class).findList();
        ArrayList<SignBoardData> sbd = new ArrayList();

        for (SignPT signPT : plClass) {
            Location l = new Location(pt.getServer().getWorld(signPT.getWorld()), signPT.getX(), signPT.getY(), signPT.getZ());
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
    public void DBExport(String filename) {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        filename).toString();

        short serverID = pt.cm.params.getMultiS();

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

        sql += "insert into Servers values (" + serverID + ", '" + pt.cm.params.getNameS() + "')"
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
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
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
                    spt.setLocation(new Location(pt.getServer().getWorld(signsPT.getWorld()),
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
            PvpTitles.logError(ex.getMessage(), ex);
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
    // </editor-fold>

}
