package es.jlh.pvptitles.Integrations;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerTag;
import es.jlh.pvptitles.Files.HologramsFile;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Managers.BoardsCustom.HologramBoard;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardData;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardModel;
import es.jlh.pvptitles.Managers.BoardsAPI.ModelController;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class HolographicSetup {

    public static String RANK_LINE = null;

    public static final double TITLE_HEIGHT = 2.6;
    public static final double CROUCH_HEIGHT = 2.2;
    public static boolean isHDEnable = false;

    public static final Map<String, Hologram> HOLOPLAYERS = new HashMap();

    private static PvpTitles plugin = null;

    public HolographicSetup(PvpTitles pt) {
        HolographicSetup.plugin = pt;
        RANK_LINE = pt.cm.params.getHolotagformat();
    }

    public void setup() {
        isHDEnable = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        if (isHDEnable) {
            PvpTitles.showMessage(ChatColor.YELLOW + "HolographicDisplays " + ChatColor.AQUA + "integrated correctly.");
            PvpTitles.showMessage(ChatColor.YELLOW + "" + loadHolograms()
                    + " scoreboards per holograms " + ChatColor.AQUA + "loaded correctly."
            );

            if (plugin.cm.params.displayLikeHolo()) {
                loadPlayersInServer();
            }
        }
    }

    public static void loadPlayersInServer() {
        HOLOPLAYERS.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!HOLOPLAYERS.containsKey(player.getUniqueId().toString())) {
                int fame = plugin.cm.getDbh().getDm().loadPlayerFame(player.getUniqueId(), player.getWorld().getName());
                int oldTime = plugin.cm.getDbh().getDm().loadPlayedTime(player.getUniqueId());
                int totalTime = oldTime + plugin.getPlayerManager().getPlayer(player).getTotalOnline();

                String rank = Ranks.getRank(fame, totalTime);

                // Rank ignored
                if (rank.equalsIgnoreCase(HandlePlayerTag.IGNORED_RANK)) {
                    continue;
                }

                // Fix prefix
                if (!HandlePlayerTag.hasPermissions(player)) {
                    continue;
                }

                Hologram h = createHoloTag(player, rank);

                HOLOPLAYERS.put(player.getUniqueId().toString(), h);
            }
        }
    }

    public static int loadHolograms() {
        // Fix para evitar duplicados
        HolographicSetup.deleteHolograms();

        new HologramsFile().load();

        int t = 0;

        for (BoardData holo : HologramsFile.loadHolograms()) {
            BoardModel sm = plugin.cm.searchModel(holo.getModelo());

            ModelController mc = new ModelController();
            mc.preprocessUnit(sm.getParams());

            HologramBoard hb = new HologramBoard(holo, sm, mc);

            plugin.cm.getLbm().loadBoard(hb);
            t++;
        }

        return t;
    }

    // Player prefix
    public static Hologram createHoloTag(Player pl, String rank) {
        Hologram h = HologramsAPI.createHologram(plugin, pl.getLocation().add(0.0, TITLE_HEIGHT, 0.0));
        h.appendTextLine(RANK_LINE.replace("%rank%", rank));

        VisibilityManager visiblityManager = h.getVisibilityManager();
        visiblityManager.setVisibleByDefault(true);
        visiblityManager.hideTo(pl);

        return h;
    }

    public static void removeHoloTag(Hologram h) {
        h.delete();
    }
    // End Player Prefix    
    
    // Crear
    public static void createHoloHead(Location l) {
        Hologram h = HologramsAPI.createHologram(plugin, l);

        //ItemStack icon = new ItemStack(Material.DIAMOND_HELMET);
        String divisor = Utils.translateColor("&6&l+&r----------&6&l+");
        h.appendTextLine(divisor);
        h.appendTextLine("| " + ChatColor.GOLD + "" + ChatColor.BOLD + "PvpTitles" + ChatColor.RESET + " |");
        h.appendTextLine(divisor);
        h.appendTextLine("");
        //h.appendItemLine(icon);
    }

    public static void createHologram(List<String> contenido, Location l) {
        Hologram h = HologramsAPI.createHologram(plugin, l);

        for (String string : contenido) {
            if (string == null) {
                continue;
            }
            h.appendTextLine(string + ChatColor.RESET);
        }
    }

    public static List<BoardData> getHolograms() {
        List<BoardData> infoholos = new ArrayList<>();
        Collection<Hologram> hs = HologramsAPI.getHolograms(plugin);

        for (Hologram holo : hs) {
            infoholos.add(new BoardData(holo.getLocation()));
        }

        return infoholos;
    }

    // Borrar
    public static void deleteHologram(Location l) {
        Collection<Hologram> holograms = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : holograms) {
            if (holo.isDeleted()) {
                continue;
            }

            if (l.equals(holo.getLocation())) {
                holo.delete();
                break;
            }
        }
    }

    // Borrar
    public static void deleteHolograms() {
        Collection<Hologram> holograms = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : holograms) {
            if (holo.isDeleted()) {
                continue;
            }

            holo.delete();
        }
    }

}
