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
import java.io.IOException;

public class MetricsManager {

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
    // </editor-fold>

    public void sendData(final PvpTitles plugin) {
        try {
            if (plugin.getManager().params.isMetrics()) {
                Metrics metrics = new Metrics(plugin);

                setMWGraph(plugin, metrics); // MultiWorld
                setTUGraph(metrics); // Time Usage
                setPDBGraph(plugin, metrics); // Preferred Database
                setDMGraph(plugin, metrics); // Display Mode
                setDLGraph(plugin, metrics); // Default Lang

                metrics.start();
            }
        } catch (IOException e) {
            LoggerManager.logError(e.getMessage(), null);
        }
    }
}
