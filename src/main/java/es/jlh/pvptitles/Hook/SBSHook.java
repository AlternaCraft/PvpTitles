/*
 * Copyright (C) 2016 AlternaCraft
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.jlh.pvptitles.Hook;

import com.github.games647.scoreboardstats.ScoreboardStats;
import com.github.games647.scoreboardstats.variables.ReplaceEvent;
import com.github.games647.scoreboardstats.variables.ReplaceManager;
import com.github.games647.scoreboardstats.variables.VariableReplacer;
import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import es.jlh.pvptitles.Misc.Ranks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SBSHook {

    private ScoreboardStats sbs = null;
    private ReplaceManager replaceManager = null;
    private PvpTitles plugin = null;

    public SBSHook(PvpTitles plugin) {
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

                /*
                 * La variable rank no funcionara hasta la proxima version del
                 * plugin scoreboardstats que lo implemente
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
