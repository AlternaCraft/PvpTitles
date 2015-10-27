package es.jlh.pvptitles.Managers;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Files.LangFile.LangType;
import es.jlh.pvptitles.Objects.LBData;
import java.util.ArrayList;
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
public class InventoryManager {
    public static Inventory createInventory(ArrayList<LBData> sd, LangType lt) {        
        org.bukkit.inventory.Inventory inv = Bukkit.getServer().createInventory(null, 27, 
                LangFile.SIGN_INVENTORY_TITLE.getText(lt));        
        
        for (int j = 0; j < sd.size() && j < 25; j++) {
            ItemStack item = new ItemStack(Material.SIGN);
            
            String[] lore;
            
            String modelo = "Model: " + sd.get(j).getModelo();
            
            String coords = "["+ sd.get(j).getL().getBlockX() + ", " + 
                    sd.get(j).getL().getBlockY() + ", " + sd.get(j).getL().getBlockZ() +"]";
                        
            if (!"".equals(sd.get(j).getServer())) {
                String server = "Server: " + sd.get(j).getServer();
                lore = new String[]{modelo, server, coords};
            }
            else {
                lore = new String[]{modelo, coords};
            }                       
            
            createDisplay(item, inv, j, sd.get(j).getNombre(), lore);
        }
        
        ItemStack item = new ItemStack(Material.WOOL, 1, DyeColor.GREEN.getData());
        createDisplay(item, inv, 25, LangFile.SIGN_INVENTORY_ACTION1.getText(lt), 
                new String[]{LangFile.SIGN_INVENTORY_INFO1.getText(lt)});        
        item = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
        createDisplay(item, inv, 26, LangFile.SIGN_INVENTORY_ACTION2.getText(lt), 
                new String[]{LangFile.SIGN_INVENTORY_INFO2.getText(lt)}); 
        
        return inv;
    }
    
    private static void createDisplay(ItemStack item, org.bukkit.inventory.Inventory inv, 
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
}
