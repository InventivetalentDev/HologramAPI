package de.inventivegames.hologram;

import de.inventivegames.hologram.touch.TouchHandler;
import de.inventivegames.hologram.view.ViewHandler;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public abstract interface Hologram {

	/**
	 * @return <code>true</code> if the hologram is spawned
	 */
	boolean isSpawned();

	/**
	 * Spawns the hologram and despawns it after the specified timeout
	 *
	 * @param ticks timeout
	 */
	void spawn(@Nonnull @Nonnegative long ticks);

	/**
	 * Spawns the hologram
	 *
	 * @return <code>true</code> if the hologram has been spawned
	 */
	boolean spawn();

	/**
	 * Despawns the hologram
	 *
	 * @return <code>true</code> if the hologram has been despawned
	 */
	boolean despawn();

	/**
	 * @return The text content of the hologram
	 */
	String getText();

	/**
	 * @param text New text content of the hologram
	 */
	void setText(@Nullable String text);

	/**
	 * Updates the content of the hologram
	 */
	void update();

	/**
	 * Automatically updates the content of the hologram <code>-1</code> as the interval argument will stop the update
	 *
	 * @param interval Update interval in ticks, <code>-1</code> to stop updating
	 */
	void update(long interval);

	/**
	 * @return The {@link Location} of the hologram
	 */
	Location getLocation();

	/**
	 * @param loc changes the {@link Location} of the hologram
	 * @see Hologram#move(Location)
	 */
	void setLocation(@Nonnull Location loc);

	/**
	 * Moves the hologram
	 *
	 * @param loc new {@link Location} of the hologram
	 */
	void move(@Nonnull Location loc);

	/**
	 * @return <code>true</code> if the hologram is touchable
	 */
	boolean isTouchable();

	/**
	 * Changes the touchability
	 *
	 * @param flag <code>true</code> if the hologram should be touchable,
	 */
	void setTouchable(boolean flag);

	/**
	 * Adds a touch handler to the hologram
	 *
	 * @param handler {@link TouchHandler} instance
	 */
	void addTouchHandler(@Nonnull TouchHandler handler);

	/**
	 * Removes a touch handler from the hologram
	 *
	 * @param handler {@link TouchHandler} instance
	 */
	void removeTouchHandler(@Nonnull TouchHandler handler);

	/**
	 * @return a {@link Collection} of registered {@link TouchHandler}s
	 */
	Collection<TouchHandler> getTouchHandlers();

	/**
	 * Removes all {@link TouchHandler}s from the hologram
	 */
	void clearTouchHandlers();

	/**
	 * Adds a view handler to the hologram
	 *
	 * @param handler {@link ViewHandler} instance
	 */
	void addViewHandler(@Nonnull ViewHandler handler);

	/**
	 * Removes a view handler from the hologram
	 *
	 * @param handler {@link ViewHandler} instance
	 */
	void removeViewHandler(@Nonnull ViewHandler handler);

	/**
	 * @return a {@link Collection} of registered {@link ViewHandler}s
	 */
	@Nonnull
	Collection<ViewHandler> getViewHandlers();

	/**
	 * Removes all {@link ViewHandler}s from the hologram
	 */
	void clearViewHandlers();

	/**
	 * Adds a {@link Hologram} line below this hologram
	 *
	 * @param text Text content of the hologram
	 * @return A new {@link Hologram} instance
	 */
	@Nonnull
	Hologram addLineBelow(String text);

	/**
	 * @return The {@link Hologram} line below this hologram
	 */
	@Nullable
	Hologram getLineBelow();

	/**
	 * Removes the line below this hologram
	 *
	 * @return <code>true</code> if the hologram has been removed
	 */
	boolean removeLineBelow();

	/**
	 * @return a {@link Collection} of all below {@link Hologram} lines
	 */
	@Nonnull
	Collection<Hologram> getLinesBelow();

	/**
	 * Adds a {@link Hologram} line above this hologram
	 *
	 * @param text Text content of the hologram
	 * @return A new {@link Hologram} instance
	 */
	@Nonnull
	Hologram addLineAbove(String text);

	/**
	 * @return The {@link Hologram} line above this hologram
	 */
	@Nullable
	Hologram getLineAbove();

	/**
	 * Removes the line above this hologram
	 *
	 * @return <code>true</code> if the hologram has been removed
	 */
	boolean removeLineAbove();

	/**
	 * @return a {@link Collection} of all above {@link Hologram} lines
	 */
	@Nonnull
	Collection<Hologram> getLinesAbove();

	/**
	 * @return a {@link Collection} of all below and above {@link Hologram} lines (Including this hologram)
	 */
	@Nonnull
	Collection<Hologram> getLines();

	/**
	 * @return The entity the hologram is attached to
	 */
	@Nullable
	Entity getAttachedTo();

	/**
	 * Attached the hologram to a entity
	 *
	 * @param entity Entity to attach the hologram to, or null to remove the attachment
	 */
	void setAttachedTo(@Nullable Entity entity);

}
