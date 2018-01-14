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

import com.alternacraft.pvptitles.Misc.StrUtils;
import static com.alternacraft.pvptitles.Misc.StrUtils.removeColors;
import java.util.ArrayList;
import java.util.List;

public class BoardModel {

    protected List<List<List<String>>> params = null;

    private String nombre = null;
    private short cantidad = 0;
    private short columnas = 0;

    protected boolean progresivo = false;

    public BoardModel(String n, short c, ArrayList formato) {
        this.nombre = n;
        this.cantidad = c;
        this.params = formato;

        this.setColumnas();
        this.setProgresivo();
    }

    public final void setColumnas() {
        short ant = 0;

        for (List<List<String>> param : params) {
            if (param.size() > ant) {
                ant = (short) param.size();
            }
        }

        this.columnas = ant;
    }

    public final void setProgresivo() {
        this.progresivo = false;

        for (List<List<String>> param : params) {
            ArrayList<String> filas = new ArrayList();
            param
                    .stream()
                    .map(param1 -> {
                        return param1
                                .stream()
                                .map(param11 -> {
                                    StringBuilder buf = new StringBuilder();
                                    int var1 = param11.indexOf('<');
                                    int var2 = param11.indexOf('>');
                                    while (var1 != -1 && var2 != -1) {
                                        buf.append(param11.substring(var1 + 1, var2));
                                        param11 = param11.substring(var2 + 1);
                                        var1 = param11.indexOf('<');
                                        var2 = param11.indexOf('>');
                                    }
                                    return buf.toString();
                                })
                                .reduce("", String::concat);
                    })
                    .forEachOrdered(concatena -> filas.add(String.valueOf(concatena)));

            if (filas.size() > 1) {
                for (int i = 0; i < filas.size() - 1; i++) {
                    String pick = removeColors(filas.get(i));
                    if (pick.equals("")) {
                        continue;
                    }

                    for (int j = 0; j < filas.size(); j++) {
                        if (j == i) {
                            continue;
                        }

                        String actual = removeColors(filas.get(j));

                        if (pick.equalsIgnoreCase(actual)) {
                            this.progresivo = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    public String getNombre() {
        return nombre;
    }

    public short getCantidad() {
        return cantidad;
    }

    public List<List<List<String>>> getParams() {
        return params;
    }

    public short getColumnas() {
        return columnas;
    }

    /**
     * Método para recibir las filas por el bloque
     *
     * @return Integer[]
     */
    private Integer[] getFilasPerBloque() {
        int total = this.params.size();
        Integer[] values = new Integer[total - 1];

        List<List<String>> v;
        int i = 0;

        while (i < total - 1) {
            int ant = 0;
            v = this.params.get(i);

            for (List<String> arrayList : v) {
                if (ant < arrayList.size()) {
                    ant = arrayList.size();
                }
            }

            values[i] = ant;
            i++;
        }

        return values;
    }

    /**
     * Método para recibir las filas de títulos
     *
     * @param divisor Entero con el divisor
     *
     * @return Entero con el número de filas
     */
    public int getFilasSinJugadores(int divisor) {
        Integer[] values = getFilasPerBloque();
        int filas = 0;

        for (Integer value : values) {
            filas += StrUtils.dividirEntero(value, divisor);
        }

        return filas;
    }

    public int getFilasJugadores(int size) {
        int vprogre = StrUtils.dividirEntero(size, columnas);
        return (this.isProgresivo()) ? vprogre : size;
    }

    public boolean isProgresivo() {
        return progresivo;
    }
}
