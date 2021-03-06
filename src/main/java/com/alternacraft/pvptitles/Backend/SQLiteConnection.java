package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Exceptions.DBException;
import com.alternacraft.pvptitles.Main.CustomLogger;
import com.alternacraft.pvptitles.Main.PvpTitles;
import com.alternacraft.pvptitles.Misc.UtilsFile;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author AlternaCraft
 */
public class SQLiteConnection extends SQLConnection {

    private final String filedir;
    private final String filename;

    public SQLiteConnection(String filedir, String filename) {
        this.filedir = filedir;
        this.filename = filename;
        this.createDatabase();
    }

    private void createDatabase() {
        File f = new File(PvpTitles.PLUGIN_DIR, this.filename);
        if (!UtilsFile.exists(f)) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                CustomLogger.logError(this.filename, ex);
            }
        }
    }

    @Override
    public void connectDB(boolean reconnect, String... args) throws DBException {
        try {
            Class.forName(DRIVER_SQLITE);
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.filedir + this.filename);
            // Enable key update on foreign changes
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            status = Status.CONNECTED;
        } catch (ClassNotFoundException ex) {
            if (!reconnect) {
                CustomLogger.logError("SQLite library not found");
            }
            status = Status.NOT_CONNECTED;
        } catch (SQLException ex) {
            status = Status.NOT_CONNECTED;
            if (!reconnect) {
                throw new DBException("SQLite error: " + ex.getErrorCode(),
                        DBException.DBMethod.DB_CONNECT);
            }
        }
    }

    @Override
    public void load() throws DBException {
        try {
            slowUpdate(getTableServers());
            slowUpdate(getTablePlayerServer());
            slowUpdate(getTablePlayerMeta());
            slowUpdate(getTablePlayerWorld());
            slowUpdate(getTableSigns());
            slowUpdate(getTriggerMeta());
        } catch (SQLException ex) {
            throw new DBException(DBException.UNKNOWN_ERROR, DBException.DBMethod.STRUCTURE, ex.getMessage()) {
                {
                    this.setStackTrace(ex.getStackTrace());
                }
            };
        }
    }

    public static String getTriggerMeta() {
        return "CREATE TRIGGER IF NOT EXISTS 'create_player_meta' "
                + "AFTER INSERT ON 'PlayerServer' BEGIN "
                    + "INSERT INTO PlayerMeta(psid) SELECT max(id) FROM PlayerServer;"
                    + "UPDATE PlayerMeta SET lastlogin = (SELECT DATE()) WHERE "
                    + "psid = (SELECT max(id) FROM PlayerServer);"
                + "END;";
    }
}
