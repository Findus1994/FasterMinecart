package de.findus1994.FasterMinecart;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

public class MinecartEntryListener implements Listener {

	private static final double DEFAULT_MAX_SPEED = 0.4;

	private final Main plugin;

	private final ArrayList<Minecart> minecartsToSpeedUp = new ArrayList<Minecart>();
	private final HashMap<Minecart, Double> prevSpeed = new HashMap<Minecart, Double>();
	private final ArrayList<Minecart> hasNotPassed = new ArrayList<Minecart>();
	private final double pluginMaxSpeed;

	public MinecartEntryListener(Main main) {
		plugin = main;
		pluginMaxSpeed = plugin.getConfig().getDouble("maximumSpeed");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onMinecartEnter(VehicleEnterEvent event) {
		Entity passenger = event.getEntered();
		Vehicle vehicle = event.getVehicle();
		if (passenger != null && vehicle != null) {
			if (passenger instanceof Player && vehicle instanceof Minecart && ((Player) passenger).hasPermission("fasterminecart.controlespeed")) {
				((Player) passenger)
						.sendMessage(ChatColor.GRAY
								+ "Type \"/speedme\" or \"/slowme\" to controle minecart speed");
			}
		}
	}

	/**
	 * Handles the move event of the minecart. If the minecart was registered as
	 * a minecart to speed up (using {@link #addMinecartToSpeedUp(Minecart)
	 * addMinecartToSpeedUp}), it will manage the velocity and maximum speed if
	 * necessary.
	 * 
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onMinecartMove(VehicleMoveEvent event) {
		Entity passenger = event.getVehicle().getPassenger();
		Vehicle vehicle = event.getVehicle();

		if (passenger != null && vehicle != null) {
			if (passenger instanceof Player && vehicle instanceof Minecart) {
				Minecart minecart = (Minecart) vehicle;
				if (minecartsToSpeedUp.contains(minecart)) {
					MaterialData minecartMaterialData = minecart.getLocation()
							.getBlock().getState().getData();

					Vector v = minecart.getVelocity();
					double x = v.getX();
					double z = v.getZ();
					if (!hasNotPassed.contains(minecart)
							&& !prevSpeed.containsKey(minecart)) {

						if (checkTrackForIncompatibleRails(minecart)) {
							prevSpeed.put(minecart, minecart.getVelocity()
									.length());
							hasNotPassed.add(minecart);
							minecart.setMaxSpeed(DEFAULT_MAX_SPEED);
						}

					} else if (hasNotPassed.contains(minecart)
							&& minecartMaterialData instanceof Rails) {
						if (checkLocationForIncompatibleRail(minecart
								.getLocation())) {
							hasNotPassed.remove(minecart);
						}
					} else if (!hasNotPassed.contains(minecart)
							&& prevSpeed.containsKey(minecart)
							&& minecartMaterialData instanceof Rails) {
						if (!checkLocationForIncompatibleRail(minecart
								.getLocation())
								&& !checkTrackForIncompatibleRails(minecart)) {
							minecart.setMaxSpeed(pluginMaxSpeed);
							Double previousSpeed = prevSpeed.get(minecart);

							Vector newVel = null;

							if (x != 0) {
								newVel = new Vector(x > 0 ? previousSpeed
										: -previousSpeed, 0, 0);
							} else if (z != 0) {
								newVel = new Vector(0, 0, z > 0 ? previousSpeed
										: -previousSpeed);
							}
							minecart.setVelocity(newVel);
							prevSpeed.remove(minecart);
						} else {
							setMinecartToCurveSpeed(minecart);
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onMinecartLeave(VehicleExitEvent event) {
		Entity passenger = event.getVehicle().getPassenger();
		Vehicle vehicle = event.getVehicle();

		if (passenger != null && vehicle != null) {
			if (passenger instanceof Player && vehicle instanceof Minecart) {
				Minecart minecart = (Minecart) vehicle;
				removeMinecartToSpeedUp(minecart);
			}
		}
	}

	/**
	 * Cheks the further track of the minecart for curved rails or rails on
	 * slope. It will <b>not</b> check the current position of the minecart for
	 * incompatible tiles!
	 * 
	 * @param minecart
	 *            The minecart which track should be checked.
	 * @return True, if there is a incompatible rail at the next few blocks
	 */
	private boolean checkTrackForIncompatibleRails(Minecart minecart) {
		Vector v = minecart.getVelocity();
		double x = v.getX();
		double z = v.getZ();
		Location locationToTest = minecart.getLocation();
		for (int i = 0; i <= v.length(); i++) {
			if (x != 0) {
				locationToTest.add(new Vector(x > 0 ? 1 : -1, 0, 0));
			}
			if (z != 0) {
				locationToTest.add(new Vector(0, 0, z > 0 ? 1 : -1));
			}
			if (checkLocationForIncompatibleRail(locationToTest)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checking location for curved rails or rails on slope
	 * 
	 * @param loc
	 *            The Location to test
	 * @return True, if there is a incompatible rail at the given location
	 */
	private boolean checkLocationForIncompatibleRail(Location location) {
		MaterialData dataToTest = location.getBlock().getState().getData();
		if (dataToTest != null && dataToTest instanceof Rails) {
			Rails rail = (Rails) dataToTest;
			if (rail.isCurve() || rail.isOnSlope()) {
				return true;
			}
		}
		return false;
	}

	private void setMinecartToCurveSpeed(Minecart minecart) {
		if (minecart != null) {
			Vector actualVelocity = minecart.getVelocity();
			double actualX = actualVelocity.getX();
			double actualZ = actualVelocity.getZ();
			actualX = actualX / actualVelocity.length();
			actualZ = actualZ / actualVelocity.length();
			minecart.setVelocity(new Vector(actualX * DEFAULT_MAX_SPEED, 0,
					actualZ * DEFAULT_MAX_SPEED));
		}
	}

	/**
	 * Register a minecart as a cart which should be increse his maximum speed
	 * 
	 * @param minecart
	 *            The minecart to register
	 */
	public void addMinecartToSpeedUp(Minecart minecart) {
		if (!minecartsToSpeedUp.contains(minecart)) {
			if (!checkTrackForIncompatibleRails(minecart)) {
				minecart.setMaxSpeed(pluginMaxSpeed);
			}
			minecartsToSpeedUp.add(minecart);
		}
	}

	/**
	 * Unregister a minecart as a cart which should be increse his maximum
	 * speed. The maximum speed of the minecart will be the default value after
	 * unregistering
	 * 
	 * @param minecart
	 *            The minecart to unregister
	 */
	public void removeMinecartToSpeedUp(Minecart minecart) {
		if (minecartsToSpeedUp.contains(minecart)) {
			minecart.setMaxSpeed(DEFAULT_MAX_SPEED);
			minecartsToSpeedUp.remove(minecart);
		}
	}

}
