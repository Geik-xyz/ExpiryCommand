package leaderos.web.tr.timed.commands;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import leaderos.web.tr.timed.Main;
import leaderos.web.tr.timed.database.DatabaseQueries;
import leaderos.web.tr.timed.utils.Scheduler;
import leaderos.web.tr.timed.utils.TimeObject;

public class Commands implements CommandExecutor {
	
	@SuppressWarnings("unused")
	private Main plugin;
	public Commands(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("expirycommand")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.isOp()) {
					if (args.length == 3) {
						if (Main.configValue.contains(args[0])) {
							if (isNumeric(args[2])) {
								String target = args[1];
								if (target.equalsIgnoreCase("server")) {
									try {DatabaseQueries.setCommandInterval("CONSOLE", args[0], Integer.valueOf(args[2])); player.sendMessage(Main.color("&aBaşarıyla zaman ayarlı komut kaydedildi."));}
									catch (NumberFormatException | SQLException | ClassNotFoundException e) {e.printStackTrace();}
									for (String s : Main.instance.getConfig().getStringList("Commands." + args[0] + ".start")) {
				            			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.color(s.replace("%player%", target)));}
								} else {
									try {
										if (DatabaseQueries.playerHasTimedValue(target, args[0])) {
											DatabaseQueries.accumulateTime(target, args[0], Integer.valueOf(args[2]));
											player.sendMessage(Main.color("&aBaşarıyla zaman ayarlı komut kaydedildi."));}
										else {DatabaseQueries.setCommandInterval(target, args[0], Integer.valueOf(args[2])); player.sendMessage(Main.color("&aBaşarıyla zaman ayarlı komut kaydedildi."));}}
									catch (NumberFormatException | SQLException | ClassNotFoundException e) {e.printStackTrace();}
									for (String s : Main.instance.getConfig().getStringList("Commands." + args[0] + ".start")) {
			            			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.color(s.replace("%player%", target)));}}
							} else if (args[2].equalsIgnoreCase("sil")) try {removeTimedEvent(sender, args[1], args[0]);} catch (ClassNotFoundException | SQLException e) {e.printStackTrace();}
							else if (args[2].equalsIgnoreCase("bitir")) try {endTimedEvent(sender, args[1], args[0]);} catch (ClassNotFoundException | SQLException e) {e.printStackTrace();}
							else player.sendMessage(Main.color("&cGün sayı formatında olmalıdır."));
						} else player.sendMessage(Main.color("&cGörev configde bulunamadı lütfen configde olan bir görevi kullanın."));
					} else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
						player.sendMessage(Main.color("&aConfig başarıyla yeniden yapılandırıldı."));
						Main.instance.reloadConfig();
						Scheduler.checker.cancel();
						Scheduler.startChecker();
						Main.configValue = Main.instance.getConfig().getConfigurationSection("Commands").getKeys(false);
					} else player.sendMessage(Main.color("&cYanlış kullanım. Doğrusu: /expirycommand <görev> <player> <gün>"));
				} else player.sendMessage(Main.color("&cBunun için yeterli yetkin yok."));
			} else {
				if (args.length == 3) {
					if (Main.configValue.contains(args[0])) {
						if (isNumeric(args[2])) {
							String target = args[1];
							if (target.equalsIgnoreCase("server")) {
								try {DatabaseQueries.setCommandInterval("CONSOLE", args[0], Integer.valueOf(args[2])); sender.sendMessage(Main.color("&aBaşarıyla zaman ayarlı komut kaydedildi."));}
								catch (NumberFormatException | SQLException | ClassNotFoundException e) {e.printStackTrace();}
								for (String s : Main.instance.getConfig().getStringList("Commands." + args[0] + ".start")) {
			            			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.color(s.replace("%player%", target)));}
							} else {
								try {
									if (DatabaseQueries.playerHasTimedValue(target, args[0])) {
										DatabaseQueries.accumulateTime(target, args[0], Integer.valueOf(args[2]));
										sender.sendMessage(Main.color("&aBaşarıyla zaman ayarlı komut kaydedildi."));}
									else {DatabaseQueries.setCommandInterval(target, args[0], Integer.valueOf(args[2])); sender.sendMessage(Main.color("&aBaşarıyla zaman ayarlı komut kaydedildi."));}}
								catch (NumberFormatException | SQLException | ClassNotFoundException e) {e.printStackTrace();}
								for (String s : Main.instance.getConfig().getStringList("Commands." + args[0] + ".start")) {
			            			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.color(s.replace("%player%", target)));}}
						} else if (args[2].equalsIgnoreCase("sil")) try {removeTimedEvent(sender, args[1], args[0]);} catch (ClassNotFoundException | SQLException e) {e.printStackTrace();}
						else if (args[2].equalsIgnoreCase("bitir")) try {endTimedEvent(sender, args[1], args[0]);} catch (ClassNotFoundException | SQLException e) {e.printStackTrace();}
						else sender.sendMessage(Main.color("&cGün sayı formatında olmalıdır."));
					} else sender.sendMessage(Main.color("&cGörev configde bulunamadı lütfen configde olan bir görevi kullanın."));
				} else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
					sender.sendMessage(Main.color("&aConfig başarıyla yeniden yapılandırıldı."));
					Main.instance.reloadConfig();
					Scheduler.checker.cancel();
					Scheduler.startChecker();
				} else if (args.length == 2 && args[0].equalsIgnoreCase("migration")) {
					if (sender instanceof ConsoleCommandSender) {
						File vipzeroFile = new File("plugins/VipZero/config.yml");
						FileConfiguration vipzero = YamlConfiguration.loadConfiguration(vipzeroFile);
						if (vipzero.getStringList("vip_groups").contains(args[1])) {
							Set<String> configValue = vipzero.getConfigurationSection("vips").getKeys(false);
							int totalVip = configValue.size();
							int currentVip = 1;
							for (String s : configValue) {
								Bukkit.getConsoleSender().sendMessage(Main.color("&bExpiryCommand &fVipZero migration: &a" + currentVip + "&f/&b" + totalVip));
								if (vipzero.get("vips." + s + "." + args[1]) == null) continue;
								String date = vipzero.getString("vips." + s + ".inicio");
								int time = vipzero.getInt("vips." + s + "." + args[1]);
								SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
								// Values
								Date startTime = null;
								try {
									startTime = dateFormatter.parse(date);
								} catch (ParseException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								long startTimeUnix = startTime.getTime() / 1000L;
								long finishTimeUnix = startTimeUnix + (time*86400);
								try {
									DatabaseQueries.migrationCommandInterval(s, args[1], startTimeUnix, finishTimeUnix);
								} catch (ClassNotFoundException | SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								currentVip = currentVip + 1;
							}
						} else sender.sendMessage(Main.color("&cVipZero içerisinde böyle bir vip grubu bulunamadı."));
					} else sender.sendMessage(Main.color("&cBu komut için konsolu kullanman gerek."));
				}
				else {
					sender.sendMessage(Main.color("&cDoğru kullanımlar:"));
					sender.sendMessage(Main.color("&a/expirycommand <görev> <player> <gün>"));
					sender.sendMessage(Main.color("&a/expirycommand migration <viptürü>"));
					sender.sendMessage(Main.color("&a/expirycommand reload"));
				}
			}
		}
		else if (label.equalsIgnoreCase("kalansorgu")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.isOp() && args.length > 0) {
					String target = args[0];
					List<TimeObject> request = null;
					try {request = DatabaseQueries.getPlayerValues(target);} catch (SQLException | ClassNotFoundException e) {e.printStackTrace();}
					player.sendMessage(Main.color("&6Kalan süre sorgusu detayları: &b" + target));
					if (request == null) player.sendMessage(Main.color("&aBu oyuncu için süreli bir içerik yok."));
					else {
						for (TimeObject object : request) {
							player.sendMessage(Main.color("&b    &b" + object.getProduct() + " &a" + calculateTime(object.getTime())));}
					}
					
				} else {
					List<TimeObject> request = null;
					try {request = DatabaseQueries.getPlayerValues(player.getName());} catch (SQLException | ClassNotFoundException e) {e.printStackTrace();}
					player.sendMessage(Main.color("&6Kalan süre sorgusu detayları:"));
					if (request == null) player.sendMessage(Main.color("&aBu oyuncu için süreli bir içerik yok."));
					else {
						for (TimeObject object : request) {
							player.sendMessage(Main.color("&b    &b" + object.getProduct() + " &a" + calculateTime(object.getTime())));}
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isNumeric(String strNum) {
	    try {
	        Integer.parseInt(strNum);
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	    return true;
	}
	public static String calculateTime(long seconds) {
	    int day = (int) TimeUnit.SECONDS.toDays(seconds);
	    long hours = TimeUnit.SECONDS.toHours(seconds) -
	                 TimeUnit.DAYS.toHours(day);
	    long minute = TimeUnit.SECONDS.toMinutes(seconds) - 
	                  TimeUnit.DAYS.toMinutes(day) -
	                  TimeUnit.HOURS.toMinutes(hours);
	    return (day + " gün " + hours + " saat " + minute + " dakika");
	}
	
	public void removeTimedEvent(CommandSender sender, String player, String product) throws ClassNotFoundException, SQLException {
		if (DatabaseQueries.playerHasTimedValue(player, product)) {
			DatabaseQueries.removeProductOfPlayer(player, product);
			sender.sendMessage(Main.color("&aOyuncunun verisi başarıyla silindi."));
		} else sender.sendMessage(Main.color("&cBu oyuncunun böyle bir ürünü bulunamadı."));
	}
	public void endTimedEvent(CommandSender sender, String player, String product) throws ClassNotFoundException, SQLException {
		if (DatabaseQueries.playerHasTimedValue(player, product)) {
			DatabaseQueries.removeProductOfPlayer(player, product);
			for (String s : Main.instance.getConfig().getStringList("Commands." + product + ".end")) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.color(s.replace("%player%", player)));
			}
			sender.sendMessage(Main.color("&aOyuncunun verisi başarıyla bitirildi."));
		} else sender.sendMessage(Main.color("&cBu oyuncunun böyle bir ürünü bulunamadı."));
	}

}
