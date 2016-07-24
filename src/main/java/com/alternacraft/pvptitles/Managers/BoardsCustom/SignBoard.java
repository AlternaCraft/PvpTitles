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
package com.alternacraft.pvptitles.Managers.BoardsCustom;

import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardData;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import com.alternacraft.pvptitles.Managers.BoardsAPI.ModelController;
import com.alternacraft.pvptitles.Misc.CustomLocation;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.StrUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

public class SignBoard extends Board {

    private static final short SIGN_ROWS = 4;

    private String[] lineas = null;
    private org.bukkit.material.Sign matSign = null;

    public SignBoard(BoardData info, BoardModel bm, ModelController model) {
        super(info, bm, model);
    }

    /**
     * Método para establecer los valores de las bloque del cartel
     *
     * @param lineas String[]
     */
    public void setLineas(String[] lineas) {
        this.lineas = lineas;
    }

    /**
     * Método para establecer las propiedades del cartel
     *
     * @param matSign org.bukkit.material.Sign
     */
    public void setMatSign(org.bukkit.material.Sign matSign) {
        this.matSign = matSign;
    }

    // <editor-fold defaultstate="collapsed" desc="LEGACY METHODS">
    @Override
    public boolean isMaterializable(short jugadores) {
        int jug = this.getModel().getFilasJugadores(jugadores);
        jug = StrUtils.dividirEntero(jug, SIGN_ROWS); // Para carteles
        
        int filas = this.getModel().getFilasSinJugadores(SIGN_ROWS) + jug;
        int cols = getModel().getColumnas();

        Location thisblock = getData().getLocation();

        Location locblock = new CustomLocation(thisblock.getWorld(),
                thisblock.getX(), thisblock.getY(), thisblock.getZ());

        // Comprobar por ambos lados
        for (int j = 0; j < filas; j++) {
            for (int k = 0; k < cols; k++) {
                if (getData().isXn()) {
                    locblock.setX(locblock.getX() - k);
                } else if (getData().isXp()) {
                    locblock.setX(locblock.getX() + k);
                } else if (getData().isZn()) {
                    locblock.setZ(locblock.getZ() - k);
                } else if (getData().isZp()) {
                    locblock.setZ(locblock.getZ() + k);
                }
                
                if (!locblock.equals(thisblock) && !locblock.getBlock().isEmpty()) {
                    return false;
                }
            }

            locblock.setX(thisblock.getX());
            locblock.setY(locblock.getY() - 1);
            locblock.setZ(thisblock.getZ());
        }

        return true;
    }

    @Override
    public void materialize(List<PlayerFame> pf) {
        Location base = this.getData().getLocation();
        Location locSign = new Location(base.getWorld(),
                base.getX(), base.getY(), base.getZ());

        int fjugadores = this.model.getFilasJugadores(pf.size());

        // Convierto el resultado en formato carteles
        List<String[][][]> resul = convertidor((String[][][]) this.modelController.processUnit(pf,
                fjugadores, SIGN_ROWS, this.getModel().isProgresivo()));

        for (int i = 0; i < resul.size(); i++) {
            String[][][] bloque = resul.get(i);

            for (int j = 0; j < bloque.length; j++) {
                String[][] fila = bloque[j];

                for (int k = 0; k < fila.length; k++) {
                    String[] col = fila[k];

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

                    for (int l = 0; l < col.length; l++) {
                        String v = (col[l] == null) ? "":col[l];
                        if (i == 0 && j == 0 && k == 0) { // Caso main
                            if (col[0].contains("<main>")) {
                                if (lineas != null && lineas.length > 0) {
                                    lineas[0] = "PvPTitles";
                                    lineas[1] = "---------";
                                    lineas[2] = "# TOP " + this.getModel().getCantidad() + " #";
                                    lineas[3] = "---------";
                                } else {
                                    newSign.setLine(0, "PvPTitles");
                                    newSign.setLine(1, "---------");
                                    newSign.setLine(2, "# TOP " + this.getModel().getCantidad() + " #");
                                    newSign.setLine(3, "---------");
                                }
                                break;
                            } else {
                                if (lineas != null && lineas.length > 0) {
                                    lineas[l] = v; // Evitar problemas con el primer cartel
                                }
                                else {
                                    newSign.setLine(l, v);
                                }
                            }
                        } else {
                            newSign.setLine(l, v);
                        }
                    }

                    // Modulo para evitar crear carteles vacios
                    boolean vacio = true;

                    if (i != 0 || j != 0 || k != 0) {
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

                    // ################### COLUMNA ###################
                    // Modifico la posicion para el proximo cartel
                    if (getData().isXp()) {
                        locSign.setX(locSign.getX() + 1);
                    } else if (getData().isXn()) {
                        locSign.setX(locSign.getX() - 1);
                    } else if (getData().isZp()) {
                        locSign.setZ(locSign.getZ() + 1);
                    } else {
                        locSign.setZ(locSign.getZ() - 1);
                    }
                    // ################### COLUMNA ###################
                }

                // ################### FILA ###################                
                // Bajo 1 la altura
                locSign.setY(locSign.getY() - 1);

                // Posicion inicial en los ejes X y Z
                locSign.setX(base.getX());
                locSign.setZ(base.getZ());
                // ################### FILA ###################                
            }
        }

        this.setLineas(null); // Evito problema con el cartel principal
    }

    @Override
    public void dematerialize(short jugadores) {
        int jug = this.getModel().getFilasJugadores(jugadores);
        jug = StrUtils.dividirEntero(jug, SIGN_ROWS);
        
        int filas = this.getModel().getFilasSinJugadores(SIGN_ROWS) + jug;
        int cols = getModel().getColumnas();

        Location locblock = new Location(getData().getLocation().getWorld(),
                getData().getLocation().getX(), getData().getLocation().getY(), getData().getLocation().getZ());

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < cols; j++) {
                Block block = null;

                if (getData().isXn()) {
                    block = new Location(locblock.getWorld(), locblock.getX() - j,
                            locblock.getY(), locblock.getZ()).getBlock();
                } else if (getData().isXp()) {
                    block = new Location(locblock.getWorld(), locblock.getX() + j,
                            locblock.getY(), locblock.getZ()).getBlock();
                } else if (getData().isZn()) {
                    block = new Location(locblock.getWorld(), locblock.getX(),
                            locblock.getY(), locblock.getZ() - j).getBlock();
                } else if (getData().isZp()) {
                    block = new Location(locblock.getWorld(), locblock.getX(),
                            locblock.getY(), locblock.getZ() + j).getBlock();
                }

                if (block != null && block.getType() == Material.WALL_SIGN) {
                    block.setType(Material.AIR);
                }
            }

            locblock.setY(locblock.getY() - 1);
        }
    }

    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="CUSTOM METHODS">
    // Custom methods
    private void checkBehind(Location locSign) {
        // Compruebo bloque de detras
        Location behindBlock = new Location(locSign.getWorld(),
                locSign.getX(), locSign.getY(), locSign.getZ());

        // Según hacia donde mire cojo un bloque u otro
        if (getData().isXp() || getData().isXn()) {
            if (getData().getBlockface().equals(BlockFace.NORTH)) {
                behindBlock.setZ(behindBlock.getZ() + 1);
            } else {
                behindBlock.setZ(behindBlock.getZ() - 1);
            }
        } else if (getData().getBlockface().equals(BlockFace.EAST)) {
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

    /**
     * Método para devolver la matriz con una estructura de carteles
     *
     * @param in Tabla vieja
     * @param columns Columnas de la tabla
     *
     * @return Tabla nueva
     */
    private List<String[][][]> convertidor(String[][][] in) {
        List<String[][][]> signs = new ArrayList();

        int columns = this.model.getColumnas();

        for (int i = 0; i < in.length; i++) {
            String[][] rows = in[i];
            signs.add(i, new String[StrUtils.dividirEntero(rows.length, SIGN_ROWS)][columns][SIGN_ROWS]);

            int k = 0;
            while (k < columns) {
                boolean rep = false;
                int fila = -1;
                int antj = 0;

                do {
                    String[] values = new String[SIGN_ROWS];

                    rep = false;
                    fila++;

                    for (int j = antj; j < rows.length; j++) {
                        if (j - antj > 3) { // Siguiente cartel
                            rep = true;
                            antj = j; // Guardo la ultima fila recorrida
                            break;
                        }

                        String[] cols = rows[j];
                        values[j - antj] = cols[k];
                    }

                    int l = 0;
                    for (String value : values) {
                        signs.get(i)[fila][k][l] = (value == null) ? "" : value;
                        l++;
                    }
                } while (rep);

                k++;
            }
        }

        return signs;
    }

    // </editor-fold>    
    // <editor-fold defaultstate="collapsed" desc="GETTERS">
    @Override
    public SignBoardData getData() {
        return (SignBoardData) this.info;
    }

    @Override
    public BoardModel getModel() {
        return this.model;
    }

    @Override
    public ModelController getModelController() {
        return this.modelController;
    }
    // </editor-fold>

}
