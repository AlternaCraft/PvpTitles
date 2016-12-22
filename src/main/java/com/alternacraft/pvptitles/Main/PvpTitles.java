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
package com.alternacraft.pvptitles.Main;

import com.alternacraft.pvptitles.Backend.MySQLConnection;
import com.alternacraft.pvptitles.Commands.BoardCommand;
import com.alternacraft.pvptitles.Commands.DBCommand;
import com.alternacraft.pvptitles.Commands.FameCommand;
import com.alternacraft.pvptitles.Commands.InfoCommand;
import com.alternacraft.pvptitles.Commands.LadderCommand;
import com.alternacraft.pvptitles.Commands.PurgeCommand;
import com.alternacraft.pvptitles.Commands.RankCommand;
import com.alternacraft.pvptitles.Commands.ReloadCommand;
import com.alternacraft.pvptitles.Events.Handlers.HandleFame;
import com.alternacraft.pvptitles.Events.Handlers.HandleInventory;
import com.alternacraft.pvptitles.Events.Handlers.HandlePlayerFame;
import com.alternacraft.pvptitles.Events.Handlers.HandlePlayerTag;
import com.alternacraft.pvptitles.Events.Handlers.HandleSign;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Hook.HolographicHook;
import com.alternacraft.pvptitles.Hook.MVdWPlaceholderHook;
import com.alternacraft.pvptitles.Hook.PlaceholderHook;
import com.alternacraft.pvptitles.Hook.SBSHook;
import com.alternacraft.pvptitles.Hook.VaultHook;
import com.alternacraft.pvptitles.Main.Handlers.DBHandler;
import com.alternacraft.pvptitles.Main.Managers.LoggerManager;
import static com.alternacraft.pvptitles.Main.Managers.LoggerManager.logMessage;
import com.alternacraft.pvptitles.Managers.MetricsManager;
import com.alternacraft.pvptitles.Managers.UpdaterManager;
import com.alternacraft.pvptitles.Misc.Inventories;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import com.alternacraft.pvptitles.Misc.Timer;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpTitles extends JavaPlugin {

    /*
     * Measuring performance
     */
    public static final Timer PERFORMANCE = new Timer();

    private static PvpTitles plugin = null;

    private static final String PLUGINMODELPREFIX = ChatColor.WHITE + "[" + ChatColor.GOLD
            + "PvPTitles" + ChatColor.WHITE + "]";

    // Custom prefix
    private static String PLUGINPREFIX = PLUGINMODELPREFIX + " ";

    public static Logger LOGGER = null;
    public static boolean debugMode = false;

    private Manager manager = null;

    private boolean works = true;

    /**
     * Lo uso para probar el plugin desde el propio IDE
     *
     * @param args String[]
     */
    public static void main(String[] args) {
    }

    @Override
    public void onEnable() {
        plugin = this;

        this.manager = Manager.getInstance();
        PvpTitles.LOGGER = this.getLogger();

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
                PERFORMANCE.start("Metrics event");
                new MetricsManager().sendData(manager.getPvpTitles());
                PERFORMANCE.recordValue("Metrics event");

                PERFORMANCE.start("Updater event");
                new UpdaterManager().testUpdate(manager.getPvpTitles(), getFile());
                PERFORMANCE.recordValue("Updater event");

                /* 
                 * -> Integraciones <-
                 */
                getServer().getConsoleSender().sendMessage(PLUGINPREFIX + ChatColor.GRAY
                        + "# STARTING INTEGRATION MODULE #");

                PERFORMANCE.start("Integrations");
                checkExternalPlugins();
                PERFORMANCE.recordValue("Integrations");

                getServer().getConsoleSender().sendMessage(PLUGINPREFIX + ChatColor.GRAY
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
            this.manager.getTimerManager().stopSessions();
            Set<TimedPlayer> players = this.manager.getTimerManager().getTimedPlayers();

            for (TimedPlayer next : players) {
                try {
                    this.manager.dbh.getDm().savePlayedTime(next);
                } catch (DBException ex) {
                    LoggerManager.logError(ex.getCustomMessage());
                }
            }

            // Holograms
            if (HolographicHook.ISHDENABLED) {
                HolographicHook.deleteHolograms();
            }

            // Inventories
            Inventories.closeInventories();

            if (DBHandler.tipo.equals(DBHandler.DBTYPE.MYSQL)) {
                try {
                    MySQLConnection.closeConnection();
                } catch (SQLException ex) {
                }
            }

            PERFORMANCE.saveToLog("performance.txt");
        }

        logMessage(LangsFile.PLUGIN_DISABLED.getText(Manager.messages));
    }

    private void checkOnlinePlayers() {
        // Creo las sesiones en caso de reload, gestiono la fama y los inventarios
        for (Player pl : this.getServer().getOnlinePlayers()) {
            if (HandlePlayerFame.shouldDoPlayerConnection(pl, false)) {
                try {
                    this.manager.dbh.getDm().playerConnection(pl);
                } catch (DBException ex) {
                    LoggerManager.logError(ex.getCustomMessage());
                    continue;
                }
            }

            // Times
            TimedPlayer tPlayer = this.manager.getTimerManager().hasPlayer(pl)
                    ? this.manager.getTimerManager().getPlayer(pl) : new TimedPlayer(this, pl);
            tPlayer.startSession();

            if (!this.manager.getTimerManager().hasPlayer(pl)) {
                this.manager.getTimerManager().addPlayer(tPlayer);
            }

            this.manager.getMovementManager().addLastMovement(pl);
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

    public static String getPluginName() {
        return PLUGINPREFIX;
    }

    public static void setPluginName(String str) {
        PLUGINPREFIX = str;
    }

    public static String getDefaultPluginName() {
        return PLUGINMODELPREFIX;
    }

    public static PvpTitles getInstance() {
        return plugin;
    }

    public Manager getManager() {
        return manager;
    }
}
