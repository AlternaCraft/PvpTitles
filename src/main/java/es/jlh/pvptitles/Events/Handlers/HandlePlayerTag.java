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
package es.jlh.pvptitles.Events.Handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Events.RankChangedEvent;
import static es.jlh.pvptitles.Hook.HolographicHook.HOLOPLAYERS;
import static es.jlh.pvptitles.Hook.HolographicHook.ISHDENABLED;
import static es.jlh.pvptitles.Hook.HolographicHook.RANK_LINE;
import static es.jlh.pvptitles.Hook.HolographicHook.TITLE_HEIGHT;
import static es.jlh.pvptitles.Hook.HolographicHook.createHoloPlayer;
import static es.jlh.pvptitles.Hook.HolographicHook.removeHoloPlayer;
import es.jlh.pvptitles.Hook.VaultHook;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.StrUtils;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
        // Fix prefix
        
        if (VaultHook.PERMISSIONS_ENABLED) { // Vault en el server
            Permission user = VaultHook.permission;
            
            if (user.hasGroupSupport() && user.getPlayerGroups(pl).length != 0) {
                String group = user.getPrimaryGroup(pl);
                
                World w = null;
                World wp = pl.getWorld();

                return (user.groupHas(w, group, IGNORED_CHAT_PERM)
                        || user.groupHas(wp, group, IGNORED_CHAT_PERM));
            } else {
                return user.has(pl, IGNORED_CHAT_PERM);
            }
        }

        return false;
    }

    private static boolean hasIgnoredRank(String rank) {
        return rank.equalsIgnoreCase(IGNORED_RANK);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player pl = event.getPlayer();
        String rank = null;

        int fame = 0;
        try {
            fame = HandlePlayerTag.cm.dbh.getDm().loadPlayerFame(event.getPlayer().getUniqueId(), null);
        } catch (DBException ex) {
            PvpTitles.logError(ex.getCustomMessage(), null);
            return;
        }
        
        int seconds = 0;
        try {
            seconds = HandlePlayerTag.cm.dbh.getDm().loadPlayedTime(event.getPlayer().getUniqueId());
        } catch (DBException ex) {
            PvpTitles.logError(ex.getCustomMessage(), null);
            return;
        }
        rank = Ranks.getRank(fame, seconds);

        String format = event.getFormat();

        if (rank != null && !rank.isEmpty()) {
            // Si ya se ha definido un prefix, en caso de que se de alguna de las condiciones lo elimino
            if (!HandlePlayerTag.cm.params.displayInChat() || !canDisplayRank(pl, rank)) {
                if (format.contains(HandlePlayerTag.cm.params.getPrefix())) {
                    format = format.replace(HandlePlayerTag.cm.params.getPrefix(), "");
                }
            } else {
                String rankName = String.format(HandlePlayerTag.cm.params.getPrefixColor()
                        + rank + ChatColor.RESET);

                // Modulo de integracion con plugin de chat
                if (format.contains(HandlePlayerTag.cm.params.getPrefix())) {
                    format = format.replace(HandlePlayerTag.cm.params.getPrefix(), rankName);
                } else {
                    format = rankName + format;
                }
            }
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

            // Holograms
            int fame = 0;
            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            
            int oldTime = 0;
            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            
            int totalTime = oldTime + plugin.getTimerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

            HOLOPLAYERS.put(uuid, createHoloPlayer(player, rank));

            // Fix reload
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) 
                    || player.isSneaking() || !canDisplayRank(player, rank)) {
                HOLOPLAYERS.get(uuid).removeLine(0);
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

            Location l = new Location(event.getTo().getWorld(),
                    event.getTo().getX(),
                    event.getTo().getY() + TITLE_HEIGHT,
                    event.getTo().getZ());

            h.teleport(l);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);
            if (!StrUtils.isHologramEmpty(h)) {
                h.removeLine(0);
            }

            Location l = new Location(player.getLocation().getWorld(),
                    player.getLocation().getX(),
                    player.getLocation().getY() + TITLE_HEIGHT,
                    player.getLocation().getZ());
            h.teleport(l);

            int fame = 0;
            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            
            int oldTime = 0;
            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            
            int totalTime = oldTime + plugin.getTimerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

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
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }
                
                int oldTime = 0;
                try {
                    oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }
                
                int totalTime = oldTime + plugin.getTimerManager().getPlayer(player).getTotalOnline();

                String rank = Ranks.getRank(fame, totalTime);

                if (canDisplayRank(player, rank)) {
                    h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
                }
            } else if (!StrUtils.isHologramEmpty(h) && 
                    player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
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

            if (!event.getNewRank().equalsIgnoreCase(IGNORED_RANK) && hasIgnoredChatPermission(player)) {
                h.insertTextLine(0, RANK_LINE.replace("%rank%", event.getNewRank()));
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

            Location l = new Location(event.getRespawnLocation().getWorld(),
                    event.getRespawnLocation().getX(),
                    event.getRespawnLocation().getY() + TITLE_HEIGHT,
                    event.getRespawnLocation().getZ());

            h.teleport(l);

            int fame = 0;
            try {
                fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            
            int oldTime = 0;
            try {
                oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            
            int totalTime = oldTime + plugin.getTimerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

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
            if (!StrUtils.isHologramEmpty(h)) {
                h.removeLine(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSneaking(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);

            if (event.isSneaking()) {
                if (!StrUtils.isHologramEmpty(h)) {
                    h.removeLine(0);
                }
            } else if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                int fame = 0;
                try {
                    fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }
                
                int oldTime = 0;
                try {
                    oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }
                
                int totalTime = oldTime + plugin.getTimerManager().getPlayer(player).getTotalOnline();

                String rank = Ranks.getRank(fame, totalTime);

                if (canDisplayRank(player, rank)) {
                    h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
                }
            }
        }
    }
}
