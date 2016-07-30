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

import com.alternacraft.pvptitles.Backend.Exceptions.DBException;
import com.alternacraft.pvptitles.Events.Handlers.HandlePlayerFame;
import static com.alternacraft.pvptitles.Events.Handlers.HandlePlayerTag.canDisplayRank;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Main.Managers.MessageManager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.Ranks;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlaceholderHook extends EZPlaceholderHook {

    private PvpTitles plugin = null;

    public PlaceholderHook(PvpTitles plugin) {
        super(plugin, "pvptitles");
        this.plugin = plugin;
        
        MessageManager.showMessage(ChatColor.YELLOW + "Placeholder API " + ChatColor.AQUA + "integrated correctly.");
    }

    @Override
    public String onPlaceholderRequest(Player player, String id) {
        if (id == null || player == null) {
            return "";
        }
        
        int fame = 0;
        try {
            fame = plugin.getManager().dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
        } catch (DBException ex) {
            LoggerManager.logError(ex.getCustomMessage(), null);
        }
        
        int seconds = 0;
        try {
            seconds = plugin.getManager().dbh.getDm().loadPlayedTime(player.getUniqueId());
        } catch (DBException ex) {
            LoggerManager.logError(ex.getCustomMessage(), null);
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
