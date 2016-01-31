package es.jlh.pvptitles.Misc;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;

/**
 *
 * @author AlternaCraft
 */
public class Utils {

    public static String translateColor(String str) {
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
