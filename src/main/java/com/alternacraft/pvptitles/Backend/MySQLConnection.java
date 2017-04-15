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

import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySQLConnection {

    /**
     * constante DRIVER para definir el conector jdbc
     */
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String SUPRESS_WARNING = "?verifyServerCertificate=false&useSSL=";

    private static Connection conexion;

    public static Estado estado_conexion = Estado.SIN_CONEXION;

    public static enum Estado {

        CONECTADO,
        SIN_CONEXION;
    }

    /**
     * Método para devolver la conexión
     *
     * @return Connection
     */
    public static Connection getConnection() {
        return MySQLConnection.conexion;
    }

    /**
     * Constructor en caso de que quiera un acceso personalizado
     *
     * @param ruta Host + puerto + db
     * @param user String con el usuario
     * @param pass String con la clave
     * @param ssl Boolean sobre el uso de SSL
     * @param reconnect Sin mostrar mensajes
     */
    public static void connectDB(String ruta, String user, String pass, boolean ssl, 
            boolean reconnect) {
        try {
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection("jdbc:mysql://" + ruta 
                    + SUPRESS_WARNING + ssl, user, pass);
            estado_conexion = Estado.CONECTADO;
        } catch (ClassNotFoundException ex) {
            if (!reconnect) CustomLogger.logError("MySQL library not found");
            estado_conexion = Estado.SIN_CONEXION;
        } catch (SQLException ex) {
            if (!reconnect) CustomLogger.logError(((ex.getErrorCode() == 0)
                    ? "Could not connect to MySQL DB" : "MySQL error: " + ex.getErrorCode())
                    + "; Using Ebean per default...");
            estado_conexion = Estado.SIN_CONEXION;
        }
    }

    /**
     * Método para comprobar si existe una conexion con la bdd
     *
     * @param reconnect Booleano para ejecutar la parte de reconexión
     * @return Booleano con el resultado
     */
    public static boolean isConnected(boolean reconnect) {
        boolean iniciada = false, valida = false;

        iniciada = (conexion != null);
        
        if (iniciada) {
            try {
                valida = conexion.isValid(3) && !conexion.isClosed();
                
                if (valida) {
                    estado_conexion = Estado.CONECTADO;
                } 
                else if (reconnect) {
                    closeConnection();
                    
                    PvpTitles.getInstance().getManager().getDbh().mysqlConnect(true);
                    valida = conexion.isValid(3) && !conexion.isClosed();
                    
                    if (valida) estado_conexion = Estado.CONECTADO;
                }
                else {
                    estado_conexion = Estado.SIN_CONEXION;
                }
            } catch (SQLException ex) {
                estado_conexion = Estado.SIN_CONEXION;
            }
        }

        return valida;
    }

    /**
     * Método para cerrar la conexion
     *
     * @throws SQLException Fallo al cerrar la conexion
     */
    public static void closeConnection() throws SQLException {
        if (!conexion.isClosed()) {
            conexion.close();
            estado_conexion = Estado.SIN_CONEXION;
        }
    }

    public static void creaDefault() {
        update(getTableServers());
        update(getTablePlayerServer());
        update(getTablePlayerMeta());
        update(getTablePlayerWorld());
        update(getTableSigns());
    }

    public static String getTableServers() {
        return "create table IF NOT EXISTS Servers ("
                + "id smallint(3) primary key,"
                + "name varchar(50)"
                + ") ENGINE=InnoDB;";
    }

    public static String getTablePlayerServer() {
        return "create table IF NOT EXISTS PlayerServer ("
                + "id smallint(3) not null unique AUTO_INCREMENT,"
                + "playerUUID varchar(100),"
                + "serverID smallint(3),"
                + "PRIMARY KEY (playerUUID, serverID),"
                + "FOREIGN KEY (serverID) REFERENCES Servers(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
                + ") ENGINE=InnoDB;";
    }

    public static String getTablePlayerMeta() {
        return "create table IF NOT EXISTS PlayerMeta ("
                + "psid smallint(3) primary key,"
                + "points int(10) default 0,"
                + "playedTime bigint default 0,"
                + "lastLogin date,"
                + "FOREIGN KEY (psid) REFERENCES PlayerServer(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
                + ") ENGINE=InnoDB;";
    }

    public static String getTablePlayerWorld() {
        return "create table IF NOT EXISTS PlayerWorld ("
                + "psid smallint(3),"
                + "worldName varchar(50),"
                + "points int(10) default 0,"
                + "PRIMARY KEY (psid, worldName),"
                + "FOREIGN KEY (psid) REFERENCES PlayerServer(id)"
                + "ON UPDATE CASCADE ON DELETE CASCADE"
                + ") ENGINE=InnoDB;";
    }

    public static String getTableSigns() {
        return "create table IF NOT EXISTS Signs ("
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
                + ") ENGINE=InnoDB;";
    }

    public static void registraServer(short id, String nombre) {
        String insert = "insert into Servers values (?,?)";
        String update = "update Servers set name=? where id=?";
        try {
            PreparedStatement insertIS = conexion.prepareStatement(insert);
            insertIS.setShort(1, id);
            insertIS.setString(2, nombre);
            insertIS.executeUpdate();
        } catch (SQLException ex) {
            try {
                PreparedStatement updateIS = conexion.prepareStatement(update);
                updateIS.setString(1, nombre);
                updateIS.setShort(2, id);
                updateIS.executeUpdate();
            } catch (SQLException ex1) {
            }
        }
    }

    private static void update(String sql) {
        if (isConnected(true)) {
            try {
                conexion.createStatement().execute(sql);
            } catch (SQLException ex) {
            }
        }
    }
}
