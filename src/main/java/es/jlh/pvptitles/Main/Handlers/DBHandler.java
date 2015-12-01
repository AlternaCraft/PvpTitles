package es.jlh.pvptitles.Main.Handlers;

import es.jlh.pvptitles.Backend.ConfigDataStore;
import es.jlh.pvptitles.Backend.DatabaseManager;
import es.jlh.pvptitles.Backend.DatabaseManagerEbean;
import es.jlh.pvptitles.Backend.DatabaseManagerMysql;
import es.jlh.pvptitles.Backend.EbeanTables.PlayerPT;
import es.jlh.pvptitles.Backend.EbeanTables.SignPT;
import es.jlh.pvptitles.Backend.EbeanTables.WorldPlayerPT;
import es.jlh.pvptitles.Backend.MySQLConnection;
import es.jlh.pvptitles.Libraries.Ebean;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.PLUGIN;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author AlternaCraft
 */
public class DBHandler {

    private DatabaseManager dm = null;
    public static DBTYPE tipo = null;

    public static enum DBTYPE {
        EBEAN,
        MYSQL;
    }

    public Ebean ebeanServer = null;
    private Connection mysql = null;

    private PvpTitles pvpTitles = null;
    private FileConfiguration config = null;

    public DBHandler(PvpTitles pvpTitles, FileConfiguration config) {
        this.pvpTitles = pvpTitles;
        this.config = config;
        tipo = DBTYPE.EBEAN;
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
                this.mysqlConnect();

                if (MySQLConnection.estado_conexion == MySQLConnection.Estado.SIN_CONEXION) {
                    tipo = DBTYPE.EBEAN;
                    selectDB();
                } else {
                    dm = new DatabaseManagerMysql(pvpTitles, mysql);
                }
                break;
        }
    }
    
    public void autoExportData() {
        if (tipo == DBTYPE.EBEAN) {
            if (pvpTitles.cm.params.isAuto_export_to_sql()) {
                dm.DBExport();
            }
        } else if (tipo == DBTYPE.MYSQL) {
            pvpTitles.cm.loadServers();
            if (pvpTitles.cm.params.isAuto_export_to_json()) {
                dm.DBExport();
            }
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
        config.set("database.logging", config.getBoolean("database.logging", PvpTitles.debugMode));
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
                config.getBoolean("database.logging"),
                config.getBoolean("database.rebuild")
        );

        this.pvpTitles.getServer().getConsoleSender().sendMessage(
                PLUGIN + ChatColor.YELLOW + "Ebean database " + ChatColor.AQUA + "loaded correctly."
        );
    }

    /**
     * Conexion a MySQL
     */
    public void mysqlConnect() {
        ConfigDataStore params = pvpTitles.cm.params;

        MySQLConnection.connectDB(params.getHost() + ":" + params.getPort()
                + "/" + params.getDb(), params.getUser(), params.getPass());

        if (MySQLConnection.estado_conexion == MySQLConnection.Estado.SIN_CONEXION) {
            tipo = DBTYPE.EBEAN;
        } else {
            tipo = DBTYPE.MYSQL;
            mysql = MySQLConnection.getConnection();

            MySQLConnection.creaDefault();

            MySQLConnection.registraServer(params.getMultiS(), params.getNameS());

            this.pvpTitles.getServer().getConsoleSender().sendMessage(
                    PLUGIN + ChatColor.YELLOW + "MySQL database " + ChatColor.AQUA + "loaded correctly."
            );
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
