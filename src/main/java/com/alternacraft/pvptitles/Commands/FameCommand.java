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
package com.alternacraft.pvptitles.Commands;

import com.alternacraft.pvptitles.Events.FameAddEvent;
import com.alternacraft.pvptitles.Events.FameSetEvent;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Files.LangsFile.LangType;
import com.alternacraft.pvptitles.Libraries.UUIDFetcher;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import static com.alternacraft.pvptitles.Main.PvpTitles.getPluginName;
import com.alternacraft.pvptitles.Managers.RankManager;
import com.alternacraft.pvptitles.Misc.Localizer;
import com.alternacraft.pvptitles.Misc.Rank;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FameCommand implements CommandExecutor {

    private PvpTitles pt = null;
    private Manager dm = null;

    public FameCommand(PvpTitles pt) {
        this.pt = pt;
        this.dm = pt.getManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        LangType messages = (sender instanceof Player) ? Localizer.getLocale((Player) sender) : Manager.messages;

        if (args.length == 0 || args.length > 5) {
            sender.sendMessage(getPluginName() + LangsFile.COMMAND_ARGUMENTS.getText(messages));
            return false;
        }

        try {
            return this.modRank(sender, args, messages);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Método para aniadir/ver/modificar los puntos de fama de un jugador
     *
     * @param sender Jugador que ejecuta el comando
     * @param args Parametros del comando
     * @return Booleano con el resultado de la ejecucion
     */
    private boolean modRank(CommandSender sender, String[] args, LangType messages)
            throws NumberFormatException {
        OfflinePlayer opl = null;

        if (args.length < 1) {
            return false;
        } else if (args.length > 1) {            
            UUID uuid = UUIDFetcher.getUUIDPlayer(args[1]);

            // Evitar NullPointerException
            if (uuid == null) {
                sender.sendMessage(getPluginName() + ChatColor.RED + args[1] + " doesn't "
                        + "exist");
                return true;
            }
            else {
                opl = Bukkit.getOfflinePlayer(uuid);
            }
        }

        switch (args[0]) {
            case "add":
                // <editor-fold defaultstate="collapsed" desc="ADD">
                if (args.length >= 3) {
                    // Evitar nullpointerexception
                    int fameIncr = 0;
                    String world = null;

                    if (dm.params.isMw_enabled()) {
                        if (args.length < 4) {
                            sender.sendMessage(getPluginName() + ChatColor.RED + "Syntax: 'pvpfame add <player> <world_name> <famepoints>'");
                            return true;
                        }

                        world = args[2];
                        if (pt.getServer().getWorld(world) == null) {
                            sender.sendMessage(getPluginName() + ChatColor.RED + "World \"" + world + "\" does not exist");
                            return true;
                        }
                        fameIncr = Integer.parseInt(args[3]);
                    } else {
                        fameIncr = Integer.parseInt(args[2]);
                    }
                    //

                    int fameA = 0;
                    try {
                        fameA = this.dm.getDBH().getDM().loadPlayerFame(opl.getUniqueId(), world);
                    } catch (DBException ex) {
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                        return true;
                    }

                    FameAddEvent event = new FameAddEvent(opl, fameA, fameIncr);
                    if (dm.params.isMw_enabled()) {
                        event.setWorldname(world);
                        if (args.length >= 5 && args[4].contains("-s")) {
                            event.setSilent(true);
                        }
                    } else if (args.length >= 4 && args[3].contains("-s")) {
                        event.setSilent(true);
                    }

                    try {
                        this.dm.getDBH().getDM().savePlayerFame(opl.getUniqueId(), event.getFameTotal(), world);
                        sender.sendMessage(getPluginName() + LangsFile.FAME_ADD.getText(messages).
                                replace("%tag%", this.dm.params.getTag())
                        );
                    } catch (DBException ex) {
                        sender.sendMessage(getPluginName() + LangsFile.FAME_MODIFY_ERROR.getText(messages).
                                replace("%player%", args[1]).
                                replace("%tag%", this.dm.params.getTag())
                        );
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                        event.setCancelled(true);
                    }
                    
                    pt.getServer().getPluginManager().callEvent(event);
                } else {
                    sender.sendMessage(getPluginName() + ChatColor.RED + "Syntax: 'pvpfame add <player> [<world_name>] <famepoints>'");
                }
                return true;
            // </editor-fold>
            case "see":
                // <editor-fold defaultstate="collapsed" desc="SEE">
                if (args.length == 2 || args.length == 3) {
                    int fameTotal = 0;

                    if (dm.params.isMw_enabled()) {
                        if (args.length == 2) {
                            String world = pt.getServer().getWorlds().get(0).getName();

                            if (sender instanceof Player) {
                                Player pl = (Player) sender;
                                world = pl.getWorld().getName();
                            }

                            try {
                                fameTotal = this.dm.getDBH().getDM().loadPlayerFame(opl.getUniqueId(), world);
                            } catch (DBException ex) {
                                CustomLogger.logArrayError(ex.getCustomStackTrace());
                            }
                        } else {
                            try {
                                fameTotal = this.dm.getDBH().getDM().loadPlayerFame(opl.getUniqueId(), args[2]);
                            } catch (DBException ex) {
                                CustomLogger.logArrayError(ex.getCustomStackTrace());
                            }
                        }
                    } else {
                        try {
                            fameTotal = this.dm.getDBH().getDM().loadPlayerFame(opl.getUniqueId(), null);
                        } catch (DBException ex) {
                            CustomLogger.logArrayError(ex.getCustomStackTrace());
                        }
                    }

                    sender.sendMessage(getPluginName() + LangsFile.FAME_SEE.getText(messages).
                            replace("%player%", args[1]).
                            replace("%fame%", String.valueOf(fameTotal)).
                            replace("%tag%", this.dm.params.getTag())
                    );
                } else {
                    sender.sendMessage(getPluginName() + ChatColor.RED + "Syntax: 'pvpfame see <player> [<world_name>]'");
                }
                return true;
            // </editor-fold>
            case "set":
                // <editor-fold defaultstate="collapsed" desc="SET">
                if (args.length >= 3) {
                    // Evitar nullpointerexception
                    String fameValue;
                    int fameTotal;
                    String world = null;

                    if (dm.params.isMw_enabled()) {
                        if (args.length < 4) {
                            sender.sendMessage(getPluginName() + ChatColor.RED + "Syntax: 'pvpfame set <player> <world_name> <fame_points|title_name>'");
                            return true;
                        }

                        world = args[2];
                        if (pt.getServer().getWorld(world) == null) {
                            sender.sendMessage(getPluginName() + ChatColor.RED + "World \"" + world + "\" does not exist");
                            return true;
                        }
                        fameValue = args[3];
                    } else {
                        fameValue = args[2];
                    }
                    
                    // Get points required to get a Rank.
                    Rank r = RankManager.getRank(fameValue);
                    if (r == null) {
                        fameTotal = Integer.valueOf(fameValue);
                    } else {
                        fameTotal = r.getPoints();
                    }

                    int fame;
                    try {
                        fame = this.dm.getDBH().getDM().loadPlayerFame(opl.getUniqueId(), world);
                    } catch (DBException ex) {
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                        return true;
                    }

                    fameTotal = (fameTotal < 0) ? 0 : fameTotal;

                    FameSetEvent event = new FameSetEvent(opl, fame, fameTotal);
                    if (dm.params.isMw_enabled()) {
                        event.setWorldname(world);
                        if (args.length >= 5 && args[4].contains("-s")) {
                            event.setSilent(true);
                        }
                    } else if (args.length >= 4 && args[3].contains("-s")) {
                        event.setSilent(true);
                    }
                    
                    try {
                        this.dm.getDBH().getDM().savePlayerFame(opl.getUniqueId(), event.getFameTotal(), world);
                        sender.sendMessage(getPluginName() + LangsFile.FAME_SET.getText(messages).
                                replace("%tag%", this.dm.params.getTag()));
                    } catch (DBException ex) {
                        sender.sendMessage(getPluginName() + LangsFile.FAME_MODIFY_ERROR.getText(messages).
                                replace("%player%", args[1]).
                                replace("%tag%", this.dm.params.getTag())
                        );
                        CustomLogger.logArrayError(ex.getCustomStackTrace());
                        event.setCancelled(true);
                    }
                    
                    pt.getServer().getPluginManager().callEvent(event);
                } else {
                    sender.sendMessage(getPluginName() + ChatColor.RED + "Syntax: 'pvpfame set <player> [<world_name>] <fame_points|title_name>'");
                }
                return true;
            // </editor-fold>
            default:
                break;
        }

        return false;
    }
}
