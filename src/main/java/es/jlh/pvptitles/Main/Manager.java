package es.jlh.pvptitles.Main;

import es.jlh.pvptitles.Backend.ConfigDataStore;
import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Files.CommandFile;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Files.LangFile.LangType;
import es.jlh.pvptitles.Files.ModelsFile;
import es.jlh.pvptitles.Files.ServersFile;
import es.jlh.pvptitles.Hook.HolographicHook;
import es.jlh.pvptitles.Main.Handlers.ConfigHandler;
import es.jlh.pvptitles.Main.Handlers.DBHandler;
import static es.jlh.pvptitles.Main.Handlers.DBHandler.tipo;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardData;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardModel;
import es.jlh.pvptitles.Managers.BoardsAPI.ModelController;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoard;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoardData;
import static es.jlh.pvptitles.Managers.CleanTaskManager.TICKS;
import es.jlh.pvptitles.Managers.LeaderBoardManager;
import es.jlh.pvptitles.Managers.Timer.TimedPlayer;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.RetroCP.DBChecker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public final class Manager {

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR + INSTANCE...">    
    // Variable que almacena el plugin
    private PvpTitles pvpTitles = null;
    // Instancia de la clase
    public static final Manager instance = new Manager();

    // Handlers
    public ConfigHandler ch = null;
    public DBHandler dbh = null;

    // Gestor de leaderboards
    private LeaderBoardManager lbm = null;

    // Objetos para guardar los Titulos con sus respectivos puntos    
    private static final Map<Integer, String> RANKLIST = new HashMap();
    private static final Map<Integer, Integer> REQFAME = new HashMap();
    private static final Map<Integer, Integer> REQTIME = new HashMap();

    // Modelos
    public ArrayList<BoardModel> modelos = null;
    // Recompensas
    public Map<String, Map<String, Map<String, List<String>>>> commandsRw = null;
    // Servers
    public HashMap<String, HashMap<Short, List<String>>> servers = null;
    // Configuracion
    public ConfigDataStore params = null;

    // Chat
    public static LangType messages = null;

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
        params = new ConfigDataStore();
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

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="SETUP...">
    /**
     * Método para cargar el config principal
     *
     * @param plugin Objeto de la clase principal
     * @return boolean
     */
    public boolean setup(PvpTitles plugin) {
        this.pvpTitles = plugin;

        this.lbm = new LeaderBoardManager(plugin);

        this.ch = new ConfigHandler(this.pvpTitles);
        this.ch.loadConfig(params);

        this.dbh = new DBHandler(pvpTitles, this.ch.getConfig());
        this.dbh.selectDB();

        // RCP
        if (!new DBChecker(pvpTitles).setup()) {
            return false;
        }

        this.loadLang();
        this.loadModels();
        this.loadSavedBoards();
        this.loadCommands();

        if (tipo == DBHandler.DBTYPE.MYSQL) {
            this.loadServers();
        }

        this.loadActualizador();
        this.loadRankTimeChecker();

        return true;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="LOADING PLUGIN BASE...">   
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

        showMessage(ChatColor.YELLOW + "" + modelos.size() + " models " + ChatColor.AQUA + "loaded correctly.");
    }

    /**
     * Método para guardar en memoria los scoreboards
     */
    public void loadSavedBoards() {
        List<SignBoardData> carteles = new ArrayList<>();
        try {
            carteles = pvpTitles.manager.dbh.getDm().buscaBoards();
        } catch (DBException ex) {
            PvpTitles.logError(ex.getCustomMessage(), null);
        }

        lbm.vaciar(); // Evito duplicados

        // Signs
        for (BoardData cartel : carteles) {
            BoardModel bm = searchModel(cartel.getModelo());

            if (bm == null) {
                try {
                    pvpTitles.manager.dbh.getDm().borraBoard(cartel.getLocation());
                    showMessage(ChatColor.RED + "Sign '" + cartel.getNombre()
                            + "' removed because the model has not been found...");
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }
                
                continue;
            }

            ModelController mc = new ModelController();
            mc.preprocessUnit(bm.getParams());

            SignBoard cs = new SignBoard(cartel, bm, mc);

            cs.setLineas(new String[0]);
            cs.setMatSign(((SignBoardData) cartel).getSignMaterial());

            lbm.loadBoard(cs);
        }

        // Holograms
        if (HolographicHook.ISHDENABLED) {
            HolographicHook.loadHoloBoards();
        }

        showMessage(ChatColor.YELLOW + "" + this.lbm.getBoards().size()
                + " scoreboards " + ((HolographicHook.ISHDENABLED) ? "" : "per signs ")
                + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Método para buscar un modelo de tabla de puntuaciones
     *
     * @param modelo String con el modelo a buscar
     * @return BoardModel con los datos del modelo
     */
    public BoardModel searchModel(String modelo) {
        for (BoardModel smc : modelos) {
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

        showMessage(ChatColor.YELLOW + "Locales " + ChatColor.AQUA + "loaded correctly.");
    }

    /**
     * Método para cargar el sistema de recompensas
     */
    public void loadCommands() {
        commandsRw = new HashMap();

        YamlConfiguration lp = new CommandFile().load();

        List<String> activos = lp.getStringList("activeRewards");
        List<String> types = new ArrayList(
                Arrays.asList("onRank", "onFame", "onKillstreak", "onKill")
        );

        for (String reward : activos) {
            boolean nulos = true; // No entro en ninguno ergo es en onkill

            for (String type : types) {
                Map data = new HashMap();

                String value = lp.getString("Rewards." + reward + "." + type);

                if (value != null || (type.equals("onKill") && nulos)) {
                    nulos = false;

                    if (commandsRw.get(type) == null) {
                        commandsRw.put(type, new HashMap());
                    }

                    // Valores de la recompensa
                    if (lp.contains("Rewards." + reward + ".money")) {
                        data.put("money", Arrays.asList(lp.getString("Rewards." + reward + ".money")));
                    }

                    data.put("commands", lp.getStringList("Rewards." + reward + ".command"));

                    // Guardo en el mapa principal los valores para ese valor
                    commandsRw.get(type).put(value, data);
                }
            }
        }

        showMessage(ChatColor.YELLOW + "" + activos.size() + " rewards " + ChatColor.AQUA + "loaded correctly.");
    }

    /**
     * Método para cargar los locales
     */
    public void loadServers() {
        YamlConfiguration sf = new ServersFile().load();

        Set<String> sfk = sf.getKeys(false);

        for (String srv : sfk) {
            // Fix para evitar campos innecesarios
            if (srv.equals("Worlds")) {
                break;
            }

            List<Short> serverIDs = sf.getShortList(srv);

            HashMap<Short, List<String>> server = new HashMap();

            for (Short serverID : serverIDs) {
                if (sf.get("Worlds." + srv + "." + serverID) != null) {
                    server.put(serverID, sf.getStringList("Worlds." + srv + "." + serverID));
                } else if (sf.get("Worlds." + serverID) != null) {
                    server.put(serverID, sf.getStringList("Worlds." + serverID));
                } else {
                    server.put(serverID, new ArrayList());
                }
            }

            servers.put(srv, server);
        }

        showMessage(ChatColor.YELLOW + "" + servers.size() + " servers combined "
                + ChatColor.AQUA + "loaded correctly.");
    }

    /**
     * Método para ejecutar el evento que actualiza los carteles
     */
    public void loadActualizador() {
        // Elimino el evento en caso de que estuviera ya creado
        if (this.eventoActualizador != -1) {
            pvpTitles.getServer().getScheduler().cancelTask(eventoActualizador);
        }

        // Optimizador
        if (this.params.getLBRefresh() == -1) {
            return;
        }

        this.eventoActualizador = pvpTitles.getServer().getScheduler().scheduleSyncRepeatingTask(pvpTitles, new Runnable() {
            @Override
            public void run() {
                getLbm().updateBoards();
            }
        }, TICKS * 5L, TICKS * (this.params.getLBRefresh() * 60L));

        showMessage(ChatColor.YELLOW + "Refresh event [" + this.params.getLBRefresh()
                + " min]" + ChatColor.AQUA + " loaded correctly."
        );
    }

    /**
     * Método para comprobar el rango según el tiempo
     */
    public void loadRankTimeChecker() {
        // Elimino el evento en caso de que estuviera ya creado
        if (this.eventoChecker != -1) {
            pvpTitles.getServer().getScheduler().cancelTask(eventoChecker);
        }

        // Optimizador
        if (this.params.getRankChecker() == -1) {
            return;
        }

        this.eventoChecker = pvpTitles.getServer().getScheduler().scheduleSyncRepeatingTask(pvpTitles, new Runnable() {
            @Override
            public void run() {
                Set<TimedPlayer> tp = pvpTitles.getTimerManager().getTimedPlayers();

                for (TimedPlayer timedPlayer : tp) {
                    // Fix para evitar nullpointerexception
                    if (!timedPlayer.hasSession() || !timedPlayer.getOfflinePlayer().isOnline()) {
                        continue;
                    }

                    int actualFame = 0;
                    try {
                        actualFame = dbh.getDm().loadPlayerFame(timedPlayer.getUniqueId(), null);
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                        return;
                    }
                    
                    int savedTimeB = 0;
                    try {
                        savedTimeB = dbh.getDm().loadPlayedTime(timedPlayer.getUniqueId());
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                        return;
                    }

                    String rankB = Ranks.getRank(actualFame, savedTimeB);
                    int savedTimeA = savedTimeB + timedPlayer.getTotalOnline();
                    String rankA = Ranks.getRank(actualFame, savedTimeA);

                    // Actualizo el tiempo del jugador en el server
                    if (!rankB.equals(rankA)) {
                        try {
                            dbh.getDm().savePlayedTime(timedPlayer);
                        } catch (DBException ex) {
                            PvpTitles.logError(ex.getCustomMessage(), null);
                            continue;
                        }
                        
                        timedPlayer.removeSessions(); // Reinicio el tiempo a cero
                        timedPlayer.startSession(); // Nueva sesion

                        if (timedPlayer.getOfflinePlayer().isOnline()) {
                            Player pl = pvpTitles.getServer().getPlayer(timedPlayer.getUniqueId());
                            pl.sendMessage(PLUGIN + LangFile.PLAYER_NEW_RANK.
                                    getText(Localizer.getLocale(pl)).replace("%newRank%", rankA));
                        }
                    }
                }
            }
        }, TICKS * 5L /* Tiempo para prevenir fallos */, TICKS * this.params.getRankChecker());

        showMessage(ChatColor.YELLOW + "Rank Checker event [" + this.params.getRankChecker()
                + " sec]" + ChatColor.AQUA + " loaded correctly."
        );
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="SOME GETTERS...">
    /**
     * Método para devolver una lista de titulos
     *
     * @return Objeto que almacena la lista de titulos
     */
    public static Map<Integer, String> rankList() {
        return RANKLIST;
    }

    /**
     * Método para devolver una lista de puntos equivalentes a cada titulo
     *
     * @return Objeto que almacena la lista de puntos de fama
     */
    public static Map<Integer, Integer> reqFame() {
        return REQFAME;
    }

    /**
     * Método para devolver una lista de tiempos requeridos
     *
     * @return Objeto que almacena la lista de dias
     */
    public static Map<Integer, Integer> reqTime() {
        return REQTIME;
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
     * Método para recibir el gestor de carteles
     *
     * @return LeaderBoardManager
     */
    public LeaderBoardManager getLbm() {
        return lbm;
    }

    /**
     * Método para devolver el handler del config principal
     *
     * @return ConfigHandler
     */
    public ConfigHandler getCh() {
        return ch;
    }

    /**
     * Método para devolver el handler de la base de datos
     *
     * @return DBHandler
     */
    public DBHandler getDbh() {
        return dbh;
    }
    // </editor-fold>
}
