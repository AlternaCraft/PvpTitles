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
package com.alternacraft.pvptitles.Files;

import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModelsFile {

    private static final String DEFAULTMODELS = ""
            + "// (En) http://dev.bukkit.org/bukkit-plugins/pvptitles/pages/boards/\n"
            + "//\n"
            + "// -------------------\n"
            + "// Lista de variables\n"
            + "// -------------------\n"
            + "// <main> | Cartel con los datos del leaderboard\n"
            + "// <player> | Nombre de cada jugador\n"
            + "// <rank> | Nombre del rango del jugador \n"
            + "// <fame> | Fama de cada jugador\n"
            + "// <pos> | Posicion de cada jugador\n"
            + "// <server> | Nombre del servidor\n"
            + "// <world> | Nombre del mundo (Multiworld)\n"
            + "//\n"
            + "// Ejemplo\n"
            + "// -------\n"
            + "// #<Nombre>(<Cantidad>)\n"
            + "// <Vars>\n"
            + "//\n"
            + "// Utiles\n"
            + "// ------\n"
            + "// | -> indica nuevo cartel\n"
            + "// / -> indica salto de linea (Nueva fila)\n"
            + "//\n"
            + "// $spacing=<caracter> -> Indica que caracter sera reemplazado por espacio\n"
            + "// $fwslash=<caracter> -> Indica que caracter sera reemplazado por /\n"
            + "// $vcbar=<caracter> -> Indica que caracter sera reemplazado por |\n"
            + "//\n"
            + "// Como usar:\n"
            + "// ----------\n"
            + "// La ultima linea del cartel tienen que ser la que contenga las variables\n"
            + "// Ademas, las variables usan todas las lineas del cartel\n"
            + "// Puedes repetir todas las variables en diferentes columnas para repartir el contenido\n"
            + "// Recuerda que puedes usar codigos de colores (&)\n"
            + "// Puedes usar lineas vacias para indicar una fila vacia\n"
            + "//\n"
            + "//\n"
            + "// ----------------------------------------------------------------\n"
            + "// Tipos prehechos (Recuerda poner '#' antes del nombre del modelo)\n"
            + "// ----------------------------------------------------------------\n"
            + "#Basico1(3)$spacing=.\n"
            + "<main>\n"
            + "<pos>.<player>[<fame>]\n"
            + "\n"
            + "#Basico2(5)\n"
            + "/&4&lTOP 5/------  | &6&lNombre/&6&ly/&6&lrango/--------\n"
            + "<pos>              | <player>[<rank>]\n"
            + "\n"
            + "#Avanzado(10)\n"
            + "<main>\n"
            + "<player>[<rank>] | &e<player>&r[<rank>]";

    private static final String FILENAME = "models.txt";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    
    private static final String FIRST_CHAR_MODEL = "#";
    private static final String SPLIT_MODELS = "&&";

    private static final String NEW_LINE = "/";
    private static final String NEW_COLUMN = "\\|";

    enum Utils {
        SPACING("$spacing=", " "),
        FORWARD_SLASH("$fwslash=", "/"),
        VERTICAL_BAR("$vcbar=", "|");

        private String var = null;
        private String value = null;

        Utils(String var, String value) {
            this.var = var;
            this.value = value;
        }

        public String getVar() {
            return var;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Método para cargar un fichero en una variable
     *
     * @param ruta Ruta del fichero
     * @return Lista de objetos
     * @throws java.io.IOException IOException
     */
    public ArrayList<BoardModel> readFile(String ruta) throws IOException {
        ArrayList<BoardModel> sm = new ArrayList();
        BufferedReader br = null;

        ArrayList<String> al;

        /* Esta la propago porque es un error al abrir el archivo */
        br = Files.newBufferedReader(Paths.get(ruta), CHARSET);

        // Lectura del fichero
        String linea, modelos;

        StringBuilder buf = new StringBuilder();

        // Filtro comentarios
        while ((linea = br.readLine()) != null) {
            if (linea.length() > 0 && linea.charAt(0) == FIRST_CHAR_MODEL.charAt(0)) {
                buf.append(linea).append(SPLIT_MODELS);
                break;
            }
        }

        while ((linea = br.readLine()) != null) {
            buf.append(linea).append(SPLIT_MODELS);
        }

        // Limpieza de espacios
        modelos = buf.toString().replace(" ", "");

        al = new ArrayList<>(Arrays.asList(modelos.split(FIRST_CHAR_MODEL)));
        // Modelos partidos
        for (int i = 1; i < al.size(); i++) {
            String[] datos = al.get(i).split(SPLIT_MODELS);

            String nombre = datos[0].substring(0, datos[0].indexOf('('));
            short cant = Short.valueOf(datos[0].substring(datos[0].indexOf('(') + 1, datos[0].indexOf(')')));

            ArrayList<ArrayList<ArrayList<String>>> params = new ArrayList();

            Map<Utils, String> replacer = new HashMap();
            // Filas partidas
            for (int j = 1; j < datos.length; j++) {
                for (Utils var : Utils.values()) { // Almaceno datos para cambiarlos posteriormente
                    if (datos[0].contains(var.getVar())) {
                        int start = datos[0].indexOf(var.getVar());
                        int end = var.getVar().length();
                        String spacing = datos[0].substring(start + end, start + end + 1);
                        replacer.put(var, spacing);
                    }
                }

                // Fix para evitar que no coja la ultima columna
                boolean b = false;
                String fixcols = datos[j];

                if (fixcols.length() > 0 && fixcols.charAt(fixcols.length() - 1) == '|') {
                    datos[j] += " ";
                    b = true;
                }

                String[] datosPartidos = datos[j].split(NEW_COLUMN);
                if (b) {
                    datosPartidos[datosPartidos.length - 1] = "";
                }
                // FIN DEL FIX

                ArrayList<ArrayList<String>> contenido = new ArrayList();

                // Columnas partidas
                for (String datosPartido : datosPartidos) {
                    ArrayList<String> fc = new ArrayList();
                    String[] dp = datosPartido.split(NEW_LINE);

                    // Filas de las columnas partidas
                    fc.addAll(Arrays.asList(dp));

                    for (int k = 0; k < fc.size(); k++) {
                        String next = fc.get(k);
                        
                        // Cambio los valores guardados anteriormente por los nuevos
                        for (Map.Entry<Utils, String> entry : replacer.entrySet()) {
                            Utils key = entry.getKey();
                            String value = entry.getValue();
                            
                            next = next.replace(value, key.getValue());
                        }
                        
                        fc.set(k, next);
                    }

                    contenido.add(fc);
                }

                params.add(contenido);
            }

            sm.add(new BoardModel(nombre, cant, params));
        }

        return sm;
    }

    /**
     * Método para crear el fichero con los modelos por defecto
     *
     * @param pl Plugin
     * @return Contenido del archivo
     */
    public ArrayList<BoardModel> makeFile(PvpTitles pl) {
        String ruta = new StringBuilder(PvpTitles.PLUGIN_DIR)
                .append(FILENAME).toString();
        FileWriter fichero = null;
        PrintWriter pw = null;

        try {
            fichero = new FileWriter(ruta);
            pw = new PrintWriter(fichero);

            String[] exampleModels = ModelsFile.DEFAULTMODELS.split("\n");
            for (String modelo : exampleModels) {
                pw.println(modelo);
            }
        } catch (IOException e) {
        } finally {
            try {
                // Nuevamente aprovechamos el finally para 
                // asegurarnos que se cierra el fichero.
                if (null != fichero) {
                    fichero.close();
                }
            } catch (IOException ex) {
            }
        }

        ArrayList<BoardModel> signmodels = null;

        try {
            signmodels = this.readFile(ruta);
        } catch (IOException ex) {
        }

        return signmodels;
    }
}
