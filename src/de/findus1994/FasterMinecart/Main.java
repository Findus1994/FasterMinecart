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

	public void onEnable() {
		minecartEntryListener = new MinecartEntryListener();
		getServer().getPluginManager().registerEvents(minecartEntryListener,
				this);
		logInfoMessage("FasterMinecart is enabled");
	}

	public void onDisable() {
		logInfoMessage("FasterMinecart is disabled");
	}

	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("speedme")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
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
			} else
				sender.sendMessage(ChatColor.RED + "Please Provide as a Player!");
		} else if(command.getName().equalsIgnoreCase("slowme")){
			if (sender instanceof Player) {
				Player player = (Player) sender;
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
			} else
				sender.sendMessage(ChatColor.RED + "Please Provide as a Player!");
		}

		return true;
	}

	public static void logInfoMessage(String message) {
		Bukkit.getServer().getLogger().info(LOGGINGPREFIX + message);
	}

}
