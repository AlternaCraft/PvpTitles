package es.jlh.pvptitles.Hook;

import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author AlternaCraft
 */
public class VaultHook {

    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chatPlugin = null;

    public static boolean PERMISSIONS_ENABLED = false;
    public static boolean ECONOMY_ENABLED = false;
    public static boolean CHAT_ENABLED = false;
    
    private PvpTitles plugin = null;

    public VaultHook(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public void setupVault() {
        PERMISSIONS_ENABLED = this.setupPermissions();
        CHAT_ENABLED = this.setupChat();
        ECONOMY_ENABLED = this.setupEconomy();        

        if (PERMISSIONS_ENABLED)
            showMessage(ChatColor.YELLOW + "(Vault)Permissions " + ChatColor.AQUA + "integrated correctly.");        
        if (CHAT_ENABLED)
            showMessage(ChatColor.YELLOW + "(Vault)ChatManager " + ChatColor.AQUA + "integrated correctly.");        
        if (ECONOMY_ENABLED)
            showMessage(ChatColor.YELLOW + "(Vault)Economy " + ChatColor.AQUA + "integrated correctly.");
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chatPlugin = chatProvider.getProvider();
        }
        return (chatPlugin != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}
