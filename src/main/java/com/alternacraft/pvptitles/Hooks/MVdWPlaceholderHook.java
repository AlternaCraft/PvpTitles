/*
 * Copyright (C) 2018 AlternaCraft
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
package com.alternacraft.pvptitles.Hooks;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Listeners.HandlePlayerFame;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Rank;
import org.bukkit.entity.Player;

import static com.alternacraft.pvptitles.Listeners.HandlePlayerTag.canDisplayRank;

public class MVdWPlaceholderHook {

    private PvpTitles plugin = null;

    public MVdWPlaceholderHook(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public String[] setup() {
        PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_valid_rank", 
                (PlaceholderReplaceEvent pre) -> {
            Player player = pre.getPlayer();

            int fame = 0;
            try {
                fame = plugin.getManager().getDBH().getDM().loadPlayerFame(player.getUniqueId(), null);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long seconds = 0;
            try {
                seconds = plugin.getManager().getDBH().getDM().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            Rank rank = null;
            try {
                rank = RankManager.getRank(fame, seconds, player);
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            if (rank == null || !canDisplayRank(player, rank.getDisplay())) {
                return "";
            }

            return rank.getDisplay();
        });

        PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_rank", 
                (PlaceholderReplaceEvent pre) -> {
            Player player = pre.getPlayer();

            int fame = 0;
            try {
                fame = plugin.getManager().getDBH().getDM().loadPlayerFame(player.getUniqueId(), null);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());                    
            }

            long seconds = 0;
            try {
                seconds = plugin.getManager().getDBH().getDM().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            Rank rank = null;
            try {
                rank = RankManager.getRank(fame, seconds, player);
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            return (rank == null) ? "":rank.getDisplay();
        });

        PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_fame", 
                (PlaceholderReplaceEvent pre) -> {
            Player player = pre.getPlayer();

            int fame = 0;
            try {
                fame = plugin.getManager().getDBH().getDM().loadPlayerFame(player.getUniqueId(), null);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            return String.valueOf(fame);
        });

        PlaceholderAPI.registerPlaceholder(plugin, "pvptitles_killstreak", 
                (PlaceholderReplaceEvent pre) -> {
            Player player = pre.getPlayer();

            int killstreak = HandlePlayerFame.getKillStreakFrom(player.getUniqueId().toString());

            return String.valueOf(killstreak);
        });

        return new String[]{"MVdWPlaceholderAPI"};
    }
}
