package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Backend.DatabaseManagerEbean;
import es.jlh.pvptitles.Libraries.Metrics;
import es.jlh.pvptitles.Libraries.Metrics.Graph;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author AlternaCraft
 */
public class MetricsManager {

    public void sendData(final PvpTitles plugin) {
        try {
            if (plugin.cm.params.isMetrics()) {
                Metrics metrics = new Metrics(plugin);                

                Graph mwUsedGraph = metrics.createGraph("MultiWorld usage");
                
                if (plugin.cm.params.isMw_enabled()) {
                    addPlotter(mwUsedGraph, "Enabled", 1);
                } else {
                    addPlotter(mwUsedGraph, "Disabled", 1);
                }
                
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
                }
                else {
                    addPlotter(timeUsageGraph, "Disabled", 1);
                }
                
                Graph preferreddb = metrics.createGraph("Preferred DB");
                
                if (plugin.cm.dbh.getDm() instanceof DatabaseManagerEbean) {
                    addPlotter(preferreddb, "Ebean", 1);
                }
                else {
                    addPlotter(preferreddb, "MySQL", 1);
                }
                
                Graph displayMode = metrics.createGraph("Display mode");
                
                if (plugin.cm.params.displayInChat()) {
                    addPlotter(displayMode, "Chat", 1);
                }
                if (plugin.cm.params.displayLikeHolo()) {
                    addPlotter(displayMode, "Holograms", 1);
                }

                Graph defaultLang = metrics.createGraph("Default lang");
                
                addPlotter(defaultLang, Manager.messages.name(), 1);                
                
                metrics.start();
            }
        } catch (IOException e) {
            PvpTitles.logError(e.getMessage(), null);
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
