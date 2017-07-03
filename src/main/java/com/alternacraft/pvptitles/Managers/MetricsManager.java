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
    private void setMWGraph(Metrics metrics) {
        metrics.addCustomChart(
                new Metrics.SimplePie("multiworld_usage",
                        () -> (Manager.getInstance().params.isMw_enabled()) ? "Enabled" : "Disabled"));
    }

    private void setTUGraph(Metrics metrics) {
        metrics.addCustomChart(
                new Metrics.SimplePie("time_as_requirement",
                        () -> (RankManager.isTimeReqUsed()) ? "Enabled" : "Disabled")
        );
    }

    private void setPDBGraph(Metrics metrics) {
        metrics.addCustomChart(
                new Metrics.SimplePie("preferred_db",
                        () -> DBLoader.tipo.toString())
        );
    }

    private void setDMGraph(Metrics metrics) {
        metrics.addCustomChart(
                new Metrics.SimplePie("display_mode", () -> {
                    if (Manager.getInstance().params.displayInChat()
                            && Manager.getInstance().params.displayLikeHolo()) {
                        return "Both";
                    } else {
                        if (Manager.getInstance().params.displayInChat()) {
                            return "Chat";
                        }
                        if (Manager.getInstance().params.displayLikeHolo()) {
                            return "Holograms";
                        }
                    }
                    return null;
                })
        );
    }

    private void setDLGraph(Metrics metrics) {
        metrics.addCustomChart(
                new Metrics.SimplePie("default_language",
                        () -> Manager.messages.name())
        );
    }

    private void setFGraph(Metrics metrics) {
        metrics.addCustomChart(
                new Metrics.DrilldownPie("formulas", () -> {
                    Map<String, Map<String, Integer>> map = new HashMap();
                    
                    boolean rp = Manager.getInstance().params.isEnableRPWhenKilling();
                    boolean lp = Manager.getInstance().params.isEnableLPWhenDying();
                    
                    if (rp) {
                        String rp_formula = Manager.getInstance().getCh().getConfig()
                            .getString("Modificator.Received.formula");
                        Map<String, Integer> data = new HashMap<>();
                        data.put(rp_formula, 1);
                        map.put("Received", data);
                    }
                    if (lp) {
                        String lp_formula = Manager.getInstance().getCh().getConfig()
                            .getString("Modificator.Lost.formula");
                        Map<String, Integer> data = new HashMap<>();
                        data.put(lp_formula, 1);
                        map.put("Lost", data);
                    }
                    
                    return map;
                })
        );
    }

    private void setDBPerformanceGraph(Metrics metrics, final List<String> lines) {
        metrics.addCustomChart(
                new Metrics.DrilldownPie("general_statistics", () -> {
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
                })
        );
    }
    // </editor-fold>

    public void sendData(PvpTitles plugin) {
        if (plugin.getManager().params.isMetrics()) {
            Metrics metrics = new Metrics(plugin);

            setDLGraph(metrics); // Default language
            setPDBGraph(metrics); // Preferred database
            setMWGraph(metrics); // Multi world
            setDMGraph(metrics); // Display mode
            setTUGraph(metrics); // Time usage as requirement
            setFGraph(metrics); // Formulas
            
            // DB's performance
            PluginLog pl = new PluginLog(plugin, "performance.txt");
            pl.importLog();
            setDBPerformanceGraph(metrics, pl.getMessages());
            UtilsFile.delete(PvpTitles.PLUGIN_DIR + PluginLog.getLogsFolder()
                    + File.separator + "performance.txt");
        }
    }
}
