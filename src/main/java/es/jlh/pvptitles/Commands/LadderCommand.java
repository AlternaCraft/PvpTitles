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

import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Misc.PlayerFame;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class LadderCommand implements CommandExecutor {    
    private final PvpTitles pvpTitles;
            
    public LadderCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player)sender) : Manager.messages;
        
        if (args.length > 0) {            
            sender.sendMessage(PLUGIN + LangFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }
        
        short top = this.pvpTitles.manager.params.getTop();
        
        ArrayList<PlayerFame> rankedPlayers = new ArrayList<>();                
        try {
            rankedPlayers = pvpTitles.manager.dbh.getDm().getTopPlayers(top, "");
        } catch (DBException ex) {
            PvpTitles.logError(ex.getCustomMessage(), null);
        }
        
        sender.sendMessage("");
        sender.sendMessage(PLUGIN);
        sender.sendMessage(ChatColor.YELLOW + "  --------");
        sender.sendMessage(ChatColor.YELLOW + "    Top " + top + " ");
        sender.sendMessage(ChatColor.YELLOW + "  --------");

        for (int i = 0; i < rankedPlayers.size() && i < top; i++) {            
            sender.sendMessage("  " + (i+1) + ". " + rankedPlayers.get(i).toString());
        }
        
        return true;
    }
}