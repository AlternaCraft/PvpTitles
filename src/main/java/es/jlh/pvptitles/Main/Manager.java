package es.jlh.pvptitles.Main;

import es.jlh.pvptitles.Files.CommandFile;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Files.LangFile.LangType;
import es.jlh.pvptitles.Files.ModelsFile;
import es.jlh.pvptitles.Files.ServersFile;
import es.jlh.pvptitles.Libraries.Ebean;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.DB.DatabaseManager;
import es.jlh.pvptitles.Managers.DB.DatabaseManagerEbean;
import es.jlh.pvptitles.Managers.DB.DatabaseManagerMysql;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Misc.MySQLConnection;
import es.jlh.pvptitles.Misc.Settings;
import es.jlh.pvptitles.Objects.LBSigns.CustomSign;
import es.jlh.pvptitles.Objects.LBSigns.LBData;
import es.jlh.pvptitles.Objects.LBSigns.LBModel;
import es.jlh.pvptitles.Managers.LeaderBoardManager;
import es.jlh.pvptitles.Objects.TimedPlayer;
import es.jlh.pvptitles.Objects.DB.PlayerPT;
import es.jlh.pvptitles.Objects.DB.WorldPlayerPT;
import es.jlh.pvptitles.Objects.DB.SignPT;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author julito
 */
public final class Manager {

    // Variable que almacena el plugin
    private PvpTitles pvpTitles = null;
    // Configuracion del config principal
    private FileConfiguration config = null;

    // Gestor de leaderboards
    private LeaderBoardManager lbm = null;

    // Instancia de la clase
    public static Manager instance = new Manager();

    // Objetos para guardar los Titulos con sus respectivos puntos    
    private static final Map<Integer, String> rankList = new HashMap();
    private static final Map<Integer, Integer> reqFame = new HashMap();
    private static final Map<Integer, Integer> reqTime = new HashMap();

    // Modelos
    public ArrayList<LBModel> modelos = new ArrayList();

    // Recompensas
    public HashMap<String, HashMap<String, List<String>>> commandsRw = null;

    // Servers
    public HashMap<String, HashMap<Short, List<String>>> servers = null;

    // Resto de parametros
    public Settings params = null;

    // Chat
    public static LangType messages = null;

    // BD
    private DatabaseManager dm = null;
    public Ebean ebeanServer = null;
    private Connection mysql = null;

    public static DBTYPE tipo = null;

    public static enum DBTYPE {

        EBEAN,
        MYSQL;
    }

    // Entero con el numero del evento
    private int eventoActualizador = -1;

    // Entero con el numero del evento
    private int eventoChecker = -1;

    /**
     * Contructor de la clase
     */
    private Manager() {
        modelos = new ArrayList();
        commandsRw = new HashMap();
        servers = new HashMap();
        params = new Settings();
        tipo = DBTYPE.EBEAN;
    }

    /**
     * Instancia de la clase
     * <p>
     * De esta forma evito que se creen diferentes objetos de la clase</p>
     *
     * @return Instancia de la clase
     */
    public static Manager getInstance() {
        return instance;
    }

    /**
     * Método para cargar el config principal
     *
     * @param plugin Objeto de la clase principal
     * @return boolean
     */
    public boolean setup(PvpTitles plugin) {
        this.pvpTitles = plugin;
        this.lbm = new LeaderBoardManager(plugin);

        this.loadConfigPrincipal();

        this.selectDB();
        if (tipo == DBTYPE.EBEAN) {
            if (params.isAuto_export_to_sql()) {
                dm.DBExport();
            }
        } else if (tipo == DBTYPE.MYSQL) {
            this.loadServers();
            if (params.isAuto_export_to_json()) {
                dm.DBExport();
            }
        }

        // Idioma, comandos y servers
        this.loadLang();
        this.loadCommands();

        /* Signs */
        this.loadModels();
        this.loadSavedSigns();
        this.loadActualizador();

        /* Rank checker */
        this.loadRankChecker();

        return true;
    }

    /**
     * Método para llamar a los Métodos que cargan el fichero y guardan los
     * datos del mismo en variables
     */
    public void loadConfigPrincipal() {
        this.createConfig();
        this.loadData();
    }

    /**
     * Método para crear el config en caso de que no exista
     */
    public void createConfig() {
        if (!new File(new StringBuilder().append(
                pvpTitles.getDataFolder()).append(
                        File.separator).append(
                        "config.yml").toString()).exists() || !checkVersion()) {
            pvpTitles.saveDefaultConfig();
        }

        pvpTitles.reloadConfig();
        config = pvpTitles.getConfig();
    }

    /**
     * Método para comprobar la version del config
     *
     * @return Si la version es la correcta o no
     */
    public boolean checkVersion() {
        File backupFile = new File(new StringBuilder().append(
                pvpTitles.getDataFolder()).append(
                        File.separator).append(
                        "config.backup.yml").toString());

        File configFile = new File(new StringBuilder().append(
                pvpTitles.getDataFolder()).append(
                        File.separator).append(
                        "config.yml").toString());

        YamlConfiguration configV = YamlConfiguration.loadConfiguration(configFile);

        if (!configV.contains("Version")
                || !configV.getString("Version").equals(pvpTitles.getConfig().getDefaults().getString("Version"))) {

            if (backupFile.exists()) {
                backupFile.delete();
            }

            configFile.renameTo(backupFile);
            pvpTitles.getServer().getConsoleSender().sendMessage(
                    PLUGIN + ChatColor.RED + "Mismatch config version, a new one has been created.");

            return false;
        } else {
            return true;
        }
    }

    /**
     * Método para guardar los datos del config en variables
     */
    private void loadData() {
        List<String> configList = (List<String>) config.getList("RankNames");
        List<Integer> requFame = (List<Integer>) config.getList("ReqFame");
        List<Integer> requTime = (List<Integer>) config.getList("ReqTime");

        for (int i = 0; i < configList.size(); i++) {
            rankList.put(i, configList.get(i));
        }

        for (int i = 0; i < requFame.size(); i++) {
            reqFame.put(i, requFame.get(i));
        }

        for (int i = 0; i < configList.size(); i++) {
            int seconds = 0;

            if (requTime.size() >= (i + 1) && requTime.get(i) != null) {
                seconds = requTime.get(i);
            }

            reqTime.put(i, seconds);
        }

        params.getAffectedWorlds().addAll(config.getStringList("MW-filter.affected-worlds"));

        // Todos los mundos a minusculas
        ListIterator<String> iterator = params.getAffectedWorlds().listIterator();
        while (iterator.hasNext()) {
            iterator.set(iterator.next().toLowerCase());
        }

        params.getNoPurge().addAll(config.getStringList("NoPurge"));

        params.setAuto_export_to_sql(config.getBoolean("Ebean.exportToSQL"));
        params.setAuto_export_to_json(config.getBoolean("Mysql.exportToJSON"));

        params.setPvpTitles_Bridge(config.getBoolean("Mysql.enable"));
        if (params.isPvpTitles_Bridge()) {
            tipo = DBTYPE.MYSQL;

            params.setHost(config.getString("Mysql.host"));
            params.setPort((short) config.getInt("Mysql.port"));
            params.setDb(config.getString("Mysql.database"));
            params.setUser(config.getString("Mysql.user"));
            params.setPass(config.getString("Mysql.pass"));
        } else {
            tipo = DBTYPE.EBEAN;
        }
        params.setMultiS((short) config.getInt("MultiS"));
        params.setNameS(config.getString("NameS"));

        String lang = config.getString("DefaultLang");
        messages = LangType.valueOf(lang);
        if (messages == null) {
            messages = LangType.EN;
        }

        params.setPrefixColor(config.getString("PrefixColor"));
        params.setTag(config.getString("Tag"));
        params.setPrefix(config.getString("Prefix"));
        params.setTop((short) config.getInt("Top"));
        params.setLBRefresh((short) config.getInt("LBRefresh"));
        params.setRankChecker((short) config.getInt("RankChecker"));
        params.setMod((float) config.getDouble("Mod"));
        params.setKills((short) config.getInt("Kills"));
        params.setTimeP((short) config.getInt("TimeP"));
        params.setTimeV((short) config.getInt("TimeV"));
        params.setTimeL((short) config.getInt("TimeL"));
        params.setCheckAFK(config.getBoolean("CheckAFK"));
        params.setAFKTime((short) config.getInt("AFKTime"));
        params.setUpdate(config.getBoolean("Update"));
        params.setAlert(config.getBoolean("Alert"));
        params.setMetrics(config.getBoolean("Metrics"));
        params.setDebug(config.getBoolean("Debug"));
        params.setMw_enabled(config.getBoolean("MW.enable"));
        params.setChat(config.getBoolean("MW-filter.chat"));
        params.setPoints(config.getBoolean("MW-filter.points"));
        params.setLeaderboard(config.getBoolean("MW-filter.show-on-leaderboard"));

        if (configList.size() != requFame.size()) {
            PvpTitles.logger.info("WARNING - RankNames and ReqFame are not equal in their numbers.");
            PvpTitles.logger.info("WARNING - RankNames and ReqFame are not equal in their numbers.");
            PvpTitles.logger.info("WARNING - RankNames and ReqFame are not equal in their numbers.");
        }
    }

    public void selectDB() {
        switch (tipo) {
            case EBEAN:
                // Ebean server
                this.loadConfiguration();
                this.initializeDatabase();

                this.dm = new DatabaseManagerEbean(pvpTitles, ebeanServer);
                break;
            case MYSQL:
                // MySQL server
                this.mysqlConnect();

                if (MySQLConnection.estado_conexion == MySQLConnection.Estado.SIN_CONEXION) {
                    tipo = DBTYPE.EBEAN;
                    selectDB();
                } else {
                    this.dm = new DatabaseManagerMysql(pvpTitles, mysql);
                }
                break;
        }
    }

    // ######################################################################
    // Ebean
    /**
     * Método para establecer la configuracion de la bd de ebeans
     */
    private void loadConfiguration() {
        config.set("database.driver", config.getString("database.driver", "org.sqlite.JDBC"));
        config.set("database.url", config.getString("database.url", "jdbc:sqlite:{DIR}{NAME}.db"));
        config.set("database.username", config.getString("database.username", "root"));
        config.set("database.password", config.getString("database.password", ""));
        config.set("database.isolation", config.getString("database.isolation", "SERIALIZABLE"));
        config.set("database.logging", config.getBoolean("database.logging", params.isDebug()));
        config.set("database.rebuild", config.getBoolean("database.rebuild", false)); // false
    }

    /**
     * Método para iniciar la bd con ebean
     */
    private void initializeDatabase() {
        ebeanServer = new Ebean(this.pvpTitles) {
            @Override
            protected java.util.List<Class<?>> getDatabaseClasses() {
                List<Class<?>> list = new ArrayList<>();
                list.add(PlayerPT.class);
                list.add(WorldPlayerPT.class);
                list.add(SignPT.class);

                return list;
            }
        ;
        };

        ebeanServer.initializeDatabase(
                config.getString("database.driver"),
                config.getString("database.url"),
                config.getString("database.username"),
                config.getString("database.password"),
                config.getString("database.isolation"),
                config.getBoolean("database.logging"),
                config.getBoolean("database.rebuild")
        );

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + "Ebean database " + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Conexion a MySQL
     */
    public void mysqlConnect() {
        MySQLConnection.connectDB(params.getHost() + ":" + params.getPort()
                + "/" + params.getDb(), params.getUser(), params.getPass());

        if (MySQLConnection.estado_conexion == MySQLConnection.Estado.SIN_CONEXION) {
            tipo = DBTYPE.EBEAN;
        } else {
            tipo = DBTYPE.MYSQL;
            mysql = MySQLConnection.getConnection();

            MySQLConnection.creaDefault();

            MySQLConnection.registraServer(params.getMultiS(), params.getNameS());

            this.pvpTitles.getServer().getConsoleSender().sendMessage(
                    PLUGIN + ChatColor.YELLOW + "MySQL database " + ChatColor.AQUA + "loaded correctly."
            );
        }
    }

    // ######################################################################
    /**
     * Método para cargar los modelos del bloc de notas
     */
    public void loadModels() {
        ModelsFile contenido = new ModelsFile();

        String fichero = new StringBuilder().append(
                this.pvpTitles.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "models.txt").toString();

        try {
            modelos = contenido.leeArchivo(fichero);
        } catch (IOException ex) {
            modelos = contenido.creaArchivo(this.pvpTitles);
        }

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + modelos.size() + " models "
                + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Método para guardar en memoria los scoreboards
     */
    public void loadSavedSigns() {
        List<LBData> carteles = pvpTitles.cm.getDm().buscaCarteles();

        for (LBData cartel : carteles) {
            LBModel sm = pvpTitles.cm.searchModel(cartel.getModelo());

            if (sm == null) {
                pvpTitles.cm.getDm().borraCartel(cartel.getL());

                pvpTitles.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.RED + "Sign '" + cartel.getNombre()
                        + "' removed because the model has not been found...");

                continue;
            }

            CustomSign cs = new CustomSign(cartel, sm);
            cs.setLineas(new String[0]);
            cs.setMatSign(cartel.getSignMaterial());

            lbm.loadSign(cs);
        }

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + this.lbm.getSigns().size()
                + " scoreboards " + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Método para buscar un modelo de tabla de puntuaciones
     *
     * @param modelo String con el modelo a buscar
     * @return SignModel con los datos del modelo
     */
    public LBModel searchModel(String modelo) {
        for (LBModel smc : modelos) {
            if (smc.getNombre().compareToIgnoreCase(modelo) == 0) {
                return smc;
            }
        }
        return null;
    }

    /**
     * Método para cargar los locales
     */
    public void loadLang() {
        File oldMessages = new File(new StringBuilder().append(
                pvpTitles.getDataFolder()).append(
                        File.separator).append(
                        "messages.yml").toString());

        File newMessages = new File(new StringBuilder().append(
                pvpTitles.getDataFolder()).append(
                        File.separator).append(
                        "messages_old.yml").toString());

        if (oldMessages.exists()) {
            oldMessages.renameTo(newMessages);
        }

        LangFile.load();

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + "Locales " + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Método para cargar el sistema de recompensas
     */
    public void loadCommands() {
        commandsRw = new HashMap();

        YamlConfiguration lp = new CommandFile().load();

        List<String> activos = lp.getStringList("activeRewards");

        for (Iterator<String> iterator = activos.iterator(); iterator.hasNext();) {
            String next = iterator.next();

            String rango = lp.getString("Rewards." + next + ".onRank");
            if (rango != null) {
                if (commandsRw.get("onRank") == null) {
                    commandsRw.put("onRank", new HashMap());
                }
                commandsRw.get("onRank").put(rango, lp.getStringList("Rewards." + next + ".command"));
            }

            String fama = lp.getString("Rewards." + next + ".onFame");
            if (fama != null) {
                if (commandsRw.get("onFame") == null) {
                    commandsRw.put("onFame", new HashMap());
                }
                commandsRw.get("onFame").put(fama, lp.getStringList("Rewards." + next + ".command"));
            }

            if (rango == null && fama == null) {
                if (commandsRw.get("onKill") == null) {
                    commandsRw.put("onKill", new HashMap());
                }
                commandsRw.get("onKill").put("", lp.getStringList("Rewards." + next + ".command"));
            }
        }

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + activos.size() + " rewards " + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Método para cargar los locales
     */
    public void loadServers() {
        YamlConfiguration sf = new ServersFile().load();

        Set<String> sfk = sf.getKeys(true);

        for (Iterator<String> iterator = sfk.iterator(); iterator.hasNext();) {
            String next = iterator.next();

            // Fix para evitar campos innecesarios
            if (next.equals("Worlds")) {
                break;
            }

            List<Short> serverIDs = sf.getShortList(next);

            HashMap<Short, List<String>> server = new HashMap();

            for (Iterator<Short> iterator1 = serverIDs.iterator(); iterator1.hasNext();) {
                Short serverID = iterator1.next();
                if (sf.get("Worlds." + serverID) != null) {
                    server.put(serverID, sf.getStringList("Worlds." + serverID));
                } else {
                    server.put(serverID, new ArrayList());
                }
            }

            servers.put(next, server);
        }

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + servers.size() + " servers combined "
                + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Método para ejecutar el evento que actualiza los carteles
     */
    public void loadActualizador() {
        // Elimino el evento en caso de que estuviera ya creado
        if (this.eventoActualizador != -1) {
            Bukkit.getServer().getScheduler().cancelTask(eventoActualizador);
        }

        // Optimizador
        if (this.params.getLBRefresh() == -1) {
            return;
        }

        this.eventoActualizador = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(pvpTitles, new Runnable() {
            @Override
            public void run() {
                getLbm().updateSigns();
            }
        }, 20 * 5, 20 * (this.params.getLBRefresh() * 60));

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + "Refresh event ["
                + this.params.getLBRefresh() + " min]"
                + ChatColor.AQUA + " loaded correctly."
        );
    }

    /**
     * Método para comprobar el rango según el tiempo
     */
    public void loadRankChecker() {
        // Elimino el evento en caso de que estuviera ya creado
        if (this.eventoChecker != -1) {
            Bukkit.getServer().getScheduler().cancelTask(eventoChecker);
        }

        // Optimizador
        if (this.params.getRankChecker() == -1) {
            return;
        }

        this.eventoChecker = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(pvpTitles, new Runnable() {
            @Override
            public void run() {
                Set<TimedPlayer> tp = pvpTitles.getPlayerManager().getTimedPlayers();

                for (Iterator<TimedPlayer> iterator = tp.iterator(); iterator.hasNext();) {
                    TimedPlayer next = iterator.next();

                    if (!next.hasSession()) {
                        continue;
                    }

                    int actualFame = dm.loadPlayerFame(next.getUniqueId(), null);
                    int savedTimeB = dm.loadPlayedTime(next.getUniqueId());

                    String rankB = Ranks.GetRank(actualFame, savedTimeB);
                    int savedTimeA = savedTimeB + next.getTotalOnline();
                    String rankA = Ranks.GetRank(actualFame, savedTimeA);

                    if (!rankB.equals(rankA)) {
                        dm.savePlayedTime(next); // Actualizo el tiempo del jugador en el server
                        next.removeSessions(); // Reinicio el tiempo a cero
                        next.startSession(); // Nueva sesion                        

                        if (next.getOfflinePlayer().isOnline()) {
                            Player pl = Bukkit.getPlayer(next.getUniqueId());
                            pl.sendMessage(PLUGIN + LangFile.PLAYER_NEW_RANK.
                                    getText(Localizer.getLocale(pl)).replace("%newRank%", rankA));
                        }
                    }
                }
            }
        }, 20 * 5 /* Tiempo para prevenir fallos */, 20 * this.params.getRankChecker());

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + "Rank Checker event ["
                + this.params.getRankChecker() + " sec]"
                + ChatColor.AQUA + " loaded correctly."
        );
    }

    // ##################################################### \\
    /**
     * Método para devolver una lista de titulos
     *
     * @return Objeto que almacena la lista de titulos
     */
    public static Map<Integer, String> rankList() {
        return rankList;
    }

    /**
     * Método para devolver una lista de puntos equivalentes a cada titulo
     *
     * @return Objeto que almacena la lista de puntos de fama
     */
    public static Map<Integer, Integer> reqFame() {
        return reqFame;
    }

    /**
     * Método para devolver una lista de tiempos requeridos
     *
     * @return Objeto que almacena la lista de dias
     */
    public static Map<Integer, Integer> reqTime() {
        return reqTime;
    }

    /**
     * Método para recibir el plugin
     *
     * @return PvpTitles object
     */
    public PvpTitles getPvpTitles() {
        return pvpTitles;
    }

    /**
     * Método para recibir el gestor de la base de datos
     *
     * @return DatabaseManager
     */
    public DatabaseManager getDm() {
        return dm;
    }

    public LeaderBoardManager getLbm() {
        return lbm;
    }
}
