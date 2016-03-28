package es.jlh.pvptitles.Objects;

import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
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
 *
 * @author AlternaCraft
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
                if ((linea.matches("\\s*-\\s?.+") && !linea.contains("#"))) {
                    continue;
                }                
                String nline = replace(linea, newFile, oldFile);
                fw.write(nline);
            }
        } catch (IOException ex) {
            PvpTitles.logDebugInfo(Level.SEVERE, null, ex);
        }

        if (outFile.exists()) {
            outFile.delete();
        }

        temp.renameTo(outFile);
        showMessage(ChatColor.GREEN + "Previous file settings have been established "
                + "into the new one.");
        showMessage(ChatColor.GREEN + "This functionality could fail, check the result.");
    }

    private String replace(String linea, YamlConfiguration newFile, YamlConfiguration oldFile) {
        String resul = linea;

        for (String value : newFile.getKeys(true)) {
            if (value.equals("Version")) // Este parametro no se toca
                continue;
            
            String cValue = value + ":";
            String spaces = ""; // Estilo

            // Para comprobar si el valor existe solo me hace falta el ultimo valor
            if (value.contains(".")) {
                String[] vals = value.split("\\.");
                cValue = vals[vals.length - 1] + ":";
                spaces = "    ";
            }

            if (linea.contains(cValue)) {
                Object v = null;
                
                if (oldFile.contains(value)) {
                    v = oldFile.get(value);
                }
                else {
                    v = newFile.get(value);
                }     
                
                resul = spaces + cValue;
                
                if (v instanceof List) {
                    List<Object> vs = (List<Object>) v;
                    for (Object v1 : vs) {
                        String val = getFilteredString(v1.toString());
                        resul += System.lineSeparator() + spaces + "- " + val;
                    }
                } else if (!(v instanceof MemorySection)) {
                    resul += " " + getFilteredString(v.toString());
                }

                resul += System.lineSeparator();
                break;
            }          
        }

        return (resul.equals(linea) ? resul + System.lineSeparator() : resul);
    }    
    
    public FileConfiguration getConfig() {
        return this.configFile;
    }

    private String getFilteredString(String str) {
        List<Character> special = Arrays.asList(':', '{', '}', '[', ']', ',', '&', 
                '*', '#', '?', '|', '<', '>', '=', '!', '%', '@', '\\');
        
        for (Character character : special) {
            if (str.contains(String.valueOf(character)))
                return "\"" + str + "\"";
        }
        
        return str;
    }
}
