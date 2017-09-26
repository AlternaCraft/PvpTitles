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

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Main.Manager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author AlternaCraft
 */

public abstract class SQLConnection {

    /**
     * constante DRIVER_MYSQL para definir el driver de MySQL
     */
    protected static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    
    /**
     * constante DRIVER_SQLITE para definir el driver de SQLite
     */    
    protected static final String DRIVER_SQLITE = "org.sqlite.JDBC";

    protected Connection connection;
    
    public STATUS_AVAILABLE status = STATUS_AVAILABLE.NOT_CONNECTED;

    public static enum STATUS_AVAILABLE {
        CONNECTED,
        NOT_CONNECTED;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean isConnected(boolean reconnect) throws DBException {
        boolean valida = false;

        if (connection != null) {
            try {
                valida = connection.isValid(3) && !connection.isClosed();

                if (valida) {
                    status = STATUS_AVAILABLE.CONNECTED;
                } else if (reconnect) {
                    closeConnection();
                    Manager.getInstance().getDBH().sqlConnect(true);
                    valida = isConnected(false);
                } else {
                    status = STATUS_AVAILABLE.NOT_CONNECTED;
                }
            } catch (SQLException ex) {
                status = STATUS_AVAILABLE.NOT_CONNECTED;
            } catch (AbstractMethodError ex) {
                valida = true; // Should continue?
            }
        }

        return valida;
    }
    
    public void closeConnection() {
        try {
            if (!connection.isClosed()) {
                connection.close();
                status = STATUS_AVAILABLE.NOT_CONNECTED;
            }
        } catch (SQLException | AbstractMethodError ex) {}
    }
    
    protected void slowUpdate(String sql) throws DBException, SQLException {
        if (isConnected(true)) {
            this.update(sql);
        }
    }
    
    protected void update(String sql) throws SQLException {
        this.connection.createStatement().executeUpdate(sql);
    }
    
    protected ResultSet query(String sql) throws SQLException {
        return this.connection.createStatement().executeQuery(sql);
    }
    
    //<editor-fold defaultstate="collapsed" desc="DEFAULT TABLES">
    public static String getTableServers() {
        return 
            "create table IF NOT EXISTS Servers ("
                + "id smallint(3) primary key,"
                + "name varchar(50)"
            + ")";
    }
    
    public static String getTablePlayerServer() {
        return 
            "create table IF NOT EXISTS PlayerServer ("
                + "id int(3) not null unique," // Avoid autoincrement
                + "playerUUID varchar(100),"
                + "serverID smallint(3),"
                + "PRIMARY KEY (playerUUID, serverID),"
                + "FOREIGN KEY (serverID) REFERENCES Servers(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";
    }    
    
    public static String getTablePlayerMeta() {
        return 
            "create table IF NOT EXISTS PlayerMeta ("
                + "psid int(3) primary key,"
                + "points int(10) default 0,"
                + "playedTime bigint default 0,"
                + "lastLogin date,"
                + "FOREIGN KEY (psid) REFERENCES PlayerServer(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";
    }

    public static String getTablePlayerWorld() {
        return 
            "create table IF NOT EXISTS PlayerWorld ("
                + "psid int(3),"
                + "worldName varchar(50),"
                + "points int(10) default 0,"
                + "PRIMARY KEY (psid, worldName),"
                + "FOREIGN KEY (psid) REFERENCES PlayerServer(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";
    }

    public static String getTableSigns() {
        return 
            "create table IF NOT EXISTS Signs ("
                + "name varchar(50) DEFAULT 'default',"
                + "signModel varchar(50),"
                + "dataModel varchar(50),"
                + "orientation varchar(2),"
                + "blockface smallint(1),"
                + "serverID smallint(3),"
                + "world varchar(50),"
                + "x int,"
                + "y int,"
                + "z int,"
                + "PRIMARY KEY (serverID, world, x, y, z),"
                + "FOREIGN KEY (serverID) REFERENCES Servers(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
            + ")";
    }    
    //</editor-fold>
    
    public void updateServer(short id, String nombre) {
        String insert = "insert into Servers values (?,?)";
        String update = "update Servers set name=? where id=?";
        try {
            PreparedStatement insertIS = this.connection.prepareStatement(insert);
            insertIS.setShort(1, id);
            insertIS.setString(2, nombre);
            insertIS.executeUpdate();
        } catch (SQLException ex) {
            try {
                PreparedStatement updateIS = this.connection.prepareStatement(update);
                updateIS.setString(1, nombre);
                updateIS.setShort(2, id);
                updateIS.executeUpdate();
            } catch (SQLException ex1) {}
        }
    }    
    
    public abstract void connectDB(boolean reconnect, String... args) throws DBException;
    public abstract void load() throws DBException;
}
