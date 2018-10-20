package de.inventivegames.hologram;

import de.inventivegames.hologram.reflection.NMUClass;
import de.inventivegames.hologram.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.util.AccessUtil;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public abstract class HologramAPI {

	protected static final List<Hologram> holograms = new ArrayList<>();
	protected static boolean is1_8/*Or 1.9*/    = Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1);
	protected static boolean packetsEnabled     = false;
	protected static boolean useProtocolSupport = false;
	//Protocol Support
	static Class<?> ProtocolSupportAPI;
	static Class<?> ProtocolVersion;
	static Method ProtocolSupportAPI_getProtocolVersion;
	static Method ProtocolVersion_getId;

	/**
	 * Creates a new {@link Hologram}
	 *
	 * @param loc  {@link Location} to spawn the hologram at
	 * @param text Initial text content of the hologram
	 * @return a new {@link Hologram}
	 */
	public static Hologram createHologram(Location loc, String text) {
		Hologram hologram = new DefaultHologram(loc, text);
		holograms.add(hologram);
		return hologram;
	}

	/**
	 * Removes a {@link Hologram}
	 *
	 * @param loc  {@link Location} of the hologram
	 * @param text content of the hologram
	 * @return <code>true</code> if a hologram was found and has been removed, <code>false</code> otherwise
	 */
	public static boolean removeHologram(Location loc, String text) {
		Hologram toRemove = null;
		for (Hologram h : holograms) {
			if (h.getLocation().equals(loc) && h.getText().equals(text)) {
				toRemove = h;
				break;
			}
		}
		if (toRemove != null) { return removeHologram(toRemove); }
		return false;
	}

	/**
	 * Removes a {@link Hologram}
	 *
	 * @param hologram {@link Hologram} to remove
	 * @return <code>true</code> if the hologram has been removed
	 */
	public static boolean removeHologram(@Nonnull Hologram hologram) {
		if (hologram.isSpawned()) {
			hologram.despawn();
		}
		return holograms.remove(hologram);
	}

	/**
	 * @return {@link Collection} of all registered {@link Hologram}s
	 */
	public static Collection<Hologram> getHolograms() {
		return new ArrayList<>(holograms);
	}

	protected static boolean spawn(@Nonnull final Hologram hologram, final Collection<? extends Player> receivers) throws Exception {
		if (hologram == null) { throw new IllegalArgumentException("hologram cannot be null"); }
		checkReceiverWorld(hologram, receivers);
		if (!receivers.isEmpty()) {
			((CraftHologram) hologram).sendSpawnPackets(receivers, true, true);
			((CraftHologram) hologram).sendTeleportPackets(receivers, true, true);
			((CraftHologram) hologram).sendNamePackets(receivers);
			((CraftHologram) hologram).sendAttachPacket(receivers);
		}
		return true;
	}

	protected static boolean despawn(@Nonnull Hologram hologram, Collection<? extends Player> receivers) throws Exception {
		if (hologram == null) { throw new IllegalArgumentException("hologram cannot be null"); }
		if (receivers.isEmpty()) { return false; }

		((CraftHologram) hologram).sendDestroyPackets(receivers);
		return true;
	}

	protected static void sendPacket(Player p, Object packet) {
		if (p == null || packet == null) { throw new IllegalArgumentException("player and packet cannot be null"); }
		try {
			Object handle = Reflection.getHandle(p);
			Object connection = Reflection.getFieldWithException(handle.getClass(), "playerConnection").get(handle);
			Reflection.getMethod(connection.getClass(), "sendPacket", Reflection.getNMSClass("Packet")).invoke(connection, new Object[] { packet });
		} catch (Exception e) {
		}
	}

	protected static Collection<? extends Player> checkReceiverWorld(final Hologram hologram, final Collection<? extends Player> receivers) {
		for (Iterator<? extends Player> iterator = receivers.iterator(); iterator.hasNext(); ) {
			Player next = iterator.next();
			if (!next.getWorld().equals(hologram.getLocation().getWorld())) {
				iterator.remove();
			}
		}
		return receivers;
	}

	protected static int getVersion(Player p) {
		try {
			if (useProtocolSupport) {
				Object protocolVersion = ProtocolSupportAPI_getProtocolVersion.invoke(null, p);
				int id = (int) ProtocolVersion_getId.invoke(protocolVersion);
				return id;
			} else {
				final Object handle = Reflection.getHandle(p);
				Object connection = AccessUtil.setAccessible(handle.getClass().getDeclaredField("playerConnection")).get(handle);
				Object network = AccessUtil.setAccessible(connection.getClass().getDeclaredField("networkManager")).get(connection);
				String name = null;
				if (HologramAPI.is1_8) {
					if (Reflection.getVersion().contains("1_8_R1")) {
						name = "i";
					}
					if (Reflection.getVersion().contains("1_8_R2")) {
						name = "k";
					}
				} else {
					name = "m";
				}
				if (name == null) {
					//				System.err.println("Invalid server version! Unable to find proper channel-field in getVersion.");
					return 99;
				}
				Object channel = AccessUtil.setAccessible(network.getClass().getDeclaredField(name)).get(network);

				Object version = 0;
				try {
					version = AccessUtil.setAccessible(network.getClass().getDeclaredMethod("getVersion", NMUClass.io_netty_channel_Channel)).invoke(network, channel);
				} catch (Exception e) {
					// e.printStackTrace();
					return 182;
				}
				return (int) version;
			}
		} catch (Exception e) {
			//			e.printStackTrace();
		}
		return 0;
	}

	public static boolean is1_8() {
		return is1_8;
	}

	public static boolean packetsEnabled() {
		return packetsEnabled;
	}

	protected static void enableProtocolSupport() {
		useProtocolSupport = true;

		try {
			ProtocolSupportAPI = Class.forName("protocolsupport.api.ProtocolSupportAPI");
			ProtocolVersion = Class.forName("protocolsupport.api.ProtocolVersion");

			ProtocolSupportAPI_getProtocolVersion = Reflection.getMethod(ProtocolSupportAPI, "getProtocolVersion", new Class[] { Player.class });
			ProtocolVersion_getId = Reflection.getMethod(ProtocolVersion, "getId");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
