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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class Titles {

    /* Title packet */
    private static Class<?> packetTitle;
    /* Title packet actions ENUM */
    private static Class<?> packetActions;
    /* Chat serializer */
    private static Class<?> nmsChatSerializer;
    private static Class<?> chatBaseComponent;
    /* Title text and color */
    private String title = null;

    public Titles(String title) {
        this.title = title;
        loadClasses();
    }

    /**
     * Load spigot and NMS classes
     */
    private void loadClasses() {
        if (packetTitle == null) {
            packetTitle = getNMSClass("PacketPlayOutTitle");
            packetActions = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
            chatBaseComponent = getNMSClass("IChatBaseComponent");
            nmsChatSerializer = getNMSClass("ChatComponentText");
        }
    }

    /**
     * Clear the title
     *
     * @param player Player
     */
    public void clearTitle(Player player) {
        try {
            // Send timings first
            Object handle = getHandle(player);
            Object connection = getField(handle.getClass(), "playerConnection").get(handle);
            Object[] actions = packetActions.getEnumConstants();
            Method sendPacket = getMethod(connection.getClass(), "sendPacket");
            Object packet = packetTitle
                    .getConstructor(packetActions, chatBaseComponent)
                    .newInstance(actions[3], null);
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTitle(Player player) {
        if (Titles.packetTitle != null) {
            try {
                Object handle = getHandle(player);
                Object connection = getField(
                        handle.getClass(),
                        "playerConnection"
                ).get(handle);
                Object[] actions = Titles.packetActions.getEnumConstants();
                Method sendPacket = getMethod(connection.getClass(), "sendPacket");
                Object serialized = nmsChatSerializer.getConstructor(String.class)
                        .newInstance(ChatColor.translateAlternateColorCodes('&', this.title));
                Object packet = Titles.packetTitle
                        .getConstructor(Titles.packetActions, chatBaseComponent)
                        .newInstance(actions[0], serialized);
                sendPacket.invoke(connection, packet);
                System.out.println("a");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Class<?> getPrimitiveType(Class<?> clazz) {
        return clazz;
    }

    private Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a = classes != null ? classes.length : 0;
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++) {
            types[i] = getPrimitiveType(classes[i]);
        }
        return types;
    }

    private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if (a.length != o.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) {
                return false;
            }
        }
        return true;
    }

    private Object getHandle(Object obj) {
        try {
            return getMethod("getHandle", obj.getClass()).invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(String name, Class<?> clazz,
            Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t)) {
                return m;
            }
        }
        return null;
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name)
                    && (args.length == 0 || ClassListEqual(
                            args,
                            m.getParameterTypes()
                    ))) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length) {
            return false;
        }
        for (int i = 0; i < l1.length; i++) {
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    private String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        return name.substring(name.lastIndexOf('.') + 1) + ".";
    }

    private Class<?> getNMSClass(String className) {
        String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }
}
