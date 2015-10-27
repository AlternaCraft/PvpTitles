package es.jlh.pvptitles.Handlers;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Managers.SignManager;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
import es.jlh.pvptitles.Objects.LBData;
import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;

/**
 * Clase evento para comprobar los jugadores que pulsan click en el inv virtual
 * @author julito
 * @version 1.0
 */
public class HandleInventoryClick implements Listener, EventExecutor {
    public static final int TICKS = 20;    
    public static final int TIME = 3;    
    
    private final PvpTitles plugin;
    private final Manager cm;
    
    public HandleInventoryClick(PvpTitles plugin) {
        this.plugin = plugin;
        this.cm = plugin.cm;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        ArrayList<LBData> sd = cm.getDm().buscaCarteles();
        Player pl = (Player)event.getWhoClicked();        
        
        if (inventory.getName().equals(LangFile.SIGN_INVENTORY_TITLE.getText(Localizer.getLocale(pl)))) {
            event.setCancelled(true);
            
            if (event.getSlot() == 25 || event.getSlot() == 26)
                return;
            
            if (inventory.getItem(event.getSlot()) == null)
                return;
            
            Location loc = getLocation(event.getSlot());
            
            if (event.getClick() == ClickType.LEFT) {
                if (loc != null) {
                    pl.closeInventory();
                    pl.teleport(loc);
                    
                    pl.sendMessage(PLUGIN + LangFile.COMPLETE_TELEPORT_PLAYER.getText(Localizer.getLocale(pl)));
                }
            }
            else if (event.getClick() == ClickType.RIGHT) {
                SignManager.borrarSBManual(cm, sd, loc);
                pl.sendMessage(PLUGIN + LangFile.SIGN_DELETED.getText(Localizer.getLocale(pl)));
                
                // 'Actualizacion' del inventario
                pl.closeInventory();
                pl.openInventory(es.jlh.pvptitles.Managers.InventoryManager
                        .createInventory(cm.getDm().buscaCarteles(), Localizer.getLocale(pl)));
            }    
        }
    }        
    
    public Location getLocation(int pos) {
        return this.plugin.cm.getDm().buscaCarteles().get(pos).getL();
    }

    @Override
    public void execute(Listener ll, Event event) throws EventException {
    }    
}
