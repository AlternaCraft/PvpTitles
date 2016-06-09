package es.jlh.pvptitles.Hook;

import es.jlh.pvptitles.Main.PvpTitles;
import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import es.jlh.pvptitles.Misc.Ranks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class MVdWPlaceholderHook {

    private PvpTitles plugin = null;

    public MVdWPlaceholderHook(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            PvpTitles.showMessage(ChatColor.YELLOW + "MVdWPlaceholderAPI " + ChatColor.AQUA + "integrated correctly.");

            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_rank", 
                    new PlaceholderReplacer() {
                        @Override
                        public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                            Player player = pre.getPlayer();
                            
                            int fame = plugin.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                            int seconds = plugin.manager.dbh.getDm().loadPlayedTime(player.getUniqueId());
                            
                            return Ranks.getRank(fame, seconds);
                        }
                    });
            
            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_fame", 
                    new PlaceholderReplacer() {
                        @Override
                        public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                            Player player = pre.getPlayer();
                            
                            int fame = plugin.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                            
                            return String.valueOf(fame);
                        }
                    });
            
            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_killstreak", 
                    new PlaceholderReplacer() {
                        @Override
                        public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                            Player player = pre.getPlayer();
                            
                            int killstreak = HandlePlayerFame.getKillStreakFrom(player.getUniqueId().toString());
                            
                            return String.valueOf(killstreak);
                        }
                    });
        }
    }
}
