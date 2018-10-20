package de.inventivegames.hologram;

import de.inventivegames.hologram.reflection.NMSClass;
import de.inventivegames.hologram.touch.TouchAction;
import de.inventivegames.hologram.touch.TouchHandler;
import de.inventivegames.hologram.view.ViewHandler;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;
import org.inventivetalent.reflection.minecraft.DataWatcher;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.MethodResolver;
import org.inventivetalent.reflection.util.AccessUtil;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PacketListener extends PacketHandler {

	protected static PacketListener instance;
	static FieldResolver  DataWatcherFieldResolver  = new FieldResolver(NMSClass.DataWatcher);
	static MethodResolver DataWatcherMethodResolver = new MethodResolver(NMSClass.DataWatcher);

	public PacketListener(Plugin pl) {
		super(pl);
		if (instance != null) { throw new IllegalStateException("Cannot instantiate PacketListener twice"); }
		instance = this;
		addHandler(instance);
	}

	public static void disable() {
		if (instance != null) {
			removeHandler(instance);
			instance = null;
		}
	}

	@SuppressWarnings({
							  "rawtypes",
							  "unchecked"
					  })
	@Override
	@PacketOptions(forcePlayer = true)
	public void onSend(SentPacket packet) {
		if (!packet.hasPlayer()) { return; }
		int type = -1;
		if (packet.getPacketName().equals("PacketPlayOutSpawnEntityLiving")) {
			type = 0;
		}
		if (packet.getPacketName().equals("PacketPlayOutEntityMetadata")) {
			type = 1;
		}
		if (packet.getPacketName().equals("PacketPlayOutMapChunkBulk")) {
			type = 2;
		}
		if (packet.getPacketName().equals("PacketPlayOutMapChunk")) {
			type = 3;
		}
		if (type == 0 || type == 1) {
			int a = (int) packet.getPacketValue("a");
			Object dataWatcher = type == 0 ? (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? packet.getPacketValue("l") : packet.getPacketValue("m")) : null;

			if (dataWatcher != null) {
				try {
					dataWatcher = this.cloneDataWatcher(dataWatcher);// Clone the DataWatcher, we don't want to change the name values permanently
					AccessUtil.setAccessible(Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? NMSClass.DataWatcher.getDeclaredField("a") : NMSClass.DataWatcher.getDeclaredField("b")).set(dataWatcher, null);
					if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {packet.setPacketValue("l", dataWatcher);} else {
						packet.setPacketValue("m", dataWatcher);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;// Allowing further changes here would mess up the packet values
				}
			}

			List list = (List) (type == 1 ? packet.getPacketValue("b") : null);
			int listIndex = -1;

			String text = null;
			try {
				if (type == 0) {
					//					text = (String) ClassBuilder.getWatchableObjectValue(ClassBuilder.getDataWatcherValue(dataWatcher, 2));
					if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
						text = (String) DataWatcher.V1_8.getWatchableObjectValue(DataWatcher.V1_8.getValue(dataWatcher, 2));
					} else {
						Field dField = AccessUtil.setAccessible(NMSClass.DataWatcher.getDeclaredField("d"));
						Object dValue = dField.get(dataWatcher);
						if (dValue == null) { return; }
						if (Map.class.isAssignableFrom(dValue.getClass())) {
							if (((Map) dValue).isEmpty()) { return; }
						}
						text = (String) DataWatcher.V1_9.getValue(dataWatcher, DataWatcher.V1_9.ValueType.ENTITY_NAME);
					}
				} else if (type == 1) {
					if (list != null) {
						if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
							for (int i = 0; i < list.size(); i++) {
								//								int index = ClassBuilder.getWatchableObjectIndex(list.get(i));
								int index = DataWatcher.V1_8.getWatchableObjectIndex(list.get(i));
								if (index == 2) {
									//									if (ClassBuilder.getWatchableObjectType(list.get(i)) == 4) {//Check if it is a string
									if (DataWatcher.V1_8.getWatchableObjectType(list.get(i)) == 4) {//Check if it is a string
										//										text = (String) ClassBuilder.getWatchableObjectValue(list.get(i));
										text = (String) DataWatcher.V1_8.getWatchableObjectValue(list.get(i));
										listIndex = i;
										break;
									}
								}
							}
						} else {
							if (list.size() > 2) {
								if (DataWatcher.V1_9.getItemType(list.get(2)) == String.class) {
									text = (String) DataWatcher.V1_9.getItemValue(list.get(2));
									listIndex = 2;
								}
							}
						}
					}
				}
			} catch (Exception e) {
				if (!HologramAPI.useProtocolSupport) {
					e.printStackTrace();//Ignore the exception(s) when using protocol support
				}
			}

			if (text == null) {
				return;// The text will (or should) never be null
			}

			for (Hologram h : HologramAPI.getHolograms()) {
				if (((CraftHologram) h).matchesHologramID(a)) {
					for (ViewHandler v : h.getViewHandlers()) {
						text = v.onView(h, packet.getPlayer(), text);
					}
				}
			}

			if (text == null) {
				packet.setCancelled(true);//Cancel the packet if the text is null after calling the view handlers
				return;
			}

			try {
				if (type == 0) {
					//					ClassBuilder.setDataWatcherValue(dataWatcher, 2, text);
					DataWatcher.setValue(dataWatcher, 2, DataWatcher.V1_9.ValueType.ENTITY_NAME, text);
				} else if (type == 1) {
					if (list == null || listIndex == -1) { return; }
					//					Object object = ClassBuilder.buildWatchableObject(2, text);
					Object object = Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? DataWatcher.V1_8.newWatchableObject(2, text) : DataWatcher.V1_9.newDataWatcherItem(DataWatcher.V1_9.ValueType.ENTITY_NAME.getType(), text);
					list.set(listIndex, object);
					packet.setPacketValue("b", list);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (type == 2) {
			int[] a = (int[]) packet.getPacketValue("a");
			int[] b = (int[]) packet.getPacketValue("b");

			for (int i = 0; i < (a.length + b.length) / 2; i++) {
				for (Hologram hologram : HologramAPI.getHolograms()) {
					if (hologram.isSpawned()) {
						int chunkX = hologram.getLocation().getBlockX() >> 4;
						int chunkZ = hologram.getLocation().getBlockZ() >> 4;
						if (chunkX == a[i] && chunkZ == b[i]) {
							try {
								HologramAPI.spawn(hologram, Collections.singletonList(packet.getPlayer()));
							} catch (Exception e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		}
		//		if (type == 3) {
		//			int a = (int) packet.getPacketValue("a");
		//			int b = (int) packet.getPacketValue("b");
		//
		//			for (Hologram hologram : HologramAPI.getHolograms()) {
		//				if (hologram.isSpawned()) {
		//					Chunk chunk = hologram.getLocation().getChunk();
		//					if (chunk.getX() == a && chunk.getZ() == b) {
		//						try {
		//							HologramAPI.spawn(hologram, Collections.singletonList(packet.getPlayer()));
		//						} catch (Exception e1) {
		//							e1.printStackTrace();
		//						}
		//					}
		//				}
		//			}
		//		}
	}

	@Override
	@PacketOptions(forcePlayer = true)
	public void onReceive(ReceivedPacket packet) {
		if (packet.hasPlayer()) {
			if (packet.getPacketName().equals("PacketPlayInUseEntity")) {
				int id = (int) packet.getPacketValue("a");
				Object useAction = packet.getPacketValue("action");
				TouchAction action = TouchAction.fromUseAction(useAction);
				if (action == TouchAction.UNKNOWN) {
					return;// UNKNOWN means an invalid packet, so just ignore it
				}
				for (Hologram h : HologramAPI.getHolograms()) {
					if (((DefaultHologram) h).matchesTouchID(id)) {
						for (TouchHandler t : h.getTouchHandlers()) {
							t.onTouch(h, packet.getPlayer(), action);
						}
					}
				}
			}
		}
	}

	public Object cloneDataWatcher(Object original) throws Exception {
		if (original == null) { return null; }
		//		Object clone = NMSClass.DataWatcher.getConstructor(new Class[] { NMSClass.Entity }).newInstance(new Object[] { null });
		Object clone = DataWatcher.newDataWatcher(null);
		int index = 0;
		Object current = null;
		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
			//			while ((current = ClassBuilder.getDataWatcherValue(original, index++)) != null) {
			//				ClassBuilder.setDataWatcherValue(clone, ClassBuilder.getWatchableObjectIndex(current), ClassBuilder.getWatchableObjectValue(current));
			//			}
			while ((current = DataWatcher.V1_8.getValue(original, index++)) != null) {
				DataWatcher.V1_8.setValue(clone, DataWatcher.V1_8.getWatchableObjectIndex(current), DataWatcher.V1_8.getWatchableObjectValue(current));
			}
		} else {
			Field mapField = DataWatcherFieldResolver.resolve("c");
			mapField.set(clone, mapField.get(original));
			//			while ((current = DataWatcher.V1_9.getItem(original, index++)) != null) {
			//				DataWatcher.V1_9.setItem(clone, index++, current);
			//			}

		}
		return clone;
	}

}
