package es.jlh.pvptitles.Main;

import es.jlh.pvptitles.Configs.CommandFile;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Configs.LangFile.LangType;
import es.jlh.pvptitles.Configs.ModelsFile;
import es.jlh.pvptitles.Configs.ServersFile;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Backend.ConfigDataStore;
import es.jlh.pvptitles.Objects.LBSigns.CustomSign;
import es.jlh.pvptitles.Objects.LBSigns.LBData;
import es.jlh.pvptitles.Objects.LBSigns.LBModel;
import es.jlh.pvptitles.Managers.LeaderBoardManager;
import es.jlh.pvptitles.Objects.TimedPlayer;
import es.jlh.pvptitles.Main.Handlers.ConfigHandler;
import es.jlh.pvptitles.Main.Handlers.DBHandler;
import static es.jlh.pvptitles.Main.Handlers.DBHandler.tipo;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import es.jlh.pvptitles.RetroCP.DBChecker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
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
    public static Manager instance = new Manager();
    
    // Handlers
    public ConfigHandler ch = null;
    public DBHandler dbh = null;
    
    // Gestor de leaderboards
    private LeaderBoardManager lbm = null;

    // Objetos para guardar los Titulos con sus respectivos puntos    
    private static final Map<Integer, String> rankList = new HashMap();
    private static final Map<Integer, Integer> reqFame = new HashMap();
    private static final Map<Integer, Integer> reqTime = new HashMap();

    // Resto de parametros
    public ConfigDataStore params = null;
    
    // Modelos
    public ArrayList<LBModel> modelos = new ArrayList();
    // Recompensas
    public HashMap<String, HashMap<String, List<String>>> commandsRw = null;
    // Servers
    public HashMap<String, HashMap<Short, List<String>>> servers = null;

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
        
        this.dbh.autoExportData();

        this.loadLang();
        this.loadModels();
        this.loadSavedSigns();
        this.loadCommands(); 
        
        if (tipo == DBHandler.DBTYPE.MYSQL) {
            this.loadServers();
        }
        
        this.loadActualizador();
        this.loadRankChecker();

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
    public void loadSavedSigns() {
        List<LBData> carteles = pvpTitles.cm.dbh.getDm().buscaCarteles();

        lbm.vaciar(); // Evito duplicados
        
        for (LBData cartel : carteles) {
            LBModel sm = pvpTitles.cm.searchModel(cartel.getModelo());

            if (sm == null) {
                pvpTitles.cm.dbh.getDm().borraCartel(cartel.getL());

                showMessage(ChatColor.RED + "Sign '" + cartel.getNombre()
                        + "' removed because the model has not been found...");

                continue;
            }

            CustomSign cs = new CustomSign(cartel, sm);
            cs.setLineas(new String[0]);
            cs.setMatSign(cartel.getSignMaterial());

            lbm.loadSign(cs);
        }

        showMessage(ChatColor.YELLOW + "" + this.lbm.getSigns().size()
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

        showMessage(ChatColor.YELLOW + "Locales " + ChatColor.AQUA + "loaded correctly.");
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

        showMessage(ChatColor.YELLOW + "" + activos.size() + " rewards " + ChatColor.AQUA + "loaded correctly.");
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
                if (sf.get("Worlds." + next + "." + serverID) != null) {
                    server.put(serverID, sf.getStringList("Worlds." + next + "." + serverID));
                } else if (sf.get("Worlds." + serverID) != null) {
                    server.put(serverID, sf.getStringList("Worlds." + serverID));
                } else {
                    server.put(serverID, new ArrayList());
                }
            }
            
            servers.put(next, server);
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

        showMessage(ChatColor.YELLOW + "Refresh event [" + this.params.getLBRefresh() 
                + " min]" + ChatColor.AQUA + " loaded correctly."
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

                    int actualFame = dbh.getDm().loadPlayerFame(next.getUniqueId(), null);
                    int savedTimeB = dbh.getDm().loadPlayedTime(next.getUniqueId());

                    String rankB = Ranks.GetRank(actualFame, savedTimeB);
                    int savedTimeA = savedTimeB + next.getTotalOnline();
                    String rankA = Ranks.GetRank(actualFame, savedTimeA);

                    if (!rankB.equals(rankA)) {
                        dbh.getDm().savePlayedTime(next); // Actualizo el tiempo del jugador en el server
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
