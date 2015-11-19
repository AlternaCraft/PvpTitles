package es.jlh.pvptitles.RetroCP;

import es.jlh.pvptitles.Main.PvpTitles;

/**
 *
 * @author Julian
 */
public class DBStructure {

    private PvpTitles plugin = null;

    public static enum RETROCP {

        TIME_CREATED,
        MW_CREATED,
        NOT_CREATED;
    }

    public DBStructure(PvpTitles plugin) {
        this.plugin = plugin;
        // HI 3
    }

    public void checkEbeanDB() {
        /*dm.conversor(); // Solo se ejecuta la primera vez para convertir los ficheros     
        dm.conversorUUID();

        RETROCP rcp = RETROCP.NOT_CREATED;

        try {
            ebeanServer.getDatabase().find(PlayerWTable.class).findList();
            rcp = RETROCP.MW_CREATED;
            ebeanServer.getDatabase().find(TimeTable.class).findList();
            rcp = RETROCP.TIME_CREATED;
        } catch (Exception e) {
            dm.exportarData(rcp);

            Bukkit.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.RED + "Ebean database structure has changed...");
            Bukkit.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.RED + "Please remove 'PvpTitles.db' to load the plugin.");
            Bukkit.getServer().getConsoleSender().sendMessage(PLUGIN + ChatColor.RED + "Don't worry, you won't lose data.");

            return false;
        }

        dm.importarData(rcp);

        if (params.isAuto_export()) {
            dm.SQLExport();
        }*/
    }
}
