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

import com.alternacraft.pvptitles.Main.Managers.MessageManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class Logger {

    private static String logsfolder = "Logs";
    
    private final List<String> messages;
    private final String fullpath;
    private final String path;

    /**
     * Register a logger which will be saved into plugin folder
     *
     * @param pl JavaPlugin
     * @param name File name
     */
    public Logger(JavaPlugin pl, String name) {
        this(pl.getDataFolder() + File.separator, name);
    }

    /**
     * Register a logger which will be saved into path
     * 
     * @param path Path
     * @param name File name
     */
    public Logger(String path, String name) {
        this.messages = new ArrayList();
        this.path = path + logsfolder + File.separator;
        this.fullpath = this.path + name;
    }

    /**
     * Add a new record to log
     * 
     * @param str Record value
     */
    public void addMessage(String str) {
        if (!messages.contains(str)) {
            messages.add(str);
        }
    }

    /**
     * Export records to log file
     * 
     * @param keepold Keep old values?
     */
    public void export(boolean keepold) {
        // Creating log folder if not exists
        if (!UtilsFile.exists(path)) {
            if (!UtilsFile.createDir(path)) {
                MessageManager.showError("Couldn't create Logs folder");
                return;
            }
        }
        
        String all = "";
        
        if (keepold) {
            // Recovering old values
            List<String> oldcontent = new ArrayList<>();        
            if (UtilsFile.exists(fullpath)) {
                oldcontent = UtilsFile.getFileLines(fullpath);
                UtilsFile.delete(fullpath);
            }

            // Writing old values            
            if (oldcontent.size() > 0) {
                for (String cont : oldcontent) {
                    all += cont + "\n";
                }
                all += "\n";            
            }
        }
        
        // Writing new values
        // Date
        all += "---\n" + DateUtils.getCurrentTimeStamp() + "\n---\n";
        for (String message : messages) {
            all += message + "\n";
        }
        UtilsFile.writeFile(fullpath, all);
    }
    
    public static String getLogsFolder() {
        return logsfolder;
    }
    
    public static void changeLogsFolderTo(String str) {
        logsfolder = str;
    }
}
