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

import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Hook.HolographicHook;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardData;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import com.alternacraft.pvptitles.Managers.BoardsAPI.ModelController;
import com.alternacraft.pvptitles.Managers.BoardsCustom.HologramBoard;
import static com.alternacraft.pvptitles.Managers.BoardsCustom.HologramBoard.DEFAULT_POSITION;
import com.alternacraft.pvptitles.Misc.Inventories;
import com.alternacraft.pvptitles.Misc.Localizer;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoardCommand implements CommandExecutor {

    enum SUPPORTED_BOARDS {
        HOLOGRAM
    }

    private final Manager cm;
    private final PvpTitles pt;

    public BoardCommand(PvpTitles pvpTitles) {
        this.pt = pvpTitles;
        this.cm = pvpTitles.getManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        // Esto no puede ser ejecutado por la consola
        if (!(sender instanceof Player)) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_FORBIDDEN.getText(messages));
            return true;
        }

        Player pl = (Player) sender;

        if (args.length >= 1) {
            if (args.length == 1 || (!args[0].equals("create") && !args[0].equals("remove"))) {
                return false;
            }
            
            if (args[1].equalsIgnoreCase(SUPPORTED_BOARDS.HOLOGRAM.name())
                    && !HolographicHook.ISHDENABLED) {
                pl.sendMessage(getPluginName() + ChatColor.RED + "HolographicDisplays is not enabled");
                return true;
            }

            if (!args[1].equalsIgnoreCase(SUPPORTED_BOARDS.HOLOGRAM.name())) {
                pl.sendMessage(getPluginName() + ChatColor.RED + "Supported types: "
                        + SUPPORTED_BOARDS.HOLOGRAM.name().toLowerCase());
                return true;
            }

            switch (args[0]) {
                case "create":
                    if (args.length >= 4) {
                        String name = args[2];
                        String model = args[3];
                        String filter = (args.length >= 5) ? args[4] : "";
                        create(name, model, filter, pl);
                    } else {
                        pl.sendMessage(getPluginName() + ChatColor.RED
                                + "Syntax: 'pvpboard create <board_type> <name> <board_model> [<server_name>]'");
                    }
                    break;
                case "remove":
                    if (args.length >= 3) {
                        BoardData bd = HologramsFile.loadHologram(args[1]);

                        if (bd == null) {
                            pl.sendMessage(getPluginName() + LangsFile.BOARD_NAME_NOT_EXISTS.getText(Localizer.getLocale(pl)));
                        } else {
                            cm.getLbm().deleteBoard(bd.getLocation(), pl);
                        }
                    } else {
                        pl.sendMessage(getPluginName() + ChatColor.RED
                                + "Syntax: 'pvpboard remove <board_type> <name>'");
                    }
                    break;
                default:
                    return false;
            }
        } else {
            List<Board> boards = pt.getManager().getLbm().getBoards();
            pl.openInventory(Inventories.createInventory(boards, Localizer.getLocale(pl)).get(0));
        }

        return true;
    }

    private void create(String name, String model, String filter, Player pl) {
        BoardData bda = HologramsFile.loadHologram(name);
        if (bda != null) {
            pl.sendMessage(getPluginName() + LangsFile.BOARD_NAME_ALREADY_EXISTS.getText(Localizer.getLocale(pl)));
            return;
        }

        BoardModel bm = cm.searchModel(model);
        if (bm == null) {
            pl.sendMessage(getPluginName() + LangsFile.BOARD_MODEL_NOT_EXISTS.getText(Localizer.getLocale(pl)));
            return;
        }

        Location ploc = pl.getLocation();
        Location l = new Location(ploc.getWorld(), ploc.getX(), ploc.getY(), ploc.getZ());
        l.add(0.0, DEFAULT_POSITION, 0.0);

        BoardData bd = new BoardData(l);
        bd.setNombre(name);
        bd.setModelo(model);
        bd.setServer(filter);

        ModelController mc = new ModelController();
        mc.preprocessUnit(bm.getParams());

        Board b = new HologramBoard(bd, bm, mc);

        cm.getLbm().addBoard(b, pl);
    }
}
