/*
 * Copyright (C) 2016 AlternaCraft
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
package com.alternacraft.pvptitles.RetroCP;

import com.alternacraft.pvptitles.Backend.Ebean;
import com.alternacraft.pvptitles.Backend.EbeanTables.PlayerPT;
import static com.alternacraft.pvptitles.Main.CustomLogger.showMessage;
import static com.alternacraft.pvptitles.Main.DBLoader.tipo;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.UtilsFile;
import com.alternacraft.pvptitles.RetroCP.oldTables.PlayerWTable;
import com.alternacraft.pvptitles.RetroCP.oldTables.TimeTable;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.ChatColor;

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
        Ebean ebeanserver = plugin.getManager().dbh.ebeanServer;
        RetroDMEbean rdm = new RetroDMEbean(plugin, ebeanserver);

        int status = EBEAN_OLD_VERSION;

        try {
            ebeanserver.getDatabase().find(PlayerPT.class).findList(); // Cualquiera de las nuevas vale
            status = EBEAN_NEW_STRUCTURE_CREATED;
        } catch (Exception e1) {
            try {
                ebeanserver.getDatabase().find(PlayerWTable.class).findList();
                status = EBEAN_MW_CREATED;
                ebeanserver.getDatabase().find(TimeTable.class).findList();
                status = EBEAN_TIME_CREATED;
            } catch (Exception e2) {
            }
            
            rdm.exportarData(status);

            showMessage(ChatColor.RED + "Ebean database structure has changed...");
            showMessage(ChatColor.RED + "Please remove 'PvpTitles.db' to load the plugin.");
            showMessage(ChatColor.RED + "Don't worry, you won't lose data.");

            return false;
        }

        rdm.conversor();
        rdm.conversorUUID();

        plugin.getManager().getDbh().getDm().DBImport(RetroDMEbean.FILENAME);
        UtilsFile.delete(new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append(RetroDMEbean.FILENAME).toString());

        return true;
    }

    public void checkMySQLDB() {
        Connection mysql = plugin.getManager().dbh.mysql;
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

        plugin.getManager().getDbh().getDm().DBImport(RetroDMMysql.FILENAME);
        UtilsFile.delete(new StringBuilder().append(plugin.getDataFolder()).append( // Ruta
                File.separator).append(RetroDMMysql.FILENAME).toString());
    }
}
