package es.jlh.pvptitles.Managers.LeaderBoards;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

    public void addSign(CustomSign cs, Player pl) {
        if (!signs.contains(cs)) {
            signs.add(cs);

            boolean ocupado = isOccupied(cs, cs.getInfo().getServer(), pl);

            if (ocupado) {
                Location locSign = cs.getInfo().getL();

                PvpTitles.logger.log(Level.WARNING, "Sign per scoreboard at [{0}, {1}, {2}" + "] "
                        + "is blocking a sign update. Please, delete it.", new Object[]{locSign.getBlockX(), locSign.getBlockY(), locSign.getBlockZ()});

                return;
            }

            createSign(cs);
        }
    }

    private boolean isOccupied(CustomSign cs, String server, Player pl) {
        // Compruebo si ya hay algo ocupando el sitio            
        ArrayList<PlayerFame> pf = this.pt.cm.getDm().getTopPlayers(cs.getModel().getCantidad(), server);
        int jugadores = pf.size();
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
                    if (pl != null) {
                        pl.sendMessage(PLUGIN + LangFile.SIGN_CANT_BE_PLACED.getText(Localizer.getLocale(pl))
                            + " (" + filas + "x" + cols + ")");
                    }
                    
                    return true;
                }
            }

            locblock.setX(thisblock.getX());
            locblock.setY(locblock.getY() - 1);
            locblock.setZ(thisblock.getZ());
        }

        return false;
    }

    private void createSign(CustomSign cs) {          
        cs.createBlock();
    }
}
