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
import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Hook.HolographicHook;
import com.alternacraft.pvptitles.Main.Managers.MessageManager;
import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import com.alternacraft.pvptitles.Managers.LeaderBoardManager;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
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
@PrepareForTest({HologramsFile.class, MessageManager.class, HologramsAPI.class})
public class IntegrationsTest extends CommandBase {

    ConfigDataStore params = null;
    
    @Test    
    public void testOnHolographic() {
        t("\n" + TEST_INFO.replace("%desc%", "Probando la integracion con HD"));
        
        p(TEST_INITIALIZE);
        PowerMockito.mockStatic(HologramsFile.class);
        PowerMockito.mockStatic(MessageManager.class);                   
        
        HolographicHook hh = new HolographicHook(mockPlugin);
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
        
        p(TEST_EXECUTE);
        hh.setup();        
        
        p(TEST_RESULTS);
        
    }

    @Override
    void somethingElse() {
        // The code
        params = mock(ConfigDataStore.class);
        mockManager.params = params;
        
        Server mockServer = mock(Server.class);
        when(mockPlugin.getServer()).thenReturn(mockServer);

        PluginManager mockPM = mock(PluginManager.class);
        when(mockServer.getPluginManager()).thenReturn(mockPM);
        
        when(mockPM.isPluginEnabled(anyString())).thenReturn(Boolean.TRUE);
    }

}
