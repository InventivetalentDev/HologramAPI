package de.inventivegames.hologram.reflection;

import java.lang.reflect.Field;

public abstract class NMUClass {

	public static Class<?> gnu_trove_map_TIntObjectMap;
	public static Class<?> gnu_trove_map_hash_TIntObjectHashMap;
	public static Class<?> gnu_trove_impl_hash_THash;
	public static Class<?> io_netty_channel_Channel;
	private static boolean initialized = false;

	static {
		if (!initialized) {
			for (Field f : NMUClass.class.getDeclaredFields()) {
				if (f.getType().equals(Class.class)) {
					try {
						String name = f.getName().replace("_", ".");
						if (Reflection.getVersion().contains("1_8")) {
							f.set(null, Class.forName(name));
						} else {
							f.set(null, Class.forName("net.minecraft.util." + name));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			initialized = true;
		}
	}

}
