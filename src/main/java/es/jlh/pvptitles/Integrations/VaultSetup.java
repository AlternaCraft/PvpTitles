package es.jlh.pvptitles.Integrations;

import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import static org.bukkit.Bukkit.getServer;
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
            plugin.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.YELLOW
                    + "(Vault)Permissions " + ChatColor.AQUA + "integrated correctly.");        
        if (chat)
            plugin.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.YELLOW
                    + "(Vault)ChatManager " + ChatColor.AQUA + "integrated correctly.");        
        if (econ)
            plugin.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.YELLOW
                    + "(Vault)Economy " + ChatColor.AQUA + "integrated correctly.");
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chatPlugin = chatProvider.getProvider();
        }
        return (chatPlugin != null);
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
}
