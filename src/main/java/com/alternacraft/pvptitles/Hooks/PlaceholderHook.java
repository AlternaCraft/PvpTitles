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

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Listeners.HandlePlayerFame;
import static com.alternacraft.pvptitles.Listeners.HandlePlayerTag.canDisplayRank;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Rank;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class PlaceholderHook extends EZPlaceholderHook {

    private PvpTitles plugin = null;

    public PlaceholderHook(PvpTitles plugin) {
        super(plugin, "pvptitles");
        this.plugin = plugin;
    }
    
    public String[] setup() {
        if (this.hook()) {            
            return new String[]{"Placeholder API"};
        }
        return new String[]{};
    }

    @Override
    public String onPlaceholderRequest(Player player, String id) {
        if (id == null || player == null) {
            return "";
        }

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

        int killstreak = HandlePlayerFame.getKillStreakFrom(player.getUniqueId().toString());

        
        Rank vRank = null;
        try {
            vRank = RankManager.getRank(fame, seconds, player);
        } catch (RanksException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }
        
        String rank = (vRank != null) ? vRank.getDisplay():"";
        
        String result = null;        
        switch (id) {
            case "rank":
                result = rank;
                break;
            case "valid_rank":
                result = (canDisplayRank(player, rank)) ? rank : "";
                break;
            case "fame":
                result = String.valueOf(fame);
                break;
            case "killstreak":
                result = String.valueOf(killstreak);
                break;
            default:
                break;
        }
        return result;
    }
}
