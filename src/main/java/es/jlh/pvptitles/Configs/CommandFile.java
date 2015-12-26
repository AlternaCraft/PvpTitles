package es.jlh.pvptitles.Configs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author AlternaCraft
 */
public class CommandFile {
    public File commandsFile = new File("plugins/PvpTitles/commands.yml");        
    
    public CommandFile() {
    }
    
    public YamlConfiguration load() {
        if (!commandsFile.exists()) {
            createConfig();
        }
        
        return YamlConfiguration.loadConfiguration(commandsFile);
    }
    
    private void createConfig() {
        YamlConfiguration newConfig = new YamlConfiguration();
        
        newConfig.options().header (
            "########################\n" + 
            "##  [COMMAND SYSTEM]  ##\n" +
            "########################\n" +
            "# money requires vault #"
        );
        newConfig.options().copyHeader(true);
        
        String[] activos = {"Rank", "Fame", "EachKill", "Killstreak"};
        
        // Comandos de ejemplo
        String[] commands1 = {"say Congratulations <Player>, now you are God"};
        String[] commands2 = {"give exp <player> 1000"};
        String[] commands3 = {"exp give <player> 10"};
        String[] commands4 = {"pvpfame add <player> 100"};
        String[] commands5 = {"economy give <player> 5000", "broadcast Wow, <Player> is on roll"};
        
        newConfig.set("activeRewards", Arrays.asList(activos));
        
        // filtro por rango
        newConfig.set("Rewards.Rank.onRank", "God");
        newConfig.set("Rewards.Rank.money", 500);
        newConfig.set("Rewards.Rank.command", Arrays.asList(commands1));
        
        // filtro por fama
        newConfig.set("Rewards.Fame.onFame", 1000);
        newConfig.set("Rewards.Fame.money", 100);
        newConfig.set("Rewards.Fame.command", Arrays.asList(commands2));

        // filtro por racha
        newConfig.set("Rewards.Killstreak.onKillstreak", 15);
        newConfig.set("Rewards.Killstreak.command", Arrays.asList(commands5));
        
        // Sin filtro, por rank up
        newConfig.set("Rewards.EachKill.command", Arrays.asList(commands3));
        
        // Sin activar
        newConfig.set("Rewards.NoActive.onRank", "Test");
        newConfig.set("Rewards.NoActive.onFame", 500);
        newConfig.set("Rewards.NoActive.command", Arrays.asList(commands4));
        
        try {
            newConfig.save(commandsFile);
        } 
        catch (IOException e) {
        }
    }
}
