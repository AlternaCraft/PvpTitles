package es.jlh.pvptitles.Main.Handlers;

import es.jlh.pvptitles.Backend.ConfigDataStore;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.Handlers.DBHandler.DBTYPE;
import es.jlh.pvptitles.Main.Manager;
import static es.jlh.pvptitles.Main.Manager.messages;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import java.io.File;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author AlternaCraft
 */
public class ConfigHandler {
    // Configuracion del config principal
    private FileConfiguration config = null;
    private PvpTitles pvpTitles = null;
    
    public ConfigHandler(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;        
    }
    
    public void loadConfig(ConfigDataStore params) {
        createConfig();
        checkVersion();
        loadData(Manager.rankList(), Manager.reqFame(), Manager.reqTime(), params);
    }
    
    /**
     * Método para crear el config en caso de que no exista
     */
    protected void createConfig() {
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
    protected boolean checkVersion() {
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
            showMessage(ChatColor.RED + "Mismatch config version, a new one has been created.");

            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Método para cargar la informacion del config principal
     * 
     * @param rankList Map<Integer, String>
     * @param reqFame Map<Integer, Integer>
     * @param reqTime Map<Integer, Integer>
     * @param params ConfigDataStore
     */
    protected void loadData(Map<Integer, String> rankList, Map<Integer, Integer> reqFame, 
            Map<Integer, Integer> reqTime, ConfigDataStore params) {
        // Set debug mode
        PvpTitles.debugMode = config.getBoolean("Debug");
        
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
            DBHandler.tipo = DBTYPE.MYSQL;

            params.setHost(config.getString("Mysql.host"));
            params.setPort((short) config.getInt("Mysql.port"));
            params.setDb(config.getString("Mysql.database"));
            params.setUser(config.getString("Mysql.user"));
            params.setPass(config.getString("Mysql.pass"));
        } else {
            DBHandler.tipo = DBTYPE.EBEAN;
        }
        params.setMultiS((short) config.getInt("MultiS"));
        params.setNameS(config.getString("NameS"));

        String lang = config.getString("DefaultLang");
        
        if (!"ES".equals(lang) && !"EN".equals(lang)) {
            try {
                messages = LangFile.LangType.valueOf("CUSTOM_"+lang);
            }
            catch (Exception ex){}
        }
        else {
            messages = LangFile.LangType.valueOf(lang);
        }
        
        if (messages == null) {
            messages = LangFile.LangType.EN;
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
        params.setMw_enabled(config.getBoolean("MW.enable"));
        params.setChat(config.getBoolean("MW-filter.chat"));
        params.setPoints(config.getBoolean("MW-filter.points"));
        params.setLeaderboard(config.getBoolean("MW-filter.show-on-leaderboard"));

        if (configList.size() != requFame.size()) {
            PvpTitles.logDebugInfo("WARNING - RankNames and ReqFame are not equal in their numbers.");
            PvpTitles.logDebugInfo("WARNING - RankNames and ReqFame are not equal in their numbers.");
            PvpTitles.logDebugInfo("WARNING - RankNames and ReqFame are not equal in their numbers.");
        }
    }    

    public FileConfiguration getConfig() {
        return config;
    }
}
