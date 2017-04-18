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
package com.alternacraft.pvptitles.Files;

import com.alternacraft.pvptitles.Main.PvpTitles;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.bukkit.configuration.file.YamlConfiguration;

public class ServersFile {
    public File serversFile = new File(PvpTitles.PLUGIN_DIR + "servers.yml");        
    
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
