package es.jlh.pvptitles.Backend;

import es.jlh.pvptitles.Main.PvpTitles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author julito
 */
public class MySQLConnection {

    /**
     * constante DRIVER para definir el conector jdbc
     */
    public static final String DRIVER = "com.mysql.jdbc.Driver";

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
     */
    public static void connectDB(String ruta, String user, String pass) {
        try {
            Class.forName(DRIVER);
            conexion = DriverManager.getConnection("jdbc:mysql://" + ruta, user, pass);
            estado_conexion = Estado.CONECTADO;
        } catch (ClassNotFoundException ex) {
            System.out.println("MySQL library not found");
            estado_conexion = Estado.SIN_CONEXION;
        } catch (SQLException ex) {
            PvpTitles.logger.log(Level.SEVERE, "[PvpTitles] {0}" + "; "
                    + "Using Ebean per default...", ((ex.getErrorCode() == 0)
                            ? "Could not connect to MySQL DB" : "Error MySQL"));
            estado_conexion = Estado.SIN_CONEXION;
        }
    }

    /**
     * Método para comprobar si existe una conexion con la bdd
     *
     * @return Booleano con el resultado
     */
    public static boolean isConnected() {
        boolean iniciada = false, valida = false;

        iniciada = (conexion != null);

        if (iniciada) {
            try {
                valida = conexion.isValid(5);
                estado_conexion = Estado.CONECTADO;
            } catch (SQLException ex) {
                estado_conexion = Estado.SIN_CONEXION;
            }
        }

        return iniciada && valida;
    }

    /**
     * Método para cerrar la conexion
     *
     * @throws SQLException Fallo al cerrar la conexion
     */
    public static void closeConnection() throws SQLException {
        if (isConnected()) {
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
        if (isConnected()) {
            try {
                conexion.createStatement().execute(sql);
            } catch (SQLException ex) {
            }
        }
    }
}
