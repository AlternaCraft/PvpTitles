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

import com.alternacraft.pvptitles.Events.FameAddEvent;
import com.alternacraft.pvptitles.Events.FameEvent;
import com.alternacraft.pvptitles.Events.FameSetEvent;
import com.alternacraft.pvptitles.Events.RankChangedEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Hooks.VaultHook;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Ranks;
import com.alternacraft.pvptitles.Misc.StrUtils;
import com.alternacraft.pvptitles.Misc.TimedPlayer;
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
        this.dm = pt.getManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFame(FameEvent e) {
        OfflinePlayer pl = e.getOfflinePlayer();
        
        if (pl == null) {
            e.setCancelled(true);
            return;
        }

        long seconds = 0;
        try {
            seconds = dm.dbh.getDm().loadPlayedTime(pl.getUniqueId());
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }
        
        // Comandos
        if (!(e instanceof FameSetEvent) && !(e instanceof FameAddEvent)) {
            Map<String, Map<String, List<String>>> kills = pt.getManager().rewards.get("onKill");
            if (kills != null) {
                setValues(kills.get(null), e.getOfflinePlayer());
            }
        }

        Map<String, Map<String, List<String>>> fame = pt.getManager().rewards.get("onFame");
        if (fame != null) {
            for (String cantidad : fame.keySet()) {
                if (e.getFame() < Integer.parseInt(cantidad)
                        && (e.getFameTotal()) >= Integer.valueOf(cantidad)) {
                    setValues(fame.get(cantidad), e.getOfflinePlayer());
                    break;
                }
            }
        }

        Map<String, Map<String, List<String>>> rank = pt.getManager().rewards.get("onRank");
        if (rank != null) {            
            for (String rango : rank.keySet()) {
                try {
                    String oldRank = StrUtils.removeColors(Ranks.getRank(e.getFame(), seconds));
                    String newRank = StrUtils.removeColors(Ranks.getRank(e.getFameTotal(), seconds));
                    if (rango.equals(newRank) && !rango.equals(oldRank)) {
                        setValues(rank.get(rango), e.getOfflinePlayer());
                        break;
                    }
                } catch (RanksException ex) {
                    CustomLogger.logArrayError(ex.getCustomStackTrace());
                }
            }
        }

        Map<String, Map<String, List<String>>> killstreak = pt.getManager().rewards.get("onKillstreak");
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
            int fameA = e.getFame();
            int fameD = e.getFameTotal();

            TimedPlayer tp = pt.getManager().getTimerManager().getPlayer(pl);
            long totalTime = seconds + ((tp == null) ? 0 : tp.getTotalOnline());

            try {
                String actualRank = Ranks.getRank(fameA, totalTime);
                String newRank = Ranks.getRank(fameD, totalTime);

                // Ha conseguido otro rango
                if (!actualRank.equalsIgnoreCase(newRank)) {
                    pt.getServer().getPluginManager().callEvent(new RankChangedEvent(
                            pl, actualRank, newRank));
                }
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
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
                cmd = StrUtils.translateColors(cmd);
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
        long seconds = 0;
        try {
            seconds = dm.dbh.getDm().loadPlayedTime(e.getOfflinePlayer().getUniqueId());
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        if (!e.isSilent()) {
            String rank = "";
            try {
                rank = Ranks.getRank(e.getFameTotal(), seconds);
            } catch (RanksException ex) {
                CustomLogger.logArrayError(ex.getCustomStackTrace());
            }

            if (e.getWorldname() != null) {
                pl.sendMessage(getPluginName() + LangsFile.FAME_MW_CHANGE_PLAYER.getText(Localizer.getLocale(pl))
                        .replace("%fame%", String.valueOf(e.getFameTotal()))
                        .replace("%rank%", rank)
                        .replace("%world%", e.getWorldname())
                        .replace("%tag%", this.dm.params.getTag())
                );
            } else {
                pl.sendMessage(getPluginName() + LangsFile.FAME_CHANGE_PLAYER.getText(Localizer.getLocale(pl))
                        .replace("%fame%", String.valueOf(e.getFameTotal()))
                        .replace("%rank%", rank)
                        .replace("%tag%", this.dm.params.getTag())
                );
            }
        }
    }
}
