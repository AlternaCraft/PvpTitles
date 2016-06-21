package es.jlh.pvptitles.Hook;

import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import static es.jlh.pvptitles.Events.Handlers.HandlePlayerTag.canDisplayRank;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.Ranks;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class PlaceholderHook extends EZPlaceholderHook {

    private PvpTitles plugin = null;

    public PlaceholderHook(PvpTitles plugin) {
        super(plugin, "pvptitles");
        this.plugin = plugin;
        
        PvpTitles.showMessage(ChatColor.YELLOW + "Placeholder API " + ChatColor.AQUA + "integrated correctly.");
    }

    @Override
    public String onPlaceholderRequest(Player player, String id) {
        if (id == null || player == null) {
            return "";
        }
        
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
        
        int killstreak = HandlePlayerFame.getKillStreakFrom(player.getUniqueId().toString());
        
        String rank = Ranks.getRank(fame, seconds);
        
        if (id.equals("rank")) {
            return rank;
        }
        else if (id.equals("valid_rank")) {            
            return (canDisplayRank(player, rank)) ? rank:"";
        }
        else if (id.equals("fame")) {
            return String.valueOf(fame);
        }
        else if (id.equals("killstreak")) {
            return String.valueOf(killstreak);
        }
        
        return null;
    }
}
