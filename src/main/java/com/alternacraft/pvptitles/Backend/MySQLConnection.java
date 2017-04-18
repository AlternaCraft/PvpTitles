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
package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Libraries.SQLConnection;
import com.alternacraft.pvptitles.Main.CustomLogger;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection extends SQLConnection {

    /**
     * constante DRIVER para definir el conector jdbc
     */
    private static final String SUPRESS_WARNING = "?verifyServerCertificate=false&useSSL=";
    private static final String INNO_DB = " ENGINE=InnoDB;";

    /**
     * Constructor en caso de que quiera un acceso personalizado
     * 
     * @param reconnect Sin mostrar mensajes
     * 
     * @param args
     */
    @Override
    public void connectDB(boolean reconnect, String... args) {
        try {
            Class.forName(DRIVER_MYSQL);
            this.connection = DriverManager.getConnection("jdbc:mysql://" + args[0]
                    + SUPRESS_WARNING + args[1], args[2], args[3]);
            this.status = STATUS_AVAILABLE.CONNECTED;
        } catch (ClassNotFoundException ex) {
            if (!reconnect) CustomLogger.logError("MySQL library not found");
            this.status = STATUS_AVAILABLE.NOT_CONNECTED;
        } catch (SQLException ex) {
            if (!reconnect) CustomLogger.logError(((ex.getErrorCode() == 0)
                    ? "Could not connect to MySQL DB" : "MySQL error: " + ex.getErrorCode())
                    + "; Using Ebean per default...");
            this.status = STATUS_AVAILABLE.NOT_CONNECTED;
        }
    }
    
    @Override
    public void load() {
        update(getTableServers() + INNO_DB);
        update(getTablePlayerServer() + INNO_DB);
        update(getTablePlayerMeta() + INNO_DB);
        update(getTablePlayerWorld() + INNO_DB);
        update(getTableSigns() + INNO_DB);
    }   
}
