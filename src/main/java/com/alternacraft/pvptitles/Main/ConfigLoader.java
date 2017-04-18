/*
 * Copyright (C) 2017 AlternaCraft
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
package com.alternacraft.pvptitles.Main;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.DBLoader.DBTYPE;
import static com.alternacraft.pvptitles.Main.Manager.messages;
import com.alternacraft.pvptitles.Misc.FileConfig;
import com.alternacraft.pvptitles.Misc.StrUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader {

    // Configuracion del config principal
    private FileConfig customConfig = null;
    private PvpTitles pvpTitles = null;

    public ConfigLoader(PvpTitles pvpTitles) {
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

        // PLUGIN CONTROL
        params.setMetrics(config.getBoolean("Metrics"));
        params.setUpdate(config.getBoolean("Update"));
        params.setAlert(config.getBoolean("Alert"));
        params.setErrorFormat((short) config.getInt("ErrorFormat"));
        String defdb = config.getString("DefaultDatabase");
        try {
            params.setDefaultDB(DBTYPE.valueOf(defdb.toUpperCase()));
        } catch (Exception ex) {
            CustomLogger.logError("Bad name for default database; Using Ebean per default...");
        }

        // MYSQL-PVPTITLES BRIDGE
        params.setPvpTitles_Bridge(config.getBoolean("Mysql.enable"));
        if (params.isPvpTitles_Bridge()) {
            DBLoader.tipo = DBTYPE.MYSQL;

            params.setUse_ssl(config.getBoolean("Mysql.enableSSL"));
            params.setHost(config.getString("Mysql.host"));
            params.setPort((short) config.getInt("Mysql.port"));
            params.setDb(config.getString("Mysql.database"));
            params.setUser(config.getString("Mysql.user"));
            params.setPass(config.getString("Mysql.pass"));
        } else {
            DBLoader.tipo = params.getDefaultDB();
        }
        params.setMultiS((short) config.getInt("MultiS"));
        params.setNameS(config.getString("NameS"));

        // MULTIWORLD
        params.setMw_enabled(config.getBoolean("MW.enable"));
        params.setTitle(config.getBoolean("MW-filter.title"));
        params.setPoints(config.getBoolean("MW-filter.points"));
        params.setLeaderboard(config.getBoolean("MW-filter.show-on-leaderboard"));
        params.getAffectedWorlds().clear();
        params.getAffectedWorlds().addAll(config.getStringList("MW-filter.affected-worlds"));
        // Todos los mundos a minusculas
        ListIterator<String> iterator = params.getAffectedWorlds().listIterator();
        while (iterator.hasNext()) {
            iterator.set(iterator.next().toLowerCase());
        }

        // Events
        params.setLBRefresh((short) config.getInt("LBRefresh"));
        params.setRankChecker((short) config.getInt("RankChecker"));

        // PURGE
        params.setTimeP((short) config.getInt("TimeP"));
        params.getNoPurge().clear();
        params.getNoPurge().addAll(config.getStringList("NoPurge"));

        // ANTIFARM
        params.setKills((short) config.getInt("Kills"));
        params.setTimeV((short) config.getInt("TimeV"));
        params.setTimeL((short) config.getInt("TimeL"));

        params.setCheckAFK(config.getBoolean("CheckAFK"));
        params.setAFKTime((short) config.getInt("AFKTime"));

        params.setMod((float) config.getDouble("Mod"));

        // CHAT & TITLES
        String lang = config.getString("DefaultLang");

        if (!"ES".equals(lang) && !"EN".equals(lang)) {
            try {
                messages = LangsFile.LangType.valueOf("CUSTOM_" + lang);
            } catch (Exception ex) {
                CustomLogger.logError(ex.getMessage(), ex);
            }
        } else {
            messages = LangsFile.LangType.valueOf(lang);
        }

        if (messages == null) {
            messages = LangsFile.LangType.EN;
        }

        params.setTag(config.getString("Tag"));
        params.setPrefixColor(config.getString("PrefixColor"));
        params.setTop((short) config.getInt("Top"));

        params.setPrefix(config.getString("Prefix"));

        params.displayInChat(config.getBoolean("DisplayTitleInChat"));
        params.displayLikeHolo(config.getBoolean("DisplayTitleOverPlayer"));
        params.setHolotagformat(StrUtils.translateColors(config.getString("HoloTitleFormat")));
        params.setHoloHeightMod((short) config.getInt("HoloHeightModifier"));

        List<String> configList = config.getStringList("RankNames");
        List<Integer> requFame = config.getIntegerList("ReqFame");
        List<Long> requTime = config.getLongList("ReqTime");

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
            long seconds = 0;
            if (requTime.size() >= (i + 1) && requTime.get(i) != null) {
                seconds = requTime.get(i);
            }
            reqTime.add(seconds);
        }

        if (configList.size() != requFame.size()) {
            CustomLogger.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
            CustomLogger.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
            CustomLogger.logMessage("WARNING - RankNames and ReqFame are not equal in their numbers.");
        }
    }

    public FileConfiguration getConfig() {
        return customConfig.getConfig();
    }
}
