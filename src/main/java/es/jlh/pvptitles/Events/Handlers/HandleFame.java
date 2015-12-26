package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Events.FameAddEvent;
import es.jlh.pvptitles.Events.FameEvent;
import es.jlh.pvptitles.Events.FameSetEvent;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Integrations.VaultSetup;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Localizer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
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
            Map<String, Map<String, List<String>>> kills = pt.cm.commandsRw.get("onKill");
            setValues(kills.get(""), e.getOfflinePlayer());
        }

        Map<String, Map<String, List<String>>> fame = pt.cm.commandsRw.get("onFame");
        if (fame != null) {
            for (String cantidad : fame.keySet()) {
                if (e.getFame() < Integer.valueOf(cantidad)
                        && (e.getFameTotal()) >= Integer.valueOf(cantidad)) {
                    setValues(fame.get(cantidad), e.getOfflinePlayer());
                    break;
                }
            }
        }

        Map<String, Map<String, List<String>>> rank = pt.cm.commandsRw.get("onRank");
        if (rank != null) {
            for (String rango : rank.keySet()) {
                int seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());
                String lastRank = Ranks.GetRank(e.getFame(), seconds);
                if (rango.equals(Ranks.GetRank(e.getFameTotal(), seconds)) && !rango.equals(lastRank)) {
                    setValues(rank.get(rango), e.getOfflinePlayer());
                    break;
                }
            }
        }

        Map<String, Map<String, List<String>>> killstreak = pt.cm.commandsRw.get("onKillstreak");
        if (killstreak != null) {
            for (String ks : killstreak.keySet()) {
                if (e.getKillstreak() == Integer.valueOf(ks)) {
                    setValues(killstreak.get(ks), e.getOfflinePlayer());
                    break;
                }
            }
        }
    }

    private void setValues(Map<String, List<String>> data, OfflinePlayer pl) {
        Economy economy = VaultSetup.economy;

        if (data.containsKey("money")) {
            if (economy != null) {
                List<String> money = data.get("money");
                economy.depositPlayer(pl, Integer.valueOf(money.get(money.size() - 1)));
            }
        }
        if (data.containsKey("commands")) {
            for (Iterator<String> it = data.get("commands").iterator(); it.hasNext();) {
                String cmd = it.next();
                cmd = cmd.replaceAll("<[pP]layer>", pl.getName());
                pt.getServer().dispatchCommand(pt.getServer().getConsoleSender(), cmd);
            }
        }
    }

    @EventHandler
    public void onSetFame(FameSetEvent e) {
        if (!e.isOnline()) {
            return;
        }

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
        if (!e.isOnline()) {
            return;
        }

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
