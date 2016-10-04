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
package com.alternacraft.pvptitles.Tests;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Hook.HolographicHook;
import com.alternacraft.pvptitles.Hook.MVdWPlaceholderHook;
import com.alternacraft.pvptitles.Hook.VaultHook;
import com.alternacraft.pvptitles.Main.Managers.MessageManager;
import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import com.alternacraft.pvptitles.Managers.LeaderBoardManager;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HologramsFile.class, MessageManager.class, HologramsAPI.class,
    PlaceholderAPI.class, JavaPlugin.class})
public class IntegrationsTest extends TestBase {

    ConfigDataStore params = null;
    ServicesManager mockServices = null;

    @Test
    public void testOnHolographic() {
        TestStructure ts = new TestStructure() {
            @Override
            public void initialize() {
                PowerMockito.mockStatic(HologramsFile.class);

                when(params.getHolotagformat()).thenReturn("TestTag");
                when(params.getHoloHeightMod()).thenReturn(Short.MAX_VALUE);
                when(params.displayLikeHolo()).thenReturn(Boolean.FALSE);

                BoardModel mockBM = mock(BoardModel.class);
                when(mockManager.searchModel(anyString())).thenReturn(mockBM);

                // LoadHoloBoards
                List<List<List<String>>> table = new ArrayList();
                when(mockBM.getParams()).thenReturn(table);
                LeaderBoardManager mockLBM = mock(LeaderBoardManager.class);
                when(mockManager.getLbm()).thenReturn(mockLBM);
                doNothing().when(mockLBM).loadBoard(any(Board.class));

                // deleteHolograms
                PowerMockito.mockStatic(HologramsAPI.class);
                Collection<Hologram> holos = new ArrayList();
                Hologram mockHolo = mock(Hologram.class);
                holos.add(mockHolo);
                when(HologramsAPI.getHolograms(mockPlugin)).thenReturn(holos);
                when(mockHolo.isDeleted()).thenReturn(Boolean.TRUE);
            }

            @Override
            public void execute() {
                new HolographicHook(mockPlugin).setup();
            }
        };

        t(TEST_INFO.replace("%desc%", "Probando la integracion con HolographicDisplays"));

        ts.premadeRun();
    }

    @Test
    public void testOnMVdWPlaceholderHook() {
        TestStructure ts = new TestStructure() {
            @Override
            public void initialize() {
                PowerMockito.mockStatic(PlaceholderAPI.class);
            }

            @Override
            public void execute() {
                new MVdWPlaceholderHook(mockPlugin).setup();
            }
        };

        t(TEST_INFO.replace("%desc%", "Probando la integracion con MVdWPlaceholderHook"));

        ts.premadeRun();
    }

    /* Incompatibility problem between Java versions, 1.8 required
    @Test
    public void testOnSBSHook() {
        TestStructure ts = new TestStructure() {
            @Override
            public void initialize() {
                PowerMockito.mockStatic(PlaceholderAPI.class);
                PowerMockito.mockStatic(JavaPlugin.class);

                ScoreboardStats mockSBS = mock(ScoreboardStats.class);
                when(JavaPlugin.getPlugin(ScoreboardStats.class)).thenReturn(mockSBS);

                ReplaceManager mockRM = mock(ReplaceManager.class);
                when(mockSBS.getReplaceManager()).thenReturn(mockRM);
                this.saveMock("replacer", mockRM);
            }

            @Override
            public void execute() {
                new SBSHook(mockPlugin).setupSBS();
            }

            @Override
            public void tests() {
                verify(((ReplaceManager) this.getMook("replacer")), Mockito.atLeastOnce())
                        .register(any(VariableReplacer.class), any(PvpTitles.class), anyString(), anyString(), anyString());
            }
        };

        t(TEST_INFO.replace("%desc%", "Probando la integracion con ScoreBoardStats"));

        ts.premadeRun();
    }
    */

    @Test
    public void testOnVaultPermission() {
        TestStructure ts = new TestStructure() {
            @Override
            public void initialize() {
                RegisteredServiceProvider mockRSP = mock(RegisteredServiceProvider.class);

                when(mockServices.getRegistration(net.milkbowl.vault.permission.Permission.class)).thenReturn(mockRSP);
                Permission mockPermission = mock(Permission.class);
                when(mockRSP.getProvider()).thenReturn(mockPermission);
            }

            @Override
            public void execute() {
                new VaultHook(mockPlugin).setupVault();
            }

            @Override
            public void tests() {
                Assert.assertEquals(VaultHook.PERMISSIONS_ENABLED, true);
            }
        };

        t(TEST_INFO.replace("%desc%", "Probando la integracion con Vault Permissions"));

        ts.premadeRun();
    }

    @Test
    public void testOnVaultEconomy() {
        TestStructure ts = new TestStructure() {
            @Override
            public void initialize() {
                RegisteredServiceProvider mockRSP = mock(RegisteredServiceProvider.class);

                when(mockServices.getRegistration(net.milkbowl.vault.economy.Economy.class)).thenReturn(mockRSP);
                Economy mockEconomy = mock(Economy.class);
                when(mockRSP.getProvider()).thenReturn(mockEconomy);
            }

            @Override
            public void execute() {
                new VaultHook(mockPlugin).setupVault();
            }

            @Override
            public void tests() {
                Assert.assertEquals(VaultHook.ECONOMY_ENABLED, true);
            }
        };

        t(TEST_INFO.replace("%desc%", "Probando la integracion con Vault Economy"));

        ts.premadeRun();
    }

    @Test
    public void testOnVaultChat() {
        TestStructure ts = new TestStructure() {
            @Override
            public void initialize() {
                RegisteredServiceProvider mockRSP = mock(RegisteredServiceProvider.class);

                when(mockServices.getRegistration(net.milkbowl.vault.chat.Chat.class)).thenReturn(mockRSP);
                Chat mockChat = mock(Chat.class);
                when(mockRSP.getProvider()).thenReturn(mockChat);
            }

            @Override
            public void execute() {
                new VaultHook(mockPlugin).setupVault();
            }

            @Override
            public void tests() {
                Assert.assertEquals(VaultHook.CHAT_ENABLED, true);
            }
        };

        t(TEST_INFO.replace("%desc%", "Probando la integracion con Vault Chat"));

        ts.premadeRun();
    }

    @Override
    void somethingElse() {
        PowerMockito.mockStatic(MessageManager.class);

        // The code
        params = mock(ConfigDataStore.class);
        mockManager.params = params;

        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);

        PluginManager mockPM = mock(PluginManager.class);
        when(mockServer.getPluginManager()).thenReturn(mockPM);

        mockServices = mock(ServicesManager.class);
        when(mockServer.getServicesManager()).thenReturn(mockServices);

        when(mockPM.isPluginEnabled(anyString())).thenReturn(Boolean.TRUE);
    }

}
