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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bstats.Metrics;
import org.bstats.Metrics.CustomChart;
import org.json.simple.JSONObject;

public class MetricsManager {

    private final static String PATTERN = "(.*) \\((.*)\\) \\- (.*)";

    // <editor-fold defaultstate="collapsed" desc="GRAPHS">
    private void setMWGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("multiworld_usage") {
            @Override
            public String getValue() {
                return (plugin.getManager().params.isMw_enabled()) ? "Enabled" : "Disabled";
            }
        });
    }

    private void setTUGraph(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("time_as_requirement") {
            @Override
            public String getValue() {
                return (RankManager.isTimeReqUsed()) ? "Enabled" : "Disabled";
            }
        });
    }

    private void setPDBGraph(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("preferred_db") {
            @Override
            public String getValue() {
                return DBLoader.tipo.toString();
            }
        });
    }

    private void setDMGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("display_mode") {
            @Override
            public String getValue() {
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
        });
    }

    private void setDLGraph(Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("default_language") {
            @Override
            public String getValue() {
                return Manager.messages.name();
            }
        });
    }

    private void setAPGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.AdvancedPie("awarded_points") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hm) {
                boolean rp = plugin.getManager().params.isEnableRPWhenKilling();
                boolean lp = plugin.getManager().params.isEnableLPWhenDying();
                hm.put("RP", (rp) ? 1:0);
                hm.put("LP", (lp) ? 1:0);
                return hm;
            }
        });
    }

    private void setDBPerformanceGraph(Metrics metrics, final List<String> lines) {
        metrics.addCustomChart(new DrilldownPieChart("general_statistics") {
            @Override
            public Map<String, Map<String, Integer>> getValues(Map<String, 
                    Map<String, Integer>> map) {

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

        });
    }
    // </editor-fold>

    public void sendData(PvpTitles plugin) {
        if (plugin.getManager().params.isMetrics()) {
            Metrics metrics = new Metrics(plugin);

            setMWGraph(plugin, metrics); // Multi world
            setTUGraph(metrics); // Time usage as requirement
            setPDBGraph(metrics); // Preferred database
            setDMGraph(plugin, metrics); // Display mode
            setDLGraph(metrics); // Default language
            setAPGraph(plugin, metrics); // Awarded points

            // DB's performance
            PluginLog pl = new PluginLog(plugin, "performance.txt");
            pl.importLog();
            
            setDBPerformanceGraph(metrics, pl.getMessages());
            
            UtilsFile.delete(PvpTitles.PLUGIN_DIR + PluginLog.getLogsFolder()
                        + File.separator + "performance.txt");
        }
    }
    
    public static abstract class DrilldownPieChart extends CustomChart {
        
        public DrilldownPieChart(String chartId) {
            super(chartId);
        }
        
        /**
         * Gets the value of the pie.
         *
         * @param map Just an empty map.
         * 
         * @return The values of the pie.
         */
        public abstract Map<String, Map<String, Integer>> getValues(Map<String, 
                Map<String, Integer>> map);       
        
        @Override
        protected JSONObject getChartData() {
            JSONObject data = new JSONObject();            
            
            Map<String, Map<String, Integer>> map = 
                    getValues(new HashMap<String, Map<String, Integer>>());    
            
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }  
            
            JSONObject firstLevelVals = new JSONObject();
            boolean allSkipped = true;
            
            JSONObject secondLevelVals;
            boolean allSkipped2;
            
            for (Map.Entry<String, Map<String, Integer>> firstLevel : map.entrySet()) {
                String k = firstLevel.getKey();                
                Map<String, Integer> v = firstLevel.getValue(); 
                
                if (v == null) {
                    continue;
                }
                
                allSkipped2 = true;  
                secondLevelVals = new JSONObject();
                
                for (Map.Entry<String, Integer> secondLevel : v.entrySet()) {
                    String kk = secondLevel.getKey();
                    Integer vv = secondLevel.getValue();
                    
                    if (vv == 0) {
                        continue; // Skip this invalid
                    }         
                    
                    allSkipped2 = false;                    
                    secondLevelVals.put(kk, vv);
                }       
                
                if (allSkipped2) {
                    continue;
                }
                
                allSkipped = false;                
                firstLevelVals.put(k, secondLevelVals);
            }
            
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            
            data.put("values", firstLevelVals);
            return data;
        }               
    }
}
