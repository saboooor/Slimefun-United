package io.github.thebusybiscuit.slimefun4.core.services.holograms;

import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;

/**
 * This represents an {@link TextDisplay} or {@link ArmorStand} that can expire and be renamed.
 *
 * @author TheBusyBiscuit
 */
class Hologram {

    /**
     * This is the minimum duration after which the {@link Hologram} will expire.
     */
    private static final long EXPIRES_AFTER = TimeUnit.MINUTES.toMillis(10);

    /**
     * The {@link UUID} of the {@link TextDisplay} or {@link ArmorStand}.
     */
    private final UUID uniqueId;

    /**
     * The timestamp of when the {@link TextDisplay} or {@link ArmorStand} was last accessed.
     */
    private long lastAccess;

    /**
     * The label of this {@link Hologram}.
     */
    private String label;

    /**
     * This creates a new {@link Hologram} for the given {@link UUID}.
     *
     * @param uniqueId The {@link UUID} of the corresponding {@link TextDisplay} or {@link ArmorStand}
     */
    Hologram(@Nonnull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.lastAccess = System.currentTimeMillis();
    }

    /**
     * This returns the corresponding {@link TextDisplay}
     * and also updates the "lastAccess" timestamp.
     * <p>
     * If the {@link TextDisplay} was removed, it will return null.
     *
     * @return The {@link TextDisplay} or null.
     */
    @Nullable TextDisplay getDisplay() {
        Entity n = Bukkit.getEntity(uniqueId);

        if (n instanceof TextDisplay textDisplay && n.isValid()) {
            this.lastAccess = System.currentTimeMillis();
            return textDisplay;
        } else {
            this.lastAccess = 0;
            return null;
        }
    }

    /**
     * This returns the corresponding {@link ArmorStand}
     * and also updates the "lastAccess" timestamp.
     * <p>
     * If the {@link ArmorStand} was removed, it will return null.
     *
     * @return The {@link ArmorStand} or null.
     */
    @Nullable ArmorStand getArmorStand() {
        Entity n = Bukkit.getEntity(uniqueId);

        if (n instanceof ArmorStand armorStand && n.isValid()) {
            this.lastAccess = System.currentTimeMillis();
            return armorStand;
        } else {
            this.lastAccess = 0;
            return null;
        }
    }

    /**
     * This checks if the associated {@link TextDisplay} or {@link ArmorStand} has despawned.
     *
     * @return Whether the {@link TextDisplay} or {@link ArmorStand} despawned
     */
    boolean hasDespawned() {
        return getDisplay() == null || getArmorStand() == null;
    }

    /**
     * This returns whether this {@link Hologram} has expired.
     * The textDisplay will expire if the last access has been more than 10
     * minutes ago.
     *
     * @return Whether this {@link Hologram} has expired
     */
    boolean hasExpired() {
        return System.currentTimeMillis() - lastAccess > EXPIRES_AFTER;
    }

    /**
     * This method sets the label of this {@link Hologram}.
     *
     * @param label The label to set
     */
    void setLabel(@Nullable String label) {
        if (Objects.equals(this.label, label)) {
            /*
             * Label is already set, no need to cause an entity
             * update. But we can update the lastAccess flag.
             */
            this.lastAccess = System.currentTimeMillis();
        } else {
            this.label = label;

            if (Slimefun.getMinecraftVersion().isAtLeast(1, 19, 4)) {
                TextDisplay textDisplay = getDisplay();

                if (textDisplay != null) {
                    if (label != null) {
                        textDisplay.text(Component.text(label));
                    } else {
                        textDisplay.text(null);
                    }
                }
            } else {
                ArmorStand armorStand = getArmorStand();

                if (armorStand != null) {
                    if (label != null) {
                        armorStand.setCustomNameVisible(true);
                        armorStand.customName(Component.text(label));
                    } else {
                        armorStand.setCustomNameVisible(false);
                        armorStand.customName(Component.text(label));
                    }
                }
            }
        }
    }

    /**
     * This will remove the {@link TextDisplay} or {@link ArmorStand} and expire this {@link Hologram}.
     */
    void remove() {
        TextDisplay textDisplay = getDisplay();
        ArmorStand armorStand = getArmorStand();

        if (textDisplay != null) {
            lastAccess = 0;
            textDisplay.remove();
        }

        if (armorStand != null) {
            lastAccess = 0;
            armorStand.remove();
        }
    }
}
