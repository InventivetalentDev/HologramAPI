package de.inventivegames.hologram.reflection;

import de.inventivegames.hologram.HologramAPI;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.inventivetalent.reflection.minecraft.DataWatcher;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.util.AccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.inventivetalent.reflection.minecraft.DataWatcher.V1_9.ValueType.*;

public abstract class ClassBuilder {

	static NMSClassResolver nmsClassResolver = new NMSClassResolver();

	static FieldResolver EntitySlimeFieldResolver                    = new FieldResolver(NMSClass.EntitySlime);
	static FieldResolver EntityFieldResolver                         = new FieldResolver(NMSClass.Entity);
	static FieldResolver PacketPlayOutSpawnEntityLivingFieldResolver = new FieldResolver(NMSClass.PacketPlayOutSpawnEntityLiving);
	static FieldResolver DataWatcherFieldResolver                    = new FieldResolver(NMSClass.DataWatcher);

	public static Object buildEntityWitherSkull(Object world, Location loc) throws Exception {
		Object witherSkull = NMSClass.EntityWitherSkull.getConstructor(NMSClass.World).newInstance(world);
		updateEntityLocation(witherSkull, loc);

		return witherSkull;
	}

	public static Object buildEntityHorse_1_7(Object world, Location loc, String name) throws Exception {
		Object horse_1_7 = NMSClass.EntityHorse.getConstructor(NMSClass.World).newInstance(world);
		updateEntityLocation(horse_1_7, loc);
		if (HologramAPI.is1_8()) {
			if (name != null) {
				NMSClass.Entity.getDeclaredMethod("setCustomName", String.class).invoke(horse_1_7, name);
			}
			NMSClass.Entity.getDeclaredMethod("setCustomNameVisible", boolean.class).invoke(horse_1_7, name != null && !name.isEmpty());
		} else {
			if (name != null) {
				NMSClass.EntityInsentient.getDeclaredMethod("setCustomName", String.class).invoke(horse_1_7, name);
			}
			NMSClass.EntityInsentient.getDeclaredMethod("setCustomNameVisible", boolean.class).invoke(horse_1_7, name != null && !name.isEmpty());
		}
		Object horseDataWatcher = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("datawatcher")).get(horse_1_7);
		NMSClass.EntityAgeable.getDeclaredMethod("setAge", int.class).invoke(horse_1_7, -1700000);
		//		NMSClass.DataWatcher.getDeclaredMethod("watch", new Class[] {
		//				int.class,
		//				Object.class }).invoke(horseDataWatcher, new Object[] {
		//				12,
		//				-1700000 });// Size
		DataWatcher.setValue(horseDataWatcher, 12, ENTITY_FLAG, (byte) -1700000);

		return horse_1_7;
	}

	public static Object buildEntityHorse_1_8(Object world, Location loc, String name) throws Exception {
		Object horse_1_8 = NMSClass.EntityHorse.getConstructor(NMSClass.World).newInstance(world);
		updateEntityLocation(horse_1_8, loc);
		if (HologramAPI.is1_8()) {
			if (name != null) {
				NMSClass.Entity.getDeclaredMethod("setCustomName", String.class).invoke(horse_1_8, name);
			}
			NMSClass.Entity.getDeclaredMethod("setCustomNameVisible", boolean.class).invoke(horse_1_8, true);
		} else {
			NMSClass.EntityInsentient.getDeclaredMethod("setCustomName", String.class).invoke(horse_1_8, name);
			NMSClass.EntityInsentient.getDeclaredMethod("setCustomNameVisible", boolean.class).invoke(horse_1_8, name != null && !name.isEmpty());
		}

		return horse_1_8;
	}

	public static Object buildEntityArmorStand(Object world, Location loc, String name) throws Exception {
		Object armorStand = NMSClass.EntityArmorStand.getConstructor(NMSClass.World).newInstance(world);
		updateEntityLocation(armorStand, loc);
		if (name != null) {
			NMSClass.Entity.getDeclaredMethod("setCustomName", String.class).invoke(armorStand, name);
		}
		NMSClass.Entity.getDeclaredMethod("setCustomNameVisible", boolean.class).invoke(armorStand, name != null && !name.isEmpty());

		return armorStand;
	}

	public static Object setupArmorStand(Object armorStand) throws Exception {
		NMSClass.EntityArmorStand.getDeclaredMethod("setInvisible", boolean.class).invoke(armorStand, true);
		NMSClass.EntityArmorStand.getDeclaredMethod("setSmall", boolean.class).invoke(armorStand, true);
		NMSClass.EntityArmorStand.getDeclaredMethod("setArms", boolean.class).invoke(armorStand, false);
		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_10_R1)) {
			NMSClass.EntityArmorStand.getDeclaredMethod("setGravity", boolean.class).invoke(armorStand, false);
		} else {
			NMSClass.Entity.getDeclaredMethod("setNoGravity", boolean.class).invoke(armorStand, true);
		}
		NMSClass.EntityArmorStand.getDeclaredMethod("setBasePlate", boolean.class).invoke(armorStand, false);

		return armorStand;
	}

	public static Object buildEntitySlime(Object world, Location loc, int size) throws Exception {
		Object slime = NMSClass.EntitySlime.getConstructor(NMSClass.World).newInstance(world);
		updateEntityLocation(slime, loc);
		Object dataWatcher = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("datawatcher")).get(slime);
		//		setDataWatcherValue(dataWatcher, 0, (byte) 32);
		//		setDataWatcherValue(dataWatcher, 16, (byte) (size < 1 ? 1 : size > 100 ? 100 : size));
		DataWatcher.setValue(dataWatcher, 0, ENTITY_FLAG, (byte) 32);
		DataWatcher.setValue(dataWatcher, 16, ENTITY_SLIME_SIZE, (size < 1 ? 1 : size > 100 ? 100 : size));//Size

		return slime;
	}

	public static Object buildWitherSkullSpawnPacket(Object skull) throws Exception {
		@SuppressWarnings("deprecation")
		Object spawnPacketSkull = NMSClass.PacketPlayOutSpawnEntity.getConstructor(NMSClass.Entity, int.class).newInstance(skull, EntityType.WITHER_SKULL.getTypeId());
		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
			AccessUtil.setAccessible(NMSClass.PacketPlayOutSpawnEntity.getDeclaredField("j")).set(spawnPacketSkull, 66);
		} else {
			AccessUtil.setAccessible(NMSClass.PacketPlayOutSpawnEntity.getDeclaredField("k")).set(spawnPacketSkull, 66);
		}

		return spawnPacketSkull;
	}

	public static Object buildSkullMetaPacket(int id, Object dataWatcher) throws Exception {
		//		setDataWatcherValue(dataWatcher, 0, (byte) 32);
		DataWatcher.setValue(dataWatcher, 0, ENTITY_FLAG, (byte) 32);
		Object packet = NMSClass.PacketPlayOutEntityMetadata.getConstructor(int.class, NMSClass.DataWatcher, boolean.class).newInstance(id, dataWatcher, true);

		return packet;
	}

	public static Object buildHorseSpawnPacket_1_7(Object horse, String name) throws Exception {
		if (horse == null) { throw new IllegalArgumentException("horse cannot be null"); }
		Object spawnPacketHorse_1_7 = NMSClass.PacketPlayOutSpawnEntityLiving.getConstructor(NMSClass.EntityLiving).newInstance(horse);
		Object dataWatcher_1_7 = AccessUtil.setAccessible(PacketPlayOutSpawnEntityLivingFieldResolver.resolveByFirstType(NMSClass.DataWatcher)).get(spawnPacketHorse_1_7);

		if (name != null) {
			//			setDataWatcherValue(dataWatcher_1_7, 10, name);
			DataWatcher.setValue(dataWatcher_1_7, 10, ENTITY_NAME, name);
		}
		//		setDataWatcherValue(dataWatcher_1_7, 11, (byte) (name != null && !name.isEmpty() ? 1 : 0));
		DataWatcher.setValue(dataWatcher_1_7, 11, ENTITY_NAME_VISIBLE, (byte) (name != null && !name.isEmpty() ? 1 : 0));
		//		NMUClass.gnu_trove_map_hash_TIntObjectHashMap.getDeclaredMethod("put", int.class, Object.class).invoke(map, 12, NMSClass.WatchableObject.getConstructor(int.class, int.class, Object.class).newInstance(2, 12, -1700000));
		DataWatcher.setValue(dataWatcher_1_7, 12, ENTITY_FLAG, -1700000);

		return spawnPacketHorse_1_7;
	}

	//No changes for 1.9 here - use ArmorStands
	public static Object buildHorseSpawnPacket_1_8(Object horse, String name) throws Exception {
		if (horse == null) { throw new IllegalArgumentException("horse cannot be null"); }
		Object spawnPacketHorse_1_8 = NMSClass.PacketPlayOutSpawnEntityLiving.getConstructor(NMSClass.EntityLiving).newInstance(horse);
		AccessUtil.setAccessible(NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("b")).setInt(spawnPacketHorse_1_8, 30);
		Object dataWatcher_1_8 = AccessUtil.setAccessible(PacketPlayOutSpawnEntityLivingFieldResolver.resolveByFirstType(NMSClass.DataWatcher)).get(spawnPacketHorse_1_8);
		Map<Integer, Object> map_1_8 = (Map) DataWatcherFieldResolver.resolveByLastType(Map.class).get(dataWatcher_1_8);
		map_1_8.put(10, NMSClass.WatchableObject.getConstructor(int.class, int.class, Object.class).newInstance(0, 10, (byte) 1));

		List<Integer> toRemove = new ArrayList<>();

		for (Map.Entry entry : map_1_8.entrySet()) {// 100 is a random value, we just want to get all values
			Object current = entry.getValue();
			if (current == null) {
				continue;
			}
			int index = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("b")).getInt(current);
			if (index == 2) {
				AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("c")).set(current, name);
			} else if (index != 3) {
				toRemove.add(Integer.valueOf(index));
			}
		}

		for (Integer i : toRemove) {
			map_1_8.remove(i);
		}

		map_1_8.put(0, NMSClass.WatchableObject.getConstructor(int.class, int.class, Object.class).newInstance(0, 0, (byte) 32));

		return spawnPacketHorse_1_8;
	}

	public static Object buildSlimeSpawnPacket(Object slime) throws Exception {
		Object packet = NMSClass.PacketPlayOutSpawnEntityLiving.getConstructor(NMSClass.EntityLiving).newInstance(slime);

		return packet;
	}

	public static Object buildNameMetadataPacket(int id, Object dataWatcher, int nameIndex, int visibilityIndex, String name) throws Exception {
		//		dataWatcher = setDataWatcherValue(dataWatcher, nameIndex, name != null ? name : "");// Pass an empty string to avoid exceptions
		//		dataWatcher = setDataWatcherValue(dataWatcher, visibilityIndex, (byte) (name != null && !name.isEmpty() ? 1 : 0));
		DataWatcher.setValue(dataWatcher, nameIndex, ENTITY_NAME, name != null ? name : "");// Pass an empty string to avoid exceptions
		DataWatcher.setValue(dataWatcher, visibilityIndex, ENTITY_NAME_VISIBLE, Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? (byte) (name != null && !name.isEmpty() ? 1 : 0) : (name != null && !name.isEmpty()));//Byte < 1.9, Boolean >= 1.9
		Object metaPacket = NMSClass.PacketPlayOutEntityMetadata.getConstructor(int.class, NMSClass.DataWatcher, boolean.class).newInstance(id, dataWatcher, true);

		return metaPacket;
	}

	public static Object updateEntityLocation(Object entity, Location loc) throws Exception {
		NMSClass.Entity.getDeclaredField("locX").set(entity, loc.getX());
		NMSClass.Entity.getDeclaredField("locY").set(entity, loc.getY());
		NMSClass.Entity.getDeclaredField("locZ").set(entity, loc.getZ());
		return entity;
	}

	//	public static Object buildDataWatcher(@Nullable Object entity) throws Exception {
	//		Object dataWatcher = NMSClass.DataWatcher.getConstructor(NMSClass.Entity).newInstance(entity);
	//		return dataWatcher;
	//	}
	//
	//	public static Object buildWatchableObject(int index, Object value) throws Exception {
	//		return buildWatchableObject(getDataWatcherValueType(value), index, value);
	//	}
	//
	//	public static Object buildWatchableObject(int type, int index, Object value) throws Exception {
	//		return NMSClass.WatchableObject.getConstructor(int.class, int.class, Object.class).newInstance(type, index, value);
	//	}
	//
	//	public static Object setDataWatcherValue(Object dataWatcher, int index, Object value) throws Exception {
	//		int type = getDataWatcherValueType(value);
	//
	//		Object map = AccessUtil.setAccessible(NMSClass.DataWatcher.getDeclaredField("dataValues")).get(dataWatcher);
	//		NMUClass.gnu_trove_map_hash_TIntObjectHashMap.getDeclaredMethod("put", int.class, Object.class).invoke(map, index, buildWatchableObject(type, index, value));
	//
	//		return dataWatcher;
	//	}
	//
	//	public static Object getDataWatcherValue(Object dataWatcher, int index) throws Exception {
	//		Object map = AccessUtil.setAccessible(NMSClass.DataWatcher.getDeclaredField("dataValues")).get(dataWatcher);
	//		Object value = NMUClass.gnu_trove_map_hash_TIntObjectHashMap.getDeclaredMethod("get", int.class).invoke(map, index);
	//
	//		return value;
	//	}
	//
	//	public static int getWatchableObjectIndex(Object object) throws Exception {
	//		int index = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("b")).getInt(object);
	//		return index;
	//	}
	//
	//	public static int getWatchableObjectType(Object object) throws Exception {
	//		int type = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("a")).getInt(object);
	//		return type;
	//	}
	//
	//	public static Object getWatchableObjectValue(Object object) throws Exception {
	//		Object value = AccessUtil.setAccessible(NMSClass.WatchableObject.getDeclaredField("c")).get(object);
	//		return value;
	//	}
	//
	//	public static int getDataWatcherValueType(Object value) {
	//		int type = 0;
	//		if (value instanceof Number) {
	//			if (value instanceof Byte) {
	//				type = 0;
	//			} else if (value instanceof Short) {
	//				type = 1;
	//			} else if (value instanceof Integer) {
	//				type = 2;
	//			} else if (value instanceof Float) {
	//				type = 3;
	//			}
	//		} else if (value instanceof String) {
	//			type = 4;
	//		} else if (value != null && value.getClass().equals(NMSClass.ItemStack)) {
	//			type = 5;
	//		} else if (value != null && (value.getClass().equals(NMSClass.ChunkCoordinates) || value.getClass().equals(NMSClass.BlockPosition))) {
	//			type = 6;
	//		} else if (value != null && value.getClass().equals(NMSClass.Vector3f)) {
	//			type = 7;
	//		}
	//
	//		return type;
	//	}

	public static Object buildArmorStandSpawnPacket(Object armorStand) throws Exception {
		Object spawnPacket = NMSClass.PacketPlayOutSpawnEntityLiving.getConstructor(NMSClass.EntityLiving).newInstance(armorStand);
		AccessUtil.setAccessible(Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1) ? NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("b") : NMSClass.PacketPlayOutSpawnEntityLiving.getDeclaredField("c")).setInt(spawnPacket, 30);

		return spawnPacket;
	}

	public static Object buildTeleportPacket(int id, Location loc, boolean onGround, boolean heightCorrection) throws Exception {
		Object packet = NMSClass.PacketPlayOutEntityTeleport.newInstance();
		AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("a")).set(packet, id);
		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("b")).set(packet, (int) (loc.getX() * 32D));
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("c")).set(packet, (int) (loc.getY() * 32D));
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("d")).set(packet, (int) (loc.getZ() * 32D));
		} else {
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("b")).set(packet, loc.getX());
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("c")).set(packet, loc.getY());
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("d")).set(packet, loc.getZ());
			AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("g")).set(packet, onGround);
		}
		AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("e")).set(packet, (byte) (int) (loc.getYaw() * 256F / 360F));
		AccessUtil.setAccessible(NMSClass.PacketPlayOutEntityTeleport.getDeclaredField("f")).set(packet, (byte) (int) (loc.getPitch() * 256F / 360F));

		return packet;
	}
}
