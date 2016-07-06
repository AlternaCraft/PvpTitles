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
package es.jlh.pvptitles.Misc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

public class StrUtils {

    public static String translateColors(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static String splitToComponentTimes(int milli) {
        String resul = "";

        // Lógica
        int hours = (int) milli / 3600;
        int remainder = (int) milli - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        // Representación
        resul += (hours > 0) ? (hours + "h ") : "";
        if (hours > 0) {
            resul += mins + "m ";
        } else {
            resul += (mins > 0) ? (mins + "m ") : "";
        }
        resul += secs + "s";

        return resul;
    }

    public static String removeColors(String str) {
        return str.replaceAll("§(\\w|\\d){1}", "");
    }

    public static int dividirEntero(int v, int div) {
        return (v % div == 0 && v != 0) ? v / div : ((int) v / div) + 1;
    }

    public static String getHologramText(String str) {
        String regex = "text=(.*)\\]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);

        if (m.find()) {
            return m.group(1);
        }

        return str;
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
