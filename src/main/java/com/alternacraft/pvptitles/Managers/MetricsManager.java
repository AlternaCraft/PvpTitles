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
package com.alternacraft.pvptitles.Managers;

import com.alternacraft.pvptitles.Main.DBLoader;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.PluginLog;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bstats.Metrics;

public class MetricsManager {

    private final static String PATTERN = "(.*) \\((.*)\\) \\- (.*)";

    // <editor-fold defaultstate="collapsed" desc="GRAPHS">
    private void setMWGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("multiworld_usage", 
                new Callable<String>() {
            @Override
            public String call() {
                return (plugin.getManager().params.isMw_enabled()) ? "Enabled" : "Disabled";
            }
        }));
    }

    private void setTUGraph(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("time_as_requirement", 
                new Callable<String>() {
            @Override
            public String call() {
                return (RankManager.isTimeReqUsed()) ? "Enabled" : "Disabled";
            }
        }));
    }

    private void setPDBGraph(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("preferred_db", 
                new Callable<String>() {
            @Override
            public String call() {
                return DBLoader.tipo.toString();
            }
        }));
    }

    private void setDMGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("display_mode", 
                new Callable<String>() {
            @Override
            public String call() {
                if (plugin.getManager().params.displayInChat()
                        && plugin.getManager().params.displayLikeHolo()) {
                    return "Both";
                } else {
                    if (plugin.getManager().params.displayInChat()) {
                        return "Chat";
                    }
                    if (plugin.getManager().params.displayLikeHolo()) {
                        return "Holograms";
                    }
                }
                return null;
            }
        }));
    }

    private void setDLGraph(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("default_language", 
                new Callable<String>() {
            @Override
            public String call() {
                return Manager.messages.name();
            }
        }));
    }

    private void setAPGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.AdvancedPie("awarded_points", 
                new Callable<Map<String, Integer>>() {
            @Override
            public Map<String, Integer> call() throws Exception {
                boolean rp = plugin.getManager().params.isEnableRPWhenKilling();
                boolean lp = plugin.getManager().params.isEnableLPWhenDying();
                Map map = new HashMap();
                map.put("RP", (rp) ? 1:0);
                map.put("LP", (lp) ? 1:0);
                return map;
            }
        }));
    }

    private void setDBPerformanceGraph(Metrics metrics, final List<String> lines) {
        metrics.addCustomChart(new Metrics.DrilldownPie("general_statistics", 
                new Callable<Map<String, Map<String, Integer>>>() {
            @Override
            public Map<String, Map<String, Integer>> call() {
                
                Map<String, Map<String, Integer>> map = new HashMap();
                
                for (String line : lines) {
                    if (!line.contains("---")
                            && !line.matches("(\\d+\\-)+\\d+ (\\d+\\:)+\\d+")) {
                        Pattern pattern = Pattern.compile(PATTERN);
                        Matcher matcher = pattern.matcher(line);

                        if (matcher.find()) {
                            String key = matcher.group(2);

                            String id = matcher.group(1);
                            String v = matcher.group(3);

                            if (!map.containsKey(key)) {
                                map.put(key, new HashMap());
                            }

                            map.get(key).put(id, Integer.valueOf(v));
                        }
                    }
                }

                return map;
            }
        }));
    }
    // </editor-fold>

    public void sendData(PvpTitles plugin) {
        if (plugin.getManager().params.isMetrics()) {
            Metrics metrics = new Metrics(plugin);

            setDLGraph(metrics); // Default language
            setPDBGraph(metrics); // Preferred database
            setMWGraph(plugin, metrics); // Multi world
            setDMGraph(plugin, metrics); // Display mode
            setTUGraph(metrics); // Time usage as requirement
            setAPGraph(plugin, metrics); // Awarded points

            // DB's performance
            PluginLog pl = new PluginLog(plugin, "performance.txt");
            pl.importLog();            
            setDBPerformanceGraph(metrics, pl.getMessages());            
            UtilsFile.delete(PvpTitles.PLUGIN_DIR + PluginLog.getLogsFolder()
                        + File.separator + "performance.txt");
        }
    }   
}
