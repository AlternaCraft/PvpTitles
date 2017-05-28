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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Main.CustomLogger;
import static com.alternacraft.pvptitles.Main.CustomLogger.showMessage;
import com.alternacraft.pvptitles.Main.PvpTitles;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Custom class for working better with the main config file. These are the
 * capabilities:
 * <ul>
 *  <li>Saving config with comments between lines</li>
 *  <li>Checking config version with internal version for checking changes
 *      <ul>
 *          <li>Setting params from previous config into the new one</li>
 *      </ul>
 *  </li>
 * </ul>
 *
 * @see FileConfiguration
 */
public class FileConfig {

    private PvpTitles plugin = null;

    private FileConfiguration configFile = null;
    private File backupFile = null;

    // Parents
    private String parent;
    // Configurable nodes
    private final String[] NODES = {"Multipliers", "Ranks"};

    public FileConfig(PvpTitles pl) {
        plugin = pl;

        File cfile = new File(PvpTitles.PLUGIN_DIR, "config.yml");

        if (!cfile.exists() || mismatchVersion(cfile)) {
            pl.saveDefaultConfig();
            if (backupFile != null) {
                copyParams(cfile);
                backupFile = null;
            }
        }

        pl.reloadConfig();
        configFile = pl.getConfig();
    }

    private boolean mismatchVersion(File cFile) {
        File backup = new File(PvpTitles.PLUGIN_DIR, "config.backup.yml");

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(cFile);
        Configuration defaults = plugin.getConfig().getDefaults();

        // Seeking for "version"
        if (!yaml.contains("Version")
                || !yaml.getString("Version").equals(defaults.getString("Version"))) {

            if (backup.exists()) {
                backup.delete();
            }

            cFile.renameTo(backup);
            showMessage(ChatColor.RED + "Mismatch config version, a new one has been created.");
            backupFile = backup;

            return true;
        }

        return false;
    }

    private void copyParams(File outFile) {
        YamlConfiguration newFile = YamlConfiguration.loadConfiguration(outFile);
        YamlConfiguration oldFile = YamlConfiguration.loadConfiguration(backupFile);

        // RetroCP
        if (oldFile.contains("RankNames")) {
            List<String> ranks = oldFile.getStringList("RankNames");
            List<String> points = oldFile.getStringList("ReqFame");
            List<String> times = oldFile.getStringList("ReqTime");

            for (int i = 0; i < ranks.size(); i++) {
                String rank = ranks.get(i);
                String key = getParsedKey(rank);
                oldFile.set("Ranks." + key + ".display", rank);
                if (points != null && points.size() > i) {
                    oldFile.set("Ranks." + key + ".points", points.get(i));
                }
                if (times != null && times.size() > i) {
                    oldFile.set("Ranks." + key + ".time", times.get(i));
                }
            }
        }

        File temp = new File(PvpTitles.PLUGIN_DIR, "config_temp.yml");

        try (BufferedReader br = new BufferedReader(new FileReader(outFile));
                FileWriter fw = new FileWriter(temp)) {

            boolean avoid = false;

            String linea;
            while ((linea = br.readLine()) != null) {
                if (avoid) {
                    if (!linea.matches("[^#]?\\s+\\w+:.*") && !linea.matches("\\s*#.*")) {
                        avoid = false;
                    }
                } else {
                    for (String node : NODES) {
                        if (getKey(linea).equals(node) && oldFile.contains(node)) {
                            fw.write(addCustom(node, oldFile));
                            avoid = true;
                        }
                    }
                }
                // List or subnode
                if (linea.matches("\\s*-\\s?.+") || avoid) {
                    continue;
                }
                String nline = replace(linea, newFile, oldFile);
                fw.write(nline);
            }
        } catch (IOException ex) {
            CustomLogger.logDebugInfo(Level.SEVERE, null, ex);
        }

        if (outFile.exists()) {
            outFile.delete();
        }

        temp.renameTo(outFile);
        showMessage(ChatColor.GREEN + "Previous file settings have been established "
                + "into the new one.");
        showMessage(ChatColor.GREEN + "Just in case, check the result.");
    }

    private String addCustom(String key, YamlConfiguration oldFile) {
        String result = key + ":" + System.lineSeparator();

        Set<String> values = oldFile.getConfigurationSection(key).getKeys(true);
        for (String value : values) {
            String spaces = fillSpaces(value.split("\\.").length);
            String kkey = value.split("\\.")[value.split("\\.").length - 1];

            Object content = oldFile.get(key + "." + value);
            String val = "";

            if (!(content instanceof MemorySection)) {
                val = getFilteredString(String.valueOf(content));
            }

            result += spaces + kkey + ": " + val + System.lineSeparator();
        }
        return result;
    }

    private String replace(String line, YamlConfiguration newFile, YamlConfiguration oldFile) {
        // Ignore values
        if (line.contains("Version") || line.matches(" *#+.*") || line.isEmpty()
                || line.matches(" +")) {
            return line + System.lineSeparator();
        }

        // Output
        String res;

        // ** BEGIN FIND NODE ** //
        String key = getKey(line);
        String cKey = key;

        Object v = newFile.get(cKey); // Default value

        // Testing with parent
        if (v == null) {
            cKey = parent + "." + key;
            v = newFile.get(cKey);
        }

        // Going back
        while (v == null && parent.contains(".")) {
            parent = parent.substring(0, parent.lastIndexOf("."));
            cKey = parent + "." + key;
            v = newFile.get(cKey);
        }
        // ** END FIND NODE ** //

        // Not found??
        if (v == null) {
            return line + System.lineSeparator();
        }

        // Spaces
        String spaces = fillSpaces(cKey.split("\\.").length - 1);

        // Old value <- This is the point
        if (oldFile.contains(cKey)) {
            v = oldFile.get(cKey); // Restore old value
        }

        // Default output [For nodes]
        res = spaces + key + ":";

        // Object type
        if (v instanceof List) {
            List<Object> list = (List<Object>) v; // Saving list
            for (Object l : list) {
                String val = getFilteredString(l.toString());
                res += System.lineSeparator() + spaces + "- " + val;
            }
        } else if (v instanceof MemorySection) {
            parent = cKey; // Saving parent
        } else {
            res += " " + getFilteredString(v.toString()); // saving value
        }

        return res += System.lineSeparator(); // Multiple lines
    }

    private String getKey(String str) {
        return str.split(":")[0].replaceAll("\\s+", "");
    }
    
    private String getParsedKey(String str) {
        return StrUtils.removeColorsWithoutTranslate(str).replaceAll(" ", "_");
    }

    private String fillSpaces(int c) {
        String res = "";
        for (int i = 0; i < c; i++) {
            res += "    ";
        }
        return res;
    }

    private String getFilteredString(String str) {
        List<Character> special = Arrays.asList(':', '{', '}', '[', ']', ',', '&',
                '*', '#', '?', '|', '<', '>', '=', '!', '%', '@', '\\');

        for (Character character : special) {
            if (str.contains(String.valueOf(character))) {
                return "\"" + str + "\"";
            }
        }

        return str;
    }

    public FileConfiguration getConfig() {
        return this.configFile;
    }
}
