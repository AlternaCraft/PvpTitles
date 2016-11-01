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
package com.alternacraft.pvptitles.Main.Handlers;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.Handlers.DBHandler.DBTYPE;
import com.alternacraft.pvptitles.Main.Manager;
import static com.alternacraft.pvptitles.Main.Manager.messages;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.FileConfig;
import com.alternacraft.pvptitles.Misc.StrUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.configuration.file.FileConfiguration;

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
     * @param rankList Map
     * @param reqFame Map
     * @param reqTime Map
     * @param params ConfigDataStore
     */
    protected void loadData(LinkedList rankList, LinkedList reqFame, 
            LinkedList reqTime, ConfigDataStore params) {
        // Set debug mode
        FileConfiguration config = getConfig();
        
        PvpTitles.setPluginName(StrUtils.translateColors(config.getString("PluginPrefix")));        
        PvpTitles.debugMode = config.getBoolean("Debug");

        List<String> configList = (List<String>) config.getList("RankNames");
        List<Integer> requFame = (List<Integer>) config.getList("ReqFame");
        List<Integer> requTime = (List<Integer>) config.getList("ReqTime");

        rankList.clear();
        for (String rank : configList) {
            rankList.add(StrUtils.translateColors(rank));
        }
        
        reqFame.clear();
        for (Integer fame : requFame) {
            reqFame.add(fame);
        }
        
        reqTime.clear();        
        for (int i = 0; i < configList.size(); i++) {
            int seconds = 0;
            if (requTime.size() >= (i + 1) && requTime.get(i) != null) {
                seconds = requTime.get(i);
            }
            reqTime.add(seconds);
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
                messages = LangsFile.LangType.valueOf("CUSTOM_"+lang);
            }
            catch (Exception ex){}
        }
        else {
            messages = LangsFile.LangType.valueOf(lang);
        }
        
        if (messages == null) {
            messages = LangsFile.LangType.EN;
        }

        params.displayInChat(config.getBoolean("DisplayTitleInChat"));
        params.displayLikeHolo(config.getBoolean("DisplayTitleOverPlayer"));
        params.setHolotagformat(StrUtils.translateColors(config.getString("HoloTitleFormat")));
        params.setHoloHeightMod((short) config.getInt("HoloHeightModifier"));
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
        params.setErrorFormat((short) config.getInt("ErrorFormat"));
        params.setMw_enabled(config.getBoolean("MW.enable"));
        params.setTitle(config.getBoolean("MW-filter.title"));
        params.setPoints(config.getBoolean("MW-filter.points"));
        params.setLeaderboard(config.getBoolean("MW-filter.show-on-leaderboard"));

        if (configList.size() != requFame.size()) {
            LoggerManager.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
            LoggerManager.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
            LoggerManager.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
        }
    }    

    public FileConfiguration getConfig() {
        return customConfig.getConfig();
    }
}
