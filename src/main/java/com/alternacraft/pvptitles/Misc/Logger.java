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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class Logger {

    private final List<String> messages;
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
        this.path = path + name;
    }

    public void addMessage(String str) {
        if (!messages.contains(str)) {
            messages.add(str);
        }
    }

    public void export() {
        if (UtilsFile.exists(path)) {
            UtilsFile.delete(path);
        }

        String all = "";
        for (String message : messages) {
            all += message + "\n";
        }

        UtilsFile.writeFile(path, all);
    }
}
