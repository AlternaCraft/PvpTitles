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
import com.alternacraft.pvptitles.Backend.DatabaseManagerSQL;
import com.alternacraft.pvptitles.Backend.MySQLConnection;
import com.alternacraft.pvptitles.Backend.SQLConnection;
import com.alternacraft.pvptitles.Backend.SQLiteConnection;
import com.alternacraft.pvptitles.Exceptions.DBException;
import static com.alternacraft.pvptitles.Main.CustomLogger.showMessage;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class DBLoader {

    private static final String SQLITE_DB = "PvpLite.db";
    
    public static DBTYPE tipo = null;
    public static enum DBTYPE {
        MYSQL,
        SQLITE;
    }

    public SQLConnection sql = null;

    private PvpTitles pvpTitles = null;
    private FileConfiguration config = null;
    
    private DatabaseManager dm = null;

    public DBLoader(PvpTitles pvpTitles, FileConfiguration config) {
        this.pvpTitles = pvpTitles;
        this.config = config;
    }

    public void selectDB() {
        if (tipo == DBTYPE.MYSQL) {
            this.sql = new MySQLConnection();
        }
        else {
            this.sql = new SQLiteConnection(PvpTitles.PLUGIN_DIR, SQLITE_DB);
        }

        try {
            this.sqlConnect(false);
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());
            this.pvpTitles.getServer().getPluginManager().disablePlugin(this.pvpTitles);
            return;
        }

        if (this.sql.status.equals(SQLConnection.STATUS_AVAILABLE.NOT_CONNECTED)) {
            selectDB();
            return;
        } else {
            dm = new DatabaseManagerSQL(pvpTitles, sql);
        }        
        
        showMessage(ChatColor.YELLOW + tipo.name() + " database " 
                + ChatColor.AQUA + "loaded correctly.");
    }

    /**
     * Conexion a SQL Database
     * 
     * @param reconnect Intentar conectar de nuevo a la bd de forma silenciosa
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Connection
     */
    public void sqlConnect(boolean reconnect) throws DBException {
        ConfigDataStore params = pvpTitles.getManager().params;       

        if (tipo == DBTYPE.MYSQL) {
            this.sql.connectDB(reconnect, params.getHost() + ":" + params.getPort()
                    + "/" + params.getDb(), String.valueOf(params.isUse_ssl()),
                    params.getUser(), params.getPass());
        } else {
            this.sql.connectDB(reconnect);
        }

        if (this.sql.status.equals(SQLConnection.STATUS_AVAILABLE.NOT_CONNECTED) && !reconnect) {
            if (tipo == DBTYPE.MYSQL) {
                tipo = DBTYPE.SQLITE;
            }
        } else {
            if (!reconnect) {            
                this.sql.load();
                this.sql.updateServer(params.getMultiS(), params.getNameS());
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
