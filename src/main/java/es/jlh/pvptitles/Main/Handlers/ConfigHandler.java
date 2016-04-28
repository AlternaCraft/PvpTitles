package es.jlh.pvptitles.Main.Handlers;

import es.jlh.pvptitles.Backend.ConfigDataStore;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Handlers.DBHandler.DBTYPE;
import es.jlh.pvptitles.Main.Manager;
import static es.jlh.pvptitles.Main.Manager.messages;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.Utils;
import es.jlh.pvptitles.Objects.FileConfig;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author AlternaCraft
 */
public class ConfigHandler {
    // Configuracion del config principal
    private FileConfig customConfig = null;
    private PvpTitles pvpTitles = null;
    
    public ConfigHandler(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;        
    }
    
    public void loadConfig(ConfigDataStore params) {
        customConfig = new FileConfig(pvpTitles);        
        loadData(Manager.rankList(), Manager.reqFame(), Manager.reqTime(), params);
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
        FileConfiguration config = getConfig();
        
        PvpTitles.debugMode = config.getBoolean("Debug");

        List<String> configList = (List<String>) config.getList("RankNames");
        List<Integer> requFame = (List<Integer>) config.getList("ReqFame");
        List<Integer> requTime = (List<Integer>) config.getList("ReqTime");

        rankList.clear();
        for (int i = 0; i < configList.size(); i++) {
            rankList.put(i, Utils.translateColor(configList.get(i)));
        }

        reqFame.clear();
        for (int i = 0; i < requFame.size(); i++) {
            reqFame.put(i, requFame.get(i));
        }

        reqTime.clear();
        for (int i = 0; i < configList.size(); i++) {
            int seconds = 0;

            if (requTime.size() >= (i + 1) && requTime.get(i) != null) {
                seconds = requTime.get(i);
            }

            reqTime.put(i, seconds);
        }

        params.getAffectedWorlds().clear();
        params.getAffectedWorlds().addAll(config.getStringList("MW-filter.affected-worlds"));

        // Todos los mundos a minusculas
        ListIterator<String> iterator = params.getAffectedWorlds().listIterator();
        while (iterator.hasNext()) {
            iterator.set(iterator.next().toLowerCase());
        }

        params.getNoPurge().clear();
        params.getNoPurge().addAll(config.getStringList("NoPurge"));

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

        params.displayInChat(config.getBoolean("DisplayTitleInChat"));
        params.displayLikeHolo(config.getBoolean("DisplayTitleOverPlayer"));
        params.setHolotagformat(Utils.translateColor(config.getString("HoloTitleFormat")));
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
        params.setTitle(config.getBoolean("MW-filter.title"));
        params.setPoints(config.getBoolean("MW-filter.points"));
        params.setLeaderboard(config.getBoolean("MW-filter.show-on-leaderboard"));

        if (configList.size() != requFame.size()) {
            PvpTitles.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
            PvpTitles.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
            PvpTitles.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
        }
    }    

    public FileConfiguration getConfig() {
        return customConfig.getConfig();
    }
}
