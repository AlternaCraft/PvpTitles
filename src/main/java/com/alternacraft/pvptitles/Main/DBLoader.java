/*
 * Copyright (C) 2018 AlternaCraft
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

import com.alternacraft.pvptitles.Backend.*;
import com.alternacraft.pvptitles.Exceptions.DBException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import static com.alternacraft.pvptitles.Main.CustomLogger.showMessage;

public class DBLoader {

    private static final String SQLITE_DB = "PvpLite.db";
    
    public static DBType tipo = null;
    public static enum DBType {
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

    public boolean selectDB() {
        if (tipo == DBType.MYSQL) {
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
            return false;
        }

        if (this.sql.status.equals(SQLConnection.Status.NOT_CONNECTED)) {            
            return selectDB();
        } else {
            dm = new DatabaseManagerSQL(pvpTitles, sql);
        }        
        
        showMessage(ChatColor.YELLOW + tipo.name() + " database " 
                + ChatColor.AQUA + "loaded correctly.");
        
        return true;
    }

    /**
     * Conexion a SQL Database
     * 
     * @param reconnect Intentar conectar de nuevo a la bd de forma silenciosa
     * @throws com.alternacraft.pvptitles.Exceptions.DBException Connection
     */
    public void sqlConnect(boolean reconnect) throws DBException {
        ConfigDataStore params = pvpTitles.getManager().params;       

        if (tipo == DBType.MYSQL) {
            this.sql.connectDB(reconnect, params.getHost() + ":" + params.getPort()
                    + "/" + params.getDb(), String.valueOf(params.isUse_ssl()),
                    params.getUser(), params.getPass());
        } else {
            this.sql.connectDB(reconnect);
        }

        if (this.sql.status.equals(SQLConnection.Status.NOT_CONNECTED) && !reconnect) {
            if (tipo == DBType.MYSQL) {
                tipo = DBType.SQLITE;
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
    public DatabaseManager getDM() {
        return dm;
    }
}
