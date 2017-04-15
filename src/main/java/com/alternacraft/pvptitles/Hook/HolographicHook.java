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
package com.alternacraft.pvptitles.Hook;

import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Listeners.HandlePlayerTag;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardData;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import com.alternacraft.pvptitles.Managers.BoardsAPI.ModelController;
import com.alternacraft.pvptitles.Managers.BoardsCustom.HologramBoard;
import com.alternacraft.pvptitles.Misc.StrUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class HolographicHook {

    public static String RANK_LINE = null;    
    
    public static final double HEIGHT_PER_ROW = 0.26D;
    public static final double DEFAULT_TITLE_HEIGHT = 2.58D; // 2.6D
    
    public static double TITLE_HEIGHT = DEFAULT_TITLE_HEIGHT;
    
    /*public static final double CROUCH_HEIGHT = 2.2D;*/
    
    public static boolean ISHDENABLED = false;

    public static final Map<String, Hologram> HOLOPLAYERS = new HashMap();

    private static PvpTitles plugin = null;

    public HolographicHook(PvpTitles pt) {
        HolographicHook.plugin = pt;
        RANK_LINE = pt.getManager().params.getHolotagformat();
        TITLE_HEIGHT = (pt.getManager().params.getHoloHeightMod() - 1) * HEIGHT_PER_ROW + DEFAULT_TITLE_HEIGHT;
    }

    public void setup() {
        ISHDENABLED = plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays");
        
        if (ISHDENABLED) {
            CustomLogger.showMessage(ChatColor.YELLOW + "HolographicDisplays " + ChatColor.AQUA + "integrated correctly.");
            CustomLogger.showMessage(ChatColor.YELLOW + "" + loadHoloBoards()
                    + " scoreboards per holograms " + ChatColor.AQUA + "loaded correctly."
            );
            
            // Ranks
            if (plugin.getManager().params.displayLikeHolo()) {
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
            BoardModel sm = plugin.getManager().searchModel(holo.getModelo());

            ModelController mc = new ModelController();
            mc.preprocessUnit(sm.getParams());

            HologramBoard hb = new HologramBoard(holo, sm, mc);

            plugin.getManager().getLbm().loadBoard(hb);
            t++;
        }

        return t;
    }

    public static void createHoloBoardHead(Location l, short top) {
        Hologram h = HologramsAPI.createHologram(plugin, l);

        h.appendTextLine(StrUtils.translateColors("&6&lPvpTitles"));
        h.appendTextLine(StrUtils.translateColors("&6&l+&r------&6&l+"));
        h.appendTextLine(StrUtils.translateColors("| &e&lTop " + top + "&r |"));
        h.appendTextLine(StrUtils.translateColors("&6&l+&r------&6&l+"));
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
