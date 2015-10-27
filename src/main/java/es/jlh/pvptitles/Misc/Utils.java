package es.jlh.pvptitles.Misc;

import org.bukkit.ChatColor;

/**
 *
 * @author julito
 */
public class Utils {

    public static String TranslateColor(String str) {
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
        resul += (hours > 0) ? (hours + "h "): "";
        if (hours > 0) {
            resul += mins + "m ";
        }
        else {
            resul += (mins > 0) ? (mins + "m "): "";
        }
        resul += secs + "s";
        
        return resul;
    }
}
