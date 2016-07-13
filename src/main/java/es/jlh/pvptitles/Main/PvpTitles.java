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

// <editor-fold defaultstate="collapsed" desc="PROJECT DOCUMENTATION">
// Proyect created: 15-02-2015
// Last Change:     28-03-2016
// Author:          AlternaCraft;
// 
// History:
//  Ver. 2.0    12.04-2015   Arreglados algunos bugs y construido a partir de
//   la versión 1.8 de spigot
//  Ver. 2.1    25-04-2015   Modificado el sistema de almacenamiento de datos, 
//   añadidos leaderboards por carteles, añadidos varios parametros del config y
//   varias traducciones a los locales
//  Ver. 2.1.1  20-05-2015   Arreglados fallos importantes en el handler de
//   la fama del jugador (El jugador no moria cuando le mataba otro jugador)
//  Ver. 2.1.2  20/05/2015   Nueva información para el fichero models.txt, mejoras
//   para el sistema de carteles, compilacion en jdk 1.7 y solucionados varios bugs
//   con los carteles
//  Ver. 2.1.3  04/06/2015   Arreglo de varios bugs con los carteles, Arreglo de
//   un bug con el comando /pvpfame see e integrado scoreboardstats
//  Ver. 2.2    06/06/2015   Mejorados los permisos
//  Ver. 2.2    20/06/2015   Añadido sistema de recompensas
//  Ver. 2.2    24/06/2015   Optimización del plugin, mejorado el comando /pvpreload
//   y añadido el soporte para Vault
//  Ver. 2.2.1  30/06/2015   Conversor UUID
//  Ver. 2.2.1  14/07/2015   Soporte MySQL y migrador de base de datos
//  Ver. 2.2.1  25/07/2015   Exportar bd Ebean a SQL
//  Ver. 2.2.1  26/07/2015   Integrado soporte de carteles por server
//  Ver. 2.2.1  04/08/2015   Arreglados algunos bugs, modificadas algunas traducciones
//   y mejorado el sistema de actualizacion de carteles
//  Ver. 2.2.2  07/08/2015   Modificada la estructura de la bd MySQL, cambiado el idioma 
//   del config y de las traducciones a ingles por defecto
//  Ver. 2.2.2  26/08/2015   Arreglados algunos fallos estructurales, añadida
//   funcionalidad para el filtrado entre servidores y mundos y añadido sistema
//   de gestión por mundos.
//  Ver. 2.2.2  27/08/2015   Arreglado fallo de visualización al añadir fama por comando y
//   añadida opción para ver la fama de jugadores no conectados
//  Ver. 2.3    15/09/2015   Añadido soporte para añadir colores personalizados a los titulos
//   añadido tiempo como requisito opcional para conseguir un rango, mejorado sistema de exportacion de
//   la información de ebeans y fixeado problema con el multiworld en ebeans ()
//  Ver. 2.3    18/09/2015   Añadido sistema de mensajes según localización
//  Ver. 2.3    19/09/2015   Añadido control de sesiones para obtener el tiempo
//   de juego
//  Ver. 2.3    23/09/2015   Mejorado sistema antifarm y arreglados varios fallos
//  Ver. 2.3    24/09/2015   Cambiada una parte de la estructura de MultiWorld, 
//   optimización de los eventos y añadidos nuevos datos al metrics
//  Ver. 2.3    26/09/2015   Añadida opción para modificar los puntos de jugadores
//   que no estén online
//  Ver. 2.3.1  27/10/2015   Cambiada la estructura del gestor de leaderboards
//  Ver. 2.3.1  09/11/2015   Arreglado fallo con el prefijo siendo OP
//  Ver. 2.3.1  11/11/2015   Mejorado el diseño del comando /pvpsign
//  Ver. 2.3.1  12/11/2015   Arreglado fallo con los eventos del plugin y arreglado
//   un fallo en la extructura de la tabla PlayerWorld de MySQL (idServer)
//  Ver. 2.4    16/11/2015   Arreglado fallo con la cuenta de servers.yml y cambiada
//   la estructura de las bases de datos.
//  Ver. 2.4    18/11/2015   Añadido comando para exportar los datos de la base de datos
//  Ver. 2.4    01/12/2015   Añadida gestion de errores, soporte para mas idiomas
//   personalizados.
//  Ver. 2.4    07/12/2015   Cambiada la estructura del sistema de retrocompatibilidad
//  Ver. 2.4    09/12/2015   Arreglado pequeño bug con los carteles en mysql, arreglado
//   fallo al establecer fama sobre usuarios desconectados usando MySQL
//  Ver. 2.4.1  22/12/2015   Añadido recompensa por racha de bajas, recompensa 
//   directa de dinero. Añadido los comandos pvpfame [time|killstreak] y mejorado
//   el gestor de los tableros de puntuacion.
//  Ver. 2.4.1  26/12/2015   Arreglado pequeño fallo con /pvpsign en reload.
//  Ver. 2.4.1  30/12/2015   Añadida compatibilidad con hologramas, modificado el
//   comando pvpsign por pvpboard con nuevas mejoras. Eliminados parametros
//   auto export del config y añadida nueva funcionalidad para autoactualizacion
//   durante el backup del config.
//  Ver. 2.4.1  11/01/2016   Arreglados algunos fallos de la version anterior, 
//  Ver. 2.4.1  13/01/2016   Añadida opcion para operar sobre jugadores que no 
//   han entrado al server aún con MW activado mediante el comando pvpfame
//  Ver. 2.4.1  16/01/2016   Mejorada la gestión de errores en la base de datos y
//   arreglado fallo de orden en la exportacion de ebean a sql
//  Ver. 2.4.1  18/01/2016   Modificado pvpsign por pvpboard, con funcionalidad para
//   gestionar tableros, añadido argumento '-silent' en el comando pvpfame para
//   evitar que el jugador recibe una alerta cuando le modifiquen la fama.
//  Ver. 2.4.1  20/01/2016   Optimizadas las clases del proyecto, arreglado fallo
//   con el timer que comprueba el tiempo AFK si CheckAFK es false y añadido
//   mensaje de veto en el comando /pvprank
//  Ver. 2.4.2  22/01/2016   Añadida opcion para incluir lineas vacios en tableros, 
//   nuevas variables para los tableros y main opcional en los hologramas. Arreglado
//   fallo al intentar otorgar premio de economia (money)
//  Ver. 2.4.2  24/01/2016   Optimizada la clase antifarm para usar uuid en vez del
//   nombre del jugador
//  Ver. 2.4.3  25/01/2016   Arreglado fallo con pvpreload mientras carga las listas
//   del config principal
//  Ver. 2.4.3  26/01/2016   Añadida variable 'world' al mensaje FAME_CHANGE_PLAYER,
//   arreglado fallo con los jugadores mostrados en boards mientras MW esta activado
//  Ver. 2.4.4  08/02/2016   Añadidos nuevos parametros a las metricas, añadido detector
//   de titulo al cambiar de mundo con multiworld activado.
//  Ver. 2.5    01/03/2016   Compilado con la version 1.9 de spigot, arreglado fallo
//   con los mensajes al usar el comando de la fama (add/set).
//  Ver. 2.5.1  24/03/2016   Mejorado el rendimiento del plugin y optimizado el código.
//   Arreglado fallo con el permiso para evitar mostrar el titulo.
//  Ver. 2.5.1  28/03/2016   Mejorado sistema de titulos por holograma, integrado
//   con las mismas condiciones que el mostrado en el chat (MW-filter y permisos).
//  Ver. 2.5.2  17/04/2016   Renombradas algunas variables y actualizadas las dependencias.
//  Ver. 2.5.2  03/06/2016   Arreglado un fallo en la interacción del evento ChangeRank
//   con el plugin holographicDisplays.
//  Ver. 2.5.2  04/06/2016   Añadidos nuevos idiomas para el Localizer.
//  Ver. 2.5.2  07/06/2016   Añadido soporte para placeholder api, arreglado fallo con el
//   contador de la racha de bajas y añadido modificador para ajustar la altura del
//   título holográfico
//  Ver. 2.5.3  09/06/2016   Añadido soporte para MVdWPlaceholderAPI.
//  Ver. 2.5.3  11/06/2016   Mejorada la gestión de errores de la base de datos,
//   añadido un nuevo placeholder, modificada la gestion del chat sobre los titulos
//   y arreglado un fallo con el integrador de Vault
//  Ver. 2.5.3  14/06/2016   Arreglado un fallo con el control de los rangos como
//   prefijos en el chat y mejorado el sistema de gestión de errores.
//  Ver. 2.5.3  16/06/2016   Mejorada la gestión de errores de la base de datos.
//  Ver. 2.5.4  21/06/2016   Arreglado un fallo con el placeholder "valid_rank" y
//   mejorada la respuesta del comando /pvprank
//  Ver. 2.5.5  03/07/2016   Mejorada la gestión de errores
//  Ver. 2.5.5  06/07/2016   Arreglados varios fallos y añadidas las licencias 
//   a todas las clases
//  Ver. 2.5.6  11/07/2016   Mejorada la gestión de UUIDs para el comando /pvpfame, 
//   arreglado un problema con los título con color en el archivo de commandos (onRank),
//   añadidos nuevos mensajes a los locales (RANK_INFO_(...)) y añadido soporte para
//   colores en los comandos de las recompensas.
// </editor-fold>
package es.jlh.pvptitles.Main;

import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Backend.MySQLConnection;
import es.jlh.pvptitles.Commands.BoardCommand;
import es.jlh.pvptitles.Commands.DBCommand;
import es.jlh.pvptitles.Commands.FameCommand;
import es.jlh.pvptitles.Commands.InfoCommand;
import es.jlh.pvptitles.Commands.LadderCommand;
import es.jlh.pvptitles.Commands.PurgeCommand;
import es.jlh.pvptitles.Commands.RankCommand;
import es.jlh.pvptitles.Commands.ReloadCommand;
import es.jlh.pvptitles.Events.Handlers.HandleFame;
import es.jlh.pvptitles.Events.Handlers.HandleInventory;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerTag;
import es.jlh.pvptitles.Events.Handlers.HandleSign;
import es.jlh.pvptitles.Files.LangsFile;
import es.jlh.pvptitles.Hook.HolographicHook;
import es.jlh.pvptitles.Hook.MVdWPlaceholderHook;
import es.jlh.pvptitles.Hook.PlaceholderHook;
import es.jlh.pvptitles.Hook.SBSHook;
import es.jlh.pvptitles.Hook.VaultHook;
import es.jlh.pvptitles.Main.Handlers.DBHandler;
import es.jlh.pvptitles.Managers.MetricsManager;
import es.jlh.pvptitles.Managers.MovementManager;
import es.jlh.pvptitles.Managers.Timer.TimedPlayer;
import es.jlh.pvptitles.Managers.TimerManager;
import es.jlh.pvptitles.Managers.UpdaterManager;
import es.jlh.pvptitles.Misc.Inventories;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpTitles extends JavaPlugin {

    private static PvpTitles plugin = null;

    public static final String PLUGIN = ChatColor.WHITE + "[" + ChatColor.GOLD
            + "PvPTitles" + ChatColor.WHITE + "] ";

    public static Logger LOGGER = null;
    public static boolean debugMode = false;

    public Manager manager = null;

    private MovementManager movementManager = null;
    private TimerManager timerManager = null;

    private boolean works = true;

    /**
     * Lo uso para probar el plugin desde el propio IDE
     *
     * @param args
     */
    public static void main(String[] args) {
    }

    @Override
    public void onEnable() {
        plugin = this;

        this.manager = Manager.getInstance();
        PvpTitles.LOGGER = this.getLogger();

        // Registro los managers del timing
        this.timerManager = new TimerManager(this);
        this.movementManager = new MovementManager(this);

        /*
         * Cargo el contenido del config principal, la gestion de la bd y el resto
         * de configuraciones.
         */
        this.works = this.manager.setup(this);

        if (!this.works) {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Instancio la clase para evitar problemas en el reload
        new Inventories().setup();

        // Registro los handlers de los eventos
        getServer().getPluginManager().registerEvents(new HandlePlayerFame(this), this);
        getServer().getPluginManager().registerEvents(new HandlePlayerTag(this), this);
        getServer().getPluginManager().registerEvents(new HandleFame(this), this);
        getServer().getPluginManager().registerEvents(new HandleSign(this), this);
        getServer().getPluginManager().registerEvents(new HandleInventory(this), this);

        // Registro los comandos
        getCommand("pvpRank").setExecutor(new RankCommand(this));
        getCommand("pvpFame").setExecutor(new FameCommand(this));
        getCommand("pvpBoard").setExecutor(new BoardCommand(this));
        getCommand("pvpPurge").setExecutor(new PurgeCommand(this));
        getCommand("pvpLadder").setExecutor(new LadderCommand(this));
        getCommand("pvpReload").setExecutor(new ReloadCommand(this));
        getCommand("pvpDatabase").setExecutor(new DBCommand(this));
        getCommand("pvpTitles").setExecutor(new InfoCommand(this));

        checkOnlinePlayers();

        // Tareas posteriores
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                new MetricsManager().sendData(manager.getPvpTitles());
                new UpdaterManager().testUpdate(manager.getPvpTitles(), getFile());

                /* 
                 * -> Integraciones <-
                 */
                getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.GRAY
                        + "# STARTING INTEGRATION MODULE #");

                checkExternalPlugins();

                getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.GRAY
                        + "# ENDING INTEGRATION MODULE #");                
                /*
                 * -> Fin integraciones <-
                 */
            }
        }, 5L);

        logMessage(LangsFile.PLUGIN_ENABLED.getText(Manager.messages));
    }

    @Override
    public void onDisable() {
        if (this.works) {
            this.timerManager.stopSessions();
            Set<TimedPlayer> players = this.timerManager.getTimedPlayers();

            for (TimedPlayer next : players) {
                try {
                    this.manager.dbh.getDm().savePlayedTime(next);
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }
            }

            // Holograms
            if (HolographicHook.ISHDENABLED) {
                HolographicHook.deleteHolograms();
            }

            // Inventories
            Inventories.closeInventories();
            
            if (DBHandler.tipo.equals(DBHandler.DBTYPE.MYSQL)) try {
                MySQLConnection.closeConnection();
            } catch (SQLException ex) {
            }
        }

        logMessage(LangsFile.PLUGIN_DISABLED.getText(Manager.messages));
    }

    private void checkOnlinePlayers() {
        // Creo las sesiones en caso de reload, gestiono la fama y los inventarios
        for (Player pl : this.getServer().getOnlinePlayers()) {
            try {
                this.manager.dbh.getDm().playerConnection(pl);
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
                continue;
            }

            // Times
            TimedPlayer tPlayer = this.getTimerManager().hasPlayer(pl)
                    ? this.getTimerManager().getPlayer(pl) : new TimedPlayer(this, pl);
            tPlayer.startSession();

            if (!this.getTimerManager().hasPlayer(pl)) {
                this.getTimerManager().addPlayer(tPlayer);
            }

            this.getMovementManager().addLastMovement(pl);
        }
    }

    private void checkExternalPlugins() {
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).hook();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            new MVdWPlaceholderHook(this).setup();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("ScoreboardStats")) {
            new SBSHook(this).setupSBS();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            new VaultHook(this).setupVault();
        }
        if (this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
            new HolographicHook(this).setup();
        }
    }

    /* PLAYER TIME */
    public MovementManager getMovementManager() {
        return this.movementManager;
    }

    public TimerManager getTimerManager() {
        return this.timerManager;
    }

    // Custom message
    public static void showMessage(String msg) {
        plugin.getServer().getConsoleSender().sendMessage(PLUGIN + msg);
    }

    public static void logMessage(String msg) {
        LOGGER.info(msg);
    }

    private static final String MYSQL_CRAP_REGEX = "com.*: ";

    /* DEBUG MANAGEMENT */
    public static void logDebugInfo(String message) {
        logDebugInfo(Level.INFO, message.replaceFirst(MYSQL_CRAP_REGEX, ""));
    }

    public static void logDebugInfo(Level level, String message) {
        logDebugInfo(level, message, null);
    }

    public static void logDebugInfo(Level level, String message, Exception ex) {
        if (debugMode) {
            PvpTitles.LOGGER.log(level, message, ex);
        }
    }

    /* ERROR MANAGEMENT */
    public static void logError(String message, Exception ex) {
        PvpTitles.LOGGER.log(Level.SEVERE, message, ex);
    }

    public static PvpTitles getInstance() {
        return plugin;
    }
}
