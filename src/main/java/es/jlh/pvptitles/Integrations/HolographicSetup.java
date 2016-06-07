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
import es.jlh.pvptitles.Misc.Utils;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class HolographicSetup {

    public static String RANK_LINE = null;    
    
    public static final double HEIGHT_PER_ROW = 0.26D;
    public static final double DEFAULT_TITLE_HEIGHT = 2.58D; // 2.6D
    
    public static double TITLE_HEIGHT = DEFAULT_TITLE_HEIGHT;
    
    /*public static final double CROUCH_HEIGHT = 2.2D;*/
    
    public static boolean isHDEnable = false;

    public static final Map<String, Hologram> HOLOPLAYERS = new HashMap();

    private static PvpTitles plugin = null;

    public HolographicSetup(PvpTitles pt) {
        HolographicSetup.plugin = pt;
        RANK_LINE = pt.manager.params.getHolotagformat();
        TITLE_HEIGHT = (pt.manager.params.getHoloHeightMod() - 1) * HEIGHT_PER_ROW + DEFAULT_TITLE_HEIGHT;
    }

    public void setup() {
        isHDEnable = plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
        
        if (isHDEnable) {
            PvpTitles.showMessage(ChatColor.YELLOW + "HolographicDisplays " + ChatColor.AQUA + "integrated correctly.");
            PvpTitles.showMessage(ChatColor.YELLOW + "" + loadHoloBoards()
                    + " scoreboards per holograms " + ChatColor.AQUA + "loaded correctly."
            );
            
            // Ranks
            if (plugin.manager.params.displayLikeHolo()) {
                loadPlayersInServer();
            }
        }
    }

    public static void loadPlayersInServer() {
        deleteHoloPlayers();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            HandlePlayerTag.holoPlayerLogin(player);
        }
    }

    // Player prefix
    public static Hologram createHoloPlayer(Player pl, String rank) {
        Location l = new Location(pl.getLocation().getWorld(), pl.getLocation().getX(),
                pl.getLocation().getY() + TITLE_HEIGHT, pl.getLocation().getZ());

        Hologram h = HologramsAPI.createHologram(plugin, l);
        h.insertTextLine(0, RANK_LINE.replace("%rank%", rank));

        VisibilityManager visiblityManager = h.getVisibilityManager();
        visiblityManager.setVisibleByDefault(true);
        visiblityManager.hideTo(pl);

        return h;
    }

    public static void removeHoloPlayer(Hologram h) {
        if (!h.isDeleted())
            h.delete();
    }

    public static void deleteHoloPlayers() {
        // Optimizacion para borrar hologramas si se desactivo la opcion
        for (Map.Entry<String, Hologram> entry : HOLOPLAYERS.entrySet()) {
            Hologram holo = entry.getValue();
            if (holo.isDeleted()) {
                continue;
            }
            holo.delete();
        }
        HOLOPLAYERS.clear();
    }
    // End Player Prefix    

    // Boards
    public static int loadHoloBoards() {
        HologramsFile.load();
        deleteHolograms(); // Fix duplicados, borra todos
        int t = 0;

        for (BoardData holo : HologramsFile.loadHolograms()) {
            BoardModel sm = plugin.manager.searchModel(holo.getModelo());

            ModelController mc = new ModelController();
            mc.preprocessUnit(sm.getParams());

            HologramBoard hb = new HologramBoard(holo, sm, mc);

            plugin.manager.getLbm().loadBoard(hb);
            t++;
        }

        return t;
    }

    public static void createHoloBoardHead(Location l, short top) {
        Hologram h = HologramsAPI.createHologram(plugin, l);

        h.appendTextLine(Utils.translateColor("&6&lPvpTitles"));
        h.appendTextLine(Utils.translateColor("&6&l+&r------&6&l+"));
        h.appendTextLine(Utils.translateColor("| &e&lTop " + top + "&r |"));
        h.appendTextLine(Utils.translateColor("&6&l+&r------&6&l+"));
    }

    public static void createHoloBoard(List<String> contenido, Location l) {
        Hologram h = HologramsAPI.createHologram(plugin, l);

        for (String string : contenido) {
            h.appendTextLine(string); // ChatColor.RESET
        }
    }

    public static void deleteHoloBoard(Location l) {
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
    // End boards

    // Todos
    public static void deleteHolograms() {
        Collection<Hologram> holograms = HologramsAPI.getHolograms(plugin);
        for (Hologram holo : holograms) {
            if (holo.isDeleted()) {
                continue;
            }
            holo.delete();
        }
    }
    // Fin todos
}
