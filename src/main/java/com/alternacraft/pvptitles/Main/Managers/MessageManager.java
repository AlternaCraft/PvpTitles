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
package com.alternacraft.pvptitles.Main.Managers;

import com.alternacraft.pvptitles.Main.PvpTitles;
import net.md_5.bungee.api.ChatColor;

public class MessageManager {

    // Custom message
    public static void showMessage(String msg) {
        PvpTitles.getInstance().getServer().getConsoleSender().sendMessage(
                PvpTitles.getPluginName() + msg);
    }

    public static void showError(String msg) {
        PvpTitles.getInstance().getServer().getConsoleSender().sendMessage(
                PvpTitles.getPluginName() + ChatColor.RED + msg);
    }
}
