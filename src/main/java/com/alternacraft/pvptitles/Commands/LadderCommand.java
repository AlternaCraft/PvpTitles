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

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Files.TemplatesFile.FILES;
import static com.alternacraft.pvptitles.Files.TemplatesFile.PLUGIN_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.TOP_PLAYER_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.TOP_POINTS_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.TOP_POS_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.TOP_TAG;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import java.util.ArrayList;
import java.util.List;
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
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length > 0) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        short top = this.pvpTitles.getManager().params.getTop();

        ArrayList<PlayerFame> rankedPlayers = new ArrayList<>();
        try {
            rankedPlayers = pvpTitles.getManager().dbh.getDm().getTopPlayers(top, "");
        } catch (DBException ex) {
            LoggerManager.logError(ex.getCustomMessage(), null);
        }

        List<String> lines = this.pvpTitles.getManager().templates.getFileContent(FILES.LADDER_COMMAND);

        for (String line : lines) {
            String msg = line;

            msg = msg
                    .replace(PLUGIN_TAG, PvpTitles.getDefaultPluginName())
                    .replace(TOP_TAG, String.valueOf(top));
            
            if (line.contains(TOP_POS_TAG) || line.contains(TOP_PLAYER_TAG)) {
                for (int i = 0; i < rankedPlayers.size() && i < top; i++) {
                    sender.sendMessage(msg
                            .replace(TOP_POS_TAG, String.valueOf(i + 1))
                            .replace(TOP_PLAYER_TAG, rankedPlayers.get(i).getMWName())
                            .replace(TOP_POINTS_TAG, String.valueOf(rankedPlayers.get(i).getFame()))
                    );                    
                }
            }
            else {
                sender.sendMessage(msg);
            }
        }
        return true;
    }
}
