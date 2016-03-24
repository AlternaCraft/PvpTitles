package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Main.PvpTitles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class NMS {
    private static final String PACKAGENAME = PvpTitles.getInstance().getServer().getClass().getPackage().getName();
    public static final String VERSION = PACKAGENAME.substring(PACKAGENAME.lastIndexOf(".") + 1);

    private static Class<?> craftPlayer;
    private static Method getHandle;

    public static Object castToCraft(Player player) {
        if (craftPlayer == null) {
            try {
                craftPlayer = Class.forName("org.bukkit.craftbukkit." + VERSION + ".entity.CraftPlayer");
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        if (craftPlayer == null) {
            return null;
        }
        return craftPlayer.cast(player);
    }

    public static Object castToNMS(Player player) {
        Object craft = castToCraft(player);
        if (craft == null) {
            return null;
        }
        if (getHandle == null) {
            try {
                getHandle = craftPlayer.getMethod("getHandle");
            } catch (NoSuchMethodException | SecurityException exc) {
                return null;
            }
        }
        try {
            return getHandle.invoke(castToCraft(player));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exc) {
            return null;
        }
    }
}
