package es.jlh.pvptitles.Handlers;

import es.jlh.pvptitles.Files.LangFile;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import es.jlh.pvptitles.Misc.Inventories;
import static es.jlh.pvptitles.Misc.Inventories.MAX_SIGNS_PER_PAGE;
import es.jlh.pvptitles.Misc.LangDetector.Localizer;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;

/**
 * Clase evento para comprobar los jugadores que pulsan click en el inv virtual
 *
 * @author julito
 * @version 1.0
 */
public class HandleInventoryClick implements Listener, EventExecutor {

    public static final int TICKS = 20;
    public static final int TIME = 3;

    private final PvpTitles plugin;

    public HandleInventoryClick(PvpTitles plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player pl = (Player) event.getWhoClicked();

        int page;

        if (inventory.getName().equals(LangFile.SIGN_INVENTORY_TITLE.getText(Localizer.getLocale(pl)))) {
            Map<Integer, Inventory> inventories = Inventories
                    .createInventory(plugin.cm.getLbm().getSigns(), Localizer.getLocale(pl));

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

            Location loc = getLocation(event.getSlot() + (MAX_SIGNS_PER_PAGE * page));

            if (event.getClick() == ClickType.LEFT) {
                if (loc != null) {
                    pl.closeInventory();
                    pl.teleport(loc);

                    pl.sendMessage(PLUGIN + LangFile.COMPLETE_TELEPORT_PLAYER.getText(Localizer.getLocale(pl)));
                }

            } else if (event.getClick() == ClickType.RIGHT) {
                // Caso para cambiar de pagina               
                plugin.cm.getLbm().deleteSign(loc);
                pl.sendMessage(PLUGIN + LangFile.SIGN_DELETED.getText(Localizer.getLocale(pl)));

                // 'Actualizacion' del inventario
                pl.closeInventory();

                inventories = Inventories.createInventory(
                        plugin.cm.getLbm().getSigns(), Localizer.getLocale(pl)
                );
                
                page = getPageNumber(inventory, inventories);
                
                pl.openInventory(inventories.get(page));

            }
        }
    }

    public Location getLocation(int pos) {
        return this.plugin.cm.getDm().buscaCarteles().get(pos).getL();
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
