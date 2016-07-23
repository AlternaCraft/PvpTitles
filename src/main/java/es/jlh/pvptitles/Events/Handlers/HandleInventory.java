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

import es.jlh.pvptitles.Events.BoardEvent;
import es.jlh.pvptitles.Files.LangsFile;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.getPluginName;
import es.jlh.pvptitles.Managers.BoardsAPI.BoardData;
import es.jlh.pvptitles.Misc.Inventories;
import static es.jlh.pvptitles.Misc.Inventories.MAX_BOARDS_PER_PAGE;
import es.jlh.pvptitles.Misc.Localizer;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;

/**
 * Clase evento para comprobar los jugadores que pulsan click en el inv virtual
 */
public class HandleInventory implements Listener, EventExecutor {

    public static final int TICKS = 20;
    public static final int TIME = 3;

    private final PvpTitles plugin;

    public HandleInventory(PvpTitles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (Inventories.opened.contains(event.getInventory())) {
            Inventories.opened.remove(event.getInventory());
        }
    }

    @EventHandler
    public void onBoardChanged(BoardEvent event) {
        Inventories.reloadInventories(Inventories.closeInventories());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player pl = (Player) event.getWhoClicked();

        int page;

        if (inventory.getName().equals(LangsFile.BOARD_INVENTORY_TITLE.getText(Localizer.getLocale(pl)))) {
            Map<Integer, Inventory> inventories = Inventories
                    .createInventory(plugin.getManager().getLbm().getBoards(), Localizer.getLocale(pl));

            event.setCancelled(true);

            page = getPageNumber(inventory, inventories);

            if (event.getSlot() < 0 || event.getSlot() == 25 || event.getSlot() == 26) {
                return;
            }

            if (inventory.getItem(event.getSlot()) == null) {
                return;
            }

            if (event.getSlot() == 19 || event.getSlot() == 20) {
                return;
            }

            if (event.getSlot() == 18) {
                if (event.getClick() == ClickType.LEFT) {
                    if (page > 0) {
                        pl.closeInventory();
                        pl.openInventory(inventories.get(page - 1));
                    }
                } else if (event.getClick() == ClickType.RIGHT) {
                    if (page < inventories.size() - 1) {
                        pl.closeInventory();
                        pl.openInventory(inventories.get(page + 1));
                    }
                }

                return;
            }

            Location loc = getLocation(event.getSlot() + (MAX_BOARDS_PER_PAGE * page));

            if (event.getClick() == ClickType.LEFT) {
                pl.closeInventory();
                pl.teleport(loc);

                pl.sendMessage(getPluginName() + LangsFile.COMPLETE_TELEPORT_PLAYER.getText(Localizer.getLocale(pl)));

            } else if (event.getClick() == ClickType.RIGHT) {
                // Caso para cambiar de pagina               
                plugin.getManager().getLbm().deleteBoard(loc, pl);

                // 'Actualizacion' del inventario
                pl.closeInventory();

                inventories = Inventories.createInventory(
                        plugin.getManager().getLbm().getBoards(), Localizer.getLocale(pl)
                );

                page = getPageNumber(inventory, inventories);

                pl.openInventory(inventories.get(page));

            }
        }
    }

    public BoardData getBoard(int pos) {
        return this.plugin.getManager().getLbm().getBoards().get(pos).getData();
    }

    public Location getLocation(int pos) {
        return this.getBoard(pos).getLocation();
    }

    public int getPageNumber(Inventory inventory, Map<Integer, Inventory> inventories) {
        int page = 0;

        // Numero de pagina
        if (inventory.getItem(18) != null) {
            for (Entry<Integer, Inventory> entry : inventories.entrySet()) {
                if (entry.getValue().getItem(18).equals(inventory.getItem(18))) {
                    page = entry.getKey();
                    break;
                }
            }
        }

        return page;
    }

    @Override
    public void execute(Listener ll, Event event) throws EventException {
    }
}
