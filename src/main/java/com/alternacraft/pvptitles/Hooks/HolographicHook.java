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
package com.alternacraft.pvptitles.Hooks;

import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Listeners.HandlePlayerTag;
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
        setBasics();
    }

    public String[] setup() {
        ISHDENABLED = plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays");

        if (ISHDENABLED) {
            int hb = loadHoloBoards();
            // Ranks
            if (plugin.getManager().params.displayLikeHolo()) {
                loadPlayersInServer();
            }
            return new String[]{"HolographicDisplays &7(" + hb + " loaded)"};
        }

        return new String[]{};
    }

    public static void setBasics() {
        RANK_LINE = plugin.getManager().params.getHolotagformat();
        TITLE_HEIGHT = (plugin.getManager().params.getHoloHeightMod() - 1) * HEIGHT_PER_ROW + DEFAULT_TITLE_HEIGHT;
    }

    public static void loadPlayersInServer() {
        deleteHoloPlayers();
        plugin.getServer().getOnlinePlayers().forEach((player) -> {
            HandlePlayerTag.holoPlayerLogin(player);
        });
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
        if (!h.isDeleted()) {
            h.delete();
        }
    }

    public static void deleteHoloPlayers() {
        // Optimizacion para borrar hologramas si se desactivo la opcion
        HOLOPLAYERS.entrySet()
                .stream()
                .map(entry -> entry.getValue())
                .filter(holo -> !(holo.isDeleted()))
                .forEachOrdered(holo -> holo.delete());
        HOLOPLAYERS.clear();
    }
    // End Player Prefix    

    // Boards
    public static int loadHoloBoards() {
        HologramsFile.load();
        deleteHolograms(); // Fix duplicados, borra todos
        
        List<BoardData> holos = HologramsFile.loadHolograms();        
        holos.forEach(holo -> {
            BoardModel sm = plugin.getManager().searchModel(holo.getModelo());
            ModelController mc = new ModelController();
            mc.preprocessUnit(sm.getParams());
            plugin.getManager().getLbm().loadBoard(
                    new HologramBoard(holo, sm, mc)
            );
        });
        return holos.size();
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
        contenido.forEach(string -> {
            h.appendTextLine(string); // ChatColor.RESET
        });
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
        HologramsAPI.getHolograms(plugin)
                .stream()
                .filter(holo -> !(holo.isDeleted()))
                .forEachOrdered(holo -> {
                    holo.delete();
                });
    }
    // Fin todos
}
