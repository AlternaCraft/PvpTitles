package es.jlh.pvptitles.Managers.DB;

import es.jlh.pvptitles.Main.Manager;
import es.jlh.pvptitles.Main.PvpTitles;
import es.jlh.pvptitles.Misc.MySQLConnection;
import es.jlh.pvptitles.Objects.LBData;
import es.jlh.pvptitles.Objects.PlayerFame;
import es.jlh.pvptitles.Objects.TimedPlayer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author julito
 */
public class DatabaseManagerMysql implements DatabaseManager {

    private PvpTitles mpt;
    private Connection mysql;

    public DatabaseManagerMysql(PvpTitles mpt, Connection mysql) {
        this.mpt = null;
        this.mysql = null;
        this.mpt = mpt;
        this.mysql = mysql;
    }

    // Combinar tablas
    @Override
    public void savePlayerFame(UUID playerUUID, int fame) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String sql = "select playerName from PlayerServer where playerName like '"
                + playerUUID.toString() + "' AND idServer=" + mpt.cm.params.getMultiS();
        String update = "update PlayerServer set famePoints=?,ultMod=? where playerName=?"
                + " AND idServer=?";

        boolean existe = false;
        try {
            ResultSet rs = mysql.createStatement().executeQuery(sql);

            if (rs.next()) {
                existe = true;
            }

            if (!existe) {
                createPlayer(playerUUID);
            }

            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            Player pl = Bukkit.getServer().getPlayer(playerUUID);

            if (mpt.cm.params.isMw_enabled()) {
                String sql2 = "select playerName from PlayerWorld where playerName like '"
                        + playerUUID.toString() + "' AND idServer=" + mpt.cm.params.getMultiS()
                        + " AND worldName like '" + pl.getWorld().getName() + "'";
                String update2 = "update PlayerWorld set famePoints=? where playerName=?"
                        + " AND idServer=? AND worldName=?";

                existe = false;

                rs = mysql.createStatement().executeQuery(sql2);

                if (rs.next()) {
                    existe = true;
                }

                if (!existe) {
                    createPlayerW(playerUUID, pl.getWorld().getName());
                }

                PreparedStatement updateFame = mysql.prepareStatement(update2);
                updateFame.setInt(1, fame);
                updateFame.setString(2, playerUUID.toString());
                updateFame.setInt(3, mpt.cm.params.getMultiS());
                updateFame.setString(4, pl.getWorld().getName());
                updateFame.executeUpdate();
            } else {
                PreparedStatement updateFame = mysql.prepareStatement(update);
                updateFame.setInt(1, fame);
                updateFame.setDate(2, sqlDate);
                updateFame.setString(3, playerUUID.toString());
                updateFame.setInt(4, mpt.cm.params.getMultiS());
                updateFame.executeUpdate();
            }
        } catch (SQLException ex) {
        }
    }

    @Override
    public int loadPlayerFame(UUID playerUUID) {
        String sql = (new StringBuilder()).append("select famePoints from PlayerServer "
                + "where playerName like '").append(playerUUID.toString()).toString() + "'"
                + " AND idServer=" + mpt.cm.params.getMultiS();
        int fama = 0;

        if (!MySQLConnection.isConnected()) {
            return fama;
        }

        try {
            Player pl = Bukkit.getServer().getPlayer(playerUUID);

            if (mpt.cm.params.isMw_enabled()) {
                String sql2 = "select famePoints from PlayerWorld where playerName like '"
                        + playerUUID.toString() + "' AND idServer=" + mpt.cm.params.getMultiS()
                        + " AND worldName like '" + pl.getWorld().getName() + "'";

                ResultSet rs = mysql.createStatement().executeQuery(sql2);
                if (rs.next()) {
                    fama = rs.getInt("famePoints");
                }
            } else {
                ResultSet rs = mysql.createStatement().executeQuery(sql);
                if (rs.next()) {
                    fama = rs.getInt("famePoints");
                }
            }
        } catch (SQLException ex) {
        }
        return fama;
    }

    @Override

    public int loadPlayerFame(UUID playerUUID, String world) {
        int fama = 0;

        if (!MySQLConnection.isConnected()) {
            return fama;
        }

        try {
            String sql = "select famePoints from PlayerWorld where playerName like '"
                    + playerUUID.toString() + "' AND idServer=" + mpt.cm.params.getMultiS()
                    + " AND worldName like '" + world + "'";

            ResultSet rs = mysql.createStatement().executeQuery(sql);
            if (rs.next()) {
                fama = rs.getInt("famePoints");
            }

        } catch (SQLException ex) {
        }

        return fama;
    }

    @Override
    public void firstRunPlayer(Player player) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        UUID playerUUID = player.getUniqueId();

        String sql = "select idServer from PlayerServer where playerName like '"
                + playerUUID.toString() + "' AND (idServer=" + mpt.cm.params.getMultiS()
                + " OR idServer=-1)";

        String update = "update PlayerServer set ultMod=? where playerName=?"
                + " AND idServer=?";
        String update2 = "update PlayerServer set idServer=? where playerName=?"
                + " AND idServer=-1";

        boolean existe = false;
        try {
            ResultSet rs = mysql.createStatement().executeQuery(sql);

            int idServer = -1;

            if (rs.next()) {
                existe = true;
                idServer = rs.getInt("idServer");
            }

            if (!existe) {
                createPlayer(playerUUID);
            } else {
                // Pequeño fix para los jugadores que no tengan server (ebean)
                if (idServer == -1 && mpt.cm.params.getMultiS() != -1) {
                    PreparedStatement modID = mysql.prepareStatement(update2);
                    modID.setInt(1, mpt.cm.params.getMultiS());
                    modID.setString(2, playerUUID.toString());
                    modID.executeUpdate();
                }
            }

            java.util.Date utilDate = new java.util.Date();
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

            PreparedStatement modFecha = mysql.prepareStatement(update);
            modFecha.setDate(1, sqlDate);
            modFecha.setString(2, playerUUID.toString());
            modFecha.setInt(3, mpt.cm.params.getMultiS());
            modFecha.executeUpdate();

            if (mpt.cm.params.isMw_enabled()) {
                sql = "select idServer from PlayerWorld where playerName like '"
                        + playerUUID.toString() + "' AND (idServer=" + mpt.cm.params.getMultiS()
                        + " OR idServer=-1) AND worldName like '" + player.getWorld().getName() + "'";

                update = "update PlayerWorld set idServer=? where playerName=?"
                        + " AND idServer=-1 AND worldName=?";

                existe = false;

                rs = mysql.createStatement().executeQuery(sql);

                idServer = -1;

                if (rs.next()) {
                    existe = true;
                }

                if (!existe) {
                    createPlayerW(playerUUID, player.getWorld().getName());
                } else {
                    // Pequeño fix para los jugadores que no tengan server (ebean)
                    if (idServer == -1 && mpt.cm.params.getMultiS() != -1) {
                        PreparedStatement modID = mysql.prepareStatement(update);
                        modID.setInt(1, mpt.cm.params.getMultiS());
                        modID.setString(2, playerUUID.toString());
                        modID.setString(3, player.getWorld().getName());
                        modID.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
        }
    }

    @Override
    public void savePlayedTime(TimedPlayer player) {
        String sql = "select playedTime from playerTime where playerName like '"
                + player.getUniqueId().toString() + "'";
        String insert = "insert into playerTime values (?,?)";
        String update = "update playerTime set playedTime = ? where playerName like ?";

        try {
            int time = player.getTotalOnline();

            ResultSet rs = mysql.createStatement().executeQuery(sql);

            if (rs.next()) {
                time += rs.getInt("playedTime");

                PreparedStatement actTime = mysql.prepareStatement(update);
                actTime.setInt(1, time);
                actTime.setString(2, player.getUniqueId().toString());
                actTime.executeUpdate();
            } else {
                PreparedStatement actTime = mysql.prepareStatement(insert);
                actTime.setString(1, player.getUniqueId().toString());
                actTime.setInt(2, time);
                actTime.executeUpdate();
            }
        } catch (SQLException ex) {
        }
    }

    @Override
    public int loadPlayedTime(UUID playerUUID) {
        String sql = "select playedTime from playerTime where playerName like '"
                + playerUUID.toString() + "'";

        try {
            ResultSet rs = mysql.createStatement().executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("playedTime");
            }
        } catch (SQLException ex) {
        }

        return 0;
    }

    /**
     * Metodo para crear un jugador en la base de datos
     *
     * @param playerUUID UUID del jugador
     */
    private void createPlayer(UUID playerUUID) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String update = "insert into PlayerServer(idServer, playerName, "
                + "famePoints, ultMod) values (?, ?, ?, ?)";
        java.util.Date utilDate = new java.util.Date();
        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

        try {
            PreparedStatement creaPlayer = mysql.prepareStatement(update);
            creaPlayer.setInt(1, mpt.cm.params.getMultiS());
            creaPlayer.setString(2, playerUUID.toString());
            creaPlayer.setInt(3, 0);
            creaPlayer.setDate(4, sqlDate);
            creaPlayer.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    /**
     * Metodo para crear un jugador en la base de datos
     *
     * @param playerUUID UUID del jugador
     */
    private void createPlayerW(UUID playerUUID, String world) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String update = "insert into PlayerWorld(idServer, playerName, "
                + "worldName, famePoints) values (?, ?, ?, ?)";

        try {
            PreparedStatement creaPlayer = mysql.prepareStatement(update);
            creaPlayer.setInt(1, mpt.cm.params.getMultiS());
            creaPlayer.setString(2, playerUUID.toString());
            creaPlayer.setString(3, world);
            creaPlayer.setInt(4, 0);
            creaPlayer.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void registraCartel(String nombre, String modelo, String server,
            Location l, String orientacion, int blockface) {
        if (!MySQLConnection.isConnected()) {
            return;
        }
        String cartel = "insert into SignsServer(nombre, modelo, server, serverID, orientacion, "
                + "world, x, y, z, blockface) values (?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement regCartel = mysql.prepareStatement(cartel);
            regCartel.setString(1, nombre);
            regCartel.setString(2, modelo);
            regCartel.setString(3, server);
            regCartel.setInt(4, mpt.cm.params.getMultiS());
            regCartel.setString(5, orientacion);
            regCartel.setString(6, l.getWorld().getName());
            regCartel.setInt(7, l.getBlockX());
            regCartel.setInt(8, l.getBlockY());
            regCartel.setInt(9, l.getBlockZ());
            regCartel.setInt(10, blockface);
            regCartel.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void modificaCartel(Location l) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String cartel = "update SignsServer set serverID = ? where serverID=-1 "
                + "AND world=? AND x=? AND y=? AND z=?";
        try {
            PreparedStatement actCartel = mysql.prepareStatement(cartel);
            actCartel.setInt(1, mpt.cm.params.getMultiS());
            actCartel.setString(2, l.getWorld().getName());
            actCartel.setInt(3, l.getBlockX());
            actCartel.setInt(4, l.getBlockY());
            actCartel.setInt(5, l.getBlockZ());
            actCartel.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public void borraCartel(Location l) {
        if (!MySQLConnection.isConnected()) {
            return;
        }

        String cartel = "delete from SignsServer where serverID=? AND world=? "
                + "AND x=? AND y=? AND z=?";
        try {
            PreparedStatement regCartel = mysql.prepareStatement(cartel);
            regCartel.setInt(1, mpt.cm.params.getMultiS());
            regCartel.setString(2, l.getWorld().getName());
            regCartel.setInt(3, l.getBlockX());
            regCartel.setInt(4, l.getBlockY());
            regCartel.setInt(5, l.getBlockZ());
            regCartel.executeUpdate();
        } catch (SQLException ex) {
        }
    }

    @Override
    public ArrayList buscaCarteles() {
        String sql = "select * from SignsServer where serverID = -1 OR "
                + "serverID = " + mpt.cm.params.getMultiS();
        ArrayList sd = new ArrayList();

        if (!MySQLConnection.isConnected()) {
            return sd;
        }

        try {
            LBData sdc;
            for (ResultSet rs = mysql.createStatement().executeQuery(sql); rs.next(); sd.add(sdc)) {
                String nombre = rs.getString("nombre");
                String modelo = rs.getString("modelo");
                String server = rs.getString("server");
                String orientacion = rs.getString("orientacion");
                int blockface = rs.getInt("blockface");
                String world = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                Location l = new Location(mpt.getServer().getWorld(world), x, y, z);
                sdc = new LBData(nombre, modelo, server, l);
                sdc.setOrientacion(orientacion);
                sdc.setBlockface(blockface);
            }

        } catch (SQLException ex) {
        }
        return sd;
    }

    @Override
    public ArrayList getTopPlayers(int cant, String server) {
        HashMap<Integer, List<String>> servidores = mpt.cm.servers.get(server);
        String sql = "";

        if (mpt.cm.params.isMw_enabled()) {
            sql = "select idServer, playerName, famePoints, worldName from PlayerWorld ";
        } else {
            sql = "select idServer, playerName, famePoints from PlayerServer ";
        }

        if (!"".equals(server) && servidores != null && mpt.cm.servers.containsKey(server)) {
            // Si hay un '-1' recojo los jugadores de todos los servers
            if (servidores.size() > 0 && !servidores.containsKey(-1)) {
                sql += "where";
                for (Iterator<Integer> iterator = servidores.keySet().iterator(); iterator.hasNext();) {
                    Integer next = iterator.next();
                    sql += " (idServer = " + next;

                    if (mpt.cm.params.isMw_enabled() && !servidores.get(next).isEmpty()) {
                        sql += " AND";
                        for (Iterator<String> mundo = servidores.get(next).iterator(); mundo.hasNext();) {
                            String mundoElegido = mundo.next();
                            sql += " worldName like '" + mundoElegido + "' OR";
                        }
                        sql = sql.substring(0, sql.length() - 3);
                    }

                    sql += ") OR";
                }
                sql = sql.substring(0, sql.length() - 4) + ")";
            }
        } else {
            sql += "where idServer=" + mpt.cm.params.getMultiS();

            if (mpt.cm.params.isMw_enabled() && servidores != null && servidores.get(mpt.cm.params.getMultiS()) != null) {
                sql += " AND";
                for (Iterator<String> mundo = servidores.get(mpt.cm.params.getMultiS()).iterator(); mundo.hasNext();) {
                    String mundoElegido = mundo.next();
                    sql += " worldName like '" + mundoElegido + "' OR";
                }
                sql = sql.substring(0, sql.length() - 3);
            }
        }

        sql += " order by famePoints DESC limit " + cant;

        ArrayList rankedPlayers = new ArrayList();

        if (!MySQLConnection.isConnected()) {
            return rankedPlayers;
        }

        try {
            List<String> worlds_disabled = mpt.cm.params.getAffectedWorlds();
            PlayerFame pf;
            
            for (ResultSet rs = mysql.createStatement().executeQuery(sql); rs.next(); rankedPlayers.add(pf)) {
                pf = new PlayerFame(rs.getString("playerName"), rs.getInt("famePoints"), this.mpt);
                pf.setServer(rs.getInt("idServer"));

                if (mpt.cm.params.isMw_enabled()) {
                    if (!mpt.cm.params.showOnLeaderBoard() && worlds_disabled.contains(rs.getString("worldName")))
                        continue;
                    
                    pf.setWorld(rs.getString("worldName"));
                }
            }
        } catch (SQLException ex) {
        }
        return rankedPlayers;
    }

    @Override
    public String getServerName(int id) {
        String nombre = "";
        String sql = "select nombreS from NameID where idServer=" + id;

        if (!MySQLConnection.isConnected()) {
            return nombre;
        }

        try {
            ResultSet rs = mysql.createStatement().executeQuery(sql);
            while (rs.next()) {
                nombre = rs.getString("nombreS");
                break;
            }
        } catch (SQLException ex) {
        }

        return nombre;
    }

    @Override
    public int purgeData() {
        int contador = 0;
        String sql = "select playerName, ultMod from PlayerServer";
        String sql2 = "delete from PlayerServer where playerName like ?";
        String sql3 = "delete from PlayerWorld where playerName like ?";

        if (!MySQLConnection.isConnected()) {
            return contador;
        }

        try {
            ResultSet rs = mysql.createStatement().executeQuery(sql);
            do {
                if (!rs.next()) {
                    break;
                }
                String nombre = rs.getString("playerName");
                Date fechaMod = rs.getDate("ultMod");
                if (!mpt.cm.params.getNoPurge().contains(nombre)) {
                    Calendar cFile = new GregorianCalendar();
                    cFile.setTime(fechaMod);
                    cFile.add(6, mpt.cm.params.getTimeP());
                    Date hoy = new Date();
                    Calendar cHoy = new GregorianCalendar();
                    cHoy.setTime(hoy);
                    if (cFile.before(cHoy)) {
                        PreparedStatement borraUsr = mysql.prepareStatement(sql2);
                        borraUsr.setString(1, nombre);
                        borraUsr.executeUpdate();

                        if (mpt.cm.params.isMw_enabled()) {
                            borraUsr = mysql.prepareStatement(sql3);
                            borraUsr.setString(1, nombre);
                            borraUsr.executeUpdate();
                        }

                        contador++;
                    }
                }
            } while (true);
        } catch (SQLException ex) {
        }
        return contador;
    }

    @Override
    public void conversor() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void conversorUUID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void SQLExport() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void exportarData(Manager.RETROCP rcp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void importarData(Manager.RETROCP rcp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
