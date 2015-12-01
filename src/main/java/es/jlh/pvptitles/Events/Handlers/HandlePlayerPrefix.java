package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Integrations.VaultSetup;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.Ranks;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 *
 * @author AlternaCraft
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

        // Fix prefix
        Permission perm = VaultSetup.permission;
        if (perm != null) {
            String group = perm.getPrimaryGroup(event.getPlayer());            
            World w = null;
            World wp = event.getPlayer().getWorld();            
            
            if (perm.groupHas(w, group, "pvptitles.hideprefix") || 
                    perm.groupHas(wp, group, "pvptitles.hideprefix")) {
                return;
            }
        }
        else if (!event.getPlayer().isOp() && event.getPlayer().hasPermission("pvptitles.hideprefix")) {
            return;
        }

        int fame = this.cm.dbh.getDm().loadPlayerFame(event.getPlayer().getUniqueId(), null);
        int seconds = this.cm.dbh.getDm().loadPlayedTime(event.getPlayer().getUniqueId());
        rank = Ranks.GetRank(fame, seconds);

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
