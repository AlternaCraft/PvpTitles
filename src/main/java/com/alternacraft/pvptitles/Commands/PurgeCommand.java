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
package com.alternacraft.pvptitles.Commands;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.PluginLog;
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

        int cantidad, purgetime;

        if (args.length > 1) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        } else if (args.length == 1) {
            int purgados;
            try {
                purgados = Integer.valueOf(args[0]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(getPluginName() + ChatColor.RED + "You have to use a number!");
                return false;
            }
            if (purgados < 0) {
                sender.sendMessage(getPluginName() + ChatColor.RED + "Number has to be above 0");
                return true;
            } else {
                purgetime = purgados;
            }
        } else {
            purgetime = pvpTitles.getManager().params.getPurgeTime();
        }

        try {
            cantidad = dh.dbh.getDm().purgeData(purgetime);

            if (cantidad > 0) {
                sender.sendMessage(getPluginName() + ChatColor.YELLOW
                        + "Check out to the user_changes.txt file (Inside of "
                        + PluginLog.getLogsFolder() + ") to see the affected players");
            }

            sender.sendMessage(getPluginName() + LangsFile.PURGE_RESULT.getText(messages).
                    replace("%cant%", String.valueOf(cantidad)));

        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        return true;
    }
}
