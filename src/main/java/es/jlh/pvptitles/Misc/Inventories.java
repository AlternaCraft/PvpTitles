package es.jlh.pvptitles.Misc;

import es.jlh.pvptitles.Configs.LangFile;
import es.jlh.pvptitles.Configs.LangFile.LangType;
import es.jlh.pvptitles.Objects.LBSigns.CustomSign;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author julito
 */
public class Inventories {

    public static final int MAX_SIGNS_PER_PAGE = 18;

    public static Map<Integer, Inventory> createInventory(List<CustomSign> cs, LangType lt) {
        Map<Integer, Inventory> inventories = new HashMap();

        int cont = 0;
        int vuelta = 0;

        if (cs.isEmpty()) {
            inventories.put(vuelta, Bukkit.getServer().createInventory(null, 27, LangFile.SIGN_INVENTORY_TITLE.getText(lt)));
        }

        while (cont < cs.size()) {
            Inventory inventory = Bukkit.getServer().createInventory(null, 27,
                    LangFile.SIGN_INVENTORY_TITLE.getText(lt));

            for (int j = 0; cont < cs.size() && j < (MAX_SIGNS_PER_PAGE * (vuelta + 1)); j++, cont++) {
                CustomSign customSign = cs.get(cont);

                ItemStack item = new ItemStack(Material.SIGN);
                String[] lore = null;
                String modelo = "Model: " + customSign.getInfo().getModelo();
                String coords = "[" + customSign.getInfo().getL().getBlockX() + ", "
                        + customSign.getInfo().getL().getBlockY() + ", "
                        + customSign.getInfo().getL().getBlockZ() + "]";

                if (!"".equals(customSign.getInfo().getServer())) {
                    String server = "Server: " + customSign.getInfo().getServer();
                    lore = new String[]{modelo, server, coords};
                } else {
                    lore = new String[]{modelo, coords};
                }

                createDisplay(item, inventory, j, customSign.getInfo().getNombre(), lore);
            }

            setDefaultItems(cs.size(), vuelta, lt, inventory);

            inventories.put(vuelta, inventory);

            vuelta++;
        }

        return inventories;
    }

    private static void createDisplay(ItemStack item, Inventory inv,
            int Slot, String name, String[] lore) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);

        ArrayList<String> Lore = new ArrayList();
        for (int i = 0; i < lore.length; i++) {
            Lore.add(lore[i]);
        }

        meta.setLore(Lore);
        item.setItemMeta(meta);
        inv.setItem(Slot, item);
    }

    private static void setDefaultItems(int total, int vuelta, LangType lt, Inventory inv) {
        ItemStack item;
        String[] content;

        // Paginas
        if (total > MAX_SIGNS_PER_PAGE * (vuelta + 1)) {
            if (vuelta == 0) {
                content = new String[]{LangFile.SIGN_INVENTORY_ACTION3_1.getText(lt)};
            } else {
                content = new String[]{LangFile.SIGN_INVENTORY_ACTION3_1.getText(lt),
                    LangFile.SIGN_INVENTORY_ACTION3_2.getText(lt)};
            }

            item = new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getData());
            createDisplay(item, inv, 18, LangFile.SIGN_INVENTORY_INFO3.getText(lt)
                    .replace("%pageNumber%", String.valueOf(vuelta + 1)), content);
        } else if (vuelta > 0) {
            content = new String[]{LangFile.SIGN_INVENTORY_ACTION3_2.getText(lt)};

            item = new ItemStack(Material.WOOL, 1, DyeColor.YELLOW.getData());
            createDisplay(item, inv, 18, LangFile.SIGN_INVENTORY_INFO3.getText(lt)
                    .replace("%pageNumber%", String.valueOf(vuelta + 1)), content);
        }

        // items de ayuda
        item = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
        createDisplay(item, inv, 25, LangFile.SIGN_INVENTORY_ACTION1.getText(lt),
                new String[]{LangFile.SIGN_INVENTORY_INFO1.getText(lt)});

        item = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
        createDisplay(item, inv, 26, LangFile.SIGN_INVENTORY_ACTION2.getText(lt),
                new String[]{LangFile.SIGN_INVENTORY_INFO2.getText(lt)});
    }
}
