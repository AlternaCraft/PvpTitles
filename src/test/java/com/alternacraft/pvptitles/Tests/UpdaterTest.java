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
package com.alternacraft.pvptitles.Tests;

import com.alternacraft.pvptitles.Libraries.Updater;
import com.alternacraft.pvptitles.Libraries.Updater.UpdateResult;
import java.io.File;
import java.util.Arrays;
import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PluginDescriptionFile.class, Server.class})
public class UpdaterTest extends TestBase {

    private PluginDescriptionFile desc = null;
    
    private UpdateResult check(boolean shouldupdate, boolean shouldalert, String fname) {
        Updater.UpdateType ut = (shouldupdate) ? Updater.UpdateType.DEFAULT : Updater.UpdateType.NO_DOWNLOAD;
        Updater updater = new Updater(mockPlugin, 89518, new File(fname), ut, shouldalert);
        return updater.getResult();
    }

    @Test
    public void testOnNewVersion() {
        boolean result, expected = true;
        boolean shouldupdate;
        
        when(desc.getVersion()).thenReturn("0");
               
        t("\n" + TEST_INFO.replace("%desc%", "Probando auto updater (v=0)..."));
        
        p(TEST_INITIALIZE + " (Update: T)");    
        shouldupdate = true;
        p(TEST_EXECUTE);
        result = check(shouldupdate, true, "pvptitles11.jar").equals(UpdateResult.SUCCESS);
        p(TEST_RESULTS);
        assertEquals(expected, result);

        p(TEST_INITIALIZE + " (Update: F)");    
        shouldupdate = false;
        p(TEST_EXECUTE);
        result = check(shouldupdate, true, "pvptitles01.jar").equals(UpdateResult.UPDATE_AVAILABLE);
        p(TEST_RESULTS);
        assertEquals(expected, result);
    }
    
    @Test
    public void testOnKeepVersion() {
        boolean result, expected = true;
        boolean shouldupdate;
        
        when(desc.getVersion()).thenReturn("10000");
        
        t("\n" + TEST_INFO.replace("%desc%", "Probando auto updater (v=10000)..."));
        
        p(TEST_INITIALIZE + " (Update: T)");    
        shouldupdate = true;
        p(TEST_EXECUTE);
        result = check(shouldupdate, true, "pvptitles10.jar").equals(UpdateResult.NO_UPDATE);
        p(TEST_RESULTS);
        assertEquals(expected, result);

        p(TEST_INITIALIZE + " (Update: F)");    
        shouldupdate = false;
        p(TEST_EXECUTE);
        result = check(shouldupdate, true, "pvptitles00.jar").equals(UpdateResult.NO_UPDATE);
        p(TEST_RESULTS);
        assertEquals(expected, result);
    }

    @Override
    void somethingElse() {     
        // Plugin getDescription getVersion
        // Plugin getDescription getAuthors
        desc = mock(PluginDescriptionFile.class);
        when(this.mockPlugin.getDescription()).thenReturn(desc);        
        when(desc.getVersion()).thenReturn("0");
        when(desc.getAuthors()).thenReturn(Arrays.asList(new String[]{"AlternaCraft"}));
        
        // Plugin getServer getUpdateFolderFile
        Server mockServer = mock(Server.class);        
        when(this.mockPlugin.getServer()).thenReturn(mockServer);
        when(mockServer.getUpdateFolderFile()).thenReturn(new File(PLUGINDATAFOLDER));        
    }
}
