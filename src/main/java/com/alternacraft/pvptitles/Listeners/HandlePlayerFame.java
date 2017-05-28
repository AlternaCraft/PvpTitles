/*
 * Copyright (C) 2017 AlternaCraft
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
package com.alternacraft.pvptitles.Listeners;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Events.FameEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Managers.AntiFarmManager;
import com.alternacraft.pvptitles.Managers.CleanTaskManager;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Rank;
import static com.alternacraft.pvptitles.Misc.StrUtils.splitToComponentTimes;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HandlePlayerFame implements Listener {

    /**
     * contante TICKS para saber el tiempo en segundos
     */
    private static final int TICKS = 20;

    /**
     * constant K_BY_PLAYER para definir la opcion: Asesinado por un jugador
     */
    private static final String K_BY_PLAYER = "PLAYER";
    /**
     * constant K_BY_PLAYER para definir la opcion: Asesinado por el medio
     */
    private static final String K_BY_ENVIRONMENT = "ENVIRONMENT";
    /**
     * constant K_BY_PLAYER para definir la opcion: Cualquiera de las anteriores
     */
    private static final int WHAT_EVER = 2;

    // To get a better performance
    public static final Map<UUID, Long> ALREADY_LOGGED = new HashMap();
    public static final Map<UUID, List<String>> ALREADY_VISITED = new HashMap();

    // Variable temporal
    private static final Map<String, Integer> KILLSTREAK = new HashMap();
    private static final Map<String, Integer> DEATHSTREAK = new HashMap();

    private Manager cm = null;
    private static AntiFarmManager afm = null;
    private final Map<String, CleanTaskManager> csKiller = new HashMap();

    private PvpTitles pvpTitles = null;

    /**
     * Contructor de la clase
     *
     * @param pt Plugin
     */
    public HandlePlayerFame(PvpTitles pt) {
        this.cm = pt.getManager();
        this.pvpTitles = pt;

        afm = new AntiFarmManager(pvpTitles);
    }

    /**
     * Método para crear el jugador en caso de que no exista
     *
     * @param event Evento PlayerJoinEvent
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (shouldDoPlayerConnection(player, false)) {
            try {
                cm.dbh.getDm().playerConnection(player);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
                return;
            }
        }

        // Time
        TimedPlayer tPlayer = this.pvpTitles.getManager().getTimerManager().hasPlayer(player)
                ? this.pvpTitles.getManager().getTimerManager().getPlayer(player)
                : new TimedPlayer(this.pvpTitles, player);
        tPlayer.startSession();

        if (!this.pvpTitles.getManager().getTimerManager().hasPlayer(player)) {
            this.pvpTitles.getManager().getTimerManager().addPlayer(tPlayer);
        }

        this.pvpTitles.getManager().getMovementManager().addLastMovement(player);

        HandlePlayerTag.holoPlayerLogin(player);
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (this.pvpTitles.getManager().params.isMw_enabled()
                && shouldDoPlayerConnection(player, true)) {
            try {
                cm.dbh.getDm().playerConnection(player);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }
        }
    }

    /**
     * Método para reiniciar los puntos del jugador cuando deje el server
     *
     * @param event Evento PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (shouldDoPlayerConnection(player, false)) {
            try {
                cm.dbh.getDm().playerConnection(player);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
                return;
            }
        }

        if (Manager.getInstance().params.isResetOnPlayerLeaving()) {
            HandlePlayerFame.KILLSTREAK.put(player.getUniqueId().toString(), 0);
            HandlePlayerFame.DEATHSTREAK.put(player.getUniqueId().toString(), 0);
        }

        // Time
        TimedPlayer tPlayer = this.pvpTitles.getManager().getTimerManager().getPlayer(player);
        tPlayer.stopSession();
        this.pvpTitles.getManager().getMovementManager().removeLastMovement(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (cm.params.isCheckAFK()) { // Optimizacion
            Player player = event.getPlayer();
            Location from = event.getFrom();
            Location to = event.getTo();

            if ((from.getX() == to.getX()) && (from.getY() == to.getY()) && (from.getZ() == to.getZ())) {
                return;
            }

            this.pvpTitles.getManager().getMovementManager().addLastMovement(player);
        }
    }

    /**
     * Método para ajustar los puntos pvp del jugador cuanto hace una kill
     *
     * @param death Evento PlayerDeathEvent
     */
    @EventHandler
    public void onKill(PlayerDeathEvent death) {
        // Compruebo si el mundo esta en la blacklist
        if (this.cm.params.getAffectedWorlds().contains(death.getEntity().getWorld().getName().toLowerCase())) {
            if (!this.cm.params.isPoints()) {
                return;
            }
        }
        
        ConfigDataStore params = Manager.getInstance().params;

        Player victim = death.getEntity();
        String victimuuid = victim.getUniqueId().toString();

        String tag = this.cm.params.getTag();
        
        boolean resetByPlayer = params.hasResetOption(K_BY_PLAYER);
        boolean resetByEnv = params.hasResetOption(K_BY_ENVIRONMENT);
        
        boolean killedByPlayer = death.getEntity().getKiller() instanceof Player;

        //<editor-fold defaultstate="collapsed" desc="VICTIM CALCULATOR">
        // Reset values for victim
        if ((resetByEnv && !killedByPlayer) || (resetByPlayer && killedByPlayer)) {
            // Final de la racha de bajas
            if (HandlePlayerFame.KILLSTREAK.containsKey(victimuuid)) {
                HandlePlayerFame.KILLSTREAK.put(victimuuid, 0);
            }
            // Añado una muerte más a la victima
            int deaths = 0;
            if (HandlePlayerFame.DEATHSTREAK.containsKey(victimuuid)) {
                deaths = DEATHSTREAK.get(victimuuid) + 1;
            }
            HandlePlayerFame.DEATHSTREAK.put(victimuuid, deaths);

            boolean meetsRequirements = (params.isLPWhenDyingJustByPlayers()) ? 
                    killedByPlayer : true;
            
            if (params.isEnableLPWhenDying() && meetsRequirements) {
                int previousFame = 0;
                try {
                    previousFame = this.cm.dbh.getDm().loadPlayerFame(victim.getUniqueId(), null);
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return; // Le bajaria los puntos posteriormente
                }

                params.addVariableToFormula("MOD",
                        params.getLostMod());
                params.addVariableToFormula("STREAK",
                        deaths);

                int gain = (int) params.getLostResult();
                int actualFame = previousFame - gain;
                if (actualFame < 0) {
                    actualFame = 0;
                }

                try {
                    this.cm.dbh.getDm().savePlayerFame(victim.getUniqueId(),
                            actualFame, null);

                    victim.sendMessage(getPluginName() + LangsFile.PLAYER_GETS_DIE
                            .getText(Localizer.getLocale(victim))
                            .replace("%fame%", Integer.toString(gain))
                            .replace("%tag%", tag));
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return;
                }

                long seconds = 0;
                try {
                    seconds = pvpTitles.getManager().dbh.getDm().loadPlayedTime(victim.getUniqueId());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return;
                }

                try {
                    Rank currentRank = RankManager.getRank(previousFame, seconds, victim);
                    Rank newRank = RankManager.getRank(actualFame, seconds, victim);

                    if (!currentRank.similar(newRank)) {
                        victim.sendMessage(getPluginName() + LangsFile
                                .PLAYER_NEW_RANK.getText(Localizer.getLocale(victim))
                                .replace("%newRank%", newRank.getDisplay()));
                    }
                } catch (RanksException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                FameEvent event = new FameEvent(victim, previousFame, gain);
                pvpTitles.getServer().getPluginManager().callEvent(event);
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="KILLER CALCULATOR">
        if (killedByPlayer) {
            Player killer = death.getEntity().getKiller();
            String killeruuid = killer.getUniqueId().toString();

            // Compruebo primero si el jugador esta vetado o se mato a si mismo
            if (afm.isVetado(killeruuid) || killeruuid.equalsIgnoreCase(victimuuid)) {
                if (afm.isVetado(killeruuid)) {
                    killer.sendMessage(getPluginName() + LangsFile.VETO_STARTED.getText(Localizer.getLocale(killer))
                            .replace("%tag%", this.cm.params.getTag())
                            .replace("%time%", splitToComponentTimes(afm.getVetoTime(killeruuid))));
                }
                return;
            }

            // Módulo anti farming
            antiFarm(killer, victimuuid);

            // Lo compruebo de nuevo por si le acaban de vetar
            if (afm.isVetado(killeruuid)) {
                return;
            }

            // Reinicio su racha de muerte
            DEATHSTREAK.put(killeruuid, 0);
            int kills = 0;
            // Si ya esta en el mapa guardo sus bajas y las actualizo
            if (KILLSTREAK.containsKey(killeruuid)) {
                kills = KILLSTREAK.get(killeruuid);
            }
            HandlePlayerFame.KILLSTREAK.put(killeruuid, ++kills);

            if (params.isEnableRPWhenKilling()) {
                int previousFame = 0;
                try {
                    previousFame = this.cm.dbh.getDm().loadPlayerFame(killer.getUniqueId(), null);
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return; // Le bajaria los puntos posteriormente
                }

                params.addVariableToFormula("MOD",
                        params.getReceivedMod());
                params.addVariableToFormula("STREAK",
                        kills);

                int gain = (int) Math.round((int) params.getReceivedResult()
                        * params.getMultiplier("Points", killer));
                int actualFame = previousFame + gain;

                try {
                    this.cm.dbh.getDm().savePlayerFame(killer.getUniqueId(), actualFame, null);
                    killer.sendMessage(getPluginName() + LangsFile.PLAYER_GETS_KILL
                            .getText(Localizer.getLocale(killer))
                            .replace("%killed%", victim.getName())
                            .replace("%fame%", Integer.toString(gain))
                            .replace("%tag%", tag));
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return;
                }

                long seconds = 0;
                try {
                    seconds = pvpTitles.getManager().dbh.getDm().loadPlayedTime(killer.getUniqueId());
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return;
                }

                try {
                    Rank currentRank = RankManager.getRank(previousFame, seconds, killer);
                    Rank newRank = RankManager.getRank(actualFame, seconds, killer);

                    if (!currentRank.similar(newRank)) {
                        killer.sendMessage(getPluginName() + LangsFile.PLAYER_NEW_RANK.getText(Localizer.getLocale(killer))
                                .replace("%newRank%", newRank.getDisplay()));
                    }
                } catch (RanksException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }

                FameEvent event = new FameEvent(killer, previousFame, gain);
                pvpTitles.getServer().getPluginManager().callEvent(event);
            }
        }
        //</editor-fold>
    }

    /**
     * Método para evitar que farmen puntos para los titulos pvp
     *
     * @param killer String con el nombre del asesino
     * @param victimuuid String con el nombre del asesinado
     */
    private void antiFarm(final Player killer, String victimuuid) {
        final String killeruuid = killer.getUniqueId().toString();

        if (afm.hasKiller(killeruuid)) {
            CleanTaskManager ck = csKiller.get(killeruuid);

            // Ya lo ha matado antes
            if (afm.hasVictim(killeruuid, victimuuid)) {
                ck.cleanVictim(victimuuid); // Cancel task

                if (afm.getKillsOnVictim(killeruuid, victimuuid) > this.cm.params.getMaxKills() - 1) {
                    afm.vetar(killeruuid, System.currentTimeMillis());
                    ck.cleanAll(); // Cancel all tasks

                    killer.sendMessage(getPluginName() + LangsFile.VETO_STARTED.getText(Localizer.getLocale(killer))
                            .replace("%tag%", this.cm.params.getTag())
                            .replace("%time%", splitToComponentTimes(this.cm.params.getVetoTime())));

                    pvpTitles.getServer().getScheduler().runTaskLaterAsynchronously(pvpTitles, new Runnable() {
                        @Override
                        public void run() {
                            afm.cleanVeto(killeruuid);

                            // Limpio su historial
                            afm.cleanAllVictims(killeruuid);

                            killer.sendMessage(getPluginName() + LangsFile.VETO_FINISHED.getText(Localizer.getLocale(killer)));
                        }
                    }, this.cm.params.getVetoTime() * TICKS);

                    return;
                }
            }

            // Si llega aqui no ha abusado, de momento...
            afm.addKillOnVictim(killeruuid, victimuuid);
            // Evento de limpieza...
            ck.addVictim(victimuuid);

        } else {
            CleanTaskManager ck = new CleanTaskManager(afm, killeruuid);

            afm.addKiller(killeruuid);
            afm.addKillOnVictim(killeruuid, victimuuid);
            ck.addVictim(victimuuid);

            csKiller.put(killeruuid, ck);
        }
    }

    public static AntiFarmManager getAfm() {
        return afm;
    }

    /**
     * Método para recibir la racha de bajas de un jugador
     *
     * @param uuid UUID
     * @return Entero
     */
    public static int getKillStreakFrom(String uuid) {
        if (HandlePlayerFame.KILLSTREAK.containsKey(uuid)) {
            return HandlePlayerFame.KILLSTREAK.get(uuid);
        } else {
            return 0;
        }
    }

    /**
     * Método para mejorar el rendimiento del playerConnection
     *
     * @param player Player
     * @param changeworld Player changed world?
     *
     * @return should connect?
     */
    public static boolean shouldDoPlayerConnection(Player player, boolean changeworld) {
        boolean s = false;

        if (ALREADY_LOGGED.containsKey(player.getUniqueId())) {
            Date before = new Date(ALREADY_LOGGED.get(player.getUniqueId()));
            int msdaytime = 1000 * 60 * 60 * 12; // Twelve hours
            Date minimum = new Date(before.getTime() + msdaytime);

            if (new Date().after(minimum)) {
                ALREADY_LOGGED.put(player.getUniqueId(), new Date().getTime());
                s = true;
            } else if (changeworld) {
                World world = player.getWorld();
                if (!ALREADY_VISITED.get(player.getUniqueId()).contains(world.getName())) {
                    ALREADY_VISITED.get(player.getUniqueId()).add(world.getName());
                    s = true;
                }
            }
        } else {
            ALREADY_LOGGED.put(player.getUniqueId(), new Date().getTime());
            final String world = player.getWorld().getName();
            ALREADY_VISITED.put(player.getUniqueId(), new ArrayList() {
                {
                    this.add(world);
                }
            });
            s = true;
        }

        return s;
    }
}
