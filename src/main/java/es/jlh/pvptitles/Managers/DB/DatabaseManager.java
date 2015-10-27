package es.jlh.pvptitles.Managers.DB;

import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Objects.LBData;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Objects.TimedPlayer;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author julito
 */
public interface DatabaseManager {

    /**
     * Método para guardar la fama obtenida por un jugador
     * <p>
     * Guarda los puntos en un fichero con el nombre del jugador</p>
     *
     * @param playerUUID
     * @param fame Puntos pvp
     */
    public void savePlayerFame(UUID playerUUID, int fame);

    /**
     * Método para cargar los puntos pvp de un jugador
     *
     * @param playerUUID
     *
     * @return Entero con la fama del jugador
     */
    public int loadPlayerFame(UUID playerUUID);

    /**
     * Método para cargar los puntos pvp de un jugador con MW activado
     *
     * @param playerUUID
     * @param world
     * @return
     */
    public int loadPlayerFame(UUID playerUUID, String world);

    /**
     * Método para la primera conexion de los jugadores en el server
     * <p>
     * Crea un archivo con el nombre del jugador y establece a cero sus puntos
     * pvp</p>
     *
     * @param player
     */
    public void firstRunPlayer(Player player);

    /**
     * Método para crear o añadir el tiempo de juego de un jugador
     *
     * @param tPlayer
     */
    public void savePlayedTime(TimedPlayer tPlayer);

    /**
     * Método para recibir los dias que lleva el jugador en el servidor con el
     * plugin activado.
     *
     * @param playerUUID
     * @return Entero con los minutos transcurridos
     */
    public int loadPlayedTime(UUID playerUUID);

    /**
     * Método para registrar un cartel en la base de datos
     *
     * @param nombre String con el nombre de la tabla de puntuaciones
     * @param modelo String con el nombre de modelo utilizado
     * @param server
     * @param l Location con la posicion del cartel base
     * @param orientacion String con la orientacion de la tabla de puntuaciones
     * @param blockface Entero que indica el eje cardinal usado en la creacion
     * de la tabla de pùntuaciones
     */
    public void registraCartel(String nombre, String modelo, String server,
            Location l, String orientacion, int blockface);

    /**
     * Método para modificar la id del server de un cartel
     *
     * @param l Location
     */
    public void modificaCartel(Location l);

    /**
     * Método para borrar un cartel de la base de datos
     *
     * @param l Localicación del cartel base
     */
    public void borraCartel(Location l);

    /**
     * Método para buscar las tablas de puntuaciones de la base de datos
     *
     * @return ArrayList con todas ellas
     */
    public ArrayList<LBData> buscaCarteles();

    /**
     * Método para recibir el top deseado de jugadores ordenado de mejor a peor
     *
     * @param cant Cantidad de jugadores a mostrarng w,
     * @param server
     * @return ArrayList con los jugadores
     */
    public ArrayList<PlayerFame> getTopPlayers(int cant, String server);

    /**
     * Método para recibir el nombre del servidor según su ID
     *
     * @param id int
     * @return String
     */
    public String getServerName(int id);

    /**
     * Método para borrar los datos de los jugadores inactivos
     *
     * @return Entero con la cantidad de ficheros borrados
     */
    public int purgeData();

    /**
     * Método para convertir los datos de la version anterior a la 2.1
     */
    public void conversor();

    /**
     * Método para convertir los nombres de los jugadores en UUID
     */
    public void conversorUUID();

    /**
     * Método para exportar todos los datos de la base de datos a un sql
     */
    public void SQLExport();

    /**
     *
     * @param rcp
     */
    public void exportarData(Manager.RETROCP rcp);

    /**
     * Método para importar todos los datos de un sql a la bd
     *
     * @param rcp
     */
    public void importarData(Manager.RETROCP rcp);
}
