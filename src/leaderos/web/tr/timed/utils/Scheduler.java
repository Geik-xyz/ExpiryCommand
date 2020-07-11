package leaderos.web.tr.timed.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import leaderos.web.tr.timed.Main;
import leaderos.web.tr.timed.database.DatabaseQueries;

public class Scheduler {
	
	public static BukkitRunnable checker;
	public static void startChecker() {
		 if (checker != null) {
			 checker.cancel();
			 checker = null;
	        }
		 checker = new BukkitRunnable() {
	            @Override
	            public synchronized void cancel() throws IllegalStateException {
	                super.cancel();
	            }
	            public void run() {	            	
	            	List<TimeObject> object = new ArrayList<>();
	            	try {object = DatabaseQueries.getExpiredPlayers();} catch (SQLException | ClassNotFoundException e) {e.printStackTrace();}
	            	if (object.isEmpty()) return;
	            	
	            	for (TimeObject d : object) {
	            		String name = d.getName();
	            		String product = d.getProduct();
	            		for (String s : Main.instance.getConfig().getStringList("Commands." + product + ".end")) {
	            			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.color(s.replace("%player%", name)));}
	            	}
	            	
	            	try {
						DatabaseQueries.removeExpiredPersons();
					} catch (SQLException | ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	  			  
	  			  
	            }
	        };
	        checker.runTaskTimer(Main.instance, Main.instance.getConfig().getInt("IntervalTick")*60*20, Main.instance.getConfig().getInt("IntervalTick")*60*20);
	    }

}
