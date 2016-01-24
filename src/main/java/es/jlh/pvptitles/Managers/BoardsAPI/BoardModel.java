package es.jlh.pvptitles.Managers.BoardsAPI;

import es.jlh.pvptitles.Misc.Utils;
import static es.jlh.pvptitles.Misc.Utils.removeColors;
import java.util.ArrayList;

/**
 *
 * @author AlternaCraft
 */
public class BoardModel {

    protected ArrayList<ArrayList<ArrayList<String>>> params = null;

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

        for (ArrayList<ArrayList<String>> param : params) {
            if (param.size() > ant) {
                ant = (short) param.size();
            }
        }

        this.columnas = ant;
    }

    public final void setProgresivo() {
        this.progresivo = false;

        for (ArrayList<ArrayList<String>> param : params) {
            ArrayList<String> filas = new ArrayList();
            for (ArrayList<String> param1 : param) {
                String concatena = "";
                for (String param11 : param1) {
                    StringBuilder buf = new StringBuilder();

                    int var1 = param11.indexOf('<');
                    int var2 = param11.indexOf('>');
                    
                    while (var1 != -1 && var2 != -1) {
                        buf.append(param11.substring(var1 + 1, var2));
                        param11 = param11.substring(var2 + 1);

                        var1 = param11.indexOf('<');
                        var2 = param11.indexOf('>');
                    }

                    concatena += buf.toString();
                }
                filas.add(concatena);
            }

            if (filas.size() > 1) {
                for (int i = 0; i < filas.size() - 1; i++) {
                    String pick = removeColors(filas.get(i));
                    if (pick.equals("")) continue;
                    
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

    public ArrayList<ArrayList<ArrayList<String>>> getParams() {
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

        ArrayList<ArrayList<String>> v;
        int i = 0;

        while (i < total - 1) {
            int ant = 0;
            v = this.params.get(i);

            for (ArrayList<String> arrayList : v) {
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
            filas += Utils.dividirEntero(value, divisor);
        }

        return filas;
    }

    public int getFilasJugadores(int size) {
        int vprogre = Utils.dividirEntero(size, columnas);
        return (this.isProgresivo()) ? vprogre : size;
    }

    public boolean isProgresivo() {
        return progresivo;
    }

}
