package es.jlh.pvptitles.Files;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardData;
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

/**
 *
 * @author AlternaCraft
 */
public class HologramsFile {

    private static final File HOLOSFILE = new File("plugins/PvpTitles/holograms.yml");
    private static YamlConfiguration holosConf = null;

    public HologramsFile() {
    }

    public YamlConfiguration load() {
        if (!HOLOSFILE.exists()) {
            createConfig();
        }

        holosConf = YamlConfiguration.loadConfiguration(HOLOSFILE);

        return holosConf;
    }

    private void createConfig() {
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
            PvpTitles.logDebugInfo(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static void saveHologram(BoardData hol) {
        holosConf.set("Holograms." + hol.getNombre() + ".model", hol.getModelo());
        holosConf.set("Holograms." + hol.getNombre() + ".location.world", hol.getLocation().getWorld().getName());
        holosConf.set("Holograms." + hol.getNombre() + ".location.x", hol.getLocation().getX());
        holosConf.set("Holograms." + hol.getNombre() + ".location.y", hol.getLocation().getY());
        holosConf.set("Holograms." + hol.getNombre() + ".location.z", hol.getLocation().getZ());

        try {
            holosConf.save(HOLOSFILE);
        } catch (IOException e) {
            PvpTitles.logDebugInfo(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static List<BoardData> loadHolograms() {
        List<BoardData> bds = new ArrayList();

        ConfigurationSection cs = holosConf.getConfigurationSection("Holograms");
        
        if (cs == null) {
            return bds;
        }
        
        Set<String> sfk = cs.getKeys(false);

        for (String holo : sfk) {
            String world = cs.getString(holo + ".location.world");
            double x = cs.getDouble(holo + ".location.x");
            double y = cs.getDouble(holo + ".location.y");
            double z = cs.getDouble(holo + ".location.z");

            Location l = new Location(Bukkit.getWorld(world), x, y, z);
            String nombre = holo;
            String modelo = cs.getString(holo + ".model");

            BoardData holos = new BoardData(l);
            holos.setNombre(nombre);
            holos.setModelo(modelo);

            bds.add(holos);
        }

        return bds;
    }

    public static BoardData loadHologram(String name) {
        BoardData bd = null;

        if (!holosConf.contains("Holograms." + name)) {
            return bd;
        }

        String nombre = name;
        String modelo = holosConf.getString("Holograms." + nombre + ".model");
        String world = holosConf.getString("Holograms." + nombre + ".location.world");
        double x = holosConf.getDouble("Holograms." + nombre + ".location.x");
        double y = holosConf.getDouble("Holograms." + nombre + ".location.y");
        double z = holosConf.getDouble("Holograms." + nombre + ".location.z");

        bd = new BoardData(new Location(Bukkit.getWorld(world), x, y, z));
        bd.setNombre(nombre);
        bd.setModelo(modelo);

        return bd;
    }

    public static void removeHologram(Location l) {
        for (BoardData bd : loadHolograms()) {
            if (l.equals(bd.getLocation())) {
                holosConf.set("Holograms." + bd.getNombre(), null);
                try {
                    holosConf.save(HOLOSFILE);
                } catch (IOException e) {
                    PvpTitles.logDebugInfo(Level.SEVERE, e.getMessage(), e);
                }
                break;
            }
        }
    }
}
