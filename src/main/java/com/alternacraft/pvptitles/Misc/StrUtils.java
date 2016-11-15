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

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.ChatColor;

public class StrUtils {

    public static String translateColors(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
    
    public static String splitToComponentTimes(long s) {
        String resul = "";
        long days, hours, mins, secs, remainder;
        
        // Lógica        
        days = s / 3600 / 24;
        remainder = s - days * 3600 * 24;
        hours = remainder / 3600;        
        remainder = remainder - hours * 3600;
        mins = remainder / 60;
        remainder = remainder - mins * 60;
        secs = remainder;

        // Representación
        resul += (days > 0) ? (days + "d ") : "";
        if (days > 0) {
            resul += hours + "h ";
            resul += mins + "m ";
        }
        else {
            resul += (hours > 0) ? (hours + "h ") : "";
            if (hours > 0) {
                resul += mins + "m ";
            } else {
                resul += (mins > 0) ? (mins + "m ") : "";
            }
        }
        resul += secs + "s";

        return resul;
    }

    public static String removeColors(String str) {
        return ChatColor.stripColor(str);
    }

    public static String removeColorsWithoutTranslate(String str) {
        return ChatColor.stripColor(translateColors(str));
    }

    public static int dividirEntero(int v, int div) {
        return (v % div == 0 && v != 0) ? v / div : ((int) v / div) + 1;
    }
    
    public static boolean isHologramEmpty(Hologram h) {
        try {
            h.getLine(0);
        }
        catch(Exception ex) {
            return true;
        }
        
        return false;
    }
}
