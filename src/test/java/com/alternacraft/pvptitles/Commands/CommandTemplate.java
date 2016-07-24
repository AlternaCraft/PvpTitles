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

import static com.alternacraft.pvptitles.Commands.CommandBase.t;
import com.alternacraft.pvptitles.Files.HologramsFile;
import com.alternacraft.pvptitles.Misc.Localizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HologramsFile.class, Localizer.class})
public class CommandTemplate extends CommandBase {

    @Test
    public void testOnCommandTemplate() {
        BoardCommand bc = new BoardCommand(mockPlugin);
        CommandBase.CommandStructure cs = new CommandBase.CommandStructure(bc) {
            @Override
            public void initialize() {
                
            }
            @Override
            public void tests(boolean expected) {
                
            }
        };
        
        t(TEST_INFO.replace("%desc%", ""));
        
        String[] args = {};
        boolean expected = true;
        
        cs.premadeRun(args, expected);
    }

}
