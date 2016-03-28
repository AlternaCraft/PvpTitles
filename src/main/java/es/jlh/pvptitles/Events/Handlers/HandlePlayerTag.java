package es.jlh.pvptitles.Events.Handlers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import es.jlh.pvptitles.Events.RankChangedEvent;
import static es.jlh.pvptitles.Integrations.HolographicSetup.HOLOPLAYERS;
import static es.jlh.pvptitles.Integrations.HolographicSetup.RANK_LINE;
import static es.jlh.pvptitles.Integrations.HolographicSetup.TITLE_HEIGHT;
import static es.jlh.pvptitles.Integrations.HolographicSetup.isHDEnable;
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
import static es.jlh.pvptitles.Integrations.HolographicSetup.createHoloPlayer;
import static es.jlh.pvptitles.Integrations.HolographicSetup.removeHoloPlayer;

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

    private static boolean validWorld(String w) {
        // Compruebo si el mundo esta en la lista de los vetados        
        if (HandlePlayerTag.cm.params.getAffectedWorlds().contains(w.toLowerCase())) {
            if (!HandlePlayerTag.cm.params.isTitleShown()) {
                return false;
            }
        }

        return true;
    }

    public static boolean canDisplayRank(Player pl) {
        // Fix prefix
        Permission perm = VaultSetup.permission;
        if (perm != null) { // Vault en el server
            if (perm.hasGroupSupport() && perm.getPlayerGroups(pl).length != 0) {
                String group = perm.getPrimaryGroup(pl);
                
                World w = null;
                World wp = pl.getWorld();

                return !(perm.groupHas(w, group, IGNORED_CHAT_PERM)
                        || perm.groupHas(wp, group, IGNORED_CHAT_PERM));
            } else {
                return !perm.has(pl, IGNORED_CHAT_PERM);
            }
        }

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!validWorld(event.getPlayer().getWorld().getName())
                || !HandlePlayerTag.cm.params.displayInChat()
                || !canDisplayRank(event.getPlayer())) {
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
        if (isHDEnable && HandlePlayerTag.cm.params.displayLikeHolo()) {
            String uuid = player.getUniqueId().toString();

            // Holograms
            int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

            HOLOPLAYERS.put(uuid, createHoloPlayer(player, rank));

            // Fix reload
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) 
                    || player.isSneaking()
                    || rank.equalsIgnoreCase(IGNORED_RANK)
                    || !canDisplayRank(player)
                    || !validWorld(player.getWorld().getName())) {
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
            h.removeLine(0);

            Location l = new Location(player.getLocation().getWorld(),
                    player.getLocation().getX(),
                    player.getLocation().getY() + TITLE_HEIGHT,
                    player.getLocation().getZ());
            h.teleport(l);

            int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

            if (!rank.equals(IGNORED_RANK)
                    && validWorld(player.getWorld().getName())
                    && canDisplayRank(player)) {
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

            if (isHologramEmpty(h) && !player.hasPotionEffect(PotionEffectType.INVISIBILITY)
                    && !player.isSneaking() && !player.isDead()) {
                int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

                String rank = Ranks.getRank(fame, totalTime);

                // Rango <> none y mundo valido
                if (!rank.equals(IGNORED_RANK)
                        && validWorld(player.getWorld().getName())
                        && canDisplayRank(player)) {
                    h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
                }
            } else if (!isHologramEmpty(h) && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
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
            h.removeLine(0);

            if (!event.getNewRank().equalsIgnoreCase(IGNORED_RANK) && canDisplayRank(player)) {
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

            int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
            int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
            int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

            String rank = Ranks.getRank(fame, totalTime);

            if (!rank.equalsIgnoreCase(IGNORED_RANK)
                    && validWorld(player.getWorld().getName())
                    && canDisplayRank(player)) {
                h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        String uuid = event.getEntity().getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            HOLOPLAYERS.get(uuid).removeLine(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerSneaking(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        if (HOLOPLAYERS.containsKey(uuid)) {
            Hologram h = HOLOPLAYERS.get(uuid);

            if (event.isSneaking()) {
                if (!Utils.isHologramEmpty(h)) {
                    h.removeLine(0);
                }
            } else if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                int fame = cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                int oldTime = cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

                String rank = Ranks.getRank(fame, totalTime);

                // Rango <> none y mundo valido
                if (!rank.equalsIgnoreCase(IGNORED_RANK)
                        && validWorld(player.getWorld().getName())
                        && canDisplayRank(player)) {
                    h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));
                }
            }
        }
    }
}
