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
package com.alternacraft.pvptitles.Main;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Backend.DatabaseManager;
import com.alternacraft.pvptitles.Backend.DatabaseManagerEbean;
import com.alternacraft.pvptitles.Backend.DatabaseManagerSQL;
import com.alternacraft.pvptitles.Backend.EbeanConnection;
import com.alternacraft.pvptitles.Backend.EbeanTables.PlayerPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.SignPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import com.alternacraft.pvptitles.Backend.MySQLConnection;
import com.alternacraft.pvptitles.Backend.SQLiteConnection;
import com.alternacraft.pvptitles.Libraries.SQLConnection;
import static com.alternacraft.pvptitles.Main.CustomLogger.showMessage;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerWTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.SignTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.TimeTable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class DBLoader {

    private static final String EBEAN_DB = "PvpTitles.db";
    private static final String SQLITE_DB = "PvpLite.db";
    
    public static DBTYPE tipo = null;
    public static enum DBTYPE {
        EBEAN,
        MYSQL,
        SQLITE;
    }

    public EbeanConnection ebeanServer = null;
    public SQLConnection sql = null;

    private PvpTitles pvpTitles = null;
    private FileConfiguration config = null;
    
    private DatabaseManager dm = null;

    public DBLoader(PvpTitles pvpTitles, FileConfiguration config) {
        this.pvpTitles = pvpTitles;
        this.config = config;
    }

    public void selectDB() {        
        if (tipo == DBTYPE.EBEAN) {
            // Ebean server
            this.loadConfiguration();
            this.initializeDatabase();
            
            dm = new DatabaseManagerEbean(pvpTitles, ebeanServer);                       
        } else {
            if (tipo == DBTYPE.MYSQL) {
                this.sql = new MySQLConnection();
            }
            else {
                this.sql = new SQLiteConnection(PvpTitles.PLUGIN_DIR, SQLITE_DB);
            }
            
            this.sqlConnect(false);
            
            if (this.sql.status.equals(SQLConnection.STATUS_AVAILABLE.NOT_CONNECTED)) {
                tipo = DBTYPE.EBEAN;
                selectDB();
                return;
            } else {
                dm = new DatabaseManagerSQL(pvpTitles, sql);
            }
        }
        
        showMessage(ChatColor.YELLOW + tipo.name() + " database " 
                + ChatColor.AQUA + "loaded correctly.");
    }
    
    // Ebean
    /**
     * Método para establecer la configuracion de la bd de ebeans
     */
    private void loadConfiguration() {
        config.set("database.driver", config.getString("database.driver", "org.sqlite.JDBC"));
        config.set("database.url", config.getString("database.url", "jdbc:sqlite:{DIR}{NAME}.db"));
        config.set("database.username", config.getString("database.username", "root"));
        config.set("database.password", config.getString("database.password", ""));
        config.set("database.isolation", config.getString("database.isolation", "SERIALIZABLE"));
        //config.set("database.logging", config.getBoolean("database.logging", PvpTitles.debugMode));
        config.set("database.rebuild", config.getBoolean("database.rebuild", false)); // false
    }

    /**
     * Método para iniciar la bd con ebean
     */
    private void initializeDatabase() {
        ebeanServer = new EbeanConnection(this.pvpTitles) {
            @Override
            protected java.util.List<Class<?>> getDatabaseClasses() {
                List<Class<?>> list = new ArrayList<>();
                
                String ruta = new StringBuilder(PvpTitles.PLUGIN_DIR)
                        .append(EBEAN_DB).toString();
                
                if (new File(ruta).length() > 0)  {
                    /* OLD */
                    list.add(PlayerTable.class);
                    list.add(PlayerWTable.class);
                    list.add(SignTable.class);
                    list.add(TimeTable.class);
                }
                
                /* NEW */
                list.add(PlayerPT.class);
                list.add(WorldPlayerPT.class);
                list.add(SignPT.class);

                return list;
            }
        ;
        };

        ebeanServer.initializeDatabase(
                config.getString("database.driver"),
                config.getString("database.url"),
                config.getString("database.username"),
                config.getString("database.password"),
                config.getString("database.isolation"),
                PvpTitles.debugMode,
                config.getBoolean("database.rebuild")
        );        
    }

    /**
     * Conexion a SQL Database
     * 
     * @param reconnect Intentar conectar de nuevo a la bd de forma silenciosa
     */
    public void sqlConnect(boolean reconnect) {
        ConfigDataStore params = pvpTitles.getManager().params;       

        if (tipo == DBTYPE.MYSQL) {
            this.sql.connectDB(reconnect, params.getHost() + ":" + params.getPort()
                    + "/" + params.getDb(), params.getUser(), params.getPass(), 
                    String.valueOf(params.isUse_ssl()));
        } else {
            this.sql.connectDB(reconnect);
        }

        if (this.sql.status.equals(SQLConnection.STATUS_AVAILABLE.NOT_CONNECTED) && !reconnect) {
            tipo = DBTYPE.EBEAN;
        } else {
            if (!reconnect) {            
                this.sql.load();
                this.sql.registraServer(params.getMultiS(), params.getNameS());
            }
            else {
                dm.updateConnection(this.sql);
            }
        }
    } 
    
    /**
     * Método para recibir el gestor de la base de datos
     *
     * @return DatabaseManager
     */    
    public DatabaseManager getDm() {
        return dm;
    }
}
