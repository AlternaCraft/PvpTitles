package es.jlh.pvptitles.Objects;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author AlternaCraft
 */
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
