package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Files.LangFile.LangType;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Managers.BoardsAPI.Board;
import es.jlh.pvptitles.Managers.BoardsCustom.SignBoard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author AlternaCraft
 */
public class Inventories {

    public static final int MAX_BOARDS_PER_PAGE = 18;

    public static List<Inventory> opened = null;

    public Inventories() {
    }

    public void setup() {
        Inventories.opened = new ArrayList();
    }

    public static List<Player> closeInventories() {
        List<Player> viewers = new ArrayList();
        List<Inventory> openedtemp = new ArrayList<>(opened);
        
        for (Inventory inv : openedtemp) {
            while (!inv.getViewers().isEmpty()) {
                Player pl = (Player) inv.getViewers().get(0);
                pl.closeInventory();
                viewers.add(pl);
            }
        }
        
        return viewers;
    }

    public static void reloadInventories(List<Player> viewers) {        
        List<Board> boards = PvpTitles.getInstance().cm.getLbm().getBoards();        
        for (Player viewer : viewers) {
            viewer.openInventory(createInventory(boards, Localizer.getLocale(viewer)).get(0));            
        }
    }
    
    public static Map<Integer, Inventory> createInventory(List<Board> cs, LangType lt) {
        Map<Integer, Inventory> inventories = new HashMap();

        int cont = 0;
        int vuelta = 0;

        if (cs.isEmpty()) {
            Inventory inventory = PvpTitles.getInstance().getServer().createInventory(
                    null, 27, LangFile.BOARD_INVENTORY_TITLE.getText(lt));
            inventories.put(vuelta, inventory);
            opened.add(inventory); // To close it on reload
        } else {
            while (cont < cs.size()) {
                Inventory inventory = PvpTitles.getInstance().getServer().createInventory(
                        null, 27, LangFile.BOARD_INVENTORY_TITLE.getText(lt));

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
                content = new String[]{LangFile.BOARD_INVENTORY_ACTION3_1.getText(lt)};
            } else {
                content = new String[]{LangFile.BOARD_INVENTORY_ACTION3_1.getText(lt),
                    LangFile.BOARD_INVENTORY_ACTION3_2.getText(lt)};
            }

            item = new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getData());
            createDisplay(item, inv, 18, LangFile.BOARD_INVENTORY_INFO3.getText(lt)
                    .replace("%pageNumber%", String.valueOf(vuelta + 1)), content);
        } else if (vuelta > 0) {
            content = new String[]{LangFile.BOARD_INVENTORY_ACTION3_2.getText(lt)};

            item = new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getData());
            createDisplay(item, inv, 18, LangFile.BOARD_INVENTORY_INFO3.getText(lt)
                    .replace("%pageNumber%", String.valueOf(vuelta + 1)), content);
        }

        // items de ayuda
        item = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
        createDisplay(item, inv, 25, LangFile.BOARD_INVENTORY_ACTION1.getText(lt),
                new String[]{LangFile.BOARD_INVENTORY_INFO1.getText(lt)});

        item = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
        createDisplay(item, inv, 26, LangFile.BOARD_INVENTORY_ACTION2.getText(lt),
                new String[]{LangFile.BOARD_INVENTORY_INFO2.getText(lt)});
    }
}
