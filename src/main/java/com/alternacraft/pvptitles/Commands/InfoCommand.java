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
import static com.alternacraft.pvptitles.Files.TemplatesFile.COMMAND_TAG;
import com.alternacraft.pvptitles.Files.TemplatesFile.FILES;
import static com.alternacraft.pvptitles.Files.TemplatesFile.INFO_COMMAND_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.PLUGIN_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.VERSION_TAG;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InfoCommand implements CommandExecutor {

    private PvpTitles pvpTitles = null;

    public InfoCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length > 0) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        Set<String> commands = pvpTitles.getDescription().getCommands().keySet();

        List<String> commandTemplate = this.pvpTitles.getManager().templates.getFileContent(FILES.INFO_COMMAND);

        for (String line : commandTemplate) {
            String msg = line;
            
            msg = msg
                    .replace(PLUGIN_TAG, PvpTitles.getDefaultPluginName())
                    .replace(VERSION_TAG, pvpTitles.getDescription().getVersion());
            
            if (msg.contains(COMMAND_TAG) || msg.contains(INFO_COMMAND_TAG)) {
                for (Iterator<String> iterator = commands.iterator(); iterator.hasNext();) {
                    String next = iterator.next();
                    String perm = pvpTitles.getDescription().getCommands().get(next).get("permission").toString();

                    if (!sender.hasPermission(perm) || next.compareToIgnoreCase("pvptitles") == 0) {
                        continue;
                    }

                    String usage = pvpTitles.getDescription().getCommands().get(next)
                            .get("usage").toString().replace("<command>", next.toLowerCase());
                    String info = LangsFile.valueOf("COMMAND_" + next.replace("pvp", "").toUpperCase() + "_INFO").getText(messages);

                    sender.sendMessage(msg
                            .replace(COMMAND_TAG, usage)
                            .replace(INFO_COMMAND_TAG, info)
                    );
                }
            } else {
                sender.sendMessage(msg);
            }
        }

        sender.sendMessage("■ " + ChatColor.GOLD + "Created By "
                + pvpTitles.getDescription().getAuthors().get(0)
                + ChatColor.RESET + " ■");

        return true;
    }
}
