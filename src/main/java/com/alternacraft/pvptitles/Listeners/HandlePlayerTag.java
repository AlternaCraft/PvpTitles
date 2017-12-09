/*
 * Copyright (C) 2017 AlternaCraft
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
package com.alternacraft.pvptitles.Listeners;

import com.alternacraft.pvptitles.Events.RankChangedEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Hooks.HolographicHook;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.HOLOPLAYERS;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.ISHDENABLED;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.RANK_LINE;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.TITLE_HEIGHT;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.createHoloPlayer;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.removeHoloPlayer;
import com.alternacraft.pvptitles.Hooks.VNPHook;
import com.alternacraft.pvptitles.Hooks.VaultHook;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Rank;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffectType;

public class HandlePlayerTag implements Listener {

    private static final String IGNORED_RANK = "None";
    private static final String IGNORED_CHAT_PERM = "pvptitles.hideprefix";

    private static PvpTitles plugin;
    private static Manager manager;

    public HandlePlayerTag(PvpTitles pt) {
        plugin = pt;
        manager = pt.getManager();
    }

    //<editor-fold defaultstate="collapsed" desc="CONDITIONS">
    public static boolean canDisplayRank(Player pl, String rank) {
        return isValidWorld(pl.getWorld().getName()) && !hasIgnoredChatPermission(pl)
                && !hasIgnoredRank(rank);
    }
    
    private static boolean canDisplayHologram(Player pl) {
        return !pl.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && (!VNPHook.ISVNPENABLED || VNPHook.ISVNPENABLED && !VNPHook.isVanished(pl))
                && !pl.isSneaking();
    }

    private static boolean isValidWorld(String w) {
        // Compruebo si el mundo esta en la lista de los vetados        
        if (HandlePlayerTag.manager.params.getAffectedWorlds().contains(w.toLowerCase())) {
            if (!HandlePlayerTag.manager.params.isTitleShown()) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasIgnoredChatPermission(Player pl) {
        return VaultHook.hasPermission(IGNORED_CHAT_PERM, pl);
    }

    private static boolean hasIgnoredRank(String rank) {
        return rank.equalsIgnoreCase(IGNORED_RANK);
    }    
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="LISTENERS">
    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player pl = event.getPlayer();
        Rank rank = null;

        try {
            int fame = HandlePlayerTag.manager.getDBH().getDM().loadPlayerFame(event.getPlayer().getUniqueId(), null);
            long seconds = HandlePlayerTag.manager.getDBH().getDM().loadPlayedTime(event.getPlayer().getUniqueId());
            rank = RankManager.getRank(fame, seconds, pl);
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
            return;
        } catch (RanksException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        String format = event.getFormat();

        // Si ya se ha definido un prefix, en caso de que se de alguna de las condiciones lo elimino
        if (rank != null && HandlePlayerTag.manager.params.displayInChat() 
                && canDisplayRank(pl, rank.getId())) {
            
            String rankName = String.format(HandlePlayerTag.manager.params
                    .parseFormat(rank.getDisplay()));

            // Modulo de integracion con plugin de chat
            if (format.contains(HandlePlayerTag.manager.params.getPrefix())) {
                format = format.replace(HandlePlayerTag.manager.params.getPrefix(), rankName);
            } else {
                format = rankName + format;
            }
        } else {
            format = clearFormat(format);
        }

        event.setFormat(format);
    }

    /**
     * Método para establecer el titulo como holograma al jugador
     * <i>No funciona como handler porque es necesario que primero se ejecute el
     * handler de la clase HandlePlayerFame para evitar un error</i>
     *
     * @param player Player
     */
    public static void holoPlayerLogin(Player player) {
        if (ISHDENABLED && HandlePlayerTag.manager.params.displayLikeHolo()) {
            String uuid = player.getUniqueId().toString();

            int fame = 0;
            long totalTime, oldTime = 0;
            Rank rank = null;

            try {
                fame = manager.getDBH().getDM().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            try {
                oldTime = manager.getDBH().getDM().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

            try {
                rank = RankManager.getRank(fame, totalTime, player);
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            String display = (rank != null) ? rank.getDisplay() : "";
            
            // Avoid errors.
            if (HOLOPLAYERS.containsKey(uuid)) {
                removeHoloPlayer(HOLOPLAYERS.get(uuid));           
            }
            // If rank fails but later it works...
            HOLOPLAYERS.put(uuid, createHoloPlayer(player, display));

            // Fix reload
            if (!canDisplayHologram(player) || !canDisplayRank(player, display)) {
                HOLOPLAYERS.get(uuid).clearLines();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Holograms
        if (HOLOPLAYERS.containsKey(uuid)) {
            removeHoloPlayer(HOLOPLAYERS.get(uuid));
            HOLOPLAYERS.remove(uuid);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
            HolographicHook.cleanHoloPlayer(player);

            Location l = new Location(event.getTo().getWorld(),
                    event.getTo().getX(),
                    event.getTo().getY() + TITLE_HEIGHT,
                    event.getTo().getZ());
            
            HolographicHook.moveHoloPlayer(player, l); // Track to avoid weird behavior

            if (canDisplayHologram(player)) {
                int fame = 0;
                try {
                    fame = manager.getDBH().getDM().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long oldTime = 0;
                try {
                    oldTime = manager.getDBH().getDM().loadPlayedTime(player.getUniqueId());
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

                if (canDisplayRank(player, rank.getId())) {
                    HolographicHook.insertHoloPlayer(player, RANK_LINE
                            .replace("%rank%", rank.getDisplay()));
                }                
            }            
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            HolographicHook.cleanHoloPlayer(player);

            Location l = new Location(player.getLocation().getWorld(),
                    player.getLocation().getX(),
                    player.getLocation().getY() + TITLE_HEIGHT,
                    player.getLocation().getZ());
            
            HolographicHook.moveHoloPlayer(player, l); // Same that teleport
            
            if (canDisplayHologram(player)) {
                int fame = 0;
                try {
                    fame = manager.getDBH().getDM().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long oldTime = 0;
                try {
                    oldTime = manager.getDBH().getDM().loadPlayedTime(player.getUniqueId());
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

                if (canDisplayRank(player, rank.getId())) {
                    HolographicHook.insertHoloPlayer(player, RANK_LINE
                            .replace("%rank%", rank.getDisplay()));
                }                
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Location to = event.getTo();

            HolographicHook.moveHoloPlayer(player, 
                    new Location(to.getWorld(), to.getX(), to.getY() + TITLE_HEIGHT, to.getZ()));

            int fame = 0;
            try {
                fame = manager.getDBH().getDM().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long oldTime = 0;
            try {
                oldTime = manager.getDBH().getDM().loadPlayedTime(player.getUniqueId());
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
            
            if (HolographicHook.isEmptyHoloPlayer(player) && canDisplayHologram(player)
                    && canDisplayRank(player, rank.getId())) {
                HolographicHook.insertHoloPlayer(player, RANK_LINE
                        .replace("%rank%", rank.getDisplay()));
            } else if (!HolographicHook.isEmptyHoloPlayer(player) 
                    && (!canDisplayHologram(player) || !canDisplayRank(player, rank.getId()))) {
                HolographicHook.cleanHoloPlayer(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeRank(RankChangedEvent event) {
        Player player = (Player) event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            HolographicHook.cleanHoloPlayer(player);

            if (canDisplayRank(player, event.getNewRankID()) && canDisplayHologram(player)
                    && !player.isDead()) {
                Rank r = RankManager.getRank(event.getNewRankID());
                if (r == null) return;
                HolographicHook.insertHoloPlayer(player, RANK_LINE
                        .replace("%rank%", r.getDisplay()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Holograms
        if (HOLOPLAYERS.containsKey(uuid)) {
            HolographicHook.cleanHoloPlayer(player);

            Location l = new Location(event.getRespawnLocation().getWorld(),
                    event.getRespawnLocation().getX(),
                    event.getRespawnLocation().getY() + TITLE_HEIGHT,
                    event.getRespawnLocation().getZ());
            
            HolographicHook.moveHoloPlayer(player, l);
            
            if (canDisplayHologram(player)) {
                int fame = 0;
                try {
                    fame = manager.getDBH().getDM().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long oldTime = 0;
                try {
                    oldTime = manager.getDBH().getDM().loadPlayedTime(player.getUniqueId());
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

                if (canDisplayRank(player, rank.getId())) {
                    HolographicHook.insertHoloPlayer(player, RANK_LINE
                            .replace("%rank%", rank.getDisplay()));
                }                
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity().getPlayer();
        String uuid = event.getEntity().getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            HolographicHook.cleanHoloPlayer(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSneaking(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();
        
        if (HOLOPLAYERS.containsKey(uuid)) {
            HolographicHook.cleanHoloPlayer(player);

            if (!event.isSneaking() && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                && (!VNPHook.ISVNPENABLED || VNPHook.ISVNPENABLED && !VNPHook.isVanished(player))) {
                int fame = 0;
                try {
                    fame = manager.getDBH().getDM().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long oldTime = 0;
                try {
                    oldTime = manager.getDBH().getDM().loadPlayedTime(player.getUniqueId());
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

                if (canDisplayRank(player, rank.getId())) {
                    HolographicHook.insertHoloPlayer(player, RANK_LINE
                            .replace("%rank%", rank.getDisplay()));
                }
            }
        }
    }
    //</editor-fold>
    
    private String clearFormat(String format) {
        String prefix = HandlePlayerTag.manager.params.getPrefix();
        if (format.contains(prefix)) {
            String removeBlank = "\\s(?:§[\\dabcdefklmnor])*" + Pattern.quote(prefix)
                    + "((?:§[\\dabcdefklmnor])*)\\s";
            if (format.matches(".*" + removeBlank + ".*")) {
                format = format.replaceAll(removeBlank, " $1");
            } else {
                format = format.replace(prefix, "");                
            }
        } 
        return format;
    }
}
