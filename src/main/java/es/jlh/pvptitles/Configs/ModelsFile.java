package es.jlh.pvptitles.Configs;

import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Objects.Boards.BoardModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author AlternaCraft
 */
public class ModelsFile {

    private static final String defaultModels = ""
            + "// (En) http://dev.bukkit.org/bukkit-plugins/pvptitles/pages/signs/\n"
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
            + "// / -> indica salto de linea en el cartel\n"
            + "//\n"
            + "// $spacing=<valor> -> <valor> Indica que elemento sera reemplazado por espacio\n"
            + "//\n"
            + "// Recuerda que puedes usar codigos de colores (&)\n"
            + "// Las variables usan todas las lineas del cartel\n"
            + "// Puedes repetir variables en diferentes carteles\n"            
            + "//\n"
            + "//\n"
            + "// ----------------------------------------------------------------\n"
            + "// Tipos prehechos (Recuerda poner '#' antes del nombre del modelo)\n"
            + "// ----------------------------------------------------------------\n"
            + "\n\n"
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
            + "<player>[<rank>] | <player>[<rank>]";

    private static final Charset cs = Charset.forName("UTF-8");

    /**
     * Método para cargar un fichero en una variable
     *
     * @param ruta Ruta del fichero
     * @return Lista de objetos
     * @throws java.io.IOException
     */
    public ArrayList<BoardModel> leeArchivo(String ruta) throws IOException {
        ArrayList<BoardModel> sm = new ArrayList();
        BufferedReader br = null;

        ArrayList<String> al;

        /* Esta la propago porque es un error al abrir el archivo */
        br = Files.newBufferedReader(Paths.get(ruta), cs);

        // Lectura del fichero
        String linea;
        String modelos = "";

        while ((linea = br.readLine()) != null) {
            if (linea.length() > 0 && linea.charAt(0) == '#') {
                modelos += linea + "&&";
                break;
            }
        }

        while ((linea = br.readLine()) != null) {
            modelos += linea + "&&";
        }

        // Limpieza de caracteres
        modelos = modelos.replace(" ", "");

        // Convertir en espacios
        //modelos = modelos.replace(".", " ");
        al = new ArrayList<>(Arrays.asList(modelos.split("#")));

        // Modelos partidos
        for (int i = 1; i < al.size(); i++) {
            String[] datos = al.get(i).split("&&");

            String nombre = datos[0].substring(0, datos[0].indexOf("("));
            short cant = Short.valueOf(datos[0].substring(datos[0].indexOf("(") + 1, datos[0].indexOf(")")));

            ArrayList<ArrayList<ArrayList<String>>> params = new ArrayList();

            // Filas partidas
            for (int j = 1; j < datos.length; j++) {
                if (datos[0].contains("$spacing=")) {
                    String spacing = datos[0].substring(datos[0].indexOf("$spacing=") + 9, datos[0].length());
                    datos[j] = datos[j].replace(spacing, " ");
                }

                String[] datosPartidos = datos[j].split("\\|");
                ArrayList<ArrayList<String>> contenido = new ArrayList();

                // Columnas partidas
                for (String datosPartido : datosPartidos) {
                    ArrayList<String> fc = new ArrayList();
                    String[] dp = datosPartido.split("\\/");

                    // Filas de las columnas partidas
                    fc.addAll(Arrays.asList(dp));

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
    public ArrayList<BoardModel> creaArchivo(PvpTitles pl) {
        String ruta = new StringBuilder().append(
                pl.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        "models.txt").toString();
        FileWriter fichero = null;
        PrintWriter pw = null;

        try {
            fichero = new FileWriter(ruta);
            pw = new PrintWriter(fichero);

            String[] exampleModels = ModelsFile.defaultModels.split("\n");
            for (String modelo : exampleModels) {
                pw.println(modelo);
            }
        } catch (Exception e) {
        } finally {
            try {
                // Nuevamente aprovechamos el finally para 
                // asegurarnos que se cierra el fichero.
                if (null != fichero) {
                    fichero.close();
                }
            } catch (Exception e2) {
            }
        }

        ArrayList<BoardModel> signmodels = null;

        try {
            signmodels = this.leeArchivo(ruta);
        } catch (IOException ex) {
        }

        return signmodels;
    }
}
