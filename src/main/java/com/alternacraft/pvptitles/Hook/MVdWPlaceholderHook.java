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
package com.alternacraft.pvptitles.Hook;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import com.alternacraft.pvptitles.Events.Handlers.HandlePlayerFame;
import static com.alternacraft.pvptitles.Events.Handlers.HandlePlayerTag.canDisplayRank;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Main.Managers.MessageManager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.Ranks;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MVdWPlaceholderHook {

    private PvpTitles plugin = null;

    public MVdWPlaceholderHook(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        if (plugin.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            MessageManager.showMessage(ChatColor.YELLOW + "MVdWPlaceholderAPI " + ChatColor.AQUA + "integrated correctly.");

            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_valid_rank",
                    new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                    Player player = pre.getPlayer();

                    int fame = 0;
                    try {
                        fame = plugin.getManager().dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                    } catch (DBException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
                    }

                    int seconds = 0;
                    try {
                        seconds = plugin.getManager().dbh.getDm().loadPlayedTime(player.getUniqueId());
                    } catch (DBException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
                    }

                    String rank = "";
                    try {
                        rank = Ranks.getRank(fame, seconds);
                    } catch (RanksException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
                    }

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
                        fame = plugin.getManager().dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                    } catch (DBException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
                    }

                    int seconds = 0;
                    try {
                        seconds = plugin.getManager().dbh.getDm().loadPlayedTime(player.getUniqueId());
                    } catch (DBException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
                    }

                    String rank = "";
                    try {
                        rank = Ranks.getRank(fame, seconds);
                    } catch (RanksException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
                    }

                    return rank;
                }
            });

            PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_fame",
                    new PlaceholderReplacer() {
                @Override
                public String onPlaceholderReplace(PlaceholderReplaceEvent pre) {
                    Player player = pre.getPlayer();

                    int fame = 0;
                    try {
                        fame = plugin.getManager().dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
                    } catch (DBException ex) {
                        LoggerManager.logError(ex.getCustomMessage());
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
