package es.jlh.pvptitles.Objects.LBSigns;

import java.util.ArrayList;

/**
 *
 * @author julito
 */
public class LBModel {    
    private String nombre = null;    
    private short cantidad = 0;
    
    private ArrayList<ArrayList<ArrayList<String>>> params = null;
    
    private short divisorFilas = 0;
    private short columnas = 0;
    
    private boolean progresivo = false;

    public LBModel(String n, short c, ArrayList formato) {
        this.nombre = n;
        this.cantidad = c;
        this.params = formato;
        
        this.setDivisorFilas();
        this.setColumnas();        
        this.setProgresivo();
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
    
    public final void setDivisorFilas() {
        short attFilas = 1;
        
        int repePlayer = 0;
        int repeRank = 0;
        int repeFame = 0;
        int repePos = 0;
                
        /* FIX PARA EVITAR QUE BORRE MAS FILAS DE LAS QUE DEBE */
        for (ArrayList<ArrayList<String>> param : params) {
            for (ArrayList<String> param1 : param) {
                for (String variable : param1) {                    
                    if (variable.contains("<player>")) {                                
                        repePlayer++;                     
                    }                
                    else if (variable.contains("<rank>")) {
                        repeRank++;
                    }
                    else if (variable.contains("<fame>")) {
                        repeFame++;
                    }
                    else if (variable.contains("<pos>")) {
                        repePos++;
                    }
                }

                if (repePlayer >= 2 || repeRank >= 2 || repeFame >= 2 || repePos >= 2) {                                            
                    attFilas = 2;                    
                    break;
                }                                                
            }
        }
        
        this.divisorFilas = attFilas;
    }

    public final void setColumnas() {
        short ant = 0;
        
        for (ArrayList<ArrayList<String>> param : params) {
            if (param.size() > ant) {
                ant = (short)param.size();
            }
        }
        
        this.columnas = ant;
    }
    
    public short getFilas(short jugadores) {        
        return (short)(this.params.size() + (((int) Math.ceil((double)(jugadores/4.0)/this.divisorFilas))-1));
    }
    
    public short getCols() {
        return this.columnas;
    }
    
    public boolean isProgresivo() {
        return progresivo;
    }

    public final void setProgresivo() {
        for (ArrayList<ArrayList<String>> param : params) {           
            ArrayList<String> filas = new ArrayList();
            for (ArrayList<String> param1 : param) {
                String concatena = "";
                for (String param11 : param1) {                     
                    String variables = "";
                    
                    int var1 = param11.indexOf("<");
                    int var2 = param11.indexOf(">");
                    
                    while (var1 != -1 && var2 != -1) {
                        variables += param11.substring(var1+1, var2);
                        param11 = param11.substring(var2+1);
                        
                        var1 = param11.indexOf("<");
                        var2 = param11.indexOf(">");
                    }
                    
                    concatena += variables;
                }                              
                filas.add(concatena);
            }
            
            if (filas.size() > 1 && !filas.contains("")) {
                if (filas.get(0).compareToIgnoreCase(filas.get(1)) == 0) {
                    this.progresivo = true;
                    return;                    
                }
            }
        }        
        
        this.progresivo = false;
    }    
    
    @Override
    public String toString() {
        return nombre + " | " + cantidad + " | " + params.toString();
    }
}
