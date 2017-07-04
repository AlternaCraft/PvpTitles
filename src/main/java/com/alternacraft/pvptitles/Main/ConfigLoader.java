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
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.FileConfig;
import com.alternacraft.pvptitles.Misc.Rank;
import com.alternacraft.pvptitles.Misc.StrUtils;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader {

    // Configuracion del config principal
    private FileConfig customConfig = null;
    private PvpTitles pvpTitles = null;

    public ConfigLoader(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    public ConfigDataStore loadConfig() {
        customConfig = new FileConfig(pvpTitles);
        return loadData();
    }

    /**
     * Método para cargar la informacion del config principal
     * 
     * @return Configuration loaded.
     */
    protected ConfigDataStore loadData() {
        ConfigDataStore params = new ConfigDataStore(); // Reset all on reload
        
        // Set debug mode
        FileConfiguration config = getConfig();

        PvpTitles.setPluginName(StrUtils.translateColors(config.getString("PluginPrefix")));
        PvpTitles.debugMode = config.getBoolean("Debug");

        // PLUGIN CONTROL
        params.setMetrics(config.getBoolean("Metrics"));
        params.setUpdate(config.getBoolean("Update"));
        params.setAlert(config.getBoolean("Alert"));
        params.setDisplayIntegrations(config.getBoolean("DisplayIntegrations"));
        params.setErrorFormat((short) config.getInt("ErrorFormat"));
        String defdb = config.getString("DefaultDatabase");
        try {
            params.setDefaultDB(DBTYPE.valueOf(defdb.toUpperCase()));
        } catch (Exception ex) {
            CustomLogger.logError("Bad name for default database; Using "
                    + params.getDefaultDB().name() + " per default...");
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
        params.setAffectedWorlds(config.getStringList("MW-filter.affected-worlds"));

        // Events
        params.setLBRefresh((short) config.getInt("LBRefresh"));
        params.setRankChecker((short) config.getInt("RankChecker"));

        // PURGE
        params.setPurgeTime((short) config.getInt("TimeP"));
        params.setNoPurgePlayers(config.getStringList("NoPurge"));

        // ANTIFARM
        params.setMaxKills((short) config.getInt("Kills"));
        params.setCleanerTime((short) config.getInt("CleanerTime"));
        params.setVetoTime((short) config.getInt("VetoTime"));

        params.setCheckAFK(config.getBoolean("CheckAFK"));
        params.setAFKTime((short) config.getInt("AFKTime"));

        // MULTIPLIERS        
        for (String type : ConfigDataStore.MP_TYPES) {
            Set<String> mults = config.getConfigurationSection("Multipliers." + type).getKeys(false);
            mults.forEach(mult -> {
                double value = config.getDouble("Multipliers." + type + "." + mult);
                if (!params.setMultiplier(type, mult, value)) {
                    CustomLogger.logError("The value for the multiplier "
                            + type + "." + mult + " has to be greater than zero!");
                }
            });
        }
        
        // POINTS
        params.setResetOptions(config.getStringList("ResetOptions"));
        params.setAddDeathOnlyByPlayer(config.getBoolean("AddDeathOnlyByPlayer"));
        params.setResetOnPlayerLeaving(config.getBoolean("ResetOnPlayerLeaving"));
        
        params.setEnableRPWhenKilling(config.getBoolean("RPWhenKilling.enable"));
        
        params.setEnableLPWhenDying(config.getBoolean("LPWhenDying.enable"));
        params.setLPWhenDyingJustByPlayers(config.getBoolean("LPWhenDying.onlyPlayers"));

        try {
            params.setReceivedFormula(config.getString("Modificator.Received.formula"));
        } catch (RuntimeException ex) {
            CustomLogger.logError("Error on parsing formula of Modificator.Received - "
                    + ex.getMessage());
        }
        params.setReceivedMod(config.getDouble("Modificator.Received.value"));

        try {
            params.setLostFormula(config.getString("Modificator.Lost.formula"));
        } catch (RuntimeException ex) {
            CustomLogger.logError("Error on parsing formula of Modificator.Lost - "
                    + ex.getMessage());
        }
        params.setLostMod(config.getDouble("Modificator.Lost.value"));

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

        Set<String> ranks = config.getConfigurationSection("Ranks").getKeys(false);
        ranks.forEach(rank -> {
            String display = config.getString("Ranks." + rank + ".display");
            int points = config.getInt("Ranks." + rank + ".points");
            long time = config.getLong("Ranks." + rank + ".time");
            boolean restricted = config.getBoolean("Ranks." + rank + ".restricted");

            RankManager.addRank(new Rank(rank, points, display, time, restricted));
        });
        
        return params;
    }

    public FileConfiguration getConfig() {
        return customConfig.getConfig();
    }
}
