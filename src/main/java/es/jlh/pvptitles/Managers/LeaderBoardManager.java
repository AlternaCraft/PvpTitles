package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Objects.Boards.CustomBoard;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Localizer;
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
    private List<CustomBoard> boards = null;

    public LeaderBoardManager(PvpTitles pt) {
        this.boards = new ArrayList();
        this.pt = pt;
    }

    public boolean addBoard(CustomBoard cb, Player pl) {
        if (!boards.contains(cb)) {
            // Compruebo si ya hay algo ocupando el sitio            
            ArrayList<PlayerFame> pf = this.pt.cm.dbh.getDm().getTopPlayers(cb.getModel().getCantidad(), cb.getInfo().getServer());
            short jugadores = (short)pf.size();
            
            if (!cb.isMaterializable(jugadores)) {
                pl.sendMessage(PLUGIN + LangFile.SIGN_CANT_BE_PLACED.getText(Localizer.getLocale(pl)));
                // Borradas coordenadas
                return false;
            }

            createSign(cb);
            boards.add(cb);

            pt.cm.dbh.getDm().registraCartel(cb.getInfo().getNombre(),
                    cb.getModel().getNombre(), cb.getInfo().getServer(),
                    cb.getInfo().getL(), cb.getInfo().getOrientacion(),
                    cb.getInfo().getPrimitiveBlockface());
        }
        else {
            return false;
        }

        return true;
    }
    
    public void loadBoard(CustomBoard cs) {        
        if (!boards.contains(cs)) {
            createSign(cs);            
            boards.add(cs);
        }
    }
    
    public void updateBoard() {
        for (CustomBoard sign : boards) {
            createSign(sign);
        }
    }

    public void deleteSign(Location l) {
        for (Iterator<CustomBoard> it = boards.iterator(); it.hasNext();) {
            CustomBoard sign = it.next();
            if (sign.getInfo().getL().equals(l)) {
                short jugadores = (short)pt.cm.dbh.getDm().getTopPlayers(
                        sign.getModel().getCantidad(), sign.getInfo().getServer()).size();

                sign.dematerialize(jugadores);

                pt.cm.dbh.getDm().borraCartel(sign.getInfo().getL());
                boards.remove(sign);

                break;
            }
        }
    }

    public void deleteSign(Location l, BlockBreakEvent event) {
        for (CustomBoard sign : boards) {
            if (sign.getInfo().getL().equals(l)) {
                short jugadores = (short)pt.cm.dbh.getDm().getTopPlayers(
                        sign.getModel().getCantidad(), sign.getInfo().getServer()).size();

                Player pl = event.getPlayer();

                // Compruebo permisos
                if (!pl.hasPermission("pvptitles.managesign")) {
                    pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                    event.setCancelled(true);
                    return;
                }

                sign.dematerialize(jugadores);

                pl.sendMessage(PLUGIN + LangFile.SIGN_DELETED.getText(Localizer.getLocale(pl)));

                pt.cm.dbh.getDm().borraCartel(sign.getInfo().getL());
                boards.remove(sign);

                break;
            }
        }
    }

    private void createSign(CustomBoard cs) {
        ArrayList<PlayerFame> pf = pt.cm.dbh.getDm().getTopPlayers(
                cs.getModel().getCantidad(), cs.getInfo().getServer());

        cs.materialize(pf);
    }

    public List<CustomBoard> getSigns() {
        return boards;
    }
    
    public void vaciar() {
        this.boards.clear();
    }
}
