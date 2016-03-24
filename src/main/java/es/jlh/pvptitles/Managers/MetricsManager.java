package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Backend.DatabaseManagerEbean;
import es.jlh.pvptitles.Libraries.Metrics;
import es.jlh.pvptitles.Libraries.Metrics.Graph;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import java.io.IOException;



/**
 *
 * @author AlternaCraft
 */
public class MetricsManager {

    // <editor-fold defaultstate="collapsed" desc="GRAPHS">
    private void setMWGraph(final PvpTitles plugin, Metrics metrics) {
        Graph mwUsedGraph = metrics.createGraph("MultiWorld usage");

        if (plugin.cm.params.isMw_enabled()) {
            addPlotter(mwUsedGraph, "Enabled", 1);
        } else {
            addPlotter(mwUsedGraph, "Disabled", 1);
        }
    }

    private void setTUGraph(Metrics metrics) {
        Graph timeUsageGraph = metrics.createGraph("Req. Time usage");

        boolean timeUsed = false;

        for (Integer value : Manager.reqTime().values()) {
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

        if (plugin.cm.dbh.getDm() instanceof DatabaseManagerEbean) {
            addPlotter(preferreddb, "Ebean", 1);
        } else {
            addPlotter(preferreddb, "MySQL", 1);
        }
    }

    private void setDMGraph(final PvpTitles plugin, Metrics metrics) {
        Graph displayMode = metrics.createGraph("Display mode");

        if (plugin.cm.params.displayInChat() && plugin.cm.params.displayLikeHolo()) {
            addPlotter(displayMode, "Both", 1);
        } else {
            if (plugin.cm.params.displayInChat()) {
                addPlotter(displayMode, "Chat", 1);
            }
            if (plugin.cm.params.displayLikeHolo()) {
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
            if (plugin.cm.params.isMetrics()) {
                Metrics metrics = new Metrics(plugin);

                setMWGraph(plugin, metrics); // MultiWorld
                setTUGraph(metrics); // Time Usage
                setPDBGraph(plugin, metrics); // Preferred Database
                setDMGraph(plugin, metrics); // Display Mode
                setDLGraph(plugin, metrics); // Default Lang

                metrics.start();
            }
        } catch (IOException e) {
            PvpTitles.logError(e.getMessage(), null);
        }
    }
}
