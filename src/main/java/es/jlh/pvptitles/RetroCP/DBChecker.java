package es.jlh.pvptitles.RetroCP;

import es.jlh.pvptitles.Backend.EbeanTables.PlayerPT;
import es.jlh.pvptitles.Libraries.Ebean;
import static es.jlh.pvptitles.Main.Handlers.DBHandler.tipo;
import es.jlh.pvptitles.RetroCP.oldTables.TimeTable;
import es.jlh.pvptitles.RetroCP.oldTables.PlayerWTable;
import es.jlh.pvptitles.Main.PvpTitles;
import static es.jlh.pvptitles.Main.PvpTitles.showMessage;
import es.jlh.pvptitles.Misc.UtilsFile;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;

/**
 *
 * @author AlternaCraft
 */
public class DBChecker {

    /* Ebean */
    public static final short EBEAN_NEW_STRUCTURE_CREATED = 4;
    public static final short EBEAN_TIME_CREATED = 3;
    public static final short EBEAN_MW_CREATED = 2;
    public static final short EBEAN_OLD_VERSION = 1;
    /* MySQL */
    public static final short MYSQL_NEW_STRUCTURE_CREATED = 3;
    public static final short MYSQL_TIME_CREATED = 2;
    public static final short MYSQL_OLD_VERSION = 1;

    private PvpTitles plugin = null;

    public DBChecker(PvpTitles plugin) {
        this.plugin = plugin;
    }

    public boolean setup() {
        switch (tipo) {
            case EBEAN:
                return this.checkEbeanDB();
            case MYSQL:
                this.checkMySQLDB();
        }

        return true;
    }

    @SuppressWarnings("UnusedAssignment")
    public boolean checkEbeanDB() {
        Ebean ebeanserver = plugin.cm.dbh.ebeanServer;
        RetroDMEbean rdm = new RetroDMEbean(plugin, ebeanserver);

        int status = EBEAN_OLD_VERSION;

        try {
            ebeanserver.getDatabase().find(PlayerPT.class).findList(); // Cualquiera de las nuevas vale
            status = EBEAN_NEW_STRUCTURE_CREATED;
        } catch (Exception ex) {
            try {
                ebeanserver.getDatabase().find(PlayerWTable.class).findList();
                status = EBEAN_MW_CREATED;
                ebeanserver.getDatabase().find(TimeTable.class).findList();
                status = EBEAN_TIME_CREATED;
            } catch (Exception e) {
            }

            rdm.exportarData(status);

            showMessage(ChatColor.RED + "Ebean database structure has changed...");
            showMessage(ChatColor.RED + "Please remove 'PvpTitles.db' to load the plugin.");
            showMessage(ChatColor.RED + "Don't worry, you won't lose data.");

            return false;
        }

        rdm.conversor();
        rdm.conversorUUID();

        plugin.cm.getDbh().getDm().DBImport(RetroDMEbean.FILENAME);
        UtilsFile.delete(new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append(RetroDMEbean.FILENAME).toString());

        return true;
    }

    public void checkMySQLDB() {
        Connection mysql = plugin.cm.dbh.mysql;
        RetroDMMysql rdm = new RetroDMMysql(plugin, mysql);

        int status = MYSQL_OLD_VERSION;

        try {
            ResultSet rs = mysql.createStatement().executeQuery("show tables like 'SignsServer'");
            if (rs.next()) {
                rs = mysql.createStatement().executeQuery("show tables like 'PlayersTime'");
                if (rs.next()) {
                    status = MYSQL_TIME_CREATED;
                }
                rdm.exportarData(status);
            }
        } catch (SQLException ex) {
        }

        plugin.cm.getDbh().getDm().DBImport(RetroDMMysql.FILENAME);
        UtilsFile.delete(new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append(RetroDMMysql.FILENAME).toString());
    }
}
