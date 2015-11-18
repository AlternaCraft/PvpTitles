package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Objects.LBSigns.CustomSign;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author julito
 */
public class LeaderBoardManager {

    private PvpTitles pt = null;
    private List<CustomSign> signs = null;

    public LeaderBoardManager(PvpTitles pt) {
        this.signs = new ArrayList();
        this.pt = pt;
    }

    public boolean addSign(CustomSign cs, Player pl) {
        if (!signs.contains(cs)) {
            // Compruebo si ya hay algo ocupando el sitio            
            ArrayList<PlayerFame> pf = this.pt.cm.getDm().getTopPlayers(cs.getModel().getCantidad(), cs.getInfo().getServer());
            short jugadores = (short)pf.size();
            
            if (isOccupied(cs, jugadores)) {
                int filas = cs.getModel().getFilas(jugadores);
                int cols = cs.getModel().getCols();
                
                pl.sendMessage(PLUGIN + LangFile.SIGN_CANT_BE_PLACED.getText(Localizer.getLocale(pl))
                        + " (" + filas + "x" + cols + ")");

                return false;
            }

            createSign(cs);
            signs.add(cs);

            pt.cm.getDm().registraCartel(cs.getInfo().getNombre(),
                    cs.getModel().getNombre(), cs.getInfo().getServer(),
                    cs.getInfo().getL(), cs.getInfo().getOrientacion(),
                    cs.getInfo().getPrimitiveBlockface());
        }
        else {
            return false;
        }

        return true;
    }
    
    public void loadSign(CustomSign cs) {        
        if (!signs.contains(cs)) {
            createSign(cs);            
            signs.add(cs);
        }
    }
    
    public void updateSigns() {
        for (CustomSign sign : signs) {
            createSign(sign);
        }
    }

    private boolean isOccupied(CustomSign cs, short jugadores) {
        int filas = cs.getModel().getFilas(jugadores);
        int cols = cs.getModel().getCols();

        Location thisblock = cs.getInfo().getL();

        Location locblock = new Location(thisblock.getWorld(),
                thisblock.getX(), thisblock.getY(), thisblock.getZ());

        // Comprobar por ambos lados
        for (int j = 0; j < filas; j++) {
            for (int k = 0; k < cols; k++) {
                if (cs.getInfo().isXn()) {
                    locblock.setX(locblock.getX() - k);
                } else if (cs.getInfo().isXp()) {
                    locblock.setX(locblock.getX() + k);
                } else if (cs.getInfo().isZn()) {
                    locblock.setZ(locblock.getZ() - k);
                } else if (cs.getInfo().isZp()) {
                    locblock.setZ(locblock.getZ() + k);
                }

                if (!locblock.equals(thisblock) && !locblock.getBlock().isEmpty()) {
                    return true;
                }
            }

            locblock.setX(thisblock.getX());
            locblock.setY(locblock.getY() - 1);
            locblock.setZ(thisblock.getZ());
        }

        return false;
    }

    public void deleteSign(Location l) {
        for (Iterator<CustomSign> it = signs.iterator(); it.hasNext();) {
            CustomSign sign = it.next();
            if (sign.getInfo().getL().equals(l)) {
                short jugadores = (short)pt.cm.getDm().getTopPlayers(
                        sign.getModel().getCantidad(), sign.getInfo().getServer()).size();

                sign.delete(jugadores);

                pt.cm.getDm().borraCartel(sign.getInfo().getL());
                signs.remove(sign);

                break;
            }
        }
    }

    public void deleteSign(Location l, BlockBreakEvent event) {
        for (CustomSign sign : signs) {
            if (sign.getInfo().getL().equals(l)) {
                short jugadores = (short)pt.cm.getDm().getTopPlayers(
                        sign.getModel().getCantidad(), sign.getInfo().getServer()).size();

                Player pl = event.getPlayer();

                // Compruebo permisos
                if (!pl.hasPermission("pvptitles.managesign")) {
                    pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                    event.setCancelled(true);
                    return;
                }

                sign.delete(jugadores);

                pl.sendMessage(PLUGIN + LangFile.SIGN_DELETED.getText(Localizer.getLocale(pl)));

                pt.cm.getDm().borraCartel(sign.getInfo().getL());
                signs.remove(sign);

                break;
            }
        }
    }

    private void createSign(CustomSign cs) {
        ArrayList<PlayerFame> pf = pt.cm.getDm().getTopPlayers(
                cs.getModel().getCantidad(), cs.getInfo().getServer());

        cs.create(pf);
    }

    public List<CustomSign> getSigns() {
        return signs;
    }
}
