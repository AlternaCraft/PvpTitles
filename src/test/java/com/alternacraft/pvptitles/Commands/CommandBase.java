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

import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import java.util.HashMap;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.powermock.api.mockito.PowerMockito;


import org.powermock.core.classloader.annotations.PrepareForTest;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(Manager.class)
public class CommandBase {

    protected final String TEST_INFO = "Prueba: %desc%";
    protected final String TEST_INITIALIZE = "Inicializo los objetos necesarios";
    protected final String TEST_EXECUTE = "Ejecuto el comando";
    protected final String TEST_RESULTS = "Comprobando resultados...";
    
    protected PvpTitles mockPlugin = null;
    protected Manager mockManager = null;
    protected Player mockPlayer = null;

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        m("[ Cargando elementos comúnes ]");

        mockPlugin = mock(PvpTitles.class);
        mockManager = PowerMockito.mock(Manager.class);
        mockPlayer = mock(Player.class);

        when(mockPlugin.getManager()).thenReturn(mockManager); // Principal   
    }

    @After
    public void tearDown() {
    }
    
    protected static void t(String m) {
        m(m + ":");
    }
    
    protected static void p(String m) {
        m(" - " + m);
    }
    
    protected static void m(String m) {
        System.out.println(m);
    }
    
    public class CommandStructure {
        
        private CommandExecutor ce = null;        
        private boolean result = false;
        
        private HashMap<String, Object> mocks = new HashMap();
        
        public CommandStructure(CommandExecutor ce) {
            this.ce = ce;
        }
        
        public final void premadeRun(String[] args, boolean expected) {
            p(TEST_INITIALIZE);
            this.initialize();            
            p(TEST_EXECUTE);
            this.execute(args);
            p(TEST_RESULTS);
            this.tests(expected);
            m("");
        }
        
        public void initialize() {
            //
        }                
        
        public final void execute(String[] args) {            
            result = this.ce.onCommand(mockPlayer, null, null, args);
        }        

        public void tests(boolean expected) {
            //
        } 
        
        public final void saveMock(String k, Object v) {
            mocks.put(k, v);
        }
        
        public final Object getMook(String k) {
            return mocks.get(k);
        }
        
        public final boolean getResult() {
            return this.result;
        }
    }
}
