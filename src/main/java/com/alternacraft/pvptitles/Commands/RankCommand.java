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
package com.alternacraft.pvptitles.Commands;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Exceptions.RanksException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Files.LangsFile.LangType;
import static com.alternacraft.pvptitles.Files.TemplatesFile.FAME_TITLE_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.FAME_VALUE_TAG;
import com.alternacraft.pvptitles.Files.TemplatesFile.FILES;
import static com.alternacraft.pvptitles.Files.TemplatesFile.KS_TITLE_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.KS_VALUE_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.NEXT_RANK_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.PLUGIN_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.RANK_TITLE_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.RANK_VALUE_TAG;
import static com.alternacraft.pvptitles.Files.TemplatesFile.VETO_TAG;
import com.alternacraft.pvptitles.Listeners.HandlePlayerFame;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Rank;
import com.alternacraft.pvptitles.Misc.Rank.NextRank;
import com.alternacraft.pvptitles.Misc.StrUtils;
import static com.alternacraft.pvptitles.Misc.StrUtils.splitToComponentTimes;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RankCommand implements CommandExecutor {

    private PvpTitles pt = null;

    public RankCommand(PvpTitles pt) {
        this.pt = pt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangsFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (!(sender instanceof Player)) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_FORBIDDEN.getText(messages));
            return true;
        }

        Player player = (Player) sender;

        if (args.length <= 1) {
            String world = (args.length == 1 && Bukkit.getWorld(args[0]) != null) 
                    ? args[0] : null;
            this.HandleRankCmd(player, world);
            return true;
        }

        return false;
    }

    /**
     * Método para enviar los datos del rango de un jugador
     *
     * @param player Jugador que consulta los datos
     * @param world Specific world
     */
    private void HandleRankCmd(Player player, String world) {
        String uuid = player.getUniqueId().toString();

        int fame = 0;
        try {
            fame = pt.getManager().getDBH().getDM().loadPlayerFame(player.getUniqueId(), world);
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        int racha = HandlePlayerFame.getKillStreakFrom(uuid);

        long seconds = 0;
        try {
            seconds = pt.getManager().getDBH().getDM().loadPlayedTime(player.getUniqueId())
                    + pt.getManager().getTimerManager().getPlayer(player).getTotalOnline();
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
        }

        Rank actual = null;
        try {
            actual = RankManager.getRank(fame, seconds, player);
        } catch (RanksException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
            return;
        }
        NextRank next = RankManager.getNextRank(actual, fame, seconds, player);

        String tag = pt.getManager().params.getTag();
        LangType lang = Localizer.getLocale(player);
        List<String> lines = this.pt.getManager().templates.getFileContent(FILES.RANK_COMMAND);

        for (String line : lines) {
            String msg = line;

            if (!line.isEmpty()) {
                msg = msg
                        .replace(PLUGIN_TAG, PvpTitles.getDefaultPluginName())
                        .replace(RANK_TITLE_TAG, LangsFile.RANK_INFO_TITLE.getText(lang))
                        .replace(RANK_VALUE_TAG, actual.getDisplay())
                        .replace(FAME_TITLE_TAG, LangsFile.RANK_INFO_TAG.getText(lang)
                                .replace("%tag%", tag))
                        .replace(FAME_VALUE_TAG, String.valueOf(fame))
                        .replace(KS_TITLE_TAG, LangsFile.RANK_INFO_KS.getText(lang))
                        .replace(KS_VALUE_TAG, String.valueOf(racha));

                if (next != null) {
                    long time = next.timeToRankUp() - seconds;
                    if (time < 0) {
                        time = 0;
                    }
                    msg = msg
                            .replace(NEXT_RANK_TAG, LangsFile.RANK_INFO_NEXTRANK.getText(lang)
                                    .replace("%rankup%", String.valueOf(next.fameToRankUp()))
                                    .replace("%timeup%", StrUtils.splitToComponentTimes(time))
                                    .replace("%tag%", tag)
                                    .replace("%nextRank%", next.nextRankTitle()));
                } else if (msg.contains(NEXT_RANK_TAG)) {
                    continue;
                }

                if (msg.contains(VETO_TAG)) {
                    Set<String> keys = HandlePlayerFame.getAfm().getVetoes(player.getUniqueId().toString()).keySet();
                    String aux = "";
                    
                    if (keys.isEmpty())
                        continue;
                    
                    for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
                        String key = iterator.next();
                        
                        String time = splitToComponentTimes(HandlePlayerFame
                                .getAfm().getVetoTimeOn(uuid, key));
                        
                        if (Manager.getInstance().params.isPreventFromEvery()) {
                            aux = new StringBuilder(aux).append(
                                    msg
                                        .replace(VETO_TAG, LangsFile.VETOED_STARTED.getText(Localizer.getLocale(player))
                                        .replace("%tag%", pt.getManager().params.getTag())
                                        .replace("%time%", time))
                            ).toString();
                        } else {
                            aux = new StringBuilder(aux).append(
                                    msg
                                        .replace(VETO_TAG, LangsFile.VETOED_BY_STARTED.getText(Localizer.getLocale(player))
                                        .replace("%tag%", pt.getManager().params.getTag())
                                        .replace("%time%", time))
                                        .replace("%player%", Bukkit.getOfflinePlayer(UUID.fromString(key)).getName())
                            ).toString();
                        }   
                        
                        if (iterator.hasNext())
                            aux = new StringBuilder(aux)
                                    .append("\n")
                                    .append(ChatColor.RESET)
                                    .toString();
                    }
                    
                    msg = aux;
                }
            }

            player.sendMessage(msg);
        }
    }
}
