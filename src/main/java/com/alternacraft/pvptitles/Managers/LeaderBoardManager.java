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
package com.alternacraft.pvptitles.Managers;

import com.alternacraft.pvptitles.Events.BoardEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsCustom.HologramBoard;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import com.alternacraft.pvptitles.Misc.CustomLocation;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class LeaderBoardManager {

    private PvpTitles pt = null;
    private List<Board> boards = null;

    public LeaderBoardManager(PvpTitles pt) {
        this.boards = new ArrayList() {
            @Override
            public boolean contains(Object o) {
                if (o instanceof Board) {
                    Location l = ((Board) o).getData().getLocation();

                    for (Iterator iterator = this.iterator(); iterator.hasNext();) {
                        Board next = (Board) iterator.next();

                        if (l.equals(next.getData().getLocation())) {
                            // MultiServer
                            if (((Board) o).getData().getServer() != null
                                    && next.getData().getServer() != null) {
                                if (!((Board) o).getData().getServer().equals(next.getData().getServer())) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                }

                return false;
            }
        };
        this.pt = pt;
    }

    public boolean addBoard(Board b, Player pl) {
        if (!boards.contains(b)) {
            // Compruebo si ya hay algo ocupando el sitio            
            ArrayList<PlayerFame> pf = new ArrayList<>();
            try {
                pf = this.pt.getManager().dbh.getDm().getTopPlayers(b.getModel().getCantidad(), b.getData().getServer());
            } catch (DBException ex) {
                CustomLogger.logError(ex.getCustomMessage());
            }
            short jugadores = (short) pf.size();

            if (!b.isMaterializable(jugadores)) {
                pl.sendMessage(getPluginName() + LangsFile.BOARD_CANT_BE_PLACED.getText(Localizer.getLocale(pl)));
                return false;
            }

            // Tipos predefinidos
            if (b instanceof SignBoard) {
                try {
                    pt.getManager().dbh.getDm().registraBoard((SignBoard) b);
                } catch (DBException ex) {
                    CustomLogger.logError(ex.getCustomMessage());
                    return false;
                }
            } else if (b instanceof HologramBoard) {
                HologramsFile.saveHologram(b.getData());
            }

            b.materialize(pf);
            boards.add(b);

            pt.getServer().getPluginManager().callEvent(new BoardEvent(pl, b.getData().getFullLocation()));
            
            pl.sendMessage(getPluginName() + LangsFile.BOARD_CREATED_CORRECTLY.
                    getText(Localizer.getLocale(pl)).replace("%name%", b.getData().getNombre()));
        } else {
            return false;
        }

        return true;
    }

    public void loadBoard(Board cs) {
        if (!boards.contains(cs)) {
            try {
                cs.materialize(pt.getManager().dbh.getDm().getTopPlayers(
                        cs.getModel().getCantidad(), cs.getData().getServer()
                ));
            } catch (DBException ex) {
                CustomLogger.logError(ex.getCustomMessage());
            }
            boards.add(cs);
        }
    }

    public void updateBoards() {
        for (Board board : boards) {
            ArrayList<PlayerFame> pf = new ArrayList<>();
            try {
                pf = pt.getManager().dbh.getDm().getTopPlayers(
                        board.getModel().getCantidad(), board.getData().getServer());
            } catch (DBException ex) {
                CustomLogger.logError(ex.getCustomMessage());
            }
            
            board.dematerialize((short) pf.size());
            board.materialize(pf);
        }
    }

    public void deleteBoard(Location l, Object o) {
        for (Board bo : boards) {
            if (bo.getData().getLocation().equals(CustomLocation.toCustomLocation(l))) {
                short jugadores = 0;
                try {
                    jugadores = (short) pt.getManager().dbh.getDm().getTopPlayers(
                            bo.getModel().getCantidad(), bo.getData().getServer()).size();
                } catch (DBException ex) {
                    CustomLogger.logError(ex.getCustomMessage());
                }

                Player pl = null;

                // Modulo Signs
                if (o != null) {
                    if (o instanceof BlockBreakEvent) {
                        BlockBreakEvent event = (BlockBreakEvent) o;
                        pl = event.getPlayer();

                        if (!pl.hasPermission("pvptitles.manageboard")) {
                            pl.sendMessage(getPluginName() + LangsFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        pl = (Player) o;

                        if (!pl.hasPermission("pvptitles.manageboard")) {
                            pl.sendMessage(getPluginName() + LangsFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                            return;
                        }
                    }
                }

                // Tipos predefinidos
                if (bo instanceof SignBoard) {
                    try {
                        pt.getManager().dbh.getDm().borraBoard(bo.getData().getLocation());
                    } catch (DBException ex) {
                        CustomLogger.logError(ex.getCustomMessage());
                        return;
                    }
                } else if (bo instanceof HologramBoard) {
                    HologramsFile.removeHologram(bo.getData().getLocation());
                }

                bo.dematerialize(jugadores);
                boards.remove(bo);

                pt.getServer().getPluginManager().callEvent(new BoardEvent(pl, bo.getData().getFullLocation()));
                
                if (pl != null) {
                    pl.sendMessage(getPluginName() + LangsFile.BOARD_DELETED.getText(Localizer.getLocale(pl)));
                }

                break;
            }
        }
    }

    public List<Board> getBoards() {
        return boards;
    }

    public void vaciar() {
        this.boards.clear();
    }
}
