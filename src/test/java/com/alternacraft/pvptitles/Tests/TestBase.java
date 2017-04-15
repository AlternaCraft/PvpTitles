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

import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;
import com.alternacraft.pvptitles.Main.PvpTitles;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;
import org.powermock.core.classloader.annotations.PrepareForTest;

@PrepareForTest({PvpTitles.class, Manager.class, CustomLogger.class})
abstract class TestBase {

    protected final String PLUGINDATAFOLDER = "target/test-stuff";

    protected final String TEST_INFO = "Prueba: %desc%";
    protected final String TEST_INITIALIZE = "Inicializo los objetos necesarios";
    protected final String TEST_EXECUTE = "Ejecuto el comando";
    protected final String TEST_RESULTS = "Comprobando resultados...";

    protected PvpTitles mockPlugin = null;
    protected Manager mockManager = null;

    protected Player mockPlayer = null;
    protected Logger mockLogger = null;

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        m("[ Cargando elementos comunes ]");

        mockPlugin = PowerMockito.mock(PvpTitles.class);
        mockManager = PowerMockito.mock(Manager.class);

        mockPlayer = mock(Player.class);
        mockLogger = mock(Logger.class);

        when(mockPlugin.getManager()).thenReturn(mockManager); // Principal
        when(mockPlugin.getLogger()).thenReturn(mockLogger); // Principal
        when(mockPlugin.getDataFolder()).thenReturn(new File(PLUGINDATAFOLDER));        

        // Plugin
        PowerMockito.mockStatic(PvpTitles.class);
        when(PvpTitles.getPluginName()).thenReturn("");
        when(PvpTitles.getInstance()).thenReturn(mockPlugin);

        // Player receive a message
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("SenderSendMessage: " + Arrays.toString(args));
                return null;
            }
        }).when(mockPlayer).sendMessage(anyString());

        // Log message
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                System.out.println("Logger: " + Arrays.toString(args));
                return null;
            }
        }).when(mockLogger).log(any(Level.class), anyString(), any(Exception.class));

        // Message manager
        PowerMockito.mockStatic(CustomLogger.class);
        
        this.somethingElse();
    }

    abstract void somethingElse();

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

    public class TestStructure {

        protected HashMap<String, Object> mocks = new HashMap();

        public final void premadeRun() {
            p(TEST_INITIALIZE);
            this.initialize();
            p(TEST_EXECUTE);
            this.execute();
            p(TEST_RESULTS);
            this.tests();
            m("");
        }

        public void initialize() {
            //
        }

        public void execute() {
            //
        }

        public void tests() {
            //
        }

        public final void saveMock(String k, Object v) {
            mocks.put(k, v);
        }

        public final Object getMook(String k) {
            return mocks.get(k);
        }

        public final HashMap<String, Object> getMocks() {
            return this.mocks;
        }

    }

    public class CommandStructure extends TestStructure {

        private boolean result = false;
        private CommandExecutor ce = null;

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

        public void tests(boolean result) {
            //
        }

        public final boolean getResult() {
            return this.result;
        }

        public final void execute(String[] args) {
            result = this.ce.onCommand(mockPlayer, null, null, args);
        }
    }
}
