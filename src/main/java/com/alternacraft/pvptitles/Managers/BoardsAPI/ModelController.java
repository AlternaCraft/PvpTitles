/*
 * Copyright (C) 2018 AlternaCraft
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
package com.alternacraft.pvptitles.Managers.BoardsAPI;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Params;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.StrUtils;

import java.util.List;

public class ModelController {

    protected Params params = null;
    protected String[][][] table = null;

    public ModelController() {
    }

    private int getRows(Integer[] totalperfila, List<List<List<String>>> params) {
        int total = 0;
        int totalfila = 0;

        for (int i = 0; i < params.size(); i++) {
            List<List<String>> filas = params.get(i);
            for (int j = 0; j < filas.size(); j++) {
                List<String> cols = filas.get(j);
                totalfila = (totalfila >= cols.size()) ? totalfila : cols.size();
            }
            totalperfila[i] = totalfila;
            total += 1;
            totalfila = 0;
        }

        return total;
    }

    private int getCols(List<List<List<String>>> params) {
        int colsperfila = 0;

        for (int i = 0; i < params.size(); i++) {
            List<List<String>> filas = params.get(i);
            colsperfila = (filas.size() > colsperfila) ? filas.size() : colsperfila;
        }

        return colsperfila;
    }

    /**
     * Método para redistribuir los valores del scoreboard en forma de tabla
     *
     * @param params Datos procesados del fichero
     */
    public void preprocessUnit(List<List<List<String>>> params) {
        Integer[] totalperfila = new Integer[params.size()];
        int colsperfila = getCols(params);

        table = new String[getRows(totalperfila, params)][][];

        for (int i = 0; i < totalperfila.length; i++) {
            table[i] = new String[totalperfila[i]][colsperfila];
        }

        for (int i = 0; i < params.size(); i++) {
            List<List<String>> filas = params.get(i);
            for (int j = 0; j < filas.size(); j++) {
                List<String> cols = filas.get(j);
                for (int k = 0; k < cols.size(); k++) {
                    String param = cols.get(k);
                    table[i][k][j] = StrUtils.translateColors(param);
                }
            }
        }
    }

    /**
     * Método para establecer el valor de las variables en la tabla
     *
     * @param pf Lista de jugadores disponibles a mostrar
     * @param nfilas Numero de filas de jugadores a mostrar
     * @param divisor Cantidad de jugadores por bloque (Min 1)
     * @param progresivo Mismos datos en diferentes columnas
     *
     * @return Tabla rellena
     */
    public String[][][] processUnit(List<PlayerFame> pf, int nfilas,
            int divisor, boolean progresivo) {
        String[][][] resul = new String[this.table.length][][];

        // Filas de titulos
        for (int i = 0; i < this.table.length - 1; i++) {
            String[][] filas = this.table[i];
            resul[i] = new String[filas.length][];

            for (int j = 0; j < filas.length; j++) {
                String[] cols = filas[j];
                resul[i][j] = new String[cols.length];

                for (int k = 0; k < cols.length; k++) {
                    String str = cols[k];
                    resul[i][j][k] = str;
                }
            }
        }

        // Filas de valores
        String[] cols = this.table[table.length - 1][0];

        this.params = new Params();
        this.params.setDivisor(divisor);
        this.params.setProgresivo(progresivo);
        this.params.setNcols(cols.length);

        int filasReales = nfilas;
        // Calculo las filas de mas si el board los divide por bloques
        if (progresivo && divisor > 1) {
            if (divisor > filasReales) {
                filasReales = divisor;
            } else {
                int extra = (nfilas % divisor == 0) ? ((int) (nfilas / divisor)) - 1 : (int) (nfilas / divisor);
                filasReales = divisor + divisor * extra;
            }
        }

        resul[resul.length - 1] = new String[filasReales][];

        for (int i = 0; i < filasReales; i++) {
            resul[resul.length - 1][i] = new String[cols.length];

            for (int j = 0; j < cols.length; j++) {
                String param = processParam(pf, cols[j], j);
                resul[resul.length - 1][i][j] = param;
            }
        }

        return resul;
    }

    /**
     * Método para procesar un parámetro
     *
     * @param pf Lista de jugadores
     * @param str String con el parámetro
     * @param col Entero con la columna
     *
     * @return String con el parámetro procesado
     */
    protected String processParam(List<PlayerFame> pf, String str, int col) {
        if (str == null) {
            return str; // Optimizacion
        }

        String temp = str;

        for (Params.Vars arg : Params.Vars.values()) {
            if (str.toUpperCase().contains("<" + arg.name() + ">")) {
                int value = params.getNext(arg, col);

                // Esa columna esta completa
                if (value >= pf.size()) {
                    params.addOne(arg, col);
                    continue;
                }

                switch (arg) {
                    case PLAYER:
                        str = str.replace("<player>", pf.get(value).getName());
                        break;
                    case RANK:
                        String rank = "";
                        try {
                            rank = RankManager.getRank(pf.get(value).getFame(), 
                                    pf.get(value).getSeconds(), 
                                    pf.get(value).getPlayer()).getDisplay();
                            str = str.replace("<rank>", rank);
                        } catch (RanksException ex) {
                            CustomLogger.logArrayError(ex.getCustomStackTrace());
                        }
                        break;
                    case FAME:
                        str = str.replace("<fame>", String.valueOf(pf.get(value).getFame()));
                        break;
                    case POS:
                        str = str.replace("<pos>", String.valueOf(value + 1));
                        break;
                    case SERVER:
                        try {
                            str = str.replace("<server>", pf.get(value).getServerName());
                        } catch (DBException ex) {
                            CustomLogger.logError(ex.getMessage());
                        }
                        break;
                    case WORLD:
                        str = str.replace("<world>", pf.get(value).getWorld());
                }

                params.addOne(arg, col);
            }
        }

        return (str.equals(temp) ? null : str); // Estilo
    }

    public String[][][] getTable() {
        return table;
    }
}
