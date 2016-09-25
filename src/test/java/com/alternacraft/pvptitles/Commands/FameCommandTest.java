/*
 * Copyright (C) 2016 AlternaCraft
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
package com.alternacraft.pvptitles.Commands;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Backend.DatabaseManager;
import static com.alternacraft.pvptitles.Commands.TestBase.t;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Files.LangsFile;
import com.alternacraft.pvptitles.Libraries.UUIDFetcher;
import com.alternacraft.pvptitles.Main.Handlers.DBHandler;
import com.alternacraft.pvptitles.Misc.Localizer;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, UUIDFetcher.class, Localizer.class})
public class FameCommandTest extends TestBase {

    private DatabaseManager mockDM = null;

    @Test
    public void FameCommand() {
        FameCommand rc = new FameCommand(mockPlugin);
        TestBase.CommandStructure cs = new TestBase.CommandStructure(rc) {
            @Override
            public void initialize() {
                ConfigDataStore mockConfig = mock(ConfigDataStore.class);
                when(mockConfig.getTag()).thenReturn("Fame");

                mockManager.params = mockConfig;
                saveMock("cds", mockConfig);
            }

            @Override
            public void tests(boolean expected) {
                try {
                    verify(mockDM, Mockito.atLeast(4)).savePlayerFame(any(UUID.class), any(Integer.class), anyString());
                    verify(mockDM, Mockito.atLeast(6)).loadPlayerFame(any(UUID.class), anyString());
                } catch (DBException ex) {
                    System.out.println("DB Error?");
                }
            }
        };

        m("* " + TEST_INITIALIZE + " *");
        cs.initialize();

        boolean expected = true;

        execute(cs, "Ejecutando el comando add sin MW...", new String[]{"add", "blabla", "10"}, expected, false);
        execute(cs, "Ejecutando el comando add con MW...", new String[]{"add", "blabla", "world", "10"}, expected, true);
        execute(cs, "Ejecutando el comando set sin MW...", new String[]{"set", "blabla", "10"}, expected, false);
        execute(cs, "Ejecutando el comando set con MW...", new String[]{"set", "blabla", "world", "10"}, expected, true);
        execute(cs, "Ejecutando el comando see sin MW...", new String[]{"see", "blabla"}, expected, false);
        execute(cs, "Ejecutando el comando see con MW...", new String[]{"see", "blabla", "world"}, expected, true);
        
        m("\n* " + TEST_RESULTS + " *");
        cs.tests(expected);

        m("");
    }

    private void execute(TestBase.CommandStructure cs, String desc, String[] args, boolean expected, boolean mw) {
        t("\n" + TEST_INFO.replace("%desc%", desc));
        when(((ConfigDataStore) cs.getMook("cds")).isMw_enabled()).thenReturn(mw);
        p(TEST_EXECUTE);
        cs.execute(args);
        assertEquals(expected, cs.getResult());
    }

    @Override
    void somethingElse() {
        // Langs 
        PowerMockito.mockStatic(Localizer.class);
        when(Localizer.getLocale(any(Player.class))).thenReturn(LangsFile.LangType.EN);

        // Basics
        PowerMockito.mockStatic(Bukkit.class);
        PowerMockito.mockStatic(UUIDFetcher.class);

        OfflinePlayer mockOPlayer = mock(OfflinePlayer.class);

        when(UUIDFetcher.getIDPlayer(anyString())).thenReturn(UUID.randomUUID());
        when(Bukkit.getOfflinePlayer(any(UUID.class))).thenReturn(mockOPlayer);

        // Database
        DBHandler mockDBHandler = mock(DBHandler.class);
        mockManager.dbh = mockDBHandler;

        mockDM = mock(DatabaseManager.class);
        when(mockDBHandler.getDm()).thenReturn(mockDM);

        try {
            when(mockDM.loadPlayerFame(any(UUID.class), anyString())).thenReturn(0);
        } catch (DBException ex) {
            System.out.println("DB Error?");
        }

        // Custom events
        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);

        World mockWorld = mock(World.class);
        when(mockServer.getWorld(anyString())).thenReturn(mockWorld);

        PluginManager mockPM = mock(PluginManager.class);
        when(mockServer.getPluginManager()).thenReturn(mockPM);

        doNothing().when(mockPM).callEvent(any(Event.class));
    }
}
