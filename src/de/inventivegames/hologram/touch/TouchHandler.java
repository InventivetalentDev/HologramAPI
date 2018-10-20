package de.inventivegames.hologram.touch;

import de.inventivegames.hologram.Hologram;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public interface TouchHandler {

	public void onTouch(@Nonnull Hologram hologram, @Nonnull Player player, @Nonnull TouchAction action);

}
