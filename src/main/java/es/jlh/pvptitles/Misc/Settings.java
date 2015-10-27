package es.jlh.pvptitles.Misc;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 *
 * @author julito
 */
public class Settings {
    /* METRICS && UPDATES */

    // Atributo para guardar si se van a usar las metricas
    private boolean metrics = true;
    // Atributo para guardar si se va a actualizar o no automaticamente
    private boolean update = false;
    // Atributo para guardar si se va a avisar de las actualizaciones o no
    private boolean alert = true;

    /* PVPTITLES BRIDGE */
    // Ebean
    private boolean auto_export = false;
    // MySQL
    private boolean PvpTitles_Bridge = false;
    // Datos de la conexion a MySQL
    private String host = "localhost";
    private int port = 3306;
    private String db = "database";
    private String user = "user";
    private String pass = "pass";
    // Server ID
    private int multiS = -1;
    // Server Name
    private String nameS = "";

    /* MULTIWORLD */
    private boolean mw_enabled = false;    

    // Atributo con los mundos
    private final List<String> mundos = new ArrayList();
    // Atributo para mostrar o no los titulos en el chat de los mundos escritos
    private boolean chat = false;
    // Atributo para permitir ganar puntos de fama a los jugadores en los mundos escritos
    private boolean points = false;
    // Atributo para mostrar los jugadores en la tabla de puntuaciones
    private boolean leaderboard = false;

    /* LEADERBOARD (SIGN) */
    // Tiempo de actualizacion de los carteles (Minutos)
    private int LBRefresh = 0;
    
    /* RANK CHECKER */
    // Tiempo para comprobar si tiene un nuevo rango
    private int rankChecker = 0;

    /* PURGE */
    // Lista de jugadores que NO seran borrados por el comando
    private final List<String> noPurge = new ArrayList();
    // Tiempo necesario para que un registro sea entendido como inactivo
    private int timeP = 0;

    /* ANTIFARM */
    // Maximo de bajas para el sistema antifarm
    private int kills = 0;
    // Tiempo para volver a matar una vez superado el limite de bajas
    private int timeV = 0;
    // Tiempo para limpiar las bajas realizadas a un jugador
    private int timeL = 0;    
    // Comprueba AFK
    private boolean checkAFK = false;
    // Tiempo AFK
    private int AFKTime = 0;
    
    /* KILLSTREAKS */
    // Modificador de los puntos ganados en una racha de bajas
    private double mod = 0;
    /* CHAT */
    // Color del titulo en el chat
    private ChatColor prefixColor = null;
    // Nombre de los puntos
    private String tag = null;
    // Tag para intercambiar por el titulo
    private String prefix = null;
    // Cantidad de jugadores que aparecen en el ranking
    private int top = 0;

    /**
     * Constructor de la clase
     */
    public Settings() {
    }

    // ** SETTERS ** \\
    public void setPrefixColor(String color) {
        this.prefixColor = this.GetPrefixColor(color);
    }

    public void setAuto_export(boolean auto_export) {
        this.auto_export = auto_export;
    }

    public void setPvpTitles_Bridge(boolean PvpTitles_Bridge) {
        this.PvpTitles_Bridge = PvpTitles_Bridge;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setMultiS(int multiS) {
        this.multiS = multiS;
    }

    public void setNameS(String nameS) {
        if ("".equals(nameS) || "Custom".equals(nameS)) {
            nameS = Bukkit.getServer().getServerName();
        }
        this.nameS = nameS;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTop(int top) {
        this.top = compNum(top);
    }

    public void setLBRefresh(int LBRefresh) {
        this.LBRefresh = LBRefresh;
    }

    public void setRankChecker(int rankChecker) {
        this.rankChecker = rankChecker;
    }

    public void setMod(double mod) {
        this.mod = compNum(mod);
    }

    public void setKills(int kills) {
        this.kills = compNum(kills);
    }

    public void setTimeP(int timeP) {
        this.timeP = compNum(timeP);
    }

    public void setTimeV(int timeV) {
        this.timeV = compNum(timeV);
    }

    public void setTimeL(int timeL) {
        this.timeL = compNum(timeL);
    }

    public void setCheckAFK(boolean checkAFK) {
        this.checkAFK = checkAFK;
    }

    public void setAFKTime(int AFKTime) {
        this.AFKTime = AFKTime;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public void setMetrics(boolean metrics) {
        this.metrics = metrics;
    }

    public void setChat(boolean chat) {
        this.chat = chat;
    }

    public void setMw_enabled(boolean mw_enabled) {
        this.mw_enabled = mw_enabled;
    }

    public void setLeaderboard(boolean leaderboard) {
        this.leaderboard = leaderboard;
    }

    public void setPoints(boolean points) {
        this.points = points;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    // ** GETTERS ** \\ 
    public ChatColor getPrefixColor() {
        return prefixColor;
    }

    public boolean isAuto_export() {
        return auto_export;
    }

    public boolean isPvpTitles_Bridge() {
        return PvpTitles_Bridge;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDb() {
        return db;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public int getMultiS() {
        return multiS;
    }

    public String getNameS() {
        return nameS;
    }

    public String getTag() {
        return this.tag;
    }

    public int getTop() {
        return this.top;
    }

    public int getLBRefresh() {
        return LBRefresh;
    }

    public int getRankChecker() {
        return rankChecker;
    }

    public double getMod() {
        return this.mod;
    }

    public int getKills() {
        return kills;
    }

    public int getTimeP() {
        return timeP;
    }

    public int getTimeV() {
        return timeV;
    }

    public int getTimeL() {
        return timeL;
    }

    public boolean isCheckAFK() {
        return checkAFK;
    }

    public int getAFKTime() {
        return AFKTime;
    }

    public String getPrefix() {
        return prefix;
    }

    public boolean isMw_enabled() {
        return mw_enabled;
    }

    public boolean showOnLeaderBoard() {
        return leaderboard;
    }

    public List<String> getAffectedWorlds() {
        return mundos;
    }

    public List<String> getNoPurge() {
        return noPurge;
    }

    public boolean isUpdate() {
        return update;
    }

    public boolean isAlert() {
        return alert;
    }

    public boolean isPoints() {
        return points;
    }

    public boolean isMetrics() {
        return metrics;
    }

    public boolean isChat() {
        return chat;
    }

    // ** COMPROBACIONES ** \\
    public int compNum(int valor) {
        if (valor < 0 || valor > 1000) {
            return 1;
        }

        return valor;
    }

    public double compNum(double valor) {
        if (valor < 0 || valor > 1000) {
            return 1;
        }

        return valor;
    }
    // ** ************** ** \\    

    /**
     * Método para convertir el nombre del color a un valor valido
     *
     * @param color String con el nombre del color
     */
    private ChatColor GetPrefixColor(String color) {
        return ChatColor.valueOf(color.toUpperCase());
    }
}
