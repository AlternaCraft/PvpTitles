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
package com.alternacraft.pvptitles.Misc;

import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Files.LangsFile.LangType;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsCustom.SignBoard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Inventories {

    public static final int MAX_BOARDS_PER_PAGE = 18;

    public static List<Inventory> opened = new ArrayList();

    public static List<Player> closeInventories() {
        List<Player> players = Inventories.opened
                .stream()
                .map(inv -> inv.getViewers())
                .filter(v -> !v.isEmpty())
                .map(v -> ((Player) v.get(0)))
                .collect(Collectors.toList());
        players.forEach(pl -> pl.closeInventory());
        return players;
    }

    public static void reloadInventories(List<Player> viewers) {
        List<Board> boards = PvpTitles.getInstance().getManager().getLBM().getBoards();
        viewers.forEach((viewer) -> {
            viewer.openInventory(createInventory(boards, Localizer.getLocale(viewer)).get(0));
        });
    }

    public static Map<Integer, Inventory> createInventory(List<Board> cs, LangType lt) {
        Map<Integer, Inventory> inventories = new HashMap();

        int cont = 0;
        int vuelta = 0;

        if (cs.isEmpty()) {
            Inventory inventory = PvpTitles.getInstance().getServer().createInventory(null, 27, LangsFile.BOARD_INVENTORY_TITLE.getText(lt));
            inventories.put(vuelta, inventory);
            opened.add(inventory); // To close it on reload
        } else {
            while (cont < cs.size()) {
                Inventory inventory = PvpTitles.getInstance().getServer().createInventory(null, 27, LangsFile.BOARD_INVENTORY_TITLE.getText(lt));

                for (int j = 0; cont < cs.size() && j < (MAX_BOARDS_PER_PAGE); j++, cont++) {
                    Board board = cs.get(cont);

                    ItemStack item = new ItemStack((board instanceof SignBoard)
                            ? Material.SIGN : Material.ITEM_FRAME);
                    String[] lore = null;
                    String modelo = "Model: " + board.getData().getModelo();
                    String coords = "[" + board.getData().getLocation().getBlockX() + ", "
                            + board.getData().getLocation().getBlockY() + ", "
                            + board.getData().getLocation().getBlockZ() + "]";

                    if (!"".equals(board.getData().getServer())) {
                        String server = "Server: " + board.getData().getServer();
                        lore = new String[]{modelo, server, coords};
                    } else {
                        lore = new String[]{modelo, coords};
                    }

                    createDisplay(item, inventory, j, board.getData().getNombre(), lore);
                }

                setDefaultItems(cs.size(), vuelta, lt, inventory);

                inventories.put(vuelta, inventory);

                vuelta++;

                opened.add(inventory); // To close it on reload
            }
        }

        return inventories;
    }

    private static void createDisplay(ItemStack item, Inventory inv,
            int Slot, String name, String[] lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        ArrayList<String> Lore = new ArrayList();
        Lore.addAll(Arrays.asList(lore));

        meta.setLore(Lore);
        item.setItemMeta(meta);
        inv.setItem(Slot, item);
    }

    private static void setDefaultItems(int total, int vuelta, LangType lt, Inventory inv) {
        ItemStack item;
        String[] content;

        // Paginas
        if (total > MAX_BOARDS_PER_PAGE * (vuelta + 1)) {
            if (vuelta == 0) {
                content = new String[]{LangsFile.BOARD_INVENTORY_ACTION3_1.getText(lt)};
            } else {
                content = new String[]{LangsFile.BOARD_INVENTORY_ACTION3_1.getText(lt),
                    LangsFile.BOARD_INVENTORY_ACTION3_2.getText(lt)};
            }

            item = new ItemStack(Material.WOOL, 1, (byte) 4);
            createDisplay(item, inv, 18, LangsFile.BOARD_INVENTORY_INFO3.getText(lt)
                    .replace("%pageNumber%", String.valueOf(vuelta + 1)), content);
        } else if (vuelta > 0) {
            content = new String[]{LangsFile.BOARD_INVENTORY_ACTION3_2.getText(lt)};

            item = new ItemStack(Material.WOOL, 1, (byte) 4);
            createDisplay(item, inv, 18, LangsFile.BOARD_INVENTORY_INFO3.getText(lt)
                    .replace("%pageNumber%", String.valueOf(vuelta + 1)), content);
        }

        // items de ayuda
        item = new ItemStack(Material.WOOL, 1, (byte) 5);
        createDisplay(item, inv, 25, LangsFile.BOARD_INVENTORY_ACTION1.getText(lt),
                new String[]{LangsFile.BOARD_INVENTORY_INFO1.getText(lt)});

        item = new ItemStack(Material.WOOL, 1, (byte) 14);
        createDisplay(item, inv, 26, LangsFile.BOARD_INVENTORY_ACTION2.getText(lt),
                new String[]{LangsFile.BOARD_INVENTORY_INFO2.getText(lt)});
    }
}
