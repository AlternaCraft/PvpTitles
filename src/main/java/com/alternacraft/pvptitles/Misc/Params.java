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
package com.alternacraft.pvptitles.Misc;

import java.util.HashMap;
import java.util.Map;

public class Params {

    public static enum Vars {

        PLAYER,
        RANK,
        FAME,
        POS,
        SERVER,
        WORLD;
    }

    private final Map<Vars, Integer> argsValues = new HashMap();

    private boolean progresivo = false;
    private int divisor = 0;
    private int ncols = 0;

    public Params() {
        resetValues();
    }

    private void resetValues() {
        for (Vars value : Vars.values()) {
            argsValues.put(value, 0);
        }
    }

    public void addOne(Vars v, int c) {
        if (progresivo && divisor > 1) {
            argsValues.put(v, argsValues.get(v) + ((c == ncols - 1) ? 1 : 0));
        } else {
            argsValues.put(v, argsValues.get(v) + 1);
        }
    }

    public int getNext(Vars var, int c) {
        int v = this.argsValues.get(var);        
        
        if (progresivo && divisor > 1) {
            if (v >= divisor) {
                int filas = ((int) v / divisor);
                v += (divisor * (ncols - 1)) * filas; // Cambio de linea
            }
            if (c > 0) {
                v += divisor * c; // Salto de columna
            }
        }

        return v;
    }

    public void setProgresivo(boolean progresivo) {
        this.progresivo = progresivo;
    }

    public void setDivisor(int divisor) {
        this.divisor = divisor;
    }

    public void setNcols(int ncols) {
        this.ncols = ncols;
    }

}
