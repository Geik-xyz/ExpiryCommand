package leaderos.web.tr.timed;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import leaderos.web.tr.timed.commands.Commands;
import leaderos.web.tr.timed.database.ConnectionPool;
import leaderos.web.tr.timed.database.DatabaseQueries;
import leaderos.web.tr.timed.utils.Scheduler;

public class Main extends JavaPlugin {
	
	public static Main instance;
	public static Set<String> configValue;
	
	
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		ConnectionPool.initsqlite();
		DatabaseQueries.createTable();
		Scheduler.startChecker();
		getCommand("expirycommand").setExecutor(new Commands(this));
		getCommand("kalansorgu").setExecutor(new Commands(this));
		configValue = Main.instance.getConfig().getConfigurationSection("Commands").getKeys(false);
		MetricLoader();
	}
	
	
	public static String color(String yazirengi){return ChatColor.translateAlternateColorCodes('&', yazirengi);}
	
	public void MetricLoader() {
		Metrics metrics = new Metrics(this, 7561);
        metrics.addCustomChart(new Metrics.DrilldownPie("java_version", () -> {
        Map<String, Map<String, Integer>> map = new HashMap<>();
        String javaVersion = System.getProperty("java.version");
        Map<String, Integer> entry = new HashMap<>();
        entry.put(javaVersion, 1);
        if (javaVersion.startsWith("1.7")) {
            map.put("Java 1.7", entry);
        } else if (javaVersion.startsWith("1.8")) {
            map.put("Java 1.8", entry);
        } else if (javaVersion.startsWith("1.9")) {
            map.put("Java 1.9", entry);
        } else {
            map.put("Other", entry);
        }
        return map;
        }));
	}

}
