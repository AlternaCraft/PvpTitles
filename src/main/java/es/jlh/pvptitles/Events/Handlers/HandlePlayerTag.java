package es.jlh.pvptitles.Events.Handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import es.jlh.pvptitles.Events.RankChangedEvent;
import es.jlh.pvptitles.Integrations.HolographicSetup;
import static es.jlh.pvptitles.Integrations.HolographicSetup.RANK_LINE;
import static es.jlh.pvptitles.Integrations.HolographicSetup.TITLE_HEIGHT;
import es.jlh.pvptitles.Integrations.VaultSetup;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Utils;
import static es.jlh.pvptitles.Misc.Utils.isHologramEmpty;
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
import static es.jlh.pvptitles.Integrations.HolographicSetup.removeHoloTag;
import static es.jlh.pvptitles.Integrations.HolographicSetup.createHoloTag;
import static es.jlh.pvptitles.Integrations.HolographicSetup.HOLOPLAYERS;

/**
 *
 * @author AlternaCraft
 */
public class HandlePlayerTag implements Listener {

    public static final String IGNORED_RANK = "None";
    private static final String IGNORED_CHAT_PERM = "pvptitles.hideprefix";

    private static PvpTitles plugin;
    private static Manager cm;

    public HandlePlayerTag(PvpTitles pt) {
        plugin = pt;
        cm = pt.cm;
    }

    public boolean validWorld(String w) {
        // Compruebo si el mundo esta en la lista de los vetados
        if (HandlePlayerTag.cm.params.getAffectedWorlds().contains(w.toLowerCase())) {
            if (!HandlePlayerTag.cm.params.isChat()) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasPermissions(Player pl) {
        // Fix prefix
        Permission perm = VaultSetup.permission;
        if (perm != null) {
            String group = perm.getPrimaryGroup(pl);
            World w = null;
            World wp = pl.getWorld();

            if (perm.groupHas(w, group, IGNORED_CHAT_PERM)
                    || perm.groupHas(wp, group, IGNORED_CHAT_PERM)) {
                return false;
            }
        } else if (!pl.isOp() && pl.hasPermission(IGNORED_CHAT_PERM)) {
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!validWorld(event.getPlayer().getWorld().getName()) 
                || !HandlePlayerTag.cm.params.displayInChat()
                || !hasPermissions(event.getPlayer())) {
            return;
        }

        String rank = null;

        int fame = HandlePlayerTag.cm.dbh.getDm().loadPlayerFame(event.getPlayer().getUniqueId(), null);
        int seconds = HandlePlayerTag.cm.dbh.getDm().loadPlayedTime(event.getPlayer().getUniqueId());
        rank = Ranks.getRank(fame, seconds);

        String format = event.getFormat();

        if (rank != null && !rank.isEmpty()) {
            if (rank.equalsIgnoreCase(IGNORED_RANK)) {
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
        // Holograms
        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

            HolographicSetup.HOLOPLAYERS.put(player.getUniqueId().toString(),
                    HolographicSetup.createHoloTag(player, rank)
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Holograms
        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            if (HolographicSetup.HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
                Hologram h = HolographicSetup.HOLOPLAYERS.get(player.getUniqueId().toString());

                Location l = new Location(event.getTo().getWorld(),
                        event.getTo().getX(),
                        event.getTo().getY() + TITLE_HEIGHT,
                        event.getTo().getZ());

                h.teleport(l);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        // Holograms
        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            if (HolographicSetup.HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
                Hologram h = HolographicSetup.HOLOPLAYERS.get(player.getUniqueId().toString());

                Location l = new Location(player.getLocation().getWorld(),
                        player.getLocation().getX(),
                        player.getLocation().getY() + TITLE_HEIGHT,
                        player.getLocation().getZ());

                h.teleport(l);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Holograms
        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            if (HolographicSetup.HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
                HolographicSetup.removeHoloTag(HolographicSetup.HOLOPLAYERS.get(player.getUniqueId().toString()));
                HolographicSetup.HOLOPLAYERS.remove(player.getUniqueId().toString());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            if (HolographicSetup.HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
                Hologram h = HolographicSetup.HOLOPLAYERS.get(player.getUniqueId().toString());

                if (!isHologramEmpty(h) && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    h.clearLines();
                } else if (isHologramEmpty(h) && !player.hasPotionEffect(PotionEffectType.INVISIBILITY) && !player.isSneaking()) {
                    int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                    int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                    int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

                    String rank = Ranks.getRank(fame, totalTime);
                    h.appendTextLine(HolographicSetup.RANK_LINE.replace("%rank%", rank));
                }

                Location l;
                l = new Location(to.getWorld(), to.getX(), to.getY() + TITLE_HEIGHT, to.getZ());
                h.teleport(l);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeRank(RankChangedEvent event) {
        // Holograms
        if (HolographicSetup.isHDEnable && cm.params.displayLikeHolo()) {
            String uuid = event.getPlayer().getUniqueId().toString();

            if (!HOLOPLAYERS.containsKey(uuid)) {
                Hologram h = createHoloTag((Player) event.getPlayer(),
                        event.getNewRank());
                HOLOPLAYERS.put(uuid, h);
            }

            if (event.getNewRank().equalsIgnoreCase(IGNORED_RANK)) {
                removeHoloTag(HOLOPLAYERS.get(uuid));
                HOLOPLAYERS.remove(uuid);
                return;
            }

            Hologram h = HOLOPLAYERS.get(uuid);
            h.clearLines();

            h.appendTextLine(RANK_LINE.replace("%rank%", event.getNewRank()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        // Holograms
        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            if (HOLOPLAYERS.containsKey(uuid)) {
                HOLOPLAYERS.get(uuid).clearLines();

                int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

                String rank = Ranks.getRank(fame, totalTime);

                Location l = new Location(event.getRespawnLocation().getWorld(),
                        event.getRespawnLocation().getX(),
                        event.getRespawnLocation().getY() + TITLE_HEIGHT,
                        event.getRespawnLocation().getZ());

                HOLOPLAYERS.get(uuid).teleport(l);
                HOLOPLAYERS.get(uuid).appendTextLine(
                        HolographicSetup.RANK_LINE.replace("%rank%", rank)
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Holograms
        if (HolographicSetup.isHDEnable && cm.params.displayLikeHolo()) {
            String uuid = event.getEntity().getUniqueId().toString();

            if (HOLOPLAYERS.containsKey(uuid)) {
                HOLOPLAYERS.get(uuid).clearLines();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSneaking(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (cm.params.displayLikeHolo() && HolographicSetup.isHDEnable) {
            if (HolographicSetup.HOLOPLAYERS.containsKey(uuid)) {
                Hologram h = HolographicSetup.HOLOPLAYERS.get(uuid);
                if (!Utils.isHologramEmpty(h) && event.isSneaking()) {
                    h.clearLines();
                } else if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                    int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                    int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

                    String rank = Ranks.getRank(fame, totalTime);

                    h.appendTextLine(HolographicSetup.RANK_LINE.replace("%rank%", rank));
                }
            }
        }
    }
}
