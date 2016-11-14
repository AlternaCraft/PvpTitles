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
package com.alternacraft.pvptitles.Managers;

import com.alternacraft.pvptitles.Backend.DatabaseManagerEbean;
import com.alternacraft.pvptitles.Libraries.Metrics;
import com.alternacraft.pvptitles.Libraries.Metrics.Graph;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.PluginLog;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        return pairs;
    }

    // <editor-fold defaultstate="collapsed" desc="GRAPHS">
    private void setMWGraph(final PvpTitles plugin, Metrics metrics) {
        Graph mwUsedGraph = metrics.createGraph("MultiWorld usage");

        if (plugin.getManager().params.isMw_enabled()) {
            addPlotter(mwUsedGraph, "Enabled", 1);
        } else {
            addPlotter(mwUsedGraph, "Disabled", 1);
        }
    }

    private void setTUGraph(Metrics metrics) {
        Graph timeUsageGraph = metrics.createGraph("Req. Time usage");

        boolean timeUsed = false;

        for (Integer value : Manager.reqTime()) {
            if (value > 0) {
                timeUsed = true;
                break;
            }
        }

        if (timeUsed) {
            addPlotter(timeUsageGraph, "Enabled", 1);
        } else {
            addPlotter(timeUsageGraph, "Disabled", 1);
        }
    }

    private void setPDBGraph(final PvpTitles plugin, Metrics metrics) {
        Graph preferreddb = metrics.createGraph("Preferred DB");

        if (plugin.getManager().dbh.getDm() instanceof DatabaseManagerEbean) {
            addPlotter(preferreddb, "Ebean", 1);
        } else {
            addPlotter(preferreddb, "MySQL", 1);
        }
    }

    private void setDMGraph(final PvpTitles plugin, Metrics metrics) {
        Graph displayMode = metrics.createGraph("Display mode");

        if (plugin.getManager().params.displayInChat() && plugin.getManager().params.displayLikeHolo()) {
            addPlotter(displayMode, "Both", 1);
        } else {
            if (plugin.getManager().params.displayInChat()) {
                addPlotter(displayMode, "Chat", 1);
            }
            if (plugin.getManager().params.displayLikeHolo()) {
                addPlotter(displayMode, "Holograms", 1);
            }
        }
    }

    private void setDLGraph(final PvpTitles plugin, Metrics metrics) {
        Graph defaultLang = metrics.createGraph("Default lang");
        addPlotter(defaultLang, Manager.messages.name(), 1);
    }

    private void setPerformanceGraph(final PvpTitles pvptitles, Metrics metrics, String db) {
        Graph performanceGraph = metrics.createGraph(db + " performance");

        Map<String, Object> pairs = importLog(pvptitles, "performance.txt").get(db.toUpperCase());

        if (pairs == null) {
            return;
        }

        for (Map.Entry<String, Object> entry : pairs.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            addPlotter(performanceGraph, key, Integer.valueOf(value));
        }
    }
    // </editor-fold>

    public void sendData(final PvpTitles plugin) {
        try {
            if (plugin.getManager().params.isMetrics() || true) {
                Metrics metrics = new Metrics(plugin);

                setMWGraph(plugin, metrics); // Multi world
                setTUGraph(metrics); // Time usage as requirement
                setPDBGraph(plugin, metrics); // Preferred database
                setDMGraph(plugin, metrics); // Display mode
                setDLGraph(plugin, metrics); // Default language
                
                setPerformanceGraph(plugin, metrics, "Ebean"); // Ebean performance
                setPerformanceGraph(plugin, metrics, "MySQL"); // MySQL performance
                
                UtilsFile.delete(plugin.getDataFolder() + File.separator
                + PluginLog.getLogsFolder() + File.separator + "performance.txt");
                
                metrics.start();
            }
        } catch (IOException e) {
            LoggerManager.logError(e.getMessage());
        }
    }

    public void addPlotter(Graph g, String plotter, final int number) {
        if (number == 0) {
            return;
        }
        g.addPlotter(new Metrics.Plotter(plotter) {
            @Override
            public int getValue() {
                return number;
            }
        });
    }
}
