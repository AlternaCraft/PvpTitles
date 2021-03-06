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
package com.alternacraft.pvptitles.Listeners;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Events.FameEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.AntiFarmManager;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Rank;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
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

import java.util.*;

import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import static com.alternacraft.pvptitles.Misc.StrUtils.splitToComponentTimes;

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

    // To get a better performance
    public static final Map<UUID, Long> ALREADY_LOGGED = new HashMap();
    public static final Map<UUID, List<String>> ALREADY_VISITED = new HashMap();

    // Variable temporal
    private static final Map<String, Integer> KILLSTREAK = new HashMap();
    private static final Map<String, Integer> DEATHSTREAK = new HashMap();

    private Manager cm = null;
    private static AntiFarmManager afm = null;

    private PvpTitles pvpTitles = null;

    /**
     * Contructor de la clase
     *
     * @param pt Plugin
     */
    public HandlePlayerFame(PvpTitles pt) {
        this.cm = pt.getManager();
        this.pvpTitles = pt;

        afm = new AntiFarmManager();
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
                cm.getDBH().getDM().playerConnection(player);
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (this.pvpTitles.getManager().params.isMw_enabled()
                && shouldDoPlayerConnection(player, true)) {
            try {
                cm.getDBH().getDM().playerConnection(player);
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
                cm.getDBH().getDM().playerConnection(player);
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

    @EventHandler(ignoreCancelled = true)
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
    @EventHandler(ignoreCancelled = true)
    public void onKill(PlayerDeathEvent death) {
        // Compruebo si el mundo esta en la blacklist
        if (this.cm.params.getAffectedWorlds().contains(death.getEntity().getWorld().getName().toLowerCase())) {
            if (!this.cm.params.isPoints()) {
                return;
            }
        }

        ConfigDataStore params = Manager.getInstance().params;

        Player victim = death.getEntity();
        UUID victimuuid = victim.getUniqueId();
        Player killer = death.getEntity().getKiller();

        String tag = this.cm.params.getTag();
        int deaths = 0;

        boolean killedByPlayer = killer != null;

        if (!params.isAddDeathOnlyByPlayer()
                || params.isAddDeathOnlyByPlayer() && killedByPlayer) {
            // Añado una muerte más a la victima
            if (HandlePlayerFame.DEATHSTREAK.containsKey(victimuuid.toString())) {
                deaths = DEATHSTREAK.get(victimuuid.toString());
            }
            HandlePlayerFame.DEATHSTREAK.put(victimuuid.toString(), ++deaths);
        }

        //<editor-fold defaultstate="collapsed" desc="VICTIM CALCULATOR">
        boolean resetByPlayer = params.hasResetOption(K_BY_PLAYER);
        boolean resetByEnv = params.hasResetOption(K_BY_ENVIRONMENT);

        // Reset values for victim
        if ((resetByEnv && !killedByPlayer) || (resetByPlayer && killedByPlayer)) {
            // Final de la racha de bajas
            if (HandlePlayerFame.KILLSTREAK.containsKey(victimuuid.toString())) {
                HandlePlayerFame.KILLSTREAK.put(victimuuid.toString(), 0);
            }
        }

        boolean meetsRequirements = (!params.isLPWhenDyingJustByPlayers()) || killedByPlayer;

        if (params.isEnableLPWhenDying() && meetsRequirements) {
            int previousFame;
            try {
                previousFame = this.cm.getDBH().getDM().loadPlayerFame(victim.getUniqueId(), null);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
                return; // Le bajaria los puntos posteriormente
            }

            params.addVariableToFormula("MOD", params.getLostMod());
            params.addVariableToFormula("STREAK", deaths);
            params.addVariableToFormula("VPOINTS", previousFame);
            
            if (killedByPlayer) {
                try {
                    params.addVariableToFormula("KPOINTS", this.cm.getDBH().getDM()
                            .loadPlayerFame(killer.getUniqueId(), null));
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }
            }

            int gain = (int) params.getLostResult();
            int actualFame = previousFame - gain;
            if (actualFame < 0) {
                actualFame = 0;
            }

            try {
                this.cm.getDBH().getDM().savePlayerFame(victimuuid, actualFame, null);
                victim.sendMessage(getPluginName() + LangsFile.PLAYER_GETS_DIE
                        .getText(Localizer.getLocale(victim))
                        .replace("%fame%", Integer.toString(gain))
                        .replace("%tag%", tag));
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
                return;
            }

            long seconds;
            try {
                seconds = pvpTitles.getManager().getDBH().getDM().loadPlayedTime(victimuuid);
            } catch (DBException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
                return;
            }

            try {
                Rank currentRank = RankManager.getRank(previousFame, seconds, victim);
                Rank newRank = RankManager.getRank(actualFame, seconds, victim);

                if (!currentRank.similar(newRank)) {
                    victim.sendMessage(getPluginName() + LangsFile.PLAYER_RANK_DEMOTE.getText(Localizer.getLocale(victim))
                            .replace("%newRank%", newRank.getDisplay()));
                }
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            FameEvent event = new FameEvent(victim, previousFame, -gain);
            pvpTitles.getServer().getPluginManager().callEvent(event);
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="KILLER CALCULATOR">
        if (killedByPlayer) {
            UUID killeruuid = killer.getUniqueId();

            // Compruebo primero si el jugador esta vetado o se mato a si mismo
            boolean vetoed = (Manager.getInstance().params.isPreventFromEvery()) ?
                    afm.isVetoed(killeruuid.toString()) : afm.isVetoedFor(
                            killeruuid.toString(), victimuuid.toString()
                    );
            boolean suicide = killeruuid.toString().equalsIgnoreCase(victimuuid.toString());
            if (vetoed || suicide) {
                if (vetoed) {
                    if (this.cm.params.isPreventFromEvery()) {
                        killer.sendMessage(getPluginName() + LangsFile.VETO_STARTED
                                .getText(Localizer.getLocale(killer))
                                .replace("%tag%", this.cm.params.getTag())
                                .replace("%time%", splitToComponentTimes(
                                        afm.getVetoTime(killeruuid.toString())
                                ))
                        );
                    } else {
                        killer.sendMessage(getPluginName() + LangsFile.VETOED_BY_STARTED
                                .getText(Localizer.getLocale(killer))
                                .replace("%tag%", this.cm.params.getTag())
                                .replace("%time%", splitToComponentTimes(
                                        afm.getVetoTimeOn(killeruuid.toString(),
                                                victimuuid.toString())
                                ))
                                .replace("%player%", victim.getName())
                        );
                    }
                }
                return;
            }

            // Módulo anti farming
            antiFarm(killer, victim);

            vetoed = (Manager.getInstance().params.isPreventFromEvery()) ?
                    afm.isVetoed(killeruuid.toString()) : afm.isVetoedFor(
                            killeruuid.toString(), victimuuid.toString()
                    );

            // Lo compruebo de nuevo por si le acaban de vetar
            if (vetoed) {
                return;
            }

            // Reinicio su racha de muerte
            DEATHSTREAK.put(killeruuid.toString(), 0);
            int kills = 0;
            // Si ya esta en el mapa guardo sus bajas y las actualizo
            if (KILLSTREAK.containsKey(killeruuid.toString())) {
                kills = KILLSTREAK.get(killeruuid.toString());
            }
            HandlePlayerFame.KILLSTREAK.put(killeruuid.toString(), ++kills);

            if (params.isEnableRPWhenKilling()) {
                int previousFame;
                try {
                    previousFame = this.cm.getDBH().getDM().loadPlayerFame(killeruuid, null);
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return; // Le bajaria los puntos posteriormente
                }

                params.addVariableToFormula("MOD",
                        params.getReceivedMod());
                params.addVariableToFormula("STREAK",
                        kills);
                try {
                    params.addVariableToFormula("VPOINTS", this.cm.getDBH().getDM()
                            .loadPlayerFame(victimuuid, null));
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }
                params.addVariableToFormula("KPOINTS",
                        previousFame);

                int gain = (int) Math.round((int) params.getReceivedResult()
                        * params.getMultiplier("Points", killer));
                int actualFame = previousFame + gain;

                try {
                    this.cm.getDBH().getDM().savePlayerFame(killeruuid, actualFame, null);
                    killer.sendMessage(getPluginName() + LangsFile.PLAYER_GETS_KILL
                            .getText(Localizer.getLocale(killer))
                            .replace("%killed%", victim.getName())
                            .replace("%fame%", Integer.toString(gain))
                            .replace("%tag%", tag));
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return;
                }

                long seconds;
                try {
                    seconds = pvpTitles.getManager().getDBH().getDM().loadPlayedTime(killeruuid);
                } catch (DBException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                    return;
                }

                try {
                    Rank currentRank = RankManager.getRank(previousFame, seconds, killer);
                    Rank newRank = RankManager.getRank(actualFame, seconds, killer);

                    if (!currentRank.similar(newRank)) {
                        killer.sendMessage(getPluginName() + LangsFile.PLAYER_RANK_PROMOTE
                                .getText(Localizer.getLocale(killer))
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
     * @param victim String con el nombre del asesinado
     */
    private void antiFarm(final Player killer, Player victim) {
        final String killeruuid = killer.getUniqueId().toString();
        final String victimuuid = victim.getUniqueId().toString();

        if (afm.hasKiller(killeruuid)) {
            // Ya lo ha matado antes
            if (afm.hasVictim(killeruuid, victimuuid)) {
                if (afm.getKillsOnVictim(killeruuid, victimuuid) > this.cm.params.getMaxKills() - 1) {
                    afm.veto(killeruuid, victimuuid, System.currentTimeMillis());
                    if (this.cm.params.isPreventFromEvery()) {
                        killer.sendMessage(getPluginName() + LangsFile.VETO_STARTED
                                .getText(Localizer.getLocale(killer))
                                .replace("%tag%", this.cm.params.getTag())
                                .replace("%time%", splitToComponentTimes(this.cm.params.getVetoTime()))
                        );
                    } else {
                        killer.sendMessage(getPluginName() + LangsFile.VETOED_BY_STARTED
                                .getText(Localizer.getLocale(killer))
                                .replace("%tag%", this.cm.params.getTag())
                                .replace("%time%", splitToComponentTimes(this.cm.params.getVetoTime()))
                                .replace("%player%", victim.getName())
                        );
                    }
                    return;
                }
            }
        } else {
            afm.addKiller(killeruuid);
        }
        afm.addKillOnVictim(killeruuid, victimuuid);
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
