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

public class MetricsManager {

    private final static String PATTERN = "(.*) \\((.*)\\) \\- (.*)";

    private Map<String, Map<String, Object>> importLog(final PvpTitles plugin, String logname) {
        Map<String, Map<String, Object>> pairs = new HashMap<>();

        PluginLog pl = new PluginLog(plugin, logname);
        pl.importLog();
        List<String> lines = pl.getMessages();

        for (String line : lines) {
            if (!line.contains("---")
                    && !line.matches("(\\d+\\-)+\\d+ (\\d+\\:)+\\d+")) {
                Pattern pattern = Pattern.compile(PATTERN);
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    String key = matcher.group(2);

                    String id = matcher.group(1);
                    Object v = matcher.group(3);

                    if (!pairs.containsKey(key)) {
                        pairs.put(key, new HashMap());
                    }

                    pairs.get(key).put(id, v);
                }
            }
        }

        UtilsFile.delete(PvpTitles.PLUGIN_DIR + PluginLog.getLogsFolder()
                + File.separator + "performance.txt");

        return pairs;
    }

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
                boolean timeUsed = false;

                for (Long value : Manager.reqTime()) {
                    if (value > 0) {
                        timeUsed = true;
                        break;
                    }
                }

                return (timeUsed) ? "Enabled" : "Disabled";
            }
        });
    }

    private void setPDBGraph(final PvpTitles plugin, Metrics metrics) {
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

    private void setDLGraph(final PvpTitles plugin, Metrics metrics) {
        metrics.addCustomChart(new Metrics.SimplePie("default_language") {
            @Override
            public String getValue() {
                return Manager.messages.name();
            }
        });
    }

    private void setPerformanceGraph(final PvpTitles pvptitles, Metrics metrics, final String db) {
        metrics.addCustomChart(new Metrics.AdvancedPie("general_statistics") {
            @Override
            public HashMap<String, Integer> getValues(HashMap<String, Integer> hm) {
                Map<String, Object> pairs = importLog(pvptitles, "performance.txt").get(db.toUpperCase());

                if (pairs != null) {
                    for (Map.Entry<String, Object> entry : pairs.entrySet()) {
                        String key = entry.getKey();
                        String value = (String) entry.getValue();
                        hm.put(key, Integer.valueOf(value));
                    }
                }

                return hm;
            }

        });
    }
    // </editor-fold>

    public void sendData(PvpTitles plugin) {
        if (plugin.getManager().params.isMetrics()) {
            Metrics metrics = new Metrics(plugin);

            setMWGraph(plugin, metrics); // Multi world
            setTUGraph(metrics); // Time usage as requirement
            setPDBGraph(plugin, metrics); // Preferred database
            setDMGraph(plugin, metrics); // Display mode
            setDLGraph(plugin, metrics); // Default language

            // DB's performance
            for (DBLoader.DBTYPE value : DBLoader.DBTYPE.values()) {
                setPerformanceGraph(plugin, metrics, value.name());
            }
        }
    }
}
