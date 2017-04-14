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
package com.alternacraft.pvptitles.Main;

import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.LOGGER;
import static com.alternacraft.pvptitles.Main.PvpTitles.debugMode;
import java.util.logging.Level;

public class CustomLogger {

    private static final String MYSQL_CRAP_REGEX = "com.*: ";

    public static void logMessage(String msg) {
        LOGGER.info(msg);
    }
    
    /* DEBUG MANAGEMENT */
    public static void logDebugInfo(String message) {
        logDebugInfo(Level.INFO, message.replaceFirst(MYSQL_CRAP_REGEX, ""));
    }

    public static void logDebugInfo(Level level, String message) {
        logDebugInfo(level, message, null);
    }

    public static void logDebugInfo(Level level, String message, Exception ex) {
        if (debugMode) {
            PvpTitles.LOGGER.log(level, message, ex);
        }
    }

    /* ERROR MANAGEMENT */
    public static void logError(String message) {
        logError(message, null);
    }
    
    public static void logError(String message, Exception ex) {
        PvpTitles.LOGGER.log(Level.SEVERE, message, ex);
    }
    
    /* Custom message */
    public static void showMessage(String msg) {
        PvpTitles.getInstance().getServer().getConsoleSender().sendMessage(
                PvpTitles.getPluginName() + msg);
    }
    
}
