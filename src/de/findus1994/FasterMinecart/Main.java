package de.findus1994.FasterMinecart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private static final String LOGGINGPREFIX = "[FasterMinecart] ";
	private MinecartEntryListener minecartEntryListener;

	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		minecartEntryListener = new MinecartEntryListener(this);
		getServer().getPluginManager().registerEvents(minecartEntryListener,
				this);
		logInfoMessage("FasterMinecart is enabled");
	}

	@Override
	public void onDisable() {
		logInfoMessage("FasterMinecart is disabled");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("speedme")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("fasterminecart.controlespeed")) {
					Entity vehicle = player.getVehicle();
					if (vehicle != null && vehicle instanceof Minecart) {
						Minecart minecart = (Minecart) vehicle;
						minecartEntryListener.addMinecartToSpeedUp(minecart);
						player.sendMessage(ChatColor.GRAY
								+ "Speeding up minecart");

					} else {
						player.sendMessage(ChatColor.RED
								+ "You can only use this command in a Minecart");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to execute this command!");
				}
			} else
				sender.sendMessage(ChatColor.RED
						+ "Please Provide as a Player!");
		} else if (command.getName().equalsIgnoreCase("slowme")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (sender.hasPermission("fasterminecart.controlespeed")) {
					Entity vehicle = player.getVehicle();
					if (vehicle != null && vehicle instanceof Minecart) {
						Minecart minecart = (Minecart) vehicle;
						minecartEntryListener.removeMinecartToSpeedUp(minecart);
						player.sendMessage(ChatColor.GRAY
								+ "Slowing down minecart");
					} else {
						player.sendMessage(ChatColor.RED
								+ "You can only use this command in a Minecart");
					}
				} else {
					player.sendMessage(ChatColor.RED
							+ "You don't have permission to execute this command!");
				}
			} else
				sender.sendMessage(ChatColor.RED
						+ "Please Provide as a Player!");
		}

		return true;
	}

	/**
	 * Standardize the log messages
	 * 
	 * @param message
	 *            The message which should be logged
	 */
	public static void logInfoMessage(String message) {
		Bukkit.getServer().getLogger().info(LOGGINGPREFIX + message);
	}

}
