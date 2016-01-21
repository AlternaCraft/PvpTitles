package es.jlh.pvptitles.Integrations;

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
public class VaultSetup {

    public static Permission permission = null;
    public static Economy economy = null;
    public static Chat chatPlugin = null;

    private PvpTitles plugin = null;

    public VaultSetup(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public void setupVault() {
        boolean perms = this.setupPermissions();
        boolean chat = this.setupChat();
        boolean econ = this.setupEconomy();        

        if (perms)
            showMessage(ChatColor.YELLOW + "(Vault)Permissions " + ChatColor.AQUA + "integrated correctly.");        
        if (chat)
            showMessage(ChatColor.YELLOW + "(Vault)ChatManager " + ChatColor.AQUA + "integrated correctly.");        
        if (econ)
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
