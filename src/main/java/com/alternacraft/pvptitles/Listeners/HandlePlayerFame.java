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
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Ranks;
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

    // To get a better performance
    public static final Map<UUID, Long> ALREADY_LOGGED = new HashMap();
    public static final Map<UUID, List<String>> ALREADY_VISITED = new HashMap();

    // Variable temporal
    private static final Map<String, Integer> KILLSTREAK = new HashMap();

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
                CustomLogger.logError(ex.getCustomMessage());
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
                CustomLogger.logError(ex.getCustomMessage());
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
                CustomLogger.logError(ex.getCustomMessage());
                return;
            }
        }

        HandlePlayerFame.KILLSTREAK.put(player.getUniqueId().toString(), 0);

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

        if (death.getEntity().getKiller() instanceof Player) {
            int kills = 0;

            Player killer = death.getEntity().getKiller();
            String killeruuid = killer.getUniqueId().toString();
            String victimuuid = death.getEntity().getUniqueId().toString();

            // Compruebo primero si el jugador esta vetado o se mato a si mismo
            if (afm.isVetado(killeruuid) || killeruuid.equalsIgnoreCase(victimuuid)) {
                if (afm.isVetado(killeruuid)) {
                    killer.sendMessage(getPluginName() + LangsFile.VETO_STARTED.getText(Localizer.getLocale(killer))
                            .replace("%tag%", this.cm.params.getTag())
                            .replace("%time%", splitToComponentTimes(afm.getVetoTime(killeruuid))));
                }
                return;
            }

            // Guardo su fama anterior
            int fame = 0;
            try {
                fame = this.cm.dbh.getDm().loadPlayerFame(killer.getUniqueId(), null);
            } catch (DBException ex) {
                CustomLogger.logError(ex.getCustomMessage());
                return; // Le bajaria los puntos posteriormente
            }

            // Modulo anti farm
            antiFarm(killer, victimuuid);

            // Lo compruebo de nuevo por si le acaban de vetar
            if (afm.isVetado(killeruuid)) {
                return;
            }

            // Si ya esta en el mapa guardo sus bajas
            if (KILLSTREAK.containsKey(killeruuid)) {
                kills = KILLSTREAK.get(killeruuid);
            }

            // Final de la racha de bajas
            if (HandlePlayerFame.KILLSTREAK.containsKey(victimuuid)) {
                HandlePlayerFame.KILLSTREAK.put(victimuuid, 0);
            }

            kills += 1;
            this.calculateRank(death.getEntity().getName(), killer, fame, kills);
            HandlePlayerFame.KILLSTREAK.put(killeruuid, kills);
        }
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

                if (afm.getKillsOnVictim(killeruuid, victimuuid) > this.cm.params.getKills() - 1) {
                    afm.vetar(killeruuid, System.currentTimeMillis());
                    ck.cleanAll(); // Cancel all tasks

                    killer.sendMessage(getPluginName() + LangsFile.VETO_STARTED.getText(Localizer.getLocale(killer))
                            .replace("%tag%", this.cm.params.getTag())
                            .replace("%time%", splitToComponentTimes(this.cm.params.getTimeV())));

                    pvpTitles.getServer().getScheduler().runTaskLaterAsynchronously(pvpTitles, new Runnable() {
                        @Override
                        public void run() {
                            afm.cleanVeto(killeruuid);

                            // Limpio su historial
                            afm.cleanAllVictims(killeruuid);

                            killer.sendMessage(getPluginName() + LangsFile.VETO_FINISHED.getText(Localizer.getLocale(killer)));
                        }
                    }, this.cm.params.getTimeV() * TICKS);

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

    /**
     * Método para calcular los puntos que ha ganado y si mantiene o no el rango
     *
     * @param killed Nombre del jugador eliminado
     * @param player Jugador superviviente
     * @param fame Puntos pvp
     * @param kills Racha de bajas
     */
    private void calculateRank(String killed, Player player, int fame, int kills) {
        int fameAntes = fame;
        String tag = this.cm.params.getTag();
        double mod = Math.abs(this.cm.params.getMod());

        int fameRec = (int) Math.ceil((kills - 1 * mod) + 1);

        int fameDespues = fameAntes + fameRec;

        try {
            this.cm.dbh.getDm().savePlayerFame(player.getUniqueId(), fameDespues, null);
        } catch (DBException ex) {
            CustomLogger.logError(ex.getCustomMessage());
            return;
        }

        player.sendMessage(getPluginName() + LangsFile.PLAYER_KILLED.getText(Localizer.getLocale(player))
                .replace("%killed%", killed)
                .replace("%fameRec%", Integer.toString(fameRec))
                .replace("%tag%", tag));

        long seconds = 0;
        try {
            seconds = pvpTitles.getManager().dbh.getDm().loadPlayedTime(player.getUniqueId());
        } catch (DBException ex) {
            CustomLogger.logError(ex.getCustomMessage());
            return;
        }

        try {
            String currentRank = Ranks.getRank(fameAntes, seconds);
            String newRank = Ranks.getRank(fameDespues, seconds);

            if (!currentRank.equalsIgnoreCase(newRank)) {
                player.sendMessage(getPluginName() + LangsFile.PLAYER_NEW_RANK.getText(Localizer.getLocale(player))
                        .replace("%newRank%", newRank));
            }
        } catch (RanksException ex) {
            CustomLogger.logError(ex.getCustomMessage());
        }

        FameEvent event = new FameEvent(player, fameAntes, fameRec);
        event.setKillstreak(kills); // Nueva baja

        pvpTitles.getServer().getPluginManager().callEvent(event);
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
            }
            else if (changeworld) {
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
