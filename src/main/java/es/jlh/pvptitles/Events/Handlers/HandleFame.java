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
package es.jlh.pvptitles.Events.Handlers;

import es.jlh.pvptitles.Backend.Exceptions.DBException;
import es.jlh.pvptitles.Events.FameAddEvent;
import es.jlh.pvptitles.Events.FameEvent;
import es.jlh.pvptitles.Events.FameSetEvent;
import es.jlh.pvptitles.Events.RankChangedEvent;
import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Hook.VaultHook;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.Timer.TimedPlayer;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Misc.Ranks;
import java.util.List;
import java.util.Map;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class HandleFame implements Listener {

    private final PvpTitles pt;
    private final Manager dm;

    /**
     * Contructor de la clase
     *
     * @param pt Plugin
     */
    public HandleFame(PvpTitles pt) {
        this.pt = pt;
        this.dm = pt.manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFame(FameEvent e) {
        if (e.getOfflinePlayer() == null) {
            e.setCancelled(true);
            return;
        }

        // Comandos
        if (!(e instanceof FameSetEvent) && !(e instanceof FameAddEvent)) {
            Map<String, Map<String, List<String>>> kills = pt.manager.commandsRw.get("onKill");
            if (kills != null) {
                setValues(kills.get(null), e.getOfflinePlayer());
            }
        }

        Map<String, Map<String, List<String>>> fame = pt.manager.commandsRw.get("onFame");
        if (fame != null) {
            for (String cantidad : fame.keySet()) {
                if (e.getFame() < Integer.parseInt(cantidad)
                        && (e.getFameTotal()) >= Integer.valueOf(cantidad)) {
                    setValues(fame.get(cantidad), e.getOfflinePlayer());
                    break;
                }
            }
        }

        Map<String, Map<String, List<String>>> rank = pt.manager.commandsRw.get("onRank");
        if (rank != null) {
            for (String rango : rank.keySet()) {
                int seconds = 0;
                try {
                    seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());
                } catch (DBException ex) {
                    PvpTitles.logError(ex.getCustomMessage(), null);
                }

                String lastRank = Ranks.getRank(e.getFame(), seconds);
                if (rango.equals(Ranks.getRank(e.getFameTotal(), seconds)) && !rango.equals(lastRank)) {
                    setValues(rank.get(rango), e.getOfflinePlayer());
                    break;
                }
            }
        }

        Map<String, Map<String, List<String>>> killstreak = pt.manager.commandsRw.get("onKillstreak");
        if (killstreak != null) {
            for (String ks : killstreak.keySet()) {
                if (e.getKillstreak() == Integer.valueOf(ks)) {
                    setValues(killstreak.get(ks), e.getOfflinePlayer());
                    break;
                }
            }
        }

        // Nuevo rango
        if (e.getOfflinePlayer().isOnline()) {
            Player pl = (Player) e.getOfflinePlayer();

            int fameA = e.getFame();
            int fameD = e.getFameTotal();

            int oldTime = 0;
            try {
                oldTime = dm.getDbh().getDm().loadPlayedTime(pl.getUniqueId());
            } catch (DBException ex) {
                PvpTitles.logError(ex.getCustomMessage(), null);
            }
            TimedPlayer tp = pt.getTimerManager().getPlayer(pl);
            int totalTime = oldTime + ((tp == null) ? 0 : tp.getTotalOnline());

            String actualRank = Ranks.getRank(fameA, totalTime);
            String newRank = Ranks.getRank(fameD, totalTime);

            // Ha conseguido otro rango
            if (!actualRank.equalsIgnoreCase(newRank)) {
                pt.getServer().getPluginManager().callEvent(new RankChangedEvent(
                        pl, actualRank, newRank));
            }
        }
    }

    private void setValues(Map<String, List<String>> data, OfflinePlayer pl) {
        if (VaultHook.ECONOMY_ENABLED) {
            Economy economy = VaultHook.economy;
            if (economy != null && data.containsKey("money")) {
                List<String> money = data.get("money");
                if (money != null && !money.isEmpty()) {
                    Double cant = Double.valueOf(money.get(0));
                    economy.depositPlayer(pl, cant);
                }
            }
        }
        if (data.containsKey("commands")) {
            for (String cmd : data.get("commands")) {
                cmd = cmd.replaceAll("<[pP]layer>", pl.getName());
                pt.getServer().dispatchCommand(pt.getServer().getConsoleSender(), cmd);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSetFame(FameSetEvent e) {
        fameLogic(e);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAddFame(FameAddEvent e) {
        fameLogic(e);
    }

    private void fameLogic(FameEvent e) {
        if (e.isCancelled() || !e.getOfflinePlayer().isOnline()) {
            return;
        }

        Player pl = (Player) e.getOfflinePlayer();
        int seconds = 0;
        try {
            seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());
        } catch (DBException ex) {
            PvpTitles.logError(ex.getCustomMessage(), null);
        }

        if (!e.isSilent()) {
            String rank = Ranks.getRank(e.getFameTotal(), seconds);

            if (e.getWorldname() != null) {
                pl.sendMessage(PLUGIN + LangFile.FAME_MW_CHANGE_PLAYER.getText(Localizer.getLocale(pl))
                        .replace("%fame%", String.valueOf(e.getFameTotal()))
                        .replace("%rank%", rank)
                        .replace("%world%", e.getWorldname())
                        .replace("%tag%", this.dm.params.getTag())
                );
            } else {
                pl.sendMessage(PLUGIN + LangFile.FAME_CHANGE_PLAYER.getText(Localizer.getLocale(pl))
                        .replace("%fame%", String.valueOf(e.getFameTotal()))
                        .replace("%rank%", rank)
                        .replace("%tag%", this.dm.params.getTag())
                );
            }
        }
    }
}
