/*
 * Copyright (C) 2018 AlternaCraft
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
package com.alternacraft.pvptitles.Tests;

import com.alternacraft.pvptitles.Misc.FileConfig;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.YamlConfiguration;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest()
public class ConfigurationFileTest extends TestBase {

    private static final String CUSTOM_CONFIG = "config.yml";
    private File config = null;
    private File customConfig;

    @Test
    public void notExists() {
        if (customConfig.exists())
            customConfig.delete();
        
        t(TEST_INFO.replace("%desc%", "File configuration does not exist"));
        
        FileConfig fc = new FileConfig(mockPlugin);
        verify(mockPlugin, Mockito.atLeast(1)).saveDefaultConfig();
        verify(mockPlugin, Mockito.atLeast(1)).reloadConfig();
    }

    @Test
    public void oldVersion() {
        if (customConfig.exists())
            customConfig.delete();
        
        t(TEST_INFO.replace("%desc%", "File configuration is outdated"));
        
        // Save config test file     
        YamlConfiguration yaml;
        
        yaml = YamlConfiguration.loadConfiguration(config);
        yaml.set("Version", 0);
        yaml.set("Debug", true);
        yaml.set("Mysql.host", "machine");
        yaml.set("RankNames", Arrays.asList(new String[]{"Test 1", "Test 2", "Test 3"}));
        try {
            yaml.save(customConfig);
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FileConfig fc = new FileConfig(mockPlugin);
        yaml = YamlConfiguration.loadConfiguration(customConfig);
        
        assertEquals(true, !yaml.getString("Version").equals("0"));
        assertEquals(true, yaml.getBoolean("Debug"));
        assertEquals("machine", yaml.getString("Mysql.host"));
    }

    @Test
    public void normalWorking() {
        if (customConfig.exists())
            customConfig.delete();
        
        t(TEST_INFO.replace("%desc%", "File configuration is up to date"));
        
        // Save config test file      
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(config);
        try {
            yaml.save(new File(PLUGINDATAFOLDER, "config.yml"));
        } catch (IOException ex) {
            Logger.getLogger(ConfigurationFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        FileConfig fc = new FileConfig(mockPlugin);
        verify(mockPlugin, Mockito.never()).saveDefaultConfig();
    }

    @Override
    void somethingElse() {
        URL url = this.getClass().getClassLoader().getResource(CUSTOM_CONFIG);
        try {
            config = new File(url.toURI());
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.setDefaults(YamlConfiguration.loadConfiguration(config));
            PowerMockito.when(mockPlugin.getConfig()).thenReturn(yaml);            
        } catch (URISyntaxException ex) {
        }
        
        this.customConfig = new File(PLUGINDATAFOLDER + "/config.yml");
        
        doAnswer((InvocationOnMock invocation) -> {
            try {
                YamlConfiguration.loadConfiguration(config).save(customConfig);
            } catch (IOException ex) {
                Logger.getLogger(ConfigurationFileTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }).when(mockPlugin).saveDefaultConfig();
    }

}
