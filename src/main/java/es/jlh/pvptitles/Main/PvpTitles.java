// Proyect created: 15-02-2015
// Last Changed:    24-09-2015
// Author:          Julito; Nick: esejuli94
// 
// History:
//  Ver. 2.0  12.04-2015    Arreglados algunos bugs y construido a partir de
//   la versión 1.8 de spigot
//  Ver. 2.1  25-04-2015    Modificado el sistema de almacenamiento de datos, 
//   añadidos leaderboards por carteles, añadidos varios parametros del config y
//   varias traducciones a los locales
//  Ver. 2.1.1  20-05-2015  Arreglados fallos importantes en el handler de
//   la fama del jugador (El jugador no moria cuando le mataba otro jugador)
//  Ver. 2.1.2  20/05/2015  Nueva información para el fichero models.txt, mejoras
//   para el sistema de carteles, compilacion en jdk 1.7 y solucionados varios bugs
//   con los carteles
//  Ver. 2.1.3  04/06/2015  Arreglo de varios bugs con los carteles, Arreglo de
//   un bug con el comando /pvpfame see e integrado scoreboardstats
//  Ver. 2.2  06/06/2015  Mejorados los permisos
//  Ver. 2.2  20/06/2015  Añadido sistema de recompensas
//  Ver. 2.2  24/06/2015  Optimización del plugin, mejorado el comando /pvpreload
//   y añadido el soporte para Vault
//  Ver. 2.2.1  30/06/2015  Conversor UUID
//  Ver. 2.2.1  14/07/2015  Soporte MySQL y migrador de base de datos
//  Ver. 2.2.1  25/07/2015  Exportar bd Ebean a SQL
//  Ver. 2.2.1  26/07/2015  Integrado soporte de carteles por server
//  Ver. 2.2.1  04/08/2015  Arreglados algunos bugs, modificadas algunas traducciones
//   y mejorado el sistema de actualizacion de carteles
//  Ver. 2.2.2  07/08/2015  Modificada la estructura de la bd MySQL, cambiado el idioma 
//   del config y de las traducciones a ingles por defecto
//  Ver. 2.2.2  26/08/2015  Arreglados algunos fallos estructurales, añadida
//   funcionalidad para el filtrado entre servidores y mundos y añadido sistema
//   de gestión por mundos.
//  Ver. 2.2.2  27/08/2015  Arreglado fallo de visualización al añadir fama por comando y
//   añadida opción para ver la fama de jugadores no conectados
//  Ver. 2.3  15/09/2015  Añadido soporte para añadir colores personalizados a los titulos
//   añadido tiempo como requisito opcional para conseguir un rango, mejorado sistema de exportacion de
//   la información de ebeans y fixeado problema con el multiworld en ebeans ()
//  Ver. 2.3  18/09/2015  Añadido sistema de mensajes según localización
//  Ver. 2.3  19/09/2015  Añadido control de sesiones para obtener el tiempo
//   de juego
//  Ver. 2.3  23/09/2015  Mejorado sistema antifarm y arreglados varios fallos
//  Ver. 2.3  24/09/2015  Cambiada una parte de la estructura de MultiWorld, 
//   optimización de los eventos y añadidos nuevos datos al metrics
//  Ver. 2.3  26/09/2015  Añadida opción para modificar los puntos de jugadores
//   que no estén online
//  Ver. 2.3.1  27/10/2015  Cambiada la estructura del gestor de leaderboards
package es.jlh.pvptitles.Main;

import es.jlh.pvptitles.Commands.FameCommand;
import es.jlh.pvptitles.Commands.InfoCommand;
import es.jlh.pvptitles.Commands.LadderCommand;
import es.jlh.pvptitles.Commands.PurgeCommand;
import es.jlh.pvptitles.Commands.RankCommand;
import es.jlh.pvptitles.Commands.ReloadCommand;
import es.jlh.pvptitles.Commands.SignCommand;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Handlers.HandleFame;
import es.jlh.pvptitles.Handlers.HandleInventoryClick;
import es.jlh.pvptitles.Handlers.HandlePlayerFame;
import es.jlh.pvptitles.Handlers.HandlePlayerPrefix;
import es.jlh.pvptitles.Handlers.HandleSign;
import es.jlh.pvptitles.Integrations.SBSSetup;
import es.jlh.pvptitles.Integrations.VaultSetup;
import es.jlh.pvptitles.Managers.MetricsManager;
import es.jlh.pvptitles.Managers.PlayedTime.MovementManager;
import es.jlh.pvptitles.Managers.PlayedTime.PlayerManager;
import es.jlh.pvptitles.Managers.UpdaterManager;
import es.jlh.pvptitles.Objects.TimedPlayer;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author julito
 */
public class PvpTitles extends JavaPlugin {

    public static final String PLUGIN = ChatColor.WHITE + "[" + ChatColor.GOLD
            + "PvPTitles" + ChatColor.WHITE + "] ";

    public PluginDescriptionFile pdf = null;
    public static Logger logger = null;

    public Manager cm = null;

    private MovementManager movementManager = null;
    private PlayerManager playerManager = null;

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
        this.cm = Manager.getInstance();
        this.logger = getLogger();
        this.pdf = this.getDescription();

        /*
         * Cargo el contenido del config principal, la gestion de la bd y el resto
         * de configuraciones.
         */
        this.works = this.cm.setup(this);
        if (!works) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Registro los handlers
        getServer().getPluginManager().registerEvents(new HandlePlayerPrefix(this), this);
        getServer().getPluginManager().registerEvents(new HandlePlayerFame(this), this);
        getServer().getPluginManager().registerEvents(new HandleFame(this), this);
        getServer().getPluginManager().registerEvents(new HandleSign(this), this);
        getServer().getPluginManager().registerEvents(new HandleInventoryClick(this), this);

        // Registro los comandos
        getCommand("pvpRank").setExecutor(new RankCommand(this));
        getCommand("pvpFame").setExecutor(new FameCommand(this));
        getCommand("pvpSign").setExecutor(new SignCommand(this));
        getCommand("pvpPurge").setExecutor(new PurgeCommand(this));
        getCommand("pvpLadder").setExecutor(new LadderCommand(this));
        getCommand("pvpReload").setExecutor(new ReloadCommand(this));
        getCommand("pvpTitles").setExecutor(new InfoCommand(this));

        // Registro los managers del timing
        movementManager = new MovementManager(this);
        playerManager = new PlayerManager(this);

        checkOnlinePlayers();

        // Tareas posteriores
        new BukkitRunnable() {
            @Override
            public void run() {
                new MetricsManager().sendData(cm.getPvpTitles());
                new UpdaterManager().testUpdate(cm.getPvpTitles(), getFile());

                /* 
                 * -> Integraciones <-
                 */
                getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.GRAY
                        + "# STARTING AUTOINTEGRATION MODULE #");

                checkExternalPlugins();

                getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.GRAY
                        + "# ENDING AUTOINTEGRATION MODULE #");

                /*
                 * -> Fin integraciones <-
                 */
            }
        }.runTaskLaterAsynchronously(this, 5);

        logger.info(LangFile.PLUGIN_ENABLED.getText(Manager.messages));
    }

    @Override
    public void onDisable() {
        if (works) {
            this.playerManager.stopSessions();
            Set<TimedPlayer> players = this.playerManager.getTimedPlayers();

            for (Iterator<TimedPlayer> iterator = players.iterator(); iterator.hasNext();) {
                TimedPlayer next = iterator.next();
                this.cm.getDm().savePlayedTime(next);
            }
        }
        logger.info(LangFile.PLUGIN_DISABLED.getText(Manager.messages));
    }

    private void checkOnlinePlayers() {
        // Creo las sesiones en caso de reload y gestiono la fama
        for (Player pl : Bukkit.getOnlinePlayers()) {
            // Fama
            this.cm.getDm().firstRunPlayer(pl);

            // Times
            TimedPlayer tPlayer = this.getPlayerManager().hasPlayer(pl)
                    ? this.getPlayerManager().getPlayer(pl) : new TimedPlayer(this, pl);

            tPlayer.startSession();
            this.getMovementManager().addLastMovement(pl);

            if (!this.getPlayerManager().hasPlayer(pl)) {
                this.getPlayerManager().addPlayer(tPlayer);
            }
        }
    }

    private void checkExternalPlugins() {
        if (Bukkit.getPluginManager().isPluginEnabled("ScoreboardStats")) {
            new SBSSetup(this).setupSBS();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            new VaultSetup(this).setupVault();
        }
    }

    public MovementManager getMovementManager() {
        return movementManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }
}
