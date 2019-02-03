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
import com.alternacraft.pvptitles.Listeners.HandlePlayerTag;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Rank;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.kitteh.vanish.VanishPlugin;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

import static com.alternacraft.pvptitles.Hooks.HolographicHook.HOLOPLAYERS;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.RANK_LINE;

/**
 *
 * @author AlternaCraft
 */
public class VNPHook {
    
    public static boolean ISVNPENABLED = false;
    
    private final PvpTitles plugin;
    private static VanishPlugin vp;
    
    public VNPHook(PvpTitles plugin) {
        this.plugin = plugin;
    }
    
    public String[] setup() { 
        ISVNPENABLED = true;
        vp = JavaPlugin.getPlugin(VanishPlugin.class);
        if (vp != null && HolographicHook.ISHDENABLED) {
            vp.getServer().getPluginManager().registerEvents(new VanishChecker(), vp);
            return new String[]{"VanishNoPacket"};
        }
        return new String[]{};
    }    
    
    public static boolean isVanished(Player pl) {
        return vp.getManager().isVanished(pl);
    }
    
    public class VanishChecker implements Listener {
        
        @EventHandler
        public void VanishEvent(VanishStatusChangeEvent event) {
            Player player = event.getPlayer();
            String uuid = player.getUniqueId().toString();
            if (HOLOPLAYERS.containsKey(uuid)) {
                HolographicHook.cleanHoloPlayer(player);
                
                if (!event.isVanishing() && !player.isSneaking() 
                        && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    int fame = 0;
                    try {
                        fame = plugin.getManager().getDBH().getDM()
                                .loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                    } catch (DBException ex) {
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                    }

                    long oldTime = 0;
                    try {
                        oldTime = plugin.getManager().getDBH().getDM()
                                .loadPlayedTime(player.getUniqueId());
                    } catch (DBException ex) {
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                    }

                    long totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

                    Rank rank;
                    try {
                        rank = RankManager.getRank(fame, totalTime, player);
                    } catch (RanksException ex) {
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                        return;
                    }

                    if (HandlePlayerTag.canDisplayRank(player, rank.getId())) {
                        HolographicHook.insertHoloPlayer(player, RANK_LINE
                            .replace("%rank%", rank.getDisplay()));
                    }
                }       
            }
        }        
    }
}
