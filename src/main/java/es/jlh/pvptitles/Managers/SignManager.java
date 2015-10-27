package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Objects.LBData;
import es.jlh.pvptitles.Objects.LBModel;
import es.jlh.pvptitles.Objects.PlayerFame;
import java.util.ArrayList;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author julito
 */
public class SignManager {

    private static int pos = 0;
    private static int jug = 0;
    private static int fama = 0;
    private static int rango = 0;
    private static int servr = 0;
    private static int world = 0;

    private static boolean alr_pos = false;
    private static boolean alr_jug = false;
    private static boolean alr_fama = false;
    private static boolean alr_rango = false;
    private static boolean alr_server = false;
    private static boolean alr_world = false;

    /**
     * Método para definir un scoreboard
     *
     * @param dm DatabaseManager
     * @param sm LBModel
     * @param locSign Location
     * @param usaXP Boolean
     * @param usaXN Boolean
     * @param usaZP Boolean
     * @param bf BlockFace
     * @param matSign Sign
     * @param lineas String[]
     * @param base Location
     * @param server String
     */
    public static void definirSB(Manager dm, LBModel sm, Location locSign,
            boolean usaXP, boolean usaXN, boolean usaZP, BlockFace bf,
            org.bukkit.material.Sign matSign, String[] lineas, Location base,
            String server) {
        // Recojo los parametros del modelo
        ArrayList<ArrayList<ArrayList<String>>> params = sm.getParams();

        ArrayList<PlayerFame> pf = dm.getDm().getTopPlayers(sm.getCantidad(), server);

        // Recorro cada fila
        for (int j = 0; j < params.size(); j++) {
            ArrayList<ArrayList<String>> smf = params.get(j);

            // Recorro cada columna de la fila
            for (int k = 0; k < smf.size(); k++) {
                Block blockToChange = null;

                // Compruebo bloque de detras
                Location behindBlock = new Location(
                        locSign.getWorld(), locSign.getX(), locSign.getY(), locSign.getZ()
                );

                // Según hacia donde mire cojo un bloque u otro
                if (usaXP || usaXN) {
                    if (bf.equals(BlockFace.NORTH)) {
                        behindBlock.setZ(behindBlock.getZ() + 1);
                    } else {
                        behindBlock.setZ(behindBlock.getZ() - 1);
                    }
                } else {
                    if (bf.equals(BlockFace.EAST)) {
                        behindBlock.setX(behindBlock.getX() - 1);
                    } else {
                        behindBlock.setX(behindBlock.getX() + 1);
                    }
                }

                Block block = behindBlock.getBlock();

                if (block.isEmpty() || block.isLiquid()) {
                    block.setType(Material.STONE);
                }

                // Modulo detector
                ArrayList<LBData> lbd = dm.getDm().buscaCarteles();

                for (LBData lbd1 : lbd) {
                    if (!lbd1.getL().equals(base) && lbd1.getL().equals(locSign)) {
                        PvpTitles.logger.log(Level.WARNING, "Sign per scoreboard at [{0}, {1}, {2}" + "] "
                                + "is blocking a sign update. Please, delete it.", new Object[]{locSign.getBlockX(), locSign.getBlockY(), locSign.getBlockZ()});

                        continue;
                    }
                }

                // Creo el cartel
                blockToChange = locSign.getBlock();
                blockToChange.setType(Material.WALL_SIGN);

                Sign newSign = (Sign) blockToChange.getState();

                // limpio el contenido del cartel (Por si esta modificando)
                for (int i = 0; i < newSign.getLines().length; i++) {
                    newSign.setLine(i, "");
                }

                // Situo el cartel segun la posicion del jugador                                
                newSign.setData(matSign);

                ArrayList<String> smc = smf.get(k);

                // Recorro cada campo de la columna
                for (int l = 0; l < smc.size(); l++) {
                    String smfc = smc.get(l);

                    if (lineas.length == 0) {
                        cartelSinEvento(smfc, l, newSign, pf, sm);
                    } else {
                        cartelConEvento(smfc, lineas, j, k, l, newSign, pf, sm);
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
                // Fin de moulo

                // Modifico la posicion para el proximo cartel
                if (usaXP || usaXN) {
                    if (usaXP) {
                        locSign.setX(locSign.getX() + 1);
                    } else {
                        locSign.setX(locSign.getX() - 1);
                    }
                } else {
                    if (usaZP) {
                        locSign.setZ(locSign.getZ() + 1);
                    } else {
                        locSign.setZ(locSign.getZ() - 1);
                    }
                }
            }

            // Bajo 1 la altura
            locSign.setY(locSign.getY() - 1);

            // Posicion inicial en los ejes X y Z
            locSign.setX(base.getX());
            locSign.setZ(base.getZ());

            // Duplico la ultima linea
            if (jug > 0 && pf.size() > jug) {
                j--;
            } else {
                // restauro los valores a cero por si se repite la var
                pos = 0;
                jug = 0;
                fama = 0;
                rango = 0;
                servr = 0;
                world = 0;
            }

            alr_pos = false;
            alr_jug = false;
            alr_fama = false;
            alr_rango = false;
            alr_server = false;
            alr_world = false;
        }
    }

    /**
     * Método para modificar un cartel editado con el evento
     *
     * @param smfc String
     * @param lineas String[]
     * @param j Entero
     * @param k Entero
     * @param l Entero
     * @param newSign Sign
     * @param pf ArrayList
     * @param sm LBModel
     */
    private static void cartelConEvento(String smfc, String[] lineas, int j, int k, int l,
            Sign newSign, ArrayList<PlayerFame> pf, LBModel sm) {

        // Caso especial dado que cuando termine de ejecutarse el evento el 
        // cartel original remplazará a otro creado
        if (smfc.compareToIgnoreCase("<main>") == 0) {
            // Primera fila caso especial
            if (j == 0 && k == 0) {
                lineas[0] = "PvPTitles";
                lineas[1] = "---------";
                lineas[2] = "# TOP " + sm.getCantidad() + " #";
                lineas[3] = "---------";
            } else {
                newSign.setLine(0, "PvPTitles");
                newSign.setLine(1, "---------");
                newSign.setLine(2, "# TOP " + sm.getCantidad() + " #");
                newSign.setLine(3, "---------");
            }
        } else if (smfc.contains("<player>")
                || smfc.contains("<fame>")
                || smfc.contains("<pos>")
                || smfc.contains("<rank>")
                || smfc.contains("<server>")
                || smfc.contains("<world>")) {
            compArg(pf, smfc, newSign, sm);
        } else {
            // Primera fila caso especial
            if (j == 0 && k == 0) {
                lineas[l] = ChatColor.translateAlternateColorCodes('&', smfc);
            } else {
                newSign.setLine(l, ChatColor.translateAlternateColorCodes('&', smfc));
            }
        }
    }

    /**
     * Método para modificar un cartel editado sin el evento
     *
     * @param smfc String
     * @param l Entero
     * @param newSign Sign
     * @param pf ArrayList
     * @param sm LBModel
     */
    private static void cartelSinEvento(String smfc, int l,
            Sign newSign, ArrayList<PlayerFame> pf, LBModel sm) {

        if (smfc.compareToIgnoreCase("<main>") == 0) {
            newSign.setLine(0, "PvPTitles");
            newSign.setLine(1, "---------");
            newSign.setLine(2, "# TOP " + sm.getCantidad() + " #");
            newSign.setLine(3, "---------");
        } else if (smfc.contains("<player>")
                || smfc.contains("<fame>")
                || smfc.contains("<pos>")
                || smfc.contains("<rank>")
                || smfc.contains("<server>")
                || smfc.contains("<world>")) {
            compArg(pf, smfc, newSign, sm);
        } else {
            newSign.setLine(l, ChatColor.translateAlternateColorCodes('&', smfc));
        }
    }

    /**
     * Método para modificar las lineas del cartel segun la variable que sea
     *
     * @param pf ArrayList con los jugadores en el ranking
     * @param smfc String con el contenido del cartel
     * @param newSign Sign a crear
     */
    private static void compArg(ArrayList<PlayerFame> pf, String smfc, Sign newSign, LBModel sm) {
        boolean entro = false;

        // Colores para todos los datos        
        smfc = ChatColor.translateAlternateColorCodes('&', smfc);

        if (smfc.contains("<player>")) {
            jug = (alr_jug && !sm.isProgresivo()) ? (jug - 4) : jug;
            int temp = jug;
            jug += 4;

            int cont = 0;
            while (temp < jug && temp < pf.size()) {
                newSign.setLine(cont, smfc.replace("<player>", pf.get(temp).getName()));
                temp++;
                cont++;
            }

            entro = true;
            alr_jug = true;
        }

        if (smfc.contains("<rank>")) {
            rango = (alr_rango && !sm.isProgresivo()) ? (rango - 4) : rango;
            int temp = rango;
            rango += 4;

            int cont = 0;
            while (temp < rango && temp < pf.size()) {
                if (entro) {
                    newSign.setLine(cont, newSign.getLine(cont).replace("<rank>",
                            RankManager.GetRank(pf.get(temp).getFame(), pf.get(temp).getSeconds())));
                } else {
                    newSign.setLine(cont, smfc.replace("<rank>",
                            RankManager.GetRank(pf.get(temp).getFame(), pf.get(temp).getSeconds())));
                }
                temp++;
                cont++;
            }

            entro = true;
            alr_rango = true;
        }

        if (smfc.contains("<fame>")) {
            fama = (alr_fama && !sm.isProgresivo()) ? (fama - 4) : fama;
            int temp = fama;
            fama += 4;

            int cont = 0;
            while (temp < fama && temp < pf.size()) {
                if (entro) {
                    newSign.setLine(cont, newSign.getLine(cont).replace("<fame>", String.valueOf(pf.get(temp).getFame())));
                } else {
                    newSign.setLine(cont, smfc.replace("<fame>", String.valueOf(pf.get(temp).getFame())));
                }
                temp++;
                cont++;
            }

            entro = true;
            alr_fama = true;
        }

        if (smfc.contains("<pos>")) {
            pos = (alr_pos && !sm.isProgresivo()) ? (pos - 4) : pos;
            int temp = pos;
            pos += 4; // Evito problemas posteriores

            int cont = 0;
            while (temp < pos && temp < pf.size()) {
                if (entro) {
                    newSign.setLine(cont, newSign.getLine(cont).replace("<pos>", String.valueOf(temp + 1)));
                } else {
                    newSign.setLine(cont, smfc.replace("<pos>", String.valueOf(temp + 1)));
                }

                temp++;
                cont++;
            }

            entro = true;
            alr_pos = true;
        }

        if (smfc.contains("<server>")) {
            servr = (alr_server && !sm.isProgresivo()) ? (servr - 4) : servr;
            int temp = servr;
            servr += 4; // Evito problemas posteriores

            int cont = 0;
            while (temp < servr && temp < pf.size()) {
                if (entro) {
                    newSign.setLine(cont, newSign.getLine(cont).replace("<server>", pf.get(temp).getServerName()));
                } else {
                    newSign.setLine(cont, smfc.replace("<server>", pf.get(temp).getServerName()));
                }

                temp++;
                cont++;
            }

            entro = true;
            alr_server = true;
        }

        if (smfc.contains("<world>")) {
            world = (alr_world && !sm.isProgresivo()) ? (world - 4) : world;
            int temp = world;
            world += 4; // Evito problemas posteriores

            int cont = 0;
            while (temp < world && temp < pf.size()) {
                if (entro) {
                    newSign.setLine(cont, newSign.getLine(cont).replace("<world>", pf.get(temp).getWorld()));
                } else {
                    newSign.setLine(cont, smfc.replace("<world>", pf.get(temp).getWorld()));
                }

                temp++;
                cont++;
            }

            alr_world = true;
        }
    }

    /**
     * Método para borrar un scoreboard con permisos
     *
     * @param event BlockBreakEvent
     * @param dm DatabaseManager
     * @param sd ArrayList
     * @param locblock Location
     */
    public static void borrarSBEvento(BlockBreakEvent event, Manager dm, ArrayList<LBData> sd,
            Location locblock) {
        Player pl = event.getPlayer();

        for (LBData sdi : sd) {
            if (locblock.equals(sdi.getL())) {
                // Compruebo permisos
                if (!pl.hasPermission("pvptitles.managesign")) {
                    pl.sendMessage(PLUGIN + LangFile.COMMAND_NO_PERMISSIONS.getText(Localizer.getLocale(pl)));
                    event.setCancelled(true);
                    return;
                }

                tareasBorrarSB(sdi, dm, locblock);

                pl.sendMessage(PLUGIN + LangFile.SIGN_DELETED.getText(Localizer.getLocale(pl)));

                break;
            }
        }
    }

    /**
     * Método para borrar un scoreboard sin necesidad de permisos
     *
     * @param dm DatabaseManager
     * @param sd ArrayList
     * @param locblock Location
     */
    public static void borrarSBManual(Manager dm, ArrayList<LBData> sd, Location locblock) {
        for (LBData sdi : sd) {
            if (locblock.equals(sdi.getL())) {
                tareasBorrarSB(sdi, dm, locblock);
                break;
            }
        }
    }

    /**
     * Método con las funciones de borrado
     *
     * @param sdi LBData
     * @param dm DatabaseManager
     * @param sd ArrayList
     * @param locblock
     */
    private static void tareasBorrarSB(LBData sdi, Manager dm, Location locblock) {
        LBModel sm = dm.searchModel(sdi.getModelo());

        if (sm == null) {
            return;
        }

        int jugadores = dm.getDm().getTopPlayers(sm.getCantidad(), sdi.getServer()).size();
        int filas = sm.getFilas(jugadores);
        int cols = sm.getCols();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < cols; j++) {
                Block block = null;

                if (sdi.isXn()) {
                    block = new Location(locblock.getWorld(), locblock.getX() - j,
                            locblock.getY(), locblock.getZ()).getBlock();
                } else if (sdi.isXp()) {
                    block = new Location(locblock.getWorld(), locblock.getX() + j,
                            locblock.getY(), locblock.getZ()).getBlock();
                } else if (sdi.isZn()) {
                    block = new Location(locblock.getWorld(), locblock.getX(),
                            locblock.getY(), locblock.getZ() - j).getBlock();
                } else if (sdi.isZp()) {
                    block = new Location(locblock.getWorld(), locblock.getX(),
                            locblock.getY(), locblock.getZ() + j).getBlock();
                }

                if (block.getType() == Material.WALL_SIGN) {
                    block.setType(Material.AIR);
                }
            }

            locblock.setY(locblock.getY() - 1);
        }

        dm.getDm().borraCartel(sdi.getL());
    }
}
