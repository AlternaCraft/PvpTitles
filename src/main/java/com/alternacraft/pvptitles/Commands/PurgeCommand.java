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
package com.alternacraft.pvptitles.Commands;

import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Logger;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PurgeCommand implements CommandExecutor {

    private final PvpTitles pvpTitles;
    private final Manager dh;

    public PurgeCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
        this.dh = this.pvpTitles.getManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        int cantidad = 0;

        if (args.length > 1) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        } else if (args.length == 1) {
            int purgados = 0;
            try {
                purgados = Integer.valueOf(args[0]);
            } catch (Exception ex) {
                sender.sendMessage(getPluginName() + ChatColor.RED + "You have to use a number!");
                return false;
            }
            if (purgados < 0) {
                sender.sendMessage(getPluginName() + ChatColor.RED + "Number has to be above 0");
                return true;
            } else {
                cantidad = dh.dbh.getDm().purgeData(purgados);
            }
        } else {
            cantidad = dh.dbh.getDm().purgeData(pvpTitles.getManager().params.getTimeP());
        }

        if (cantidad > 0) {
            sender.sendMessage(getPluginName() + ChatColor.YELLOW +
                    "Check out to the user_changes.txt file (Inside of " 
                            + Logger.getLogsFolder() + ") to see the affected players");
        }
        
        sender.sendMessage(getPluginName() + LangsFile.PURGE_RESULT.getText(messages).
                replace("%cant%", String.valueOf(cantidad)));

        return true;
    }
}
