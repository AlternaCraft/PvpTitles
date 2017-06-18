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
package com.alternacraft.pvptitles.Files;

import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardData;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class HologramsFile {

    private static final File HOLOSFILE = new File(PvpTitles.PLUGIN_DIR, "holograms.yml");
    private static YamlConfiguration holosConf = null;

    public static YamlConfiguration load() {
        if (!HOLOSFILE.exists()) {
            createConfig();
        }

        holosConf = YamlConfiguration.loadConfiguration(HOLOSFILE);

        return holosConf;
    }

    private static void createConfig() {
        YamlConfiguration newConfig = new YamlConfiguration();

        newConfig.options().header(
                "#################\n"
                + "##  [HOLO DB]  ##\n"
                + "#################\n"
        );
        newConfig.options().copyHeader(true);

        newConfig.set("Holograms", "");
        
        try {
            newConfig.save(HOLOSFILE);
        } catch (IOException e) {
            CustomLogger.logDebugInfo(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void saveHologram(BoardData hol) {
        holosConf.set("Holograms." + hol.getNombre() + ".model", hol.getModelo());
        holosConf.set("Holograms." + hol.getNombre() + ".filter", hol.getServer());
        holosConf.set("Holograms." + hol.getNombre() + ".location.world", hol.getLocation().getWorld().getName());
        holosConf.set("Holograms." + hol.getNombre() + ".location.x", hol.getLocation().getX());
        holosConf.set("Holograms." + hol.getNombre() + ".location.y", hol.getLocation().getY());
        holosConf.set("Holograms." + hol.getNombre() + ".location.z", hol.getLocation().getZ());

        try {
            holosConf.save(HOLOSFILE);
        } catch (IOException e) {
            CustomLogger.logDebugInfo(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static List<BoardData> loadHolograms() {
        List<BoardData> bds = new ArrayList();

        ConfigurationSection cs = holosConf.getConfigurationSection("Holograms");
        
        if (cs == null) {
            return bds;
        }
        
        Set<String> sfk = cs.getKeys(false);

        sfk
                .stream()
                .map(holo -> {
                    String world = cs.getString(holo + ".location.world");
                    double x = cs.getDouble(holo + ".location.x");
                    double y = cs.getDouble(holo + ".location.y");
                    double z = cs.getDouble(holo + ".location.z");
                    Location l = new Location(Bukkit.getWorld(world), x, y, z);
                    String nombre = holo;
                    String modelo = cs.getString(holo + ".model");
                    String server = cs.getString(holo + ".filter");
                    BoardData holos = new BoardData(l);
                    holos.setNombre(nombre);
                    holos.setModelo(modelo);
                    holos.setServer(server);
                    return holos;
                }).forEachOrdered(holos -> {
                    bds.add(holos);
                });

        return bds;
    }

    public static BoardData loadHologram(String name) {
        BoardData bd = null;

        if (!holosConf.contains("Holograms." + name)) {
            return bd;
        }

        String nombre = name;
        String modelo = holosConf.getString("Holograms." + nombre + ".model");
        String server = holosConf.getString("Holograms." + nombre + ".filter");
        String world = holosConf.getString("Holograms." + nombre + ".location.world");
        double x = holosConf.getDouble("Holograms." + nombre + ".location.x");
        double y = holosConf.getDouble("Holograms." + nombre + ".location.y");
        double z = holosConf.getDouble("Holograms." + nombre + ".location.z");

        bd = new BoardData(new Location(Bukkit.getWorld(world), x, y, z));
        bd.setNombre(nombre);
        bd.setModelo(modelo);
        bd.setServer(server);

        return bd;
    }

    public static void removeHologram(Location l) {
        for (BoardData bd : loadHolograms()) {
            if (l.equals(bd.getLocation())) {
                holosConf.set("Holograms." + bd.getNombre(), null);
                try {
                    holosConf.save(HOLOSFILE);
                } catch (IOException e) {
                    CustomLogger.logDebugInfo(Level.SEVERE, e.getMessage(), e);
                }
                break;
            }
        }
    }
}
