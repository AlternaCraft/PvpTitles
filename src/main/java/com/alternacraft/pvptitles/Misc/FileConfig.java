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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import static com.alternacraft.pvptitles.Main.Managers.MessageManager.showMessage;
import com.alternacraft.pvptitles.Main.PvpTitles;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
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

    public FileConfig(PvpTitles pl) {
        plugin = pl;

        File cfile = new File(new StringBuilder().append(
                pl.getDataFolder()).append(
                        File.separator).append(
                        "config.yml").toString());

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
        File bFile = new File(new StringBuilder().append(
                plugin.getDataFolder()).append(
                        File.separator).append(
                        "config.backup.yml").toString());

        YamlConfiguration configV = YamlConfiguration.loadConfiguration(cFile);

        if (!configV.contains("Version")
                || !configV.getString("Version").equals(plugin.getConfig().getDefaults().getString("Version"))) {

            if (bFile.exists()) {
                bFile.delete();
            }

            cFile.renameTo(bFile);
            showMessage(ChatColor.RED + "Mismatch config version, a new one has been created.");

            backupFile = bFile;

            return true;
        }

        return false;
    }

    private void copyParams(File outFile) {
        YamlConfiguration newFile = YamlConfiguration.loadConfiguration(outFile);
        YamlConfiguration oldFile = YamlConfiguration.loadConfiguration(backupFile);

        File temp = new File(new StringBuilder().append(
                plugin.getDataFolder()).append(
                        File.separator).append(
                        "config_temp.yml").toString());

        try (BufferedReader br = new BufferedReader(new FileReader(outFile));
                FileWriter fw = new FileWriter(temp)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.matches("\\s*-\\s?.+")) {
                    continue;
                }
                String nline = replace(linea, newFile, oldFile);
                fw.write(nline);
            }
        } catch (IOException ex) {
            LoggerManager.logDebugInfo(Level.SEVERE, null, ex);
        }

        if (outFile.exists()) {
            outFile.delete();
        }

        temp.renameTo(outFile);
        showMessage(ChatColor.GREEN + "Previous file settings have been established "
                + "into the new one.");
        showMessage(ChatColor.GREEN + "Just in case, check the result.");
    }

    private String parent;

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
        
        // Testing with parent (Maybe it is a children)
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

        // Unhandled error
        if (v == null) {
            return line + System.lineSeparator();
        }

        // Style
        String spaces = fillSpaces(cKey.split("\\.").length - 1);

        // Old value <- This is the point
        if (oldFile.contains(cKey)) {
            v = oldFile.get(cKey);
        }

        // Default output
        res = spaces + key + ":";
        
        // Object type
        if (v instanceof List) {
            List<Object> list = (List<Object>) v;
            for (Object l : list) {
                String val = getFilteredString(l.toString());
                res += System.lineSeparator() + spaces + "- " + val;
            }
        } else if (v instanceof MemorySection) {
            parent = cKey;            
        } else {
            res += " " + getFilteredString(v.toString());
        }

        return res += System.lineSeparator();
    }

    private String getKey(String str) {
        return str.split(":")[0].replaceAll("\\s+", "");
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
