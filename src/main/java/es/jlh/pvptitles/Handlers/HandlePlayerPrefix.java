package es.jlh.pvptitles.Handlers;

import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Managers.RankManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author julito
 */
public class HandlePlayerPrefix implements Listener {

    private final Manager cm;

    public HandlePlayerPrefix(PvpTitles pt) {
        this.cm = pt.cm;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // Compruebo si el mundo esta en la lista de los vetados
        if (this.cm.params.getAffectedWorlds().contains(event.getPlayer().getWorld().getName().toLowerCase())) {
            if (!this.cm.params.isChat()) {
                return;
            }
        }

        String rank = null;

        if (event.getPlayer().hasPermission("pvptitles.chat")) {
            return;
        }

        int fame = this.cm.getDm().loadPlayerFame(event.getPlayer().getUniqueId());
        int seconds = this.cm.getDm().loadPlayedTime(event.getPlayer().getUniqueId());
        rank = RankManager.GetRank(fame, seconds);

        String format = event.getFormat();
        
        if (rank != null && !rank.isEmpty()) {
            if (rank.equalsIgnoreCase("None")) {
                if (format.contains(this.cm.params.getPrefix())) {
                    format = format.replace(this.cm.params.getPrefix(), "");
                }
            } else {
                String rankName = String.format(this.cm.params.getPrefixColor()
                        + rank + ChatColor.RESET);

                // Modulo de integracion con plugin de chat
                if (format.contains(this.cm.params.getPrefix())) {
                    format = format.replace(this.cm.params.getPrefix(), rankName);
                } else {
                    format = rankName + format;
                }
            }
        }

        event.setFormat(format);
    }
}
