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
package com.alternacraft.pvptitles.Main;

import com.alternacraft.pvptitles.Backend.ConfigDataStore;
import com.alternacraft.pvptitles.Backend.DatabaseManager;
import com.alternacraft.pvptitles.Backend.DatabaseManagerEbean;
import com.alternacraft.pvptitles.Backend.DatabaseManagerMysql;
import com.alternacraft.pvptitles.Backend.Ebean;
import com.alternacraft.pvptitles.Backend.EbeanTables.PlayerPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.SignPT;
import com.alternacraft.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import com.alternacraft.pvptitles.Backend.MySQLConnection;
import static com.alternacraft.pvptitles.Main.CustomLogger.showMessage;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerWTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.SignTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.TimeTable;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class DBLoader {

    private DatabaseManager dm = null;
    public static DBTYPE tipo = null;

    public static enum DBTYPE {
        EBEAN,
        MYSQL;
    }

    public Ebean ebeanServer = null;
    public Connection mysql = null;

    private PvpTitles pvpTitles = null;
    private FileConfiguration config = null;

    public DBLoader(PvpTitles pvpTitles, FileConfiguration config) {
        this.pvpTitles = pvpTitles;
        this.config = config;
    }

    public void selectDB() {
        switch (tipo) {
            case EBEAN:
                // Ebean server
                this.loadConfiguration();
                this.initializeDatabase();

                dm = new DatabaseManagerEbean(pvpTitles, ebeanServer);
                break;                
            case MYSQL:
                // MySQL server
                this.mysqlConnect(false);

                if (MySQLConnection.estado_conexion == MySQLConnection.Estado.SIN_CONEXION) {
                    tipo = DBTYPE.EBEAN;
                    selectDB();
                } else {
                    dm = new DatabaseManagerMysql(pvpTitles, mysql);
                }
                break;
        }
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
        ebeanServer = new Ebean(this.pvpTitles) {
            @Override
            protected java.util.List<Class<?>> getDatabaseClasses() {
                List<Class<?>> list = new ArrayList<>();
                
                String ruta = (new StringBuilder()).append(
                        pvpTitles.getDataFolder()).append( // Ruta
                                File.separator).append( // Separador
                                "PvpTitles.db").toString();
                
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

        showMessage(ChatColor.YELLOW + "Ebean database " + ChatColor.AQUA + "loaded correctly.");
    }

    /**
     * Conexion a MySQL
     * @param reconnect Intentar conectar de nuevo a la bd de forma silenciosa
     */
    public void mysqlConnect(boolean reconnect) {
        ConfigDataStore params = pvpTitles.getManager().params;

        MySQLConnection.connectDB(params.getHost() + ":" + params.getPort()
                + "/" + params.getDb(), params.getUser(), params.getPass(), reconnect);

        // No lo cambio porque sigue usando mysql (reconnect)
        if (MySQLConnection.estado_conexion == MySQLConnection.Estado.SIN_CONEXION && !reconnect) {
            tipo = DBTYPE.EBEAN;
        } else {
            tipo = DBTYPE.MYSQL;
            mysql = MySQLConnection.getConnection();

            if (!reconnect) {            
                MySQLConnection.creaDefault();
                MySQLConnection.registraServer(params.getMultiS(), params.getNameS());
                showMessage(ChatColor.YELLOW + "MySQL database " + ChatColor.AQUA + "loaded correctly.");
            }
            else {
                dm.updateConnection(mysql);
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
