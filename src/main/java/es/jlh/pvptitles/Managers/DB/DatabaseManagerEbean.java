package es.jlh.pvptitles.Managers.DB;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import es.jlh.pvptitles.Libraries.Ebean;
import es.jlh.pvptitles.Main.Manager.RETROCP;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.MySQLConnection;
import es.jlh.pvptitles.Misc.TagsClass;
import es.jlh.pvptitles.Misc.UtilFile;
import es.jlh.pvptitles.Objects.LBData;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Objects.TimedPlayer;
import es.jlh.pvptitles.Tables.PlayerTable;
import es.jlh.pvptitles.Tables.PlayerWTable;
import es.jlh.pvptitles.Tables.SignTable;
import es.jlh.pvptitles.Tables.TimeTable;
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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    /* TABLA PLAYERS */
    @Override
    public void savePlayerFame(UUID playerUUID, int fame) {
        PlayerTable plClass = null;

        plClass = ebeanServer.getDatabase().find(PlayerTable.class)
                .where()
                .ieq("playerName", playerUUID.toString())
                .findUnique();

        if (plClass == null) {
            plClass = new PlayerTable();
            plClass.setPlayerName(playerUUID.toString());
        }

        // Multiworld + mundo permitido
        Player pl = Bukkit.getPlayer(playerUUID);

        PlayerWTable plwClass = null;

        if (pt.cm.params.isMw_enabled()) {
            plwClass = ebeanServer.getDatabase().find(PlayerWTable.class)
                    .where()
                    .ieq("playerName", playerUUID.toString())
                    .ieq("world", pl.getWorld().getName())
                    .findUnique();

            if (plwClass == null) {
                plwClass = new PlayerWTable();

                plwClass.setPlayerName(playerUUID.toString());
                plwClass.setWorld(pl.getWorld().getName());
            }
        }

        if (plwClass != null) {
            plwClass.setFamePoints(fame);
            ebeanServer.getDatabase().save(plwClass);
        } else {
            plClass.setFamePoints(fame);
        }

        plClass.setUltMod(new Date());

        ebeanServer.getDatabase().save(plClass);
    }

    @Override
    public int loadPlayerFame(UUID playerUUID) {
        PlayerTable plClass = null;

        Player pl = Bukkit.getServer().getPlayer(playerUUID);

        plClass = ebeanServer.getDatabase().find(PlayerTable.class)
                .where()
                .ieq("playerName", playerUUID.toString())
                .findUnique();

        if (pt.cm.params.isMw_enabled()) {
            PlayerWTable plwClass = ebeanServer.getDatabase().find(PlayerWTable.class)
                    .where()
                    .ieq("playerName", playerUUID.toString())
                    .ieq("world", pl.getWorld().getName())
                    .findUnique();

            if (plwClass == null) {
                return 0;
            }

            return plwClass.getFamePoints();
        } else {
            if (plClass == null) {
                return 0;
            }

            return plClass.getFamePoints();
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID, String world) {
        int fame = 0;

        PlayerWTable plwClass = ebeanServer.getDatabase().find(PlayerWTable.class)
                .where()
                .ieq("playerName", playerUUID.toString())
                .ieq("world", world)
                .findUnique();

        if (plwClass != null) {
            return plwClass.getFamePoints();
        }

        return fame;
    }

    @Override
    public void firstRunPlayer(Player player) {
        PlayerTable plClass = null;

        UUID playerUUID = player.getUniqueId();

        plClass = ebeanServer.getDatabase().find(PlayerTable.class)
                .where()
                .ieq("playerName", playerUUID.toString())
                .findUnique();

        if (plClass == null) {
            plClass = new PlayerTable();
            plClass.setPlayerName(playerUUID.toString());
            plClass.setFamePoints(0);
        }

        plClass.setUltMod(new Date());

        ebeanServer.getDatabase().save(plClass);

        // MultiWorld   
        if (pt.cm.params.isMw_enabled()) {
            PlayerWTable plwClass = ebeanServer.getDatabase().find(PlayerWTable.class)
                    .where()
                    .ieq("playerName", playerUUID.toString())
                    .ieq("world", player.getWorld().getName())
                    .findUnique();

            if (plwClass == null) {
                plwClass = new PlayerWTable();

                plwClass.setPlayerName(playerUUID.toString());
                plwClass.setFamePoints(0);
                plwClass.setWorld(player.getWorld().getName());

                ebeanServer.getDatabase().save(plwClass);
            }
        }
    }

    @Override
    public void savePlayedTime(TimedPlayer tPlayer) {
        TimeTable timeClass = null;

        timeClass = ebeanServer.getDatabase().find(TimeTable.class)
                .where()
                .ieq("playerName", tPlayer.getUniqueId().toString())
                .findUnique();

        if (timeClass == null) {
            timeClass = new TimeTable();
            timeClass.setPlayerName(tPlayer.getUniqueId().toString());
            timeClass.setPlayedTime(tPlayer.getTotalOnline());
        } else {
            timeClass.setPlayedTime(tPlayer.getTotalOnline() + timeClass.getPlayedTime());
        }

        ebeanServer.getDatabase().save(timeClass);
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) {
        TimeTable timeClass = null;

        timeClass = ebeanServer.getDatabase().find(TimeTable.class)
                .where()
                .ieq("playerName", playerUUID.toString())
                .findUnique();

        if (timeClass == null) {
            return 0;
        }

        return timeClass.getPlayedTime();
    }

    /* TABLA CARTELES */
    @Override
    public void registraCartel(String nombre, String modelo, String server,
            Location l, String orientacion, int blockface) {
        SignTable st = new SignTable();

        st.setNombre(nombre);
        st.setModelo(modelo);
        st.setLocation(l);
        st.setOrientacion(orientacion);
        st.setBlockface(blockface);

        ebeanServer.getDatabase().save(st);
    }

    @Override
    public void modificaCartel(Location l) {
        // Nothing yet
    }

    @Override
    public void borraCartel(Location l) {
        SignTable st = ebeanServer.getDatabase().find(SignTable.class)
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
        List<SignTable> plClass = ebeanServer.getDatabase().find(SignTable.class).findList();
        ArrayList<LBData> sd = new ArrayList();

        for (Iterator<SignTable> it = plClass.iterator(); it.hasNext();) {
            SignTable st = it.next();

            Location l = new Location(pt.getServer().getWorld(st.getWorld()), st.getX(), st.getY(), st.getZ());
            LBData sdc = new LBData(st.getNombre(), st.getModelo(), "", l);
            sdc.setOrientacion(st.getOrientacion());
            sdc.setBlockface(st.getBlockface());

            sd.add(sdc);
        }

        return sd;
    }

    /* OTROS */
    @Override
    public ArrayList<PlayerFame> getTopPlayers(int cant, String server) {
        List<PlayerWTable> allPlayersW = new ArrayList();
        List<PlayerTable> allPlayers = new ArrayList();

        if (pt.cm.params.isMw_enabled()) {
            String mundos = "";

            for (HashMap<Integer, List<String>> allServers : pt.cm.servers.values()) {
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

            allPlayersW = ebeanServer.getDatabase().find(PlayerWTable.class)
                    .select("playerName, famePoints, world")
                    .where(mundos)
                    .orderBy("famePoints desc")
                    .setMaxRows(cant)
                    .findList();

        } else {
            allPlayers = ebeanServer.getDatabase().find(PlayerTable.class)
                    .select("playerName, famePoints")
                    .orderBy("famePoints desc")
                    .setMaxRows(cant)
                    .findList();
        }

        ArrayList<PlayerFame> rankedPlayers = new ArrayList();

        if (pt.cm.params.isMw_enabled()) {
            List<String> worlds_disabled = pt.cm.params.getAffectedWorlds();
            
            for (int i = 0; i < allPlayersW.size(); i++) {
                if (!pt.cm.params.showOnLeaderBoard() && worlds_disabled.contains(allPlayersW.get(i).getWorld())) 
                    continue;
                
                PlayerFame pf = new PlayerFame(allPlayersW.get(i).getPlayerName(),
                        allPlayersW.get(i).getFamePoints(), this.pt);
                pf.setWorld(allPlayersW.get(i).getWorld());
                rankedPlayers.add(pf);
            }
        } else {
            for (int i = 0; i < allPlayers.size(); i++) {
                PlayerFame pf = new PlayerFame(allPlayers.get(i).getPlayerName(),
                        allPlayers.get(i).getFamePoints(), this.pt);
                rankedPlayers.add(pf);
            }
        }

        return rankedPlayers;
    }

    @Override
    public String getServerName(int id) {
        return this.pt.cm.params.getNameS();
    }

    @Override
    public int purgeData() {
        int contador = 0;

        List<PlayerTable> allDates = ebeanServer.getDatabase().find(PlayerTable.class)
                .select("playerName, ultMod")
                .where()
                .lt("ultMod", new Date())
                .findList();

        for (PlayerTable player : allDates) {
            if (pt.cm.params.getNoPurge().contains(player.getPlayerName())) {
                continue;
            }

            Date fechaFile = player.getUltMod();
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
                    List<PlayerWTable> allPlayers = ebeanServer.getDatabase().find(PlayerWTable.class)
                            .where()
                            .lt("playerName", player.getPlayerName())
                            .findList();

                    for (PlayerWTable allPlayer : allPlayers) {
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
    public void conversorUUID() {
        //Guardo usuarios de la bbdd         
        List<PlayerTable> plClass = (List<PlayerTable>) ebeanServer.getDatabase().find(PlayerTable.class)
                .findList();

        for (Iterator<PlayerTable> iterator = plClass.iterator(); iterator.hasNext();) {
            PlayerTable next = iterator.next();

            try {
                UUID.fromString(next.getPlayerName());
            } catch (IllegalArgumentException ex) {
                next.setPlayerName(Bukkit.getServer().getOfflinePlayer(next.getPlayerName()).getUniqueId().toString());
            }
        }

        ebeanServer.getDatabase().save(plClass);
    }

    @Override
    public void conversor() {
        File file = new File((new StringBuilder()).append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "players").toString()); // Players

        if (file.exists()) {
            int contador = 0;

            File[] allFiles = file.listFiles();

            for (File item : allFiles) {
                // Fama del jugador
                FileConfiguration yaml = YamlConfiguration.loadConfiguration(item);

                String nPlayer = item.getName().substring(0, item.getName().indexOf("."));
                int fame = yaml.getInt("Fame");

                // Recibo el contenido de la bd
                List<PlayerTable> plClass = (List<PlayerTable>) ebeanServer.getDatabase().find(PlayerTable.class)
                        .findList();

                // Compruebo si esa vacia
                if (plClass == null) {
                    plClass = new ArrayList();
                }

                boolean continuar = true;

                // Compruebo si ya existia
                for (Iterator<PlayerTable> it = plClass.iterator(); it.hasNext();) {
                    if (it.next().getPlayerName().equals(nPlayer)) {
                        continuar = false;
                        break;
                    }
                }

                if (!continuar) {
                    continue;
                }

                PlayerTable pl = new PlayerTable();

                pl.setPlayerName(nPlayer);
                pl.setFamePoints(fame);
                pl.setUltMod(new Date());

                plClass.add(pl);
                ebeanServer.getDatabase().save(plClass);

                contador++;
            }

            pt.getServer().getConsoleSender().sendMessage(
                    PLUGIN + ChatColor.YELLOW + contador + " users imported correctly");

            File backup = new File((new StringBuilder()).append(
                    this.pt.getDataFolder()).append( // Ruta
                            File.separator).append( // Separador
                            "you_can_delete_this").toString()); // Players

            if (backup.exists()) {
                // Elimino la carpeta si ya existia
                File[] content = backup.listFiles();
                for (File user : content) {
                    user.delete();
                }
                backup.delete();
            }

            file.renameTo(backup);
        }
    }

    @Override
    public void SQLExport() {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "database.sql").toString();

        String sql = "";
        sql += MySQLConnection.getTablePS() + "\n";
        sql += MySQLConnection.getTablePW() + "\n";
        sql += MySQLConnection.getTableSS() + "\n";
        sql += MySQLConnection.getTablePT() + "\n";

        List<PlayerTable> plClass = (List<PlayerTable>) ebeanServer.getDatabase().
                find(PlayerTable.class).findList();

        List<PlayerWTable> pwClass = (List<PlayerWTable>) ebeanServer.getDatabase().
                find(PlayerWTable.class).findList();

        List<SignTable> stClass = (List<SignTable>) ebeanServer.getDatabase().
                find(SignTable.class).findList();

        List<TimeTable> ttClass = (List<TimeTable>) ebeanServer.getDatabase().
                find(TimeTable.class).findList();

        if (plClass != null && plClass.size() > 0) {
            sql += "insert into PlayerServer(playerName, famePoints, ultMod) values ";

            for (Iterator<PlayerTable> iterator = plClass.iterator(); iterator.hasNext();) {
                PlayerTable next = iterator.next();
                String fecha = new java.sql.Date(next.getUltMod().getTime()).toString();
                sql += "('" + next.getPlayerName() + "', "
                        + next.getFamePoints() + ", '" + fecha + "'),";
            }

            sql = sql.substring(0, sql.length() - 1); // Quito la coma sobrante
            sql += ";\n";
        }

        if (pwClass != null && pwClass.size() > 0) {
            sql += "insert into PlayerWorld(playerName, worldName, famePoints) values ";

            for (Iterator<PlayerWTable> iterator = pwClass.iterator(); iterator.hasNext();) {
                PlayerWTable next = iterator.next();
                sql += "('" + next.getPlayerName() + "', '" + next.getWorld()
                        + "', " + next.getFamePoints() + "),";
            }

            sql = sql.substring(0, sql.length() - 1); // Quito la coma sobrante
            sql += ";\n";
        }

        if (stClass != null && stClass.size() > 0) {
            sql += "insert into SignsServer values";

            for (Iterator<SignTable> iterator = stClass.iterator(); iterator.hasNext();) {
                SignTable next = iterator.next();
                sql += "('" + next.getNombre() + "', '" + next.getModelo()
                        + "', '', -1, '" + next.getOrientacion() + "', '"
                        + next.getWorld() + "', " + next.getX() + ", " + next.getY() + ", "
                        + next.getZ() + ", " + next.getBlockface() + "),";
            }

            sql = sql.substring(0, sql.length() - 1); // Quito la coma sobrante
            sql += ";";
        }

        if (ttClass != null && ttClass.size() > 0) {
            sql += "insert into PlayerTime values";

            for (Iterator<TimeTable> iterator = ttClass.iterator(); iterator.hasNext();) {
                TimeTable next = iterator.next();
                sql += "('" + next.getPlayerName() + "', " + next.getPlayedTime() + "),";
            }

            sql = sql.substring(0, sql.length() - 1); // Quito la coma sobrante
            sql += ";";
        }

        UtilFile.writeFile(ruta, sql);
    }

    @Override
    public void exportarData(RETROCP rcp) {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "database_temp.json").toString();

        // Estilo
        JsonParser parser = new JsonParser();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JSONObject jo = new JSONObject();

        JSONArray players = new JSONArray();
        JSONArray signs = new JSONArray();
        JSONArray pworlds = new JSONArray();
        JSONArray ptimes = new JSONArray();

        List<PlayerTable> plClass = (List<PlayerTable>) ebeanServer.getDatabase().
                find(PlayerTable.class).findList();

        List<SignTable> stClass = (List<SignTable>) ebeanServer.getDatabase().
                find(SignTable.class).findList();

        for (PlayerTable next : plClass) {
            players.add(TagsClass.createPlayer(next));
        }
        jo.put("Players", players);

        for (SignTable next : stClass) {
            signs.add(TagsClass.createSign(next));
        }
        jo.put("Signs", signs);

        if (rcp.equals(RETROCP.MW_CREATED) || rcp.equals(RETROCP.TIME_CREATED)) {
            List<PlayerWTable> pwClass = (List<PlayerWTable>) ebeanServer.getDatabase().
                    find(PlayerWTable.class).findList();

            for (PlayerWTable next : pwClass) {
                pworlds.add(TagsClass.createPlayerW(next));
            }
            jo.put("PlayersPerWorld", pworlds);
        }

        if (rcp.equals(RETROCP.TIME_CREATED)) {
            List<TimeTable> ttClass = (List<TimeTable>) ebeanServer.getDatabase().
                    find(TimeTable.class).findList();

            for (TimeTable ttClas : ttClass) {
                ptimes.add(TagsClass.createPlayerTime(ttClas));
            }
            jo.put("PlayedTime", ptimes);
        }

        // Escribo el fichero
        JsonElement el = parser.parse(jo.toJSONString());
        UtilFile.writeFile(ruta, gson.toJson(el));
    }

    @Override
    public void importarData(RETROCP rcp) {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "database_temp.json").toString();

        if (UtilFile.exists(ruta)) {
            JSONParser parser = new JSONParser();

            try {
                Object obj = parser.parse(UtilFile.readFile(ruta));
                JSONObject jsonObject = (JSONObject) obj;

                JSONArray players = (JSONArray) jsonObject.get("Players");
                JSONArray signs = (JSONArray) jsonObject.get("Signs");
                JSONArray pworlds = (JSONArray) jsonObject.get("PlayersPerWorld");
                JSONArray ptimes = (JSONArray) jsonObject.get("PlayedTime");

                List<PlayerTable> playersT = TagsClass.getPlayers(players);
                for (PlayerTable playersT1 : playersT) {
                    PlayerTable pst = ebeanServer.getDatabase().find(PlayerTable.class)
                            .where()
                            .ieq("playerName", playersT1.getPlayerName())
                            .findUnique();

                    // Evito duplicados
                    if (pst != null) {
                        pst.setFamePoints(playersT1.getFamePoints());
                        pst.setUltMod(playersT1.getUltMod());
                        ebeanServer.getDatabase().save(pst);
                    } else {
                        ebeanServer.getDatabase().save(playersT1);
                    }
                }

                List<SignTable> signsT = TagsClass.getSigns(signs);
                for (SignTable signsT1 : signsT) {
                    SignTable st = ebeanServer.getDatabase().find(SignTable.class)
                            .where()
                            .eq("x", signsT1.getX())
                            .eq("y", signsT1.getY())
                            .eq("z", signsT1.getZ())
                            .ieq("world", signsT1.getWorld())
                            .findUnique();

                    if (st != null) {
                        st.setNombre(signsT1.getNombre());
                        st.setModelo(signsT1.getModelo());
                        st.setBlockface(signsT1.getBlockface());
                        st.setOrientacion(signsT1.getOrientacion());

                        ebeanServer.getDatabase().save(st);
                    } else {
                        ebeanServer.getDatabase().save(signsT1);
                    }
                }

                if (rcp.equals(RETROCP.MW_CREATED) || rcp.equals(RETROCP.TIME_CREATED)) {
                    List<PlayerWTable> pworldsT = TagsClass.getPWorlds(pworlds);
                    for (PlayerWTable pworldsT1 : pworldsT) {
                        PlayerWTable pwt = ebeanServer.getDatabase().find(PlayerWTable.class)
                                .where()
                                .ieq("playerName", pworldsT1.getPlayerName())
                                .findUnique();

                        // Evito duplicados
                        if (pwt != null) {
                            pwt.setFamePoints(pworldsT1.getFamePoints());
                            pwt.setWorld(pworldsT1.getWorld());
                            ebeanServer.getDatabase().save(pwt);
                        } else {
                            ebeanServer.getDatabase().save(pworldsT1);
                        }
                    }
                }

                if (rcp.equals(RETROCP.TIME_CREATED)) {
                    List<TimeTable> pTime = TagsClass.getPlayersTime(ptimes);

                    for (TimeTable pTime1 : pTime) {
                        TimeTable tt = ebeanServer.getDatabase().find(TimeTable.class)
                                .where()
                                .ieq("playerName", pTime1.getPlayerName())
                                .findUnique();

                        // Evito duplicados
                        if (tt != null) {
                            tt.setPlayedTime(pTime1.getPlayedTime());
                            ebeanServer.getDatabase().save(tt);
                        } else {
                            ebeanServer.getDatabase().save(pTime1);
                        }
                    }
                }

                UtilFile.delete(ruta);
            } catch (ParseException ex) {
            }
        }
    }
}
