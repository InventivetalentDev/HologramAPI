package de.inventivegames.hologram;

import de.inventivegames.hologram.reflection.ClassBuilder;
import de.inventivegames.hologram.reflection.NMSClass;
import de.inventivegames.hologram.reflection.Reflection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.reflection.minecraft.DataWatcher;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.FieldResolver;
import org.inventivetalent.reflection.util.AccessUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

abstract class CraftHologram implements Hologram {

	static FieldResolver PacketPlayOutSpawnEntityLivingFieldResolver = new FieldResolver(NMSClass.PacketPlayOutSpawnEntityLiving);

	protected int[] hologramIDs;
	protected int[] touchIDs;

	protected boolean packetsBuilt;

	/***
	 * Packets
	 ***/
	/* Hologram */
	protected Object spawnPacketArmorStand;
	protected Object spawnPacketWitherSkull;
	protected Object spawnPacketHorse_1_7;
	protected Object spawnPacketHorse_1_8;
	protected Object attachPacket;
	protected Object teleportPacketArmorStand;
	protected Object teleportPacketSkull;
	protected Object teleportPacketHorse_1_7;
	protected Object teleportPacketHorse_1_8;
	protected Object destroyPacket;
	protected Object ridingAttachPacket;
	protected Object ridingEjectPacket;
	/* Touch */
	protected Object spawnPacketTouchSlime;
	protected Object spawnPacketTouchVehicle;
	protected Object attachPacketTouch;
	protected Object destroyPacketTouch;
	protected Object teleportPacketTouchSlime;
	protected Object teleportPacketTouchVehicle;

	/***
	 * DataWatchers
	 ***/
	/* Hologram */
	protected Object dataWatcherArmorStand;
	protected Object dataWatcherWitherSkull;
	protected Object dataWatcherHorse_1_7;
	protected Object dataWatcherHorse_1_8;
	/* Touch */
	protected Object dataWatcherTouchSlime;
	protected Object dataWatcherTouchVehicle;

	protected boolean matchesTouchID(int id) {
		if (!this.isTouchable() || this.touchIDs == null) { return false; }
		for (int i : this.touchIDs) {
			if (i == id) { return true; }
		}
		return false;
	}

	protected boolean matchesHologramID(int id) {
		if (!HologramAPI.packetsEnabled() || this.hologramIDs == null) { return false; }
		for (int i : this.hologramIDs) {
			if (i == id) { return true; }
		}
		return false;
	}

	protected void buildPackets(boolean rebuild) throws Exception {
		if (!rebuild && this.packetsBuilt) { throw new IllegalStateException("packets already built"); }
		if (rebuild && !this.packetsBuilt) { throw new IllegalStateException("cannot rebuild packets before building once"); }
		Object world = Reflection.getHandle(this.getLocation().getWorld());

		/* Hologram packets */
		if (HologramAPI.is1_8 && !HologramAPI.useProtocolSupport) {
			Object armorStand = ClassBuilder.buildEntityArmorStand(world, this.getLocation().add(0, HologramOffsets.ARMOR_STAND_DEFAULT, 0), this.getText());

			ClassBuilder.setupArmorStand(armorStand);

			if (rebuild) {
				AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).set(armorStand, this.hologramIDs[0]);
			} else {
				this.hologramIDs = new int[] { AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).getInt(armorStand) };
			}

			this.spawnPacketArmorStand = ClassBuilder.buildArmorStandSpawnPacket(armorStand);
			this.dataWatcherArmorStand = AccessUtil.setAccessible(PacketPlayOutSpawnEntityLivingFieldResolver.resolveByFirstType(NMSClass.DataWatcher)).get(this.spawnPacketArmorStand);

			this.teleportPacketArmorStand = ClassBuilder.buildTeleportPacket(this.hologramIDs[0], this.getLocation().add(0, HologramOffsets.ARMOR_STAND_DEFAULT, 0), true, false);
		} else {

			// Spawn Horse
			Object horse_1_7 = ClassBuilder.buildEntityHorse_1_7(world, this.getLocation().add(0, HologramOffsets.WITHER_SKULL_HORSE, 0), this.getText());
			Object horse_1_8 = ClassBuilder.buildEntityHorse_1_8(world, this.getLocation().add(0, HologramOffsets.ARMOR_STAND_PACKET, 0), this.getText());

			// Spawn WitherSkull
			Object witherSkull_1_7 = ClassBuilder.buildEntityWitherSkull(world, this.getLocation().add(0, HologramOffsets.WITHER_SKULL_HORSE, 0));
			this.dataWatcherWitherSkull = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("datawatcher")).get(witherSkull_1_7);

			if (rebuild) {
				AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).set(witherSkull_1_7, this.hologramIDs[0]);
				AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).set(horse_1_7, this.hologramIDs[1]);
				AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).set(horse_1_8, this.hologramIDs[2]);
				// Reset the entity count
				Field entityCountField = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("entityCount"));
				entityCountField.set(null, (int) entityCountField.get(null) - 3);
			} else {
				this.hologramIDs = new int[] {
						AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).getInt(witherSkull_1_7),
						//
						AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).getInt(horse_1_7),
						//
						AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).getInt(horse_1_8)
				};
			}

			/* Packets */

			// 1.7 Horse
			this.spawnPacketHorse_1_7 = ClassBuilder.buildHorseSpawnPacket_1_7(horse_1_7, this.getText());
			this.dataWatcherHorse_1_7 = AccessUtil.setAccessible(PacketPlayOutSpawnEntityLivingFieldResolver.resolveByFirstType(NMSClass.DataWatcher)).get(this.spawnPacketHorse_1_7);

			// Special 1.8 client packet which replaces the entity with an ArmorStand
			this.spawnPacketHorse_1_8 = ClassBuilder.buildHorseSpawnPacket_1_8(horse_1_8, this.getText());
			this.dataWatcherHorse_1_8 = AccessUtil.setAccessible(PacketPlayOutSpawnEntityLivingFieldResolver.resolveByFirstType(NMSClass.DataWatcher)).get(this.spawnPacketHorse_1_8);

			// WitherSkull
			this.spawnPacketWitherSkull = ClassBuilder.buildWitherSkullSpawnPacket(witherSkull_1_7);

			if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
				this.attachPacket = NMSClass.PacketPlayOutAttachEntity.getConstructor(int.class, NMSClass.Entity, NMSClass.Entity).newInstance(0, horse_1_7, witherSkull_1_7);
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("b")).set(this.attachPacket, this.hologramIDs[1]);
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("c")).set(this.attachPacket, this.hologramIDs[0]);
			} else {
				this.attachPacket = NMSClass.PacketPlayOutAttachEntity.newInstance();
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("a")).set(this.attachPacket, this.hologramIDs[1]);
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("b")).set(this.attachPacket, this.hologramIDs[0]);
			}

			if (!HologramAPI.is1_8 || HologramAPI.useProtocolSupport) {
				this.teleportPacketSkull = ClassBuilder.buildTeleportPacket(this.hologramIDs[0], this.getLocation().add(0, HologramOffsets.WITHER_SKULL_HORSE, 0), true, false);
				this.teleportPacketHorse_1_7 = ClassBuilder.buildTeleportPacket(this.hologramIDs[1], this.getLocation().add(0, HologramOffsets.WITHER_SKULL_HORSE, 0), true, false);
			}
			this.teleportPacketHorse_1_8 = ClassBuilder.buildTeleportPacket(this.hologramIDs[2], this.getLocation().add(0, HologramOffsets.ARMOR_STAND_PACKET, 0), true, false);
		}

		/* Touch packets */
		if (this.isTouchable()) {
			int size = this.getText() == null ? 1 : (int) (this.getText().length() / 2 / 3);
			Object touchSlime = ClassBuilder.buildEntitySlime(world, this.getLocation().add(0, HologramOffsets.TOUCH_SLIME_SKULL, 0), size);
			this.dataWatcherTouchSlime = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("datawatcher")).get(touchSlime);

			Object touchVehicle = null;

			if (HologramAPI.is1_8 && !HologramAPI.useProtocolSupport) {
				touchVehicle = ClassBuilder.buildEntityArmorStand(world, this.getLocation().add(0, HologramOffsets.ARMOR_STAND_PACKET + HologramOffsets.TOUCH_SLIME_ARMOR_STAND, 0), null);
				this.dataWatcherTouchVehicle = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("datawatcher")).get(touchVehicle);

				ClassBuilder.setupArmorStand(touchVehicle);
			} else {
				touchVehicle = ClassBuilder.buildEntityWitherSkull(world, this.getLocation().add(0, HologramOffsets.TOUCH_SLIME_SKULL, 0));
				this.dataWatcherTouchVehicle = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("datawatcher")).get(touchVehicle);
				//				ClassBuilder.setDataWatcherValue(this.dataWatcherTouchVehicle, 0, (byte) 32);
				DataWatcher.setValue(this.dataWatcherTouchVehicle, 0, DataWatcher.V1_9.ValueType.ENTITY_FLAG, (byte) 32);
			}

			if (rebuild) {
				AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).set(touchSlime, this.touchIDs[0]);
				AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).set(touchVehicle, this.touchIDs[1]);
				// Reset the entity count
				Field entityCountField = AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("entityCount"));
				entityCountField.set(null, (int) entityCountField.get(null) - 2);
			} else {
				this.touchIDs = new int[] {
						AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).getInt(touchSlime),
						//
						AccessUtil.setAccessible(NMSClass.Entity.getDeclaredField("id")).getInt(touchVehicle)
				};
			}

			this.spawnPacketTouchSlime = ClassBuilder.buildSlimeSpawnPacket(touchSlime);
			if (HologramAPI.is1_8 && !HologramAPI.useProtocolSupport) {
				this.spawnPacketTouchVehicle = ClassBuilder.buildArmorStandSpawnPacket(touchVehicle);
			} else {
				this.spawnPacketTouchVehicle = ClassBuilder.buildWitherSkullSpawnPacket(touchVehicle);
			}

			if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
				this.attachPacketTouch = NMSClass.PacketPlayOutAttachEntity.getConstructor(int.class, NMSClass.Entity, NMSClass.Entity).newInstance(0, touchSlime, touchVehicle);
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("b")).set(this.attachPacketTouch, this.touchIDs[0]);
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("c")).set(this.attachPacketTouch, this.touchIDs[1]);
			} else {
				this.attachPacketTouch = NMSClass.PacketPlayOutAttachEntity.newInstance();
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("a")).set(this.attachPacketTouch, this.touchIDs[0]);
				AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("b")).set(this.attachPacketTouch, this.touchIDs[1]);
			}

			this.teleportPacketTouchSlime = ClassBuilder.buildTeleportPacket(this.touchIDs[0], this.getLocation().add(0, HologramOffsets.TOUCH_SLIME_SKULL, 0), true, false);
			if (HologramAPI.is1_8 && !HologramAPI.useProtocolSupport) {
				this.teleportPacketTouchVehicle = ClassBuilder.buildTeleportPacket(this.touchIDs[1], this.getLocation().add(0, HologramOffsets.ARMOR_STAND_PACKET + HologramOffsets.TOUCH_SLIME_ARMOR_STAND, 0), true, false);
			} else {
				this.teleportPacketTouchVehicle = ClassBuilder.buildTeleportPacket(this.touchIDs[1], this.getLocation().add(0, HologramOffsets.TOUCH_SLIME_SKULL, 0), true, false);
			}

			if (!rebuild) {
				this.destroyPacketTouch = NMSClass.PacketPlayOutEntityDestroy.getConstructor(int[].class).newInstance(this.touchIDs);
			}

		}

		if (Minecraft.VERSION.olderThan(Minecraft.Version.v1_9_R1)) {
			this.ridingAttachPacket = NMSClass.PacketPlayOutAttachEntity.newInstance();
			this.ridingEjectPacket = NMSClass.PacketPlayOutAttachEntity.newInstance();

			AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("a")).set(this.ridingAttachPacket, 0);
			AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("a")).set(this.ridingEjectPacket, 0);

			AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("b")).set(this.ridingAttachPacket, this.hologramIDs[0]);
			AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("b")).set(this.ridingEjectPacket, this.hologramIDs[0]);

			AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("c")).set(this.ridingAttachPacket, this.getAttachedTo() != null ? this.getAttachedTo().getEntityId() : -1);
			AccessUtil.setAccessible(NMSClass.PacketPlayOutAttachEntity.getDeclaredField("c")).set(this.ridingEjectPacket, -1);
		} else {
			this.ridingAttachPacket = NMSClass.PacketPlayOutMount.newInstance();
			this.ridingEjectPacket = NMSClass.PacketPlayOutMount.newInstance();

			AccessUtil.setAccessible(NMSClass.PacketPlayOutMount.getDeclaredField("a")).set(this.ridingAttachPacket, ((DefaultHologram) this).isAttached() && this.getAttachedTo() != null ? this.getAttachedTo().getEntityId() : -1);
			AccessUtil.setAccessible(NMSClass.PacketPlayOutMount.getDeclaredField("a")).set(this.ridingEjectPacket, ((DefaultHologram) this).isAttached() && this.getAttachedTo() != null ? this.getAttachedTo().getEntityId() : -1);

			AccessUtil.setAccessible(NMSClass.PacketPlayOutMount.getDeclaredField("b")).set(this.ridingAttachPacket, new int[] { this.hologramIDs[0] });
			AccessUtil.setAccessible(NMSClass.PacketPlayOutMount.getDeclaredField("b")).set(this.ridingEjectPacket, new int[0]);
		}

		if (!rebuild) {
			this.destroyPacket = NMSClass.PacketPlayOutEntityDestroy.getConstructor(int[].class).newInstance(this.hologramIDs);
		}
	}

	protected void sendSpawnPackets(final Collection<? extends Player> receivers, final boolean holo, final boolean touch) {
		if (holo) {
			if (HologramAPI.is1_8 && !HologramAPI.useProtocolSupport) {
				for (Player p : receivers) {
					HologramAPI.sendPacket(p, this.spawnPacketArmorStand);
				}
			} else {
				for (Player p : receivers) {
					if (HologramAPI.getVersion(p) > 5) {
						HologramAPI.sendPacket(p, this.spawnPacketHorse_1_8);
					} else {
						HologramAPI.sendPacket(p, this.spawnPacketHorse_1_7);
						HologramAPI.sendPacket(p, this.spawnPacketWitherSkull);
						HologramAPI.sendPacket(p, this.attachPacket);
					}
				}
			}
		}
		if (touch && this.isTouchable()) {
			for (Player p : receivers) {
				HologramAPI.sendPacket(p, this.spawnPacketTouchSlime);
				HologramAPI.sendPacket(p, this.spawnPacketTouchVehicle);
				HologramAPI.sendPacket(p, this.attachPacketTouch);
			}
		}
		if (holo || touch) {
			new BukkitRunnable() {

				@Override
				public void run() {
					CraftHologram.this.sendTeleportPackets(receivers, holo, touch);
				}
			}.runTaskLater(HologramPlugin.instance, 1L);
		}
	}

	protected void sendTeleportPackets(final Collection<? extends Player> receivers, boolean holo, boolean touch) {
		if (!holo && !touch) { return; }
		for (Player p : receivers) {
			if (holo) {
				if (!HologramAPI.is1_8 || HologramAPI.useProtocolSupport) {
					if (HologramAPI.getVersion(p) > 5) {
						HologramAPI.sendPacket(p, this.teleportPacketHorse_1_8);
					} else {
						HologramAPI.sendPacket(p, this.teleportPacketHorse_1_7);
						HologramAPI.sendPacket(p, this.teleportPacketSkull);
					}
				} else {
					HologramAPI.sendPacket(p, this.teleportPacketArmorStand);
				}
			}
			if (touch && this.isTouchable()) {
				HologramAPI.sendPacket(p, this.teleportPacketTouchSlime);
				HologramAPI.sendPacket(p, this.teleportPacketTouchVehicle);
			}
		}
	}

	protected void sendNamePackets(final Collection<? extends Player> receivers) {
		for (Player p : receivers) {
			try {
				int id = HologramAPI.is1_8 && !HologramAPI.useProtocolSupport ? this.hologramIDs[0] : HologramAPI.getVersion(p) > 5 ? this.hologramIDs[2] : this.hologramIDs[1];
				Object dataWatcher = HologramAPI.is1_8 && !HologramAPI.useProtocolSupport ? this.dataWatcherArmorStand : HologramAPI.getVersion(p) > 5 ? this.dataWatcherHorse_1_8 : this.dataWatcherHorse_1_7;
				Object packet = ClassBuilder.buildNameMetadataPacket(id, dataWatcher, 2, 3, this.getText());
				HologramAPI.sendPacket(p, packet);
				if (HologramAPI.getVersion(p) <= 5) {
					if (this.hologramIDs.length > 1) {
						HologramAPI.sendPacket(p, ClassBuilder.buildNameMetadataPacket(this.hologramIDs[1], this.dataWatcherHorse_1_7, 10, 11, this.getText()));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void sendDestroyPackets(Collection<? extends Player> receivers) {
		for (Player p : receivers) {
			HologramAPI.sendPacket(p, this.destroyPacket);
			if (this.isTouchable()) {
				HologramAPI.sendPacket(p, this.destroyPacketTouch);
			}
		}
	}

	protected void sendAttachPacket(Collection<? extends Player> receivers) {
		for (Player p : receivers) {
			if (!((DefaultHologram) this).isAttached()) {
				HologramAPI.sendPacket(p, this.ridingEjectPacket);
			} else {
				HologramAPI.sendPacket(p, this.ridingAttachPacket);
			}
		}
	}

	@Override
	public abstract Location getLocation();

	@Override
	public abstract void setLocation(Location loc);

	@Override
	public abstract String getText();

	@Override
	public abstract void setText(String text);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.attachPacket == null ? 0 : this.attachPacket.hashCode());
		result = prime * result + (this.attachPacketTouch == null ? 0 : this.attachPacketTouch.hashCode());
		result = prime * result + (this.dataWatcherArmorStand == null ? 0 : this.dataWatcherArmorStand.hashCode());
		result = prime * result + (this.dataWatcherHorse_1_7 == null ? 0 : this.dataWatcherHorse_1_7.hashCode());
		result = prime * result + (this.dataWatcherHorse_1_8 == null ? 0 : this.dataWatcherHorse_1_8.hashCode());
		result = prime * result + (this.dataWatcherTouchSlime == null ? 0 : this.dataWatcherTouchSlime.hashCode());
		result = prime * result + (this.dataWatcherTouchVehicle == null ? 0 : this.dataWatcherTouchVehicle.hashCode());
		result = prime * result + (this.dataWatcherWitherSkull == null ? 0 : this.dataWatcherWitherSkull.hashCode());
		result = prime * result + (this.destroyPacket == null ? 0 : this.destroyPacket.hashCode());
		result = prime * result + (this.destroyPacketTouch == null ? 0 : this.destroyPacketTouch.hashCode());
		result = prime * result + Arrays.hashCode(this.hologramIDs);
		result = prime * result + (this.packetsBuilt ? 1231 : 1237);
		result = prime * result + (this.spawnPacketArmorStand == null ? 0 : this.spawnPacketArmorStand.hashCode());
		result = prime * result + (this.spawnPacketHorse_1_7 == null ? 0 : this.spawnPacketHorse_1_7.hashCode());
		result = prime * result + (this.spawnPacketHorse_1_8 == null ? 0 : this.spawnPacketHorse_1_8.hashCode());
		result = prime * result + (this.spawnPacketTouchSlime == null ? 0 : this.spawnPacketTouchSlime.hashCode());
		result = prime * result + (this.spawnPacketTouchVehicle == null ? 0 : this.spawnPacketTouchVehicle.hashCode());
		result = prime * result + (this.spawnPacketWitherSkull == null ? 0 : this.spawnPacketWitherSkull.hashCode());
		result = prime * result + (this.teleportPacketArmorStand == null ? 0 : this.teleportPacketArmorStand.hashCode());
		result = prime * result + (this.teleportPacketHorse_1_7 == null ? 0 : this.teleportPacketHorse_1_7.hashCode());
		result = prime * result + (this.teleportPacketHorse_1_8 == null ? 0 : this.teleportPacketHorse_1_8.hashCode());
		result = prime * result + (this.teleportPacketSkull == null ? 0 : this.teleportPacketSkull.hashCode());
		result = prime * result + (this.teleportPacketTouchSlime == null ? 0 : this.teleportPacketTouchSlime.hashCode());
		result = prime * result + (this.teleportPacketTouchVehicle == null ? 0 : this.teleportPacketTouchVehicle.hashCode());
		result = prime * result + Arrays.hashCode(this.touchIDs);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (this.getClass() != obj.getClass()) { return false; }
		CraftHologram other = (CraftHologram) obj;
		if (this.attachPacket == null) {
			if (other.attachPacket != null) { return false; }
		} else if (!this.attachPacket.equals(other.attachPacket)) { return false; }
		if (this.attachPacketTouch == null) {
			if (other.attachPacketTouch != null) { return false; }
		} else if (!this.attachPacketTouch.equals(other.attachPacketTouch)) { return false; }
		if (this.dataWatcherArmorStand == null) {
			if (other.dataWatcherArmorStand != null) { return false; }
		} else if (!this.dataWatcherArmorStand.equals(other.dataWatcherArmorStand)) { return false; }
		if (this.dataWatcherHorse_1_7 == null) {
			if (other.dataWatcherHorse_1_7 != null) { return false; }
		} else if (!this.dataWatcherHorse_1_7.equals(other.dataWatcherHorse_1_7)) { return false; }
		if (this.dataWatcherHorse_1_8 == null) {
			if (other.dataWatcherHorse_1_8 != null) { return false; }
		} else if (!this.dataWatcherHorse_1_8.equals(other.dataWatcherHorse_1_8)) { return false; }
		if (this.dataWatcherTouchSlime == null) {
			if (other.dataWatcherTouchSlime != null) { return false; }
		} else if (!this.dataWatcherTouchSlime.equals(other.dataWatcherTouchSlime)) { return false; }
		if (this.dataWatcherTouchVehicle == null) {
			if (other.dataWatcherTouchVehicle != null) { return false; }
		} else if (!this.dataWatcherTouchVehicle.equals(other.dataWatcherTouchVehicle)) { return false; }
		if (this.dataWatcherWitherSkull == null) {
			if (other.dataWatcherWitherSkull != null) { return false; }
		} else if (!this.dataWatcherWitherSkull.equals(other.dataWatcherWitherSkull)) { return false; }
		if (this.destroyPacket == null) {
			if (other.destroyPacket != null) { return false; }
		} else if (!this.destroyPacket.equals(other.destroyPacket)) { return false; }
		if (this.destroyPacketTouch == null) {
			if (other.destroyPacketTouch != null) { return false; }
		} else if (!this.destroyPacketTouch.equals(other.destroyPacketTouch)) { return false; }
		if (!Arrays.equals(this.hologramIDs, other.hologramIDs)) { return false; }
		if (this.packetsBuilt != other.packetsBuilt) { return false; }
		if (this.spawnPacketArmorStand == null) {
			if (other.spawnPacketArmorStand != null) { return false; }
		} else if (!this.spawnPacketArmorStand.equals(other.spawnPacketArmorStand)) { return false; }
		if (this.spawnPacketHorse_1_7 == null) {
			if (other.spawnPacketHorse_1_7 != null) { return false; }
		} else if (!this.spawnPacketHorse_1_7.equals(other.spawnPacketHorse_1_7)) { return false; }
		if (this.spawnPacketHorse_1_8 == null) {
			if (other.spawnPacketHorse_1_8 != null) { return false; }
		} else if (!this.spawnPacketHorse_1_8.equals(other.spawnPacketHorse_1_8)) { return false; }
		if (this.spawnPacketTouchSlime == null) {
			if (other.spawnPacketTouchSlime != null) { return false; }
		} else if (!this.spawnPacketTouchSlime.equals(other.spawnPacketTouchSlime)) { return false; }
		if (this.spawnPacketTouchVehicle == null) {
			if (other.spawnPacketTouchVehicle != null) { return false; }
		} else if (!this.spawnPacketTouchVehicle.equals(other.spawnPacketTouchVehicle)) { return false; }
		if (this.spawnPacketWitherSkull == null) {
			if (other.spawnPacketWitherSkull != null) { return false; }
		} else if (!this.spawnPacketWitherSkull.equals(other.spawnPacketWitherSkull)) { return false; }
		if (this.teleportPacketArmorStand == null) {
			if (other.teleportPacketArmorStand != null) { return false; }
		} else if (!this.teleportPacketArmorStand.equals(other.teleportPacketArmorStand)) { return false; }
		if (this.teleportPacketHorse_1_7 == null) {
			if (other.teleportPacketHorse_1_7 != null) { return false; }
		} else if (!this.teleportPacketHorse_1_7.equals(other.teleportPacketHorse_1_7)) { return false; }
		if (this.teleportPacketHorse_1_8 == null) {
			if (other.teleportPacketHorse_1_8 != null) { return false; }
		} else if (!this.teleportPacketHorse_1_8.equals(other.teleportPacketHorse_1_8)) { return false; }
		if (this.teleportPacketSkull == null) {
			if (other.teleportPacketSkull != null) { return false; }
		} else if (!this.teleportPacketSkull.equals(other.teleportPacketSkull)) { return false; }
		if (this.teleportPacketTouchSlime == null) {
			if (other.teleportPacketTouchSlime != null) { return false; }
		} else if (!this.teleportPacketTouchSlime.equals(other.teleportPacketTouchSlime)) { return false; }
		if (this.teleportPacketTouchVehicle == null) {
			if (other.teleportPacketTouchVehicle != null) { return false; }
		} else if (!this.teleportPacketTouchVehicle.equals(other.teleportPacketTouchVehicle)) { return false; }
		if (!Arrays.equals(this.touchIDs, other.touchIDs)) { return false; }
		return true;
	}

	@Override
	public String toString() {
		return "{\"hologramIDs\":\"" + Arrays.toString(this.hologramIDs) + "\",\"touchIDs\":\"" + Arrays
				.toString(this.touchIDs) + "\",\"packetsBuilt\":\"" + this.packetsBuilt + "\",\"spawnPacketArmorStand\":\"" + this.spawnPacketArmorStand + "\",\"spawnPacketWitherSkull\":\"" + this.spawnPacketWitherSkull + "\",\"spawnPacketHorse_1_7\":\"" + this.spawnPacketHorse_1_7 + "\",\"spawnPacketHorse_1_8\":\"" + this.spawnPacketHorse_1_8 + "\",\"attachPacket\":\"" + this.attachPacket
				+ "\",\"teleportPacketArmorStand\":\"" + this.teleportPacketArmorStand + "\",\"teleportPacketSkull\":\"" + this.teleportPacketSkull + "\",\"teleportPacketHorse_1_7\":\"" + this.teleportPacketHorse_1_7 + "\",\"teleportPacketHorse_1_8\":\"" + this.teleportPacketHorse_1_8 + "\",\"destroyPacket\":\"" + this.destroyPacket + "\",\"spawnPacketTouchSlime\":\"" + this.spawnPacketTouchSlime
				+ "\",\"spawnPacketTouchWitherSkull\":\"" + this.spawnPacketTouchVehicle + "\",\"attachPacketTouch\":\"" + this.attachPacketTouch + "\",\"destroyPacketTouch\":\"" + this.destroyPacketTouch + "\",\"teleportPacketTouchSlime\":\"" + this.teleportPacketTouchSlime + "\",\"teleportPacketTouchWitherSkull\":\"" + this.teleportPacketTouchVehicle + "\",\"dataWatcherArmorStand\":\""
				+ this.dataWatcherArmorStand + "\",\"dataWatcherWitherSkull\":\"" + this.dataWatcherWitherSkull + "\",\"dataWatcherHorse_1_7\":\"" + this.dataWatcherHorse_1_7 + "\",\"dataWatcherHorse_1_8\":\"" + this.dataWatcherHorse_1_8 + "\",\"dataWatcherTouchSlime\":\"" + this.dataWatcherTouchSlime + "\",\"dataWatcherTouchWitherSkull\":\"" + this.dataWatcherTouchVehicle + "\"}";
	}

}
