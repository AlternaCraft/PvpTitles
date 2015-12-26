package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Events.FameEvent;
import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Localizer;
import static es.jlh.pvptitles.Misc.Utils.splitToComponentTimes;
import es.jlh.pvptitles.Managers.AntiFarmManager;
import es.jlh.pvptitles.Managers.CleanTaskManager;
import es.jlh.pvptitles.Objects.TimedPlayer;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author AlternaCraft
 */
public class HandlePlayerFame implements Listener {

    /**
     * contante TICKS para saber el tiempo en segundos
     */
    private static final int TICKS = 20;

    // Variable temporal
    public static final Map<String, Integer> racha = new HashMap();

    private Manager cm = null;
    private AntiFarmManager afm = null;
    private Map<String, CleanTaskManager> csKiller = new HashMap();

    private final PvpTitles pvpTitles;

    /**
     * Contructor de la clase
     *
     * @param pt Plugin
     */
    public HandlePlayerFame(PvpTitles pt) {
        this.cm = pt.cm;
        this.pvpTitles = pt;

        afm = new AntiFarmManager(pvpTitles);
    }

    /**
     * Método para crear el jugador en caso de que no exista
     *
     * @param event Evento PlayerLoginEvent
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        cm.dbh.getDm().PlayerConnection(player);

        // Time
        TimedPlayer tPlayer = this.pvpTitles.getPlayerManager().hasPlayer(player)
                ? this.pvpTitles.getPlayerManager().getPlayer(player) : new TimedPlayer(this.pvpTitles, player);

        tPlayer.startSession();

        this.pvpTitles.getMovementManager().addLastMovement(player);

        if (!this.pvpTitles.getPlayerManager().hasPlayer(player)) {
            this.pvpTitles.getPlayerManager().addPlayer(tPlayer);
        }
    }
    
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        cm.dbh.getDm().PlayerConnection(player);
    }

    /**
     * Método para reiniciar los puntos del jugador cuando deje el server
     *
     * @param event Evento PlayerQuitEvent
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cm.dbh.getDm().PlayerConnection(player);
        HandlePlayerFame.racha.put(player.getName(), 0);

        // Time
        TimedPlayer tPlayer = this.pvpTitles.getPlayerManager().getPlayer(player);

        tPlayer.stopSession();

        this.pvpTitles.getMovementManager().removeLastMovement(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if ((from.getX() == to.getX()) && (from.getY() == to.getY()) && (from.getZ() == to.getZ())) {
            return;
        }

        this.pvpTitles.getMovementManager().addLastMovement(player);
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
            String victim = death.getEntity().getName();
            
            // Compruebo primero si el jugador esta vetado o se mato a si mismo
            if (afm.isVetado(killer.getName()) || killer.getName().equalsIgnoreCase(victim)) {
                if (afm.isVetado(killer.getName())) {
                    killer.sendMessage(PLUGIN + LangFile.VETO_STARTED.getText(Localizer.getLocale(killer))
                            .replace("%tag%", this.cm.params.getTag())
                            .replace("%time%", splitToComponentTimes(afm.getVetoTime(killer.getName()))));
                }
                return;
            }

            // Modulo anti farm
            antiFarm(killer, victim);

            // Lo compruebo de nuevo por si le acaban de vetar
            if (afm.isVetado(killer.getName())) {
                return;
            }

            // Si ya esta en el mapa guardo sus bajas
            if (racha.containsKey(killer.getName())) {
                kills = racha.get(killer.getName());
            }

            // Final de la racha de bajas
            if (HandlePlayerFame.racha.containsKey(victim)) {
                HandlePlayerFame.racha.put(victim, 0);
            }

            // Aniado el nuevo valor de kills
            int fame = this.cm.dbh.getDm().loadPlayerFame(killer.getUniqueId(), null);

            kills++;
            this.calculateRank(victim, killer, fame, kills);            
            HandlePlayerFame.racha.put(killer.getName(), kills);
        }
    }

    /**
     * Método para evitar que farmen puntos para los titulos pvp
     *
     * @param killer String con el nombre del asesino
     * @param victim String con el nombre del asesinado
     */
    private void antiFarm(final Player killer, String victim) {
        if (afm.hasKiller(killer.getName())) {
            CleanTaskManager ck = csKiller.get(killer.getName());

            // Ya lo ha matado antes
            if (afm.hasVictim(killer.getName(), victim)) {
                ck.cleanVictim(victim); // Cancel task

                if (afm.getKillsOnVictim(killer.getName(), victim) > this.cm.params.getKills() - 1) {
                    afm.vetar(killer.getName(), System.currentTimeMillis());
                    ck.cleanAll(); // Cancel all tasks

                    killer.sendMessage(PLUGIN + LangFile.VETO_STARTED.getText(Localizer.getLocale(killer))
                            .replace("%tag%", this.cm.params.getTag())
                            .replace("%time%", splitToComponentTimes(this.cm.params.getTimeV())));

                    Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(pvpTitles, new Runnable() {
                        @Override
                        public void run() {
                            afm.cleanVeto(killer.getName());

                            // Limpio su historial
                            afm.cleanAllVictims(killer.getName());

                            killer.sendMessage(PLUGIN + LangFile.VETO_FINISHED.getText(Localizer.getLocale(killer)));
                        }
                    }, this.cm.params.getTimeV() * TICKS);

                    return;
                }
            }

            // Si llega aqui no ha abusado, de momento...
            afm.addKillOnVictim(killer.getName(), victim);
            // Evento de limpieza...
            ck.addVictim(victim);

        } else {
            CleanTaskManager ck = new CleanTaskManager(afm, killer.getName());

            afm.addKiller(killer.getName());
            afm.addKillOnVictim(killer.getName(), victim);
            ck.addVictim(victim);

            csKiller.put(killer.getName(), ck);
        }
    }

    /**
     * Método para calcular los puntos que ha ganado y si mantiene o no el rango
     *
     * @param killed Jugador eliminado
     * @param player Jugador superviviente
     * @param fame Puntos pvp
     * @param kills Racha de bajas
     */
    private void calculateRank(String killed, Player player, int fame, int kills) {
        int seconds = pvpTitles.cm.dbh.getDm().loadPlayedTime(player.getUniqueId());

        int fameAntes = fame;
        String tag = this.cm.params.getTag();
        double mod = Math.abs(this.cm.params.getMod());

        int fameRec = (int) Math.ceil((kills-1 * mod) + 1);

        player.sendMessage(PLUGIN + LangFile.PLAYER_KILLED.getText(Localizer.getLocale(player))
                .replace("%killed%", killed)
                .replace("%fameRec%", Integer.toString(fameRec))
                .replace("%tag%", tag));

        int fameDespues = fameAntes + fameRec;

        this.cm.dbh.getDm().savePlayerFame(player.getUniqueId(), fameDespues);

        String currentRank = Ranks.GetRank(fameAntes, seconds);
        String newRank = Ranks.GetRank(fameDespues, seconds);

        if (!currentRank.equalsIgnoreCase(newRank)) {
            player.sendMessage(PLUGIN + LangFile.PLAYER_NEW_RANK.getText(Localizer.getLocale(player))
                    .replace("%newRank%", newRank));
        }

        FameEvent event = new FameEvent(player, fameAntes, fameRec);
        event.setKillstreak(kills); // Nueva baja
        
        Bukkit.getServer().getPluginManager().callEvent(event);
    }
}
