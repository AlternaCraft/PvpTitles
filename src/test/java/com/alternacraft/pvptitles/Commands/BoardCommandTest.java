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

import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Files.LangsFile.LangType;
import com.alternacraft.pvptitles.Hook.HolographicHook;
import com.alternacraft.pvptitles.Managers.BoardsAPI.Board;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardData;
import com.alternacraft.pvptitles.Managers.BoardsAPI.BoardModel;
import com.alternacraft.pvptitles.Managers.LeaderBoardManager;
import com.alternacraft.pvptitles.Misc.Localizer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.Test;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HologramsFile.class, Localizer.class})
public class BoardCommandTest extends CommandBase {
    
    @Test
    public void testBoardCommandCreate() {
        BoardCommand command = new BoardCommand(mockPlugin);

        CommandStructure cs = new CommandStructure(command) {
            @Override
            public void initialize() {
                PowerMockito.mockStatic(HologramsFile.class);
                when(HologramsFile.loadHologram(anyString())).thenReturn(null);

                PowerMockito.mockStatic(Localizer.class);
                when(Localizer.getLocale(any(Player.class))).thenReturn(LangType.EN);

                LeaderBoardManager mockLBM = mock(LeaderBoardManager.class);
                when(mockManager.getLbm()).thenReturn(mockLBM);
                when(mockLBM.addBoard(any(Board.class), any(Player.class))).thenReturn(Boolean.FALSE);
                saveMock("LBM", mockLBM);

                when(mockPlayer.getLocation()).thenReturn(new Location(null, 0, 0, 0));

                BoardModel mockBoardModel = mock(BoardModel.class);
                when(mockManager.searchModel(anyString())).thenReturn(mockBoardModel);

                // Para evitar problemas
                HolographicHook.ISHDENABLED = true;
            }

            @Override
            public void tests(boolean expected) {
                assertEquals(expected, getResult());
                
                verify(mockPlayer, never()).sendMessage(anyString());
                verify(mockManager).searchModel(anyString());
                verify((LeaderBoardManager) getMook("LBM")).addBoard(any(Board.class), any(Player.class));
                PowerMockito.verifyStatic();
            }
        };

        t(TEST_INFO.replace("%desc%", "Creacion de holograma (Suponiendo que los datos sean correctos)"));

        String[] args = new String[]{"create", "hologram", "testing", "asdd"};
        boolean expected = true;

        cs.premadeRun(args, expected);
    }

    @Test
    public void testBoardCommandRemove() {
        BoardCommand command = new BoardCommand(mockPlugin);

        CommandStructure cs = new CommandStructure(command) {
            @Override
            public void initialize() {
                PowerMockito.mockStatic(HologramsFile.class);
                BoardData mockBoardData = mock(BoardData.class);
                when(HologramsFile.loadHologram(anyString())).thenReturn(mockBoardData);

                PowerMockito.mockStatic(Localizer.class);
                when(Localizer.getLocale(any(Player.class))).thenReturn(LangType.EN);

                LeaderBoardManager mockLBM = mock(LeaderBoardManager.class);
                when(mockManager.getLbm()).thenReturn(mockLBM);
                when(mockLBM.addBoard(any(Board.class), any(Player.class))).thenReturn(Boolean.FALSE);
                saveMock("LBM", mockLBM);
                
                // Empieza
                HolographicHook.ISHDENABLED = true;
            }

            @Override
            public void tests(boolean expected) {
                assertEquals(expected, getResult());
                
                verify((LeaderBoardManager)getMook("LBM")).deleteBoard(any(Location.class), anyObject());
            }
        };

        t(TEST_INFO.replace("%desc%", "Eliminacion de tablero (Suponiendo que los datos sean correctos)"));

        String[] args = new String[]{"remove", "hologram", "testing"};
        boolean expected = true;
        
        cs.premadeRun(args, expected);
    }
}
