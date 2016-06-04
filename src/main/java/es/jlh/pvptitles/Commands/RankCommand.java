package es.jlh.pvptitles.Commands;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Events.Handlers.HandlePlayerFame;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Ranks;
import es.jlh.pvptitles.Misc.Localizer;
import es.jlh.pvptitles.Misc.Utils;
import static es.jlh.pvptitles.Misc.Utils.splitToComponentTimes;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author AlternaCraft
 */
public class RankCommand implements CommandExecutor {

    private PvpTitles pt = null;

    public RankCommand(PvpTitles pt) {
        this.pt = pt;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangFile.LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (!(sender instanceof Player)) {
            sender.sendMessage(PLUGIN + LangFile.COMMAND_FORBIDDEN.getText(messages));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            this.HandleRankCmd(player);
            return true;
        }

        return false;
    }

    /**
     * Método para enviar los datos del rango de un jugador
     *
     * @param player Jugador que consulta los datos
     */
    private void HandleRankCmd(Player player) {
        int fame = pt.manager.dbh.getDm().loadPlayerFame(player.getUniqueId(), null);
        int racha = (HandlePlayerFame.KILLSTREAK.containsKey(player.getName())) ? HandlePlayerFame.KILLSTREAK.get(player.getName()) : 0;
        int seconds = pt.manager.dbh.getDm().loadPlayedTime(player.getUniqueId())
                + pt.getTimerManager().getPlayer(pt.getServer().getOfflinePlayer(player.getUniqueId())).getTotalOnline();
        
        String rank = Ranks.getRank(fame, seconds);
        
        int rankup = Ranks.fameToRankUp();
        int timeup = Ranks.nextRankTime();
        
        String nextRank = Ranks.nextRankTitle();
        String tag = pt.manager.params.getTag();

        player.sendMessage("");
        player.sendMessage(PLUGIN);
        player.sendMessage("  - " + ChatColor.AQUA + "Title: " + ChatColor.RESET + rank);
        player.sendMessage("  - " + ChatColor.AQUA + tag + ": " + ChatColor.RESET + fame);
        player.sendMessage("  - " + ChatColor.AQUA + "KillStreak: " + ChatColor.RESET + racha);

        if (rankup < 999999 /* y el tiempo */) {
            player.sendMessage("  - " + LangFile.RANK_INFO.getText(Localizer.getLocale(player))
                    .replace("%rankup%", String.valueOf(rankup))
                    .replace("%timeup%", Utils.splitToComponentTimes(timeup))
                    .replace("%tag%", tag).replace("%nextRank%", nextRank));
        }

        if (HandlePlayerFame.getAfm().isVetado(player.getUniqueId().toString())) {
            player.sendMessage("  * " + LangFile.VETO_STARTED.getText(Localizer.getLocale(player))
                    .replace("%tag%", pt.manager.params.getTag())
                    .replace("%time%", splitToComponentTimes(HandlePlayerFame.getAfm().getVetoTime(player.getUniqueId().toString()))));
        }
    }
}
