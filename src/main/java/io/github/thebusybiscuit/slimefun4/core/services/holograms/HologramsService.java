package io.github.thebusybiscuit.slimefun4.core.services.holograms;

import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.util.Vector;

/**
 * This service is responsible for handling holograms.
 *
 * @author TheBusyBiscuit
 *
 * @see HologramOwner
 */
public interface HologramsService {
    /**
     * This will start the {@link HologramsService} and schedule a repeating
     * purge-task.
     */
    public void start();

    /**
     * This returns the default {@link Hologram} offset.
     *
     * @return The default offset
     */
    @Nonnull
    public Vector getDefaultOffset();

    /**
     * This removes the {@link Hologram} at that given {@link Location}.
     * <p>
     * <strong>This method must be executed on the main {@link Server} {@link Thread}.</strong>
     *
     * @param loc
     *            The {@link Location}
     *
     * @return Whether the {@link Hologram} could be removed, false if the {@link Hologram} does not
     *         exist or was already removed
     */
    public boolean removeHologram(@Nonnull Location loc);

    /**
     * This will update the label of the {@link Hologram}.
     *
     * @param loc
     *            The {@link Location} of this {@link Hologram}
     * @param label
     *            The label to set, can be null
     */
    public void setHologramLabel(@Nonnull Location loc, @Nullable String label);
}
