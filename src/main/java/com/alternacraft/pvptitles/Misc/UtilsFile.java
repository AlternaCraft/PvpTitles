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
import com.google.common.io.Files;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    public static void writeFile(File fout, String cont) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fout);
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
                String[] lines = cont.split("\n");
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            CustomLogger.logError(ex.getMessage());
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    public static String getFileAsString(String path) {
        List<String> lines = UtilsFile.getFileLines(path);
        StringBuilder res = new StringBuilder();

        lines.forEach(line -> {
            res.append(line.replace(" ", "")); // Optimization
        });

        return res.toString();
    }

    public static List<String> getFileLines(String path) {
        return UtilsFile.getFileLines(new File(path));
    }

    public static List<String> getFileLines(File file) {
        try {
            return Files.readLines(file, Charset.defaultCharset());
        } catch (IOException ex) {
            CustomLogger.logError(ex.getMessage());
        }
        return new ArrayList();
    }

    public static void delete(String path) {
        delete(path, false);
    }

    public static void delete(String path, boolean delete_on_exit) {
        File todelete = new File(path);
        if (!todelete.delete() && delete_on_exit) {
            todelete.deleteOnExit();
        }
    }

    public static boolean createDirs(String path) {
        return new File(path).mkdirs();
    }

    public static boolean createDirsFromFile(String path) {
        return new File(path).getParentFile().mkdirs();
    }

    public static boolean createDir(String path) {
        return new File(path).mkdir();
    }

    public static File[] getFilesIntoDir(String dir) {
        File f = new File(dir);
        if (f.exists()) {
            return f.listFiles();
        }
        return new File[0];
    }
}
