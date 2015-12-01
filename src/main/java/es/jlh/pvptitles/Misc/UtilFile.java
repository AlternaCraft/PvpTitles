package es.jlh.pvptitles.Misc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author AlternaCraft
 */
public class UtilFile {

    public static boolean exists(String ruta) {
        return new File(ruta).exists();
    }

    public static void writeFile(String ruta, String cont) {
        FileWriter fichero = null;

        try {
            fichero = new FileWriter(ruta);
            fichero.write(cont);
        } catch (Exception e) {
        } finally {
            try {
                // Nuevamente aprovechamos el finally para 
                // asegurarnos que se cierra el fichero.
                if (null != fichero) {
                    fichero.flush();
                    fichero.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    public static String readFile(String ruta) {
        BufferedReader br = null;
        
        try {
            br = new BufferedReader(new FileReader(ruta));

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();

        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            try {                
                br.close();
            } catch (IOException ex) {
            }
        }

        return "";
    }
    
    public static void delete(String ruta) {
        new File(ruta).delete();
    }
}
