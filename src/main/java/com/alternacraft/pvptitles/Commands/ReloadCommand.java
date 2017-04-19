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

import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Hook.HolographicHook;
import static com.alternacraft.pvptitles.Hook.HolographicHook.DEFAULT_TITLE_HEIGHT;
import static com.alternacraft.pvptitles.Hook.HolographicHook.HEIGHT_PER_ROW;
import com.alternacraft.pvptitles.Listeners.HandlePlayerFame;
import com.alternacraft.pvptitles.Main.DBLoader.DBTYPE;
import static com.alternacraft.pvptitles.Main.DBLoader.tipo;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.RetroCP.DBChecker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final PvpTitles pvpTitles;

    public ReloadCommand(PvpTitles pvpTitles) {
        this.pvpTitles = pvpTitles;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length > 0) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        if (tipo.equals(DBTYPE.MYSQL) || tipo.equals(DBTYPE.SQLITE)) {
            this.pvpTitles.getManager().dbh.sql.closeConnection();
        }

        pvpTitles.getManager().getCh().loadConfig(pvpTitles.getManager().params);

        pvpTitles.getManager().getMovementManager().updateTimeAFK();
        
        pvpTitles.getManager().getDbh().selectDB();
        new DBChecker(pvpTitles).setup();

        pvpTitles.getManager().loadLang();
        pvpTitles.getManager().loadModels();
        pvpTitles.getManager().loadSavedBoards();
        pvpTitles.getManager().loadRewards();
        pvpTitles.getManager().loadTemplates();

        if (tipo == DBTYPE.MYSQL) {
            pvpTitles.getManager().loadServers();
        }

        pvpTitles.getManager().loadActualizador();
        pvpTitles.getManager().loadRankTimeChecker();

        if (HolographicHook.ISHDENABLED && pvpTitles.getManager().params.displayLikeHolo()) {
            HolographicHook.RANK_LINE = pvpTitles.getManager().params.getHolotagformat();
            HolographicHook.TITLE_HEIGHT = ((pvpTitles.getManager().params.getHoloHeightMod() - 1) * HEIGHT_PER_ROW) + DEFAULT_TITLE_HEIGHT;
            HolographicHook.loadPlayersInServer();
        } else if (HolographicHook.ISHDENABLED && HolographicHook.HOLOPLAYERS.size() > 0) {
            /*
             * En caso de hacer un pvpreload habiendo desactivado los hologramas en
             * el config, borro los que haya en el server creados anteriormente.
             */
            HolographicHook.deleteHoloPlayers();
        }

        // Just in case
        HandlePlayerFame.ALREADY_LOGGED.clear();
        HandlePlayerFame.ALREADY_VISITED.clear();
        
        sender.sendMessage(getPluginName() + LangsFile.PLUGIN_RELOAD.getText(messages));

        return true;
    }
}
