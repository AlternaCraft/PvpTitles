package es.jlh.pvptitles.Objects.LBSigns;

import es.jlh.pvptitles.Misc.Utils;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

/**
 *
 * @author AlternaCraft
 */
public class CustomSign {

    private String[] lineas = null;
    private org.bukkit.material.Sign matSign = null;

    private LBData info = null;
    private LBModel model = null;
    private LBArgs args = null;

    public CustomSign(LBData info, LBModel model) {
        this.info = info;
        this.model = model;
        this.args = new LBArgs();
    }

    public void setLineas(String[] lineas) {
        this.lineas = lineas;
    }

    public void setMatSign(org.bukkit.material.Sign matSign) {
        this.matSign = matSign;
    }

    public LBData getInfo() {
        return info;
    }

    public LBModel getModel() {
        return model;
    }

    public LBArgs getArgs() {
        return args;
    }

    public void create(ArrayList<PlayerFame> pf) {
        Location base = this.info.getL();
        Location locSign = new Location(base.getWorld(),
                base.getX(), base.getY(), base.getZ());

        ArrayList<ArrayList<ArrayList<String>>> params = this.getModel().getParams();
        // Recorro cada fila
        for (int j = 0; j < params.size(); j++) {
            ArrayList<ArrayList<String>> smf = params.get(j);

            // Recorro cada columna de la fila
            for (int k = 0; k < smf.size(); k++) {
                ArrayList<String> smc = smf.get(k);

                // Si detras no hay bloque lo creo
                checkBehind(locSign);

                // Creo el cartel
                Block blockToChange = locSign.getBlock();
                blockToChange.setType(Material.WALL_SIGN);

                // Oriento el cartel
                Sign newSign = (Sign) blockToChange.getState();
                newSign.setData(matSign);

                // Limpio las lineas
                cleanLines(newSign);

                // Recorro cada fila del cartel
                for (int l = 0; l < smc.size(); l++) {
                    String smfc = smc.get(l);
                    
                    if (smfc.compareToIgnoreCase("<main>") == 0) {
                        if (lineas.length == 0) {
                            newSign.setLine(0, "PvPTitles");
                            newSign.setLine(1, "---------");
                            newSign.setLine(2, "# TOP " + this.model.getCantidad() + " #");
                            newSign.setLine(3, "---------");
                        } else // Primera fila caso especial
                        {
                            if (j == 0 && k == 0) {
                                lineas[0] = "PvPTitles";
                                lineas[1] = "---------";
                                lineas[2] = "# TOP " + this.model.getCantidad() + " #";
                                lineas[3] = "---------";
                            } else {
                                newSign.setLine(0, "PvPTitles");
                                newSign.setLine(1, "---------");
                                newSign.setLine(2, "# TOP " + this.model.getCantidad() + " #");
                                newSign.setLine(3, "---------");
                            }
                        }
                    } else if (args.containSomeArg(smfc)) {
                        this.args.checkArgs(newSign, pf, smfc, this.getModel().isProgresivo());
                    } else {
                        // Primera fila caso especial
                        if (lineas.length > 0 && j == 0 && k == 0) {
                            lineas[l] = Utils.TranslateColor(smfc);
                        } else {
                            newSign.setLine(l, Utils.TranslateColor(smfc));
                        }
                    }
                }

                // Modulo para evitar crear carteles vacios
                boolean vacio = true;

                if (j != 0 && k != 0) {
                    for (String nsl : newSign.getLines()) {
                        if (!nsl.isEmpty()) {
                            vacio = false;
                            break;
                        }
                    }
                } else {
                    vacio = false;
                }

                if (vacio) {
                    blockToChange.setType(Material.AIR);
                } else {
                    newSign.update(); // Para que lo escriba                            
                }
                // Fin de modulo

                // Modifico la posicion para el proximo cartel
                if (info.isXp()) {
                    locSign.setX(locSign.getX() + 1);
                } else if (info.isXn()) {
                    locSign.setX(locSign.getX() - 1);
                } else if (info.isZp()) {
                    locSign.setZ(locSign.getZ() + 1);
                } else {
                    locSign.setZ(locSign.getZ() - 1);
                }
            }

            // Bajo 1 la altura
            locSign.setY(locSign.getY() - 1);

            // Posicion inicial en los ejes X y Z
            locSign.setX(base.getX());
            locSign.setZ(base.getZ());

            if (this.args.checkValues(pf)) {
                j--;
            } else {
                this.args.resetValues();
            }

            this.args.resetBooleans();
        }
    }

    public void delete(short jugadores) {
        int filas = model.getFilas(jugadores);
        int cols = model.getCols();

        Location locblock = new Location(info.getL().getWorld(),
                info.getL().getX(), info.getL().getY(), info.getL().getZ());

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < cols; j++) {
                Block block = null;

                if (info.isXn()) {
                    block = new Location(locblock.getWorld(), locblock.getX() - j,
                            locblock.getY(), locblock.getZ()).getBlock();
                } else if (info.isXp()) {
                    block = new Location(locblock.getWorld(), locblock.getX() + j,
                            locblock.getY(), locblock.getZ()).getBlock();
                } else if (info.isZn()) {
                    block = new Location(locblock.getWorld(), locblock.getX(),
                            locblock.getY(), locblock.getZ() - j).getBlock();
                } else if (info.isZp()) {
                    block = new Location(locblock.getWorld(), locblock.getX(),
                            locblock.getY(), locblock.getZ() + j).getBlock();
                }

                if (block.getType() == Material.WALL_SIGN) {
                    block.setType(Material.AIR);
                }
            }

            locblock.setY(locblock.getY() - 1);
        }
    }

    private void checkBehind(Location locSign) {
        // Compruebo bloque de detras
        Location behindBlock = new Location(locSign.getWorld(), 
                locSign.getX(), locSign.getY(), locSign.getZ());

        // Según hacia donde mire cojo un bloque u otro
        if (info.isXp() || info.isXn()) {
            if (info.getBlockface().equals(BlockFace.NORTH)) {
                behindBlock.setZ(behindBlock.getZ() + 1);
            } else {
                behindBlock.setZ(behindBlock.getZ() - 1);
            }
        } else if (info.getBlockface().equals(BlockFace.EAST)) {
            behindBlock.setX(behindBlock.getX() - 1);
        } else {
            behindBlock.setX(behindBlock.getX() + 1);
        }

        Block block = behindBlock.getBlock();

        if (block.isEmpty() || block.isLiquid()) {
            block.setType(Material.STONE);
        }
    }

    private void cleanLines(Sign sign) {
        // limpio el contenido del cartel (Por si esta modificando)
        for (int i = 0; i < sign.getLines().length; i++) {
            sign.setLine(i, "");
        }
    }
}
