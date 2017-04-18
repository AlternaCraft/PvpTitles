package com.alternacraft.pvptitles.Backend;

import com.alternacraft.pvptitles.Libraries.SQLConnection;
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
    public void connectDB(boolean reconnect, String... args) {        
        try {
            Class.forName(DRIVER_SQLITE);
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.filedir + this.filename);
            status = STATUS_AVAILABLE.CONNECTED;
        } catch (ClassNotFoundException ex) {
            if (!reconnect) CustomLogger.logError("SQLite library not found");
                status = STATUS_AVAILABLE.NOT_CONNECTED;
        } catch (SQLException ex) {
            if (!reconnect) CustomLogger.logError("SQLite error: " 
                    + ex.getErrorCode() + "; Using Ebean per default...");
            status = STATUS_AVAILABLE.NOT_CONNECTED;
        }   
    }

    @Override
    public void load() {
        update(getTableServers());
        update(getTablePlayerServer());
        update(getTablePlayerMeta());
        update(getTablePlayerWorld());
        update(getTableSigns());
    }
}
