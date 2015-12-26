package es.jlh.pvptitles.Backend;

import es.jlh.pvptitles.Objects.Boards.BoardData;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Objects.TimedPlayer;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public interface DatabaseManager {

    /**
     * Método para gestionar los jugadores cuando entran y cuando salen del
     * server
     * <p>
     * Registra el jugador en la bd</p>
     *
     * @param player
     */
    public void PlayerConnection(Player player);

    /**
     * Método para guardar la fama obtenida por un jugador
     * <p>
     * Guarda los puntos del jugador</p>
     *
     * @param playerUUID
     * @param fame Puntos pvp
     */
    public void savePlayerFame(UUID playerUUID, int fame);

    /**
     * Método para cargar los puntos pvp de un jugador
     *
     * @param playerUUID
     * @param world En caso de MW activado, opción para ver puntos en un mundo
     * específico
     *
     * @return Entero con la fama del jugador
     */
    public int loadPlayerFame(UUID playerUUID, String world);

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
     * Método para recibir el top deseado de jugadores ordenado de mejor a peor
     *
     * @param cant Cantidad de jugadores a mostrarng w,
     * @param server
     * @return ArrayList con los jugadores
     */
    public ArrayList<PlayerFame> getTopPlayers(short cant, String server);
    
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
            Location l, String orientacion, short blockface);

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
    public ArrayList<BoardData> buscaCarteles();

    /**
     * Método para recibir el nombre del servidor según su ID
     *
     * @param id int
     * @return String
     */
    public String getServerName(short id);

    /**
     * Método para borrar los datos de los jugadores inactivos
     *
     * @return Entero con la cantidad de ficheros borrados
     */
    public int purgeData();

    /**
     * Método para exportar todos los datos de la base de datos
     */
    public void DBExport();

    /**
     * Método para importar todos los datos desde un fichero
     * 
     * @param filename
     * @return 
     */
    public boolean DBImport(String filename);
    
    public String getDefaultFileName();
}
