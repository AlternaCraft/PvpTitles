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
package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangsFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PurgeCommand implements CommandExecutor {
    private final PvpTitles pvpTitles;
    private final Manager dh;

    public PurgeCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
        this.dh = this.pvpTitles.manager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {
            sender.sendMessage(PLUGIN + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }
        
        int cantidad = dh.dbh.getDm().purgeData();
        
        sender.sendMessage(PLUGIN + LangsFile.PURGE_RESULT.getText(messages).
                replace("%cant%", String.valueOf(cantidad)));
        
        return true;
    }
}
