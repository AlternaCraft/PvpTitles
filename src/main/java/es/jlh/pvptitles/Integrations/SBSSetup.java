package es.jlh.pvptitles.Integrations;

import com.github.games647.scoreboardstats.ScoreboardStats;
import com.github.games647.scoreboardstats.variables.ReplaceEvent;
import com.github.games647.scoreboardstats.variables.ReplaceManager;
import com.github.games647.scoreboardstats.variables.VariableReplacer;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Ranks;
import org.bukkit.Bukkit;
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
        if (Bukkit.getPluginManager().isPluginEnabled("ScoreboardStats")) {
            sbs = JavaPlugin.getPlugin(ScoreboardStats.class);

            if (sbs != null) {
                replaceManager = sbs.getReplaceManager();
                registerReplacerInterface(replaceManager);

                plugin.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.YELLOW
                        + "ScoreBoardStats " + ChatColor.AQUA + "integrated correctly.");
            }
        }
    }

    private void registerReplacerInterface(ReplaceManager replaceManager) {
        replaceManager.register(new VariableReplacer() {
            @Override
            public void onReplace(Player player, String var, ReplaceEvent replaceEvent) {
                int puntos = plugin.cm.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                int seconds = plugin.cm.dbh.getDm().loadPlayedTime(player.getUniqueId());

                /*
                 * La variable rank no funcionara hasta la proxima version del
                 * plugin scoreboardstats
                 */
                switch (var) {
                    case "fame":
                        replaceEvent.setScore(puntos);
                        break;
                    case "rank":
                        replaceEvent.setScoreOrText(Ranks.GetRank(puntos, seconds));
                        break;
                }
            }
        }, plugin, "fame", "rank");
    }
}
