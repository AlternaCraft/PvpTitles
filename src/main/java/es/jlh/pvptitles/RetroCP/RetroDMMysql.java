package es.jlh.pvptitles.RetroCP;

import es.jlh.pvptitles.Backend.MySQLConnection;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.UtilsFile;
import static es.jlh.pvptitles.RetroCP.DBChecker.MYSQL_TIME_CREATED;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author AlternaCraft
 */
public class RetroDMMysql {

    public static final String FILENAME = "database_temp.sql";

    // <editor-fold defaultstate="collapsed" desc="VARIABLES AND CONSTRUCTOR">
    private PvpTitles pt = null;
    private Connection mysql = null;

    public RetroDMMysql(PvpTitles pt, Connection mysql) {
        this.pt = pt;
        this.mysql = mysql;
    }
    // </editor-fold>

    public void exportarData(int status) {
        String ruta = new StringBuilder().append(
                pt.getDataFolder()).append( // Ruta
                        File.separator).append( // Separador
                        FILENAME).toString();

        String sql = "";

        sql += "drop table if exists PlayerServer;\n";
        sql += "drop table if exists PlayerTime;\n";
        sql += "drop table if exists PlayerWorld;\n";
        sql += "drop table if exists nameid;\n";
        sql += "drop table if exists SignsServer;\n";

        sql += MySQLConnection.getTableServers() + "\n";
        sql += MySQLConnection.getTablePlayerServer() + "\n";
        sql += MySQLConnection.getTablePlayerMeta() + "\n";
        sql += MySQLConnection.getTablePlayerWorld() + "\n";
        sql += MySQLConnection.getTableSigns() + "\n";

        try {
            ResultSet ni = mysql.createStatement().executeQuery("select * from NameID");
            ResultSet ps = mysql.createStatement().executeQuery("select * from PlayerServer");
            ResultSet ptt = null;
            if (status >= MYSQL_TIME_CREATED) {
                ptt = mysql.createStatement().executeQuery("select * from PlayerTime");
            }
            ResultSet pw = mysql.createStatement().executeQuery("select * from PlayerWorld");
            ResultSet ss = mysql.createStatement().executeQuery("select * from SignsServer");

            while (ni.next()) {
                if (ni.getInt("idServer") == pt.manager.params.getMultiS()) {
                    continue;
                }

                sql += "insert into Servers values (" + ni.getInt("idServer")
                        + ", '" + ni.getString("nombreS") + "');\n";
            }

            Map<String, Integer> ids = new HashMap();
            int id = 1;
            while (ps.next()) {
                sql += "insert into PlayerServer(id, playerUUID, serverID) values ("
                        + id + ", '" + ps.getString("playerName") + "', "
                        + ps.getInt("idServer") + ");\n";

                sql += "insert into PlayerMeta(psid, points, lastLogin) values ("
                        + id + ", " 
                        + ps.getInt("famePoints") + ", '"
                        + ps.getDate("ultMod") + "');\n";
                
                ids.put(ps.getString("playerName"), id);

                id++;
            }

            while (ptt != null && ptt.next()) {
                sql += "update PlayerMeta set playedTime=" + ptt.getInt("playedTime")
                        + " where psid=" + ids.get(ptt.getString("playerName"));
            }

            while (pw.next()) {
                sql += "insert into PlayerWorld(psid, worldName, points) values ("
                        + ids.get(pw.getString("playerName")) + ", '"
                        + pw.getString("worldName") + "', "
                        + pw.getString("famePoints") + ");\n";
            }

            while (ss.next()) {
                sql += "insert into Signs values ("
                        + "'" + ss.getString("nombre") + "', '" + ss.getString("modelo")
                        + "', '" + ss.getString("server") + "', '" + ss.getString("orientacion") 
                        + "', " + ss.getInt("blockface") + ", " + ss.getInt("serverID") 
                        + ", '" + ss.getString("world") + "', " + ss.getInt("x") 
                        + ", " + ss.getInt("y") + ", " + ss.getInt("z")
                        + ");\n";
            }
        } catch (SQLException ex) {
            PvpTitles.logError(ex.getMessage(), ex);
        }

        UtilsFile.writeFile(ruta, sql);
    }
}
