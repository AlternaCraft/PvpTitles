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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class UtilsFile {

    public static boolean exists(String path) {
        return exists(new File(path));
    }

    public static boolean exists(File file) {
        return file.exists();
    }

    public static void writeFile(String path, String cont) {
        writeFile(new File(path), cont);
    }

    public static void writeFile(File file, String cont) {
        FileWriter fichero = null;

        try {
            fichero = new FileWriter(file);
            fichero.write(cont);
        } catch (Exception ex) {            
            MessageManager.showError("Error creating file: '" + file.getName() + "'");
        } finally {
            try {
                if (null != fichero) {
                    fichero.flush();
                    fichero.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    public static List<String> getFileLines(String path) {
        return UtilsFile.getFileLines(new File(path));
    }

    public static List<String> getFileLines(File file) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(file.toPath(), Charset.defaultCharset());
        } catch (IOException ex) {
            MessageManager.showError("Error getting content from '" + file.getName() + "'");
        }
        return lines;
    }
    
    public static String getFileAsString(String path) {
        List<String> lines = UtilsFile.getFileLines(path);
        StringBuilder res = new StringBuilder();
        
        for (String line : lines) { 
            res.append(line.replace(" ", "")); // Optimization
        }
        
        return res.toString();
    }
    
    public static void delete(String ruta) {
        File todelete = new File(ruta);
        if (!todelete.delete()) {
            todelete.deleteOnExit();
        }
    }
    
    public static boolean createDir(String ruta) {
        return new File(ruta).mkdir();
    }
}
