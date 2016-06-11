package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Events.BoardEvent;
import es.jlh.pvptitles.Files.HologramsFile;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.BoardsAPI.Board;
import es.jlh.pvptitles.Managers.BoardsCustom.HologramBoard;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoard;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Objects.CustomLocation;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author AlternaCraft
 */
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
                pf = this.pt.manager.dbh.getDm().getTopPlayers(b.getModel().getCantidad(), b.getData().getServer());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            short jugadores = (short) pf.size();

            if (!b.isMaterializable(jugadores)) {
                pl.sendMessage(PLUGIN + LangFile.BOARD_CANT_BE_PLACED.getText(Localizer.getLocale(pl)));
                return false;
            }

            // Tipos predefinidos
            if (b instanceof SignBoard) {
                try {
                    pt.manager.dbh.getDm().registraBoard((SignBoard) b);
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                    return false;
                }
            } else if (b instanceof HologramBoard) {
                HologramsFile.saveHologram(b.getData());
            }

            b.materialize(pf);
            boards.add(b);

            pt.getServer().getPluginManager().callEvent(new BoardEvent(pl, b.getData().getFullLocation()));
            
            pl.sendMessage(PLUGIN + LangFile.BOARD_CREATED_CORRECTLY.
                    getText(Localizer.getLocale(pl)).replace("%name%", b.getData().getNombre()));
        } else {
            return false;
        }

        return true;
    }

    public void loadBoard(Board cs) {
        if (!boards.contains(cs)) {
            try {
                cs.materialize(pt.manager.dbh.getDm().getTopPlayers(
                        cs.getModel().getCantidad(), cs.getData().getServer()
                ));
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            boards.add(cs);
        }
    }

    public void updateBoards() {
        for (Board board : boards) {
            ArrayList<PlayerFame> pf = new ArrayList<>();
            try {
                pf = pt.manager.dbh.getDm().getTopPlayers(
                        board.getModel().getCantidad(), board.getData().getServer());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
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
                    jugadores = (short) pt.manager.dbh.getDm().getTopPlayers(
                            bo.getModel().getCantidad(), bo.getData().getServer()).size();
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }

                Player pl = null;

                // Modulo Signs
                if (o != null) {
                    if (o instanceof BlockBreakEvent) {
                        BlockBreakEvent event = (BlockBreakEvent) o;
                        pl = event.getPlayer();

                        if (!pl.hasPermission("pvptitles.manageboard")) {
                            pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                            event.setCancelled(true);
                            return;
                        }
                    } else {
                        pl = (Player) o;

                        if (!pl.hasPermission("pvptitles.manageboard")) {
                            pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                            return;
                        }
                    }
                }

                // Tipos predefinidos
                if (bo instanceof SignBoard) {
                    try {
                        pt.manager.dbh.getDm().borraBoard(bo.getData().getLocation());
                    } catch (DBException ex) {
                        PvpTitles.logError(ex.getCustomMessage(), null);
                        return;
                    }
                } else if (bo instanceof HologramBoard) {
                    HologramsFile.removeHologram(bo.getData().getLocation());
                }

                bo.dematerialize(jugadores);
                boards.remove(bo);

                pt.getServer().getPluginManager().callEvent(new BoardEvent(pl, bo.getData().getFullLocation()));
                
                if (pl != null) {
                    pl.sendMessage(PLUGIN + LangFile.BOARD_DELETED.getText(Localizer.getLocale(pl)));
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
