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
package com.alternacraft.pvptitles.Hooks;

import com.alternacraft.pvptitles.Main.PvpTitles;
import java.util.Arrays;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {

    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chatPlugin = null;

    public static boolean PERMISSIONS_ENABLED = false;
    public static boolean ECONOMY_ENABLED = false;
    public static boolean CHAT_ENABLED = false;

    private PvpTitles plugin = null;

    public VaultHook(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public String[] setupVault() {
        String[] data = {};
        
        PERMISSIONS_ENABLED = this.setupPermissions();
        CHAT_ENABLED = this.setupChat();
        ECONOMY_ENABLED = this.setupEconomy();

        if (PERMISSIONS_ENABLED) {
            data = Arrays.copyOf(data, data.length + 1);
            data[data.length - 1] = "(Vault)Permissions";
        }
        if (CHAT_ENABLED) {
            data = Arrays.copyOf(data, data.length + 1);
            data[data.length - 1] = "(Vault)ChatManager";
        }
        if (ECONOMY_ENABLED) {
            data = Arrays.copyOf(data, data.length + 1);
            data[data.length - 1] = "(Vault)Economy";
        }
        
        return data;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chatPlugin = chatProvider.getProvider();
        }
        return (chatPlugin != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }

    public static boolean hasPermission(String perm, Player pl) {
        if (pl.isOp() && VaultHook.PERMISSIONS_ENABLED) {
            if (permission.hasGroupSupport() && permission.getPlayerGroups(pl).length != 0) {
                String group = permission.getPrimaryGroup(pl);
                World w = null;
                World wp = pl.getWorld();
                return (permission.groupHas(w, group, perm)
                        || permission.groupHas(wp, group, perm));
            } else {
                return permission.has(pl, perm);
            }
        } else {
            return !pl.isOp() && pl.hasPermission(perm);
        }
    }
}
