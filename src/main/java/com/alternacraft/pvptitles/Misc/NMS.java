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

import com.alternacraft.pvptitles.Main.PvpTitles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.bukkit.entity.Player;


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
