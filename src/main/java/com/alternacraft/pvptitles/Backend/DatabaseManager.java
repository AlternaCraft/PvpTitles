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
package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoardData;
import com.alternacraft.pvptitles.Misc.PlayerFame;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface DatabaseManager {

    /**
     * Método para gestionar los jugadores cuando entran y cuando salen del
     * server
     * <p>
     * Registra el jugador en la bd
     * </p>
     *
     * @param player Player
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public void playerConnection(Player player) throws DBException;

    /**
     * Método para guardar la fama obtenida por un jugador
     * <p>
     * Guarda los puntos del jugador
     * </p>
     *
     * @param playerUUID UUID
     * @param fame Entero con los puntos PvP
     * @param world En caso de MW activado, opción para establecer puntos en un
     * mundo específico
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public void savePlayerFame(UUID playerUUID, int fame, String world) throws DBException;

    /**
     * Método para cargar los puntos pvp de un jugador
     *
     * @param playerUUID UUID
     * @param world En caso de MW activado, opción para ver puntos en un mundo
     * específico
     *
     * @return Entero con la fama del jugador
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public int loadPlayerFame(UUID playerUUID, String world) throws DBException;

    /**
     * Método para crear o añadir el tiempo de juego de un jugador
     *
     * @param tPlayer TimedPlayer
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public void savePlayedTime(TimedPlayer tPlayer) throws DBException;

    /**
     * Método para recibir los dias que lleva el jugador en el servidor con el
     * plugin activado.
     *
     * @param playerUUID UUID
     *
     * @return Entero con los minutos transcurridos
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public long loadPlayedTime(UUID playerUUID) throws DBException;

    /**
     * Método para recibir el top deseado de jugadores ordenado de mejor a peor
     *
     * @param cant Cantidad de jugadores a mostrar
     * @param server String
     *
     * @return ArrayList con los jugadores
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public ArrayList<PlayerFame> getTopPlayers(short cant, String server) throws DBException;

    /**
     * Método para registrar un cartel en la base de datos
     *
     * @param sb SignBoard
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public void registraBoard(SignBoard sb) throws DBException;

    /**
     * Método para modificar la id del server de un cartel
     *
     * @param l Location
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public void modificaBoard(Location l) throws DBException;

    /**
     * Método para borrar un cartel de la base de datos
     *
     * @param l Localicación del cartel base
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public void borraBoard(Location l) throws DBException;

    /**
     * Método para buscar las tablas de puntuaciones de la base de datos
     *
     * @return ArrayList con todas ellas
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Error
     */
    public ArrayList<SignBoardData> buscaBoards() throws DBException;

    /**
     * Método para recibir el nombre del servidor según su ID
     *
     * @param id int
     *
     * @return String
     */
    public String getServerName(short id);

    /**
     * Método para borrar los datos de los jugadores inactivos
     *
     * @param q Entero con la cantidad de dias necesarios para ser inactivo
     * @return Entero con la cantidad de ficheros borrados
     */
    public int purgeData(int q);

    /**
     * Método para exportar todos los datos de la base de datos
     *
     * @param filename String
     */
    public void DBExport(String filename);

    /**
     * Método para importar todos los datos desde un fichero
     *
     * @param filename String
     *
     * @return False si no se pudo importar
     */
    public boolean DBImport(String filename);

    /**
     * Método para recibir el nombre del fichero por defecto para importar
     *
     * @return String
     */
    public String getDefaultFImport();

    /**
     * Método para recibir el nombre del fichero por defecto para exportar
     *
     * @return String
     */
    public String getDefaultFExport();

    /**
     * Método para corregir valores de la base de datos
     * 
     * @return Cantidad de registros reparados
     */
    public int repair();
    
    /**
     * Método para actualizar la conexion a la base de datos
     *
     * @param connection Nueva conexión
     */
    public void updateConnection(Object connection);
}
