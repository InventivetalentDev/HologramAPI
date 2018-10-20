package de.inventivegames.hologram;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

public class HologramPlugin extends JavaPlugin implements Listener {

	protected static HologramPlugin instance;

	boolean usePackets = false;

	@Override
	@SuppressWarnings("unused")
	public void onEnable() {
		instance = this;
		Bukkit.getPluginManager().registerEvents(new HologramListeners(), this);

		if (Bukkit.getPluginManager().isPluginEnabled("PacketListenerApi")) {
			usePackets = true;
			System.out.println("[HologramAPI] Found PacketListenerAPI. Enabled touch-holograms.");
			new PacketListener(instance);
			HologramAPI.packetsEnabled = true;
		}

		if (Bukkit.getPluginManager().isPluginEnabled("ProtocolSupport")) {
			System.out.println("[HologramAPI] Found ProtocolSupport.");
			HologramAPI.enableProtocolSupport();
		}

		try {
			MetricsLite metrics = new MetricsLite(this);
			if (metrics.start()) {
				getLogger().info("Metrics started");
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onDisable() {
		for (Hologram h : HologramAPI.getHolograms()) {
			HologramAPI.removeHologram(h);
		}
		if (usePackets) {
			PacketListener.disable();
		}
	}

}
