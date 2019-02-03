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
package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.Manager;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection extends SQLConnection {

    private static final String SUPRESS_WARNING = "?verifyServerCertificate=false&useSSL=";
    private static final String INNO_DB = " ENGINE=InnoDB;";

    /**
     * Constructor en caso de que quiera un acceso personalizado
     * 
     * @param reconnect Sin mostrar mensajes
     * @param args Para indicar datos de acceso a la base de datos.
     */
    @Override
    public void connectDB(boolean reconnect, String... args) {
        try {
            Class.forName(DRIVER_MYSQL);
            this.connection = DriverManager.getConnection("jdbc:mysql://" + args[0]
                    + SUPRESS_WARNING + args[1], args[2], args[3]);
            this.status = Status.CONNECTED;
        } catch (ClassNotFoundException ex) {
            if (!reconnect) CustomLogger.logError("MySQL library not found");
            this.status = Status.NOT_CONNECTED;
        } catch (SQLException ex) {
            if (!reconnect) CustomLogger.logError(((ex.getErrorCode() == 0)
                    ? "Could not connect to MySQL" : "MySQL error: " + ex.getErrorCode())
                    + "; Using " + Manager.getInstance().params.getDefaultDB() + " per default...");
            this.status = Status.NOT_CONNECTED;
        }
    }
    
    @Override
    public void load() throws DBException {
        try {
            slowUpdate(getTableServers() + INNO_DB);
            slowUpdate(getTablePlayerServer() + INNO_DB);
            slowUpdate(getTablePlayerMeta() + INNO_DB);
            slowUpdate(getTablePlayerWorld() + INNO_DB);
            slowUpdate(getTableSigns() + INNO_DB);
            slowUpdate("DROP TRIGGER IF EXISTS `create_player_meta`");
            slowUpdate(getTriggerMeta());
            slowUpdate("DROP TRIGGER IF EXISTS `update_lastlogin`");
            slowUpdate(getTriggerMeta2());
        } catch (SQLException ex) {
            throw new DBException(DBException.UNKNOWN_ERROR, DBException.DBMethod.STRUCTURE, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }   
    
    public static String getTriggerMeta() {
        return "CREATE TRIGGER `create_player_meta` AFTER INSERT ON `PlayerServer`"
                + " FOR EACH ROW INSERT INTO PlayerMeta(psid) SELECT max(id) FROM PlayerServer";
    }

    public static String getTriggerMeta2() {
        return "CREATE TRIGGER `update_lastlogin` AFTER INSERT ON `PlayerServer`"
                + " FOR EACH ROW UPDATE PlayerMeta SET lastlogin = (SELECT CURDATE()) WHERE psid = (SELECT max(id) FROM PlayerServer)";
    }
}
