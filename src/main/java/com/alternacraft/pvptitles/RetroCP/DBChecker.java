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
package com.alternacraft.pvptitles.RetroCP;

import com.alternacraft.pvptitles.Backend.SQLConnection;
import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.DBLoader;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.UtilsFile;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.alternacraft.pvptitles.Main.DBLoader.tipo;

public class DBChecker {

    /* MySQL */
    public static final short MYSQL_NEW_STRUCTURE_CREATED = 3;
    public static final short MYSQL_TIME_CREATED = 2;
    public static final short MYSQL_OLD_VERSION = 1;

    private PvpTitles plugin = null;

    public DBChecker(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        if (tipo == DBLoader.DBType.MYSQL) {
            this.checkMySQLDB();
        }
        return true;
    }

    public void checkMySQLDB() {
        SQLConnection mysql = plugin.getManager().getDBH().sql;
        RetroDMMysql rdm = new RetroDMMysql(plugin, mysql.getConnection());

        int status = MYSQL_OLD_VERSION;

        try {
            ResultSet rs = mysql.getConnection().createStatement().executeQuery("show tables like 'SignsServer'");
            if (rs.next()) {
                rs = mysql.getConnection().createStatement().executeQuery("show tables like 'PlayersTime'");
                if (rs.next()) {
                    status = MYSQL_TIME_CREATED;
                }
                rdm.exportarData(status);
            }
        } catch (SQLException ex) {
        }

        try {
            plugin.getManager().getDBH().getDM().DBImport(RetroDMMysql.FILENAME);
            UtilsFile.delete(new StringBuilder(PvpTitles.PLUGIN_DIR)
                    .append(RetroDMMysql.FILENAME).toString());            
        } catch (DBException ex) {
            CustomLogger.logArrayError(ex.getCustomStackTrace());            
        }                
    }
}
