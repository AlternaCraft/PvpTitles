package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Events.FameAddEvent;
import es.jlh.pvptitles.Events.FameEvent;
import es.jlh.pvptitles.Events.FameSetEvent;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Localizer;
import java.util.HashMap;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author AlternaCraft
 */
public class HandleFame implements Listener {

    private final PvpTitles pt;
    private final Manager dm;

    /**
     * Contructor de la clase
     *
     * @param pt Plugin
     */
    public HandleFame(PvpTitles pt) {
        this.pt = pt;
        this.dm = pt.cm;
    }

    @EventHandler
    public void onFame(FameEvent e) {        
        if (!(e instanceof FameSetEvent) && !(e instanceof FameAddEvent)) {
            HashMap<String, List<String>> kill = pt.cm.commandsRw.get("onKill");
            if (kill != null) {
                for (String cmd : kill.get("")) {
                    cmd = cmd.replace("<player>", e.getOfflinePlayer().getName());
                    pt.getServer().dispatchCommand(pt.getServer().getConsoleSender(), cmd);
                }
            }
        }

        HashMap<String, List<String>> fame = pt.cm.commandsRw.get("onFame");
        if (fame != null) {
            for (String cantidad : fame.keySet()) {
                if (e.getFame() < Integer.valueOf(cantidad)
                        && (e.getFameTotal()) >= Integer.valueOf(cantidad)) {
                    for (String cmd : fame.get(cantidad)) {
                        cmd = cmd.replace("<player>", e.getOfflinePlayer().getName());
                        pt.getServer().dispatchCommand(pt.getServer().getConsoleSender(), cmd);
                    }
                }
            }
        }

        HashMap<String, List<String>> rank = pt.cm.commandsRw.get("onRank");
        if (rank != null) {
            for (String rango : rank.keySet()) {
                int seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());
                if (rango.equals(Ranks.GetRank(e.getFameTotal(), seconds))) {
                    for (String cmd : rank.get(rango)) {
                        cmd = cmd.replace("<player>", e.getOfflinePlayer().getName());
                        pt.getServer().dispatchCommand(pt.getServer().getConsoleSender(), cmd);
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onSetFame(FameSetEvent e) {
        if (!e.isOnline())
            return;
        
        Player pl = e.getPlayer();
        int seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());

        if (pl != null) {
            String rank = Ranks.GetRank(e.getFameTotal(), seconds);

            pl.sendMessage(PLUGIN + LangFile.FAME_EDIT_PLAYER.getText(Localizer.getLocale(pl)).
                    replace("%fame%", String.valueOf(e.getFameTotal())).
                    replace("%rank%", rank).
                    replace("%tag%", this.dm.params.getTag())
            );
        } else {
            //e.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onAddFame(FameAddEvent e) {
        if (!e.isOnline())
            return;
        
        Player pl = e.getPlayer();
        int seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());

        if (pl != null) {
            String rank = Ranks.GetRank(e.getFameTotal(), seconds);

            pl.sendMessage(PLUGIN + LangFile.FAME_EDIT_PLAYER.getText(Localizer.getLocale(pl)).
                    replace("%fame%", String.valueOf(e.getFameTotal())).
                    replace("%rank%", rank).
                    replace("%tag%", this.dm.params.getTag())
            );
        } else {
            //e.setCancelled(true);
        }
    }
}
