package es.jlh.pvptitles.Hook;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import static es.jlh.pvptitles.Events.Handlers.HandlePlayerTag.canDisplayRank;
import es.jlh.pvptitles.Main.PvpTitles;
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

            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_valid_rank",
                    new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                    Player player = pre.getPlayer();

                    int fame = 0;
                    try {
                        fame = plugin.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                    }

                    int seconds = 0;
                    try {
                        seconds = plugin.manager.dbh.getDm().loadPlayedTime(player.getUniqueId());
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                    }

                    String rank = Ranks.getRank(fame, seconds);
                    
                    if (!canDisplayRank(player, rank)) {
                        return "";
                    }
                    
                    return rank;
                }
            });

            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_rank",
                    new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                    Player player = pre.getPlayer();

                    int fame = 0;
                    try {
                        fame = plugin.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                    }

                    int seconds = 0;
                    try {
                        seconds = plugin.manager.dbh.getDm().loadPlayedTime(player.getUniqueId());
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                    }

                    return Ranks.getRank(fame, seconds);
                }
            });

            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_fame",
                    new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                    Player player = pre.getPlayer();

                    int fame = 0;
                    try {
                        fame = plugin.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                    }

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
