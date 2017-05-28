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
import static com.alternacraft.pvptitles.Hooks.HolographicHook.HOLOPLAYERS;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.ISHDENABLED;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.RANK_LINE;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.TITLE_HEIGHT;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.createHoloPlayer;
import static com.alternacraft.pvptitles.Hooks.HolographicHook.removeHoloPlayer;
import com.alternacraft.pvptitles.Hooks.VaultHook;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Rank;
import com.alternacraft.pvptitles.Misc.StrUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    private static Manager cm;

    public HandlePlayerTag(PvpTitles pt) {
        plugin = pt;
        cm = pt.getManager();
    }

    public static boolean canDisplayRank(Player pl, String rank) {
        return isValidWorld(pl.getWorld().getName()) && !hasIgnoredChatPermission(pl)
                && !hasIgnoredRank(rank);
    }

    private static boolean isValidWorld(String w) {
        // Compruebo si el mundo esta en la lista de los vetados        
        if (HandlePlayerTag.cm.params.getAffectedWorlds().contains(w.toLowerCase())) {
            if (!HandlePlayerTag.cm.params.isTitleShown()) {
                return false;
            }
        }

        return true;
    }

    private static boolean hasIgnoredChatPermission(Player pl) {
        return (pl.isOp() && VaultHook.PERMISSIONS_ENABLED) 
                ? VaultHook.hasPermission(IGNORED_CHAT_PERM, pl)
                :pl.hasPermission(IGNORED_CHAT_PERM);
    }

    private static boolean hasIgnoredRank(String rank) {
        return rank.equalsIgnoreCase(IGNORED_RANK);
    }

    private String clearFormat(String format) {
        if (format.contains(HandlePlayerTag.cm.params.getPrefix())) {
            format = format.replace(HandlePlayerTag.cm.params.getPrefix(), "");
        }
        return format;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player pl = event.getPlayer();
        Rank rank = null;

        try {
            int fame = HandlePlayerTag.cm.dbh.getDm().loadPlayerFame(event.getPlayer().getUniqueId(), null);
            long seconds = HandlePlayerTag.cm.dbh.getDm().loadPlayedTime(event.getPlayer().getUniqueId());
            rank = RankManager.getRank(fame, seconds, pl);
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
            return;
        } catch (RanksException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        String format = event.getFormat();

        if (rank != null) {
            // Si ya se ha definido un prefix, en caso de que se de alguna de las condiciones lo elimino
            if (!HandlePlayerTag.cm.params.displayInChat() || !canDisplayRank(pl, rank.getId())) {
                clearFormat(format);
            } else {
                String rankName = String.format(HandlePlayerTag.cm.params.getPrefixColor()
                        + rank.getDisplay() + ChatColor.RESET);

                // Modulo de integracion con plugin de chat
                if (format.contains(HandlePlayerTag.cm.params.getPrefix())) {
                    format = format.replace(HandlePlayerTag.cm.params.getPrefix(), rankName);
                } else {
                    format = rankName + format;
                }
            }
        } else {
            clearFormat(format);
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
        if (ISHDENABLED && HandlePlayerTag.cm.params.displayLikeHolo()) {
            String uuid = player.getUniqueId().toString();

            int fame = 0;
            long totalTime, oldTime = 0;
            Rank rank = null;

            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

            try {
                rank = RankManager.getRank(fame, totalTime, player);
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            String display = (rank != null) ? rank.getDisplay():"";
            
            // If rank fails but later it works...
            HOLOPLAYERS.put(uuid, createHoloPlayer(player, display));

            // Fix reload
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                    || player.isSneaking() || !canDisplayRank(player, display)) {
                HOLOPLAYERS.get(uuid).clearLines();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Holograms
        if (HOLOPLAYERS.containsKey(uuid)) {
            removeHoloPlayer(HOLOPLAYERS.get(uuid));
            HOLOPLAYERS.remove(uuid);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
            Hologram h = HOLOPLAYERS.get(player.getUniqueId().toString());
            h.clearLines();

            Location l = new Location(event.getTo().getWorld(),
                    event.getTo().getX(),
                    event.getTo().getY() + TITLE_HEIGHT,
                    event.getTo().getZ());
            h.teleport(l);

            int fame = 0;
            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long oldTime = 0;
            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

            String rank = "";
            try {                
                rank = RankManager.getRank(fame, totalTime, player).getDisplay();
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            if (canDisplayRank(player, rank)) {
                h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);
            h.clearLines();

            Location l = new Location(player.getLocation().getWorld(),
                    player.getLocation().getX(),
                    player.getLocation().getY() + TITLE_HEIGHT,
                    player.getLocation().getZ());
            h.teleport(l);

            int fame = 0;
            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long oldTime = 0;
            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

            String rank = "";
            try {
                rank = RankManager.getRank(fame, totalTime, player).getDisplay();
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            if (canDisplayRank(player, rank)) {
                h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Location to = event.getTo();

            Hologram h = HOLOPLAYERS.get(uuid);
            h.teleport(new Location(to.getWorld(), to.getX(), to.getY() + TITLE_HEIGHT, to.getZ()));

            if (StrUtils.isHologramEmpty(h) && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                    && !player.isSneaking() && !player.isDead()) {
                int fame = 0;
                try {
                    fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long oldTime = 0;
                try {
                    oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

                String rank = "";
                try {
                    rank = RankManager.getRank(fame, totalTime, player).getDisplay();
                } catch (RanksException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                if (canDisplayRank(player, rank)) {
                    h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
                }
            } else if (!StrUtils.isHologramEmpty(h)
                    && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                h.removeLine(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeRank(RankChangedEvent event) {
        Player player = (Player) event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);
            h.clearLines();

            if (canDisplayRank(player, event.getNewRank())) {
                Rank r = RankManager.getRank(event.getNewRank());
                if (r == null) return;
                h.insertTextLine(0, RANK_LINE.replace("%rank%", r.getDisplay()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Holograms
        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);
            h.clearLines();

            Location l = new Location(event.getRespawnLocation().getWorld(),
                    event.getRespawnLocation().getX(),
                    event.getRespawnLocation().getY() + TITLE_HEIGHT,
                    event.getRespawnLocation().getZ());

            h.teleport(l);

            int fame = 0;
            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long oldTime = 0;
            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            long totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

            String rank = "";
            try {
                rank = RankManager.getRank(fame, totalTime, player).getDisplay();
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            if (canDisplayRank(player, rank)) {
                h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String uuid = event.getEntity().getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);
            h.clearLines();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSneaking(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);
            h.clearLines();

            if (!event.isSneaking() && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                int fame = 0;
                try {
                    fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long oldTime = 0;
                try {
                    oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                long totalTime = oldTime + plugin.getManager().getTimerManager().getPlayer(player).getTotalOnline();

                String rank = "";
                try {
                    rank = RankManager.getRank(fame, totalTime, player).getDisplay();
                } catch (RanksException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                if (canDisplayRank(player, rank)) {
                    h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
                }
            }
        }
    }
}
