package es.jlh.pvptitles.Integrations;

import com.github.games647.scoreboardstats.ScoreboardStats;
import com.github.games647.scoreboardstats.variables.ReplaceEvent;
import com.github.games647.scoreboardstats.variables.ReplaceManager;
import com.github.games647.scoreboardstats.variables.VariableReplacer;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import es.jlh.pvptitles.Misc.Ranks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author AlternaCraft
 */
public class SBSSetup {

    private ScoreboardStats sbs = null;
    private ReplaceManager replaceManager = null;
    private PvpTitles plugin = null;

    public SBSSetup(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public void setupSBS() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("ScoreboardStats")) {
            sbs = JavaPlugin.getPlugin(ScoreboardStats.class);

            if (sbs != null) {
                replaceManager = sbs.getReplaceManager();
                registerReplacerInterface(replaceManager);

               showMessage(ChatColor.YELLOW + "ScoreBoardStats " + ChatColor.AQUA + "integrated correctly.");
            }
        }
    }

    private void registerReplacerInterface(ReplaceManager replaceManager) {
        replaceManager.register(new VariableReplacer() {
            @Override
            public void onReplace(Player player, String var, ReplaceEvent replaceEvent) {
                int fame = plugin.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                int seconds = plugin.manager.dbh.getDm().loadPlayedTime(player.getUniqueId());
                int killstreak = HandlePlayerFame.getKillStreakFrom(player.getUniqueId().toString());

                /*
                 * La variable rank no funcionara hasta la proxima version del
                 * plugin scoreboardstats
                 */
                switch (var) {
                    case "fame":
                        replaceEvent.setScore(fame);
                        break;
                    case "rank":
                        replaceEvent.setScoreOrText(Ranks.getRank(fame, seconds));
                        break;
                    case "killstreak":
                        replaceEvent.setScore(killstreak);
                }
            }
        }, plugin, "fame", "rank", "killstreak");
    }
}
