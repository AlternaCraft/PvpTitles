package es.jlh.pvptitles.Files;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author AlternaCraft
 */
public class ServersFile {
    public File serversFile = new File("plugins/PvpTitles/servers.yml");        
    
    public ServersFile() {
    }
    
    public YamlConfiguration load() {
        if (!serversFile.exists()) {
            createConfig();
        }
        
        return YamlConfiguration.loadConfiguration(serversFile);
    }
    
    private void createConfig() {
        YamlConfiguration newConfig = new YamlConfiguration();
        
        newConfig.options().header (
            "####################################\n" + 
            "## [SERVERS/WORLDS FILTER SYSTEM] ##\n" +
            "####################################"
        );
        newConfig.options().copyHeader(true);

        // filtro por rango
        newConfig.set("OneServer", Arrays.asList(new Integer[]{1}));
        newConfig.set("TwoServers", Arrays.asList(new Integer[]{1,2}));
        newConfig.set("AllServers", Arrays.asList(new Integer[]{-1}));
        
        newConfig.set("Worlds.1", Arrays.asList(new String[]{"exampleWorld", "myWorld", "myOtherWorld"}));
        newConfig.set("Worlds.2", Arrays.asList(new String[]{"myWorld"}));
        newConfig.set("Worlds.OneServer.1", Arrays.asList(new String[]{"customWorld"}));
        newConfig.set("Worlds.TwoServers.2", Arrays.asList(new String[]{"otherWorld", "myWorld"}));        
        
        try {
            newConfig.save(serversFile);
        } 
        catch (IOException e) {
        }
    }
}
