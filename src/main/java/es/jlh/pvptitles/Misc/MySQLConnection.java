package es.jlh.pvptitles.Misc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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
            System.out.println("No se ha encontrado la libreria del programa");
            estado_conexion = Estado.SIN_CONEXION;
        } catch (SQLException ex) {
            System.out.println("[PvpTitles] " + ((ex.getErrorCode() == 0)
                    ? "Could not connect to MySQL DB" : "Error MySQL") + "; "
                    + "Using Ebean per default...");
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
        update(getTablePS());
        update(getTablePW());
        update(getTableSS());
        update(getTableNI());
        update(getTablePT());
    }

    public static String getTablePS() {
        return "create table IF NOT EXISTS PlayerServer ("
                + "idServer int(3) DEFAULT -1,"
                + "playerName varchar(100),"
                + "famePoints int(3),"
                + "ultMod date,"
                + "PRIMARY KEY (idServer, playerName)"
                + ") ENGINE=InnoDB;";
    }

    public static String getTablePW() {
        return "create table IF NOT EXISTS PlayerWorld ("
                + "idServer int(3),"
                + "playerName varchar(100),"
                + "worldName varchar(100),"
                + "famePoints int(3),"
                + "PRIMARY KEY (idServer, playerName, worldName)"
                + ") ENGINE=InnoDB;";
    }

    public static String getTableSS() {
        return "create table IF NOT EXISTS SignsServer ("
                + "nombre varchar(100),"
                + "modelo varchar(100),"
                + "server varchar(100),"
                + "serverID int,"
                + "orientacion varchar(100),"
                + "world varchar(100),"
                + "x int,"
                + "y int,"
                + "z int,"
                + "blockface int,"
                + "PRIMARY KEY (serverID, world, x, y, z)"
                + ") ENGINE=InnoDB;";
    }

    public static String getTableNI() {
        return "create table IF NOT EXISTS NameID ("
                + "idServer int(3) primary key,"
                + "nombreS varchar(100)"
                + ") ENGINE=InnoDB;";
    }
    
    public static String getTablePT() {
        return "create table IF NOT EXISTS PlayerTime ("
                + "playerName varchar(100),"
                + "playedTime int,"
                + "PRIMARY KEY (playerName, playedTime)"
                + ") ENGINE=InnoDB;";
    }

    public static void registraServer(int id, String nombre) {
        String insert = "insert into NameID values (?,?)";
        String update = "update NameID set nombreS=? where idServer=?";
        try {
            PreparedStatement insertIS = conexion.prepareStatement(insert);
            insertIS.setInt(1, id);
            insertIS.setString(2, nombre);
            insertIS.executeUpdate();
        } catch (SQLException ex) {
            try {
                PreparedStatement updateIS = conexion.prepareStatement(update);
                updateIS.setString(1, nombre);
                updateIS.setInt(2, id);
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
