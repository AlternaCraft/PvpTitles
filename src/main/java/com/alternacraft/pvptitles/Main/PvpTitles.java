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
package com.alternacraft.pvptitles.Main;

import com.alternacraft.pvptitles.Commands.*;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Hooks.*;
import com.alternacraft.pvptitles.Listeners.*;
import com.alternacraft.pvptitles.Managers.MetricsManager;
import com.alternacraft.pvptitles.Managers.UpdaterManager;
import com.alternacraft.pvptitles.Misc.Inventories;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import com.alternacraft.pvptitles.Misc.Timer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.alternacraft.pvptitles.Main.CustomLogger.logMessage;

public class PvpTitles extends JavaPlugin {

    /*
     * Measuring performance
     */
    public static final Timer PERFORMANCE = new Timer();

    private static PvpTitles plugin = null;

    private static final String PLUGINMODELPREFIX = ChatColor.WHITE + "[" + ChatColor.GOLD
            + "PvPTitles" + ChatColor.WHITE + "]";

    public static String PLUGIN_DIR;

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
        PLUGIN_DIR = new StringBuilder(this.getDataFolder().toString())
                .append(File.separator).toString();

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
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            PERFORMANCE.start("Metrics event");
            new MetricsManager().sendData(manager.getPvpTitles());
            PERFORMANCE.recordValue("Metrics event");
            
            PERFORMANCE.start("Updater event");
            new UpdaterManager().testUpdate(manager.getPvpTitles(), getFile());
            PERFORMANCE.recordValue("Updater event");
            
            /*
            * -> Integraciones <-
            */
            PERFORMANCE.start("Integrations");
            Object[] results = checkExternalPlugins();
            PERFORMANCE.recordValue("Integrations");
            
            if (Manager.getInstance().params.isDisplayIntegrations()) {
                CustomLogger.showMessage(ChatColor.GRAY
                        + "# STARTING INTEGRATION MODULE #");
                for (Object o : results) {
                    CustomLogger.showMessage(ChatColor.YELLOW + (String) o
                            + ChatColor.AQUA + " integrated correctly");
                }
                CustomLogger.showMessage(ChatColor.GRAY
                        + "# ENDING INTEGRATION MODULE #");
            }
            /*
            * -> Fin integraciones <-
            */
        }, 5L);

        logMessage(LangsFile.PLUGIN_ENABLED.getText(Manager.messages));
    }

    @Override
    public void onDisable() {
        if (this.works) {
            this.manager.getTimerManager().stopSessions();
            Set<TimedPlayer> players = this.manager.getTimerManager().getTimedPlayers();

            players.forEach(next -> {
                try {
                    this.manager.getDBH().getDM().savePlayedTime(next.getUniqueId(),
                            next.getTotalOnline());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }
            });

            // Holograms
            if (HolographicHook.ISHDENABLED) {
                HolographicHook.deleteHolograms();
            }

            // Inventories
            Inventories.closeInventories();

            if (DBLoader.tipo.equals(DBLoader.DBType.MYSQL)
                    || DBLoader.tipo.equals(DBLoader.DBType.SQLITE)) {
                this.manager.getDBH().sql.closeConnection();
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
                    this.manager.getDBH().getDM().playerConnection(pl);
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
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

    private Object[] checkExternalPlugins() {
        List<String> messages = new ArrayList();

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            String[] hookDetails = new PlaceholderHook(this).setup();
            messages.addAll(Arrays.asList(hookDetails));
        }
        if (this.getServer().getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            String[] hookDetails = new MVdWPlaceholderHook(this).setup();
            messages.addAll(Arrays.asList(hookDetails));
        }
        if (this.getServer().getPluginManager().isPluginEnabled("ScoreboardStats")) {
            String[] hookDetails = new SBSHook(this).setupSBS();
            messages.addAll(Arrays.asList(hookDetails));
        }
        if (this.getServer().getPluginManager().isPluginEnabled("Vault")) {
            String[] hookDetails = new VaultHook(this).setupVault();
            messages.addAll(Arrays.asList(hookDetails));
        }
        if (this.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
            String[] hookDetails = new HolographicHook(this).setup();
            messages.addAll(Arrays.asList(hookDetails));
        }
        if (this.getServer().getPluginManager().isPluginEnabled("VanishNoPacket")) {
            String[] hookDetails = new VNPHook(this).setup();
            messages.addAll(Arrays.asList(hookDetails));
        }

        return messages.toArray();
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
