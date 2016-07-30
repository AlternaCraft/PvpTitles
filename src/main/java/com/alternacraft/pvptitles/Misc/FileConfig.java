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
import org.apache.commons.lang.StringUtils;
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

    private String before = "";

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
                if (linea.matches("\\s*-\\s?.+") && !linea.contains("#")) {
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

    private String replace(String line, YamlConfiguration newFile, YamlConfiguration oldFile) {
        String resul = line;

        for (String value : newFile.getKeys(true)) {
            // Este parametro no se toca
            if (value.equals("version")) {
                continue;
            }

            String cValue = value;
            String spaces = ""; // Estilo

            // Para comprobar si el valor existe solo me hace falta el ultimo valor
            if (value.contains(".")) {
                String[] vals = value.split("\\.");
                cValue = vals[vals.length - 1];

                // Style fix
                int i = 0;
                while (i < StringUtils.countMatches(value, ".")) {
                    spaces += "    ";
                    i++;
                }
            }

            if (line.contains(cValue + ":")) {
                Object v = null;
                // Previous structure
                if (oldFile.contains(before + "." + cValue)) {
                    v = oldFile.get(before + "." + cValue);
                } else if (newFile.contains(before + "." + cValue)) {
                    v = newFile.get(before + "." + cValue);
                // New structure
                } else if (oldFile.contains(value)) {
                    v = oldFile.get(value);
                } else {
                    v = newFile.get(value);
                }

                resul = spaces + cValue + ":";

                if (v instanceof List) {
                    List<Object> vs = (List<Object>) v;
                    for (Object v1 : vs) {
                        String val = getFilteredString(v1.toString());
                        resul += System.lineSeparator() + spaces + "- " + val;
                    }
                } else if (!(v instanceof MemorySection)) {
                    resul += " " + getFilteredString(v.toString());
                } else if (v instanceof MemorySection) {
                    before = value;
                }

                resul += System.lineSeparator();
                break;
            }
        }

        return (resul.equals(line) ? resul + System.lineSeparator() : resul);
    }

    public FileConfiguration getConfig() {
        return this.configFile;
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
}
