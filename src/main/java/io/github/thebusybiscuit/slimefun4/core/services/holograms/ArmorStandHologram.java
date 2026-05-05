package io.github.thebusybiscuit.slimefun4.core.services.holograms;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

/**
 * This represents an {@link ArmorStand} that can expire and be renamed.
 *
 * @author TheBusyBiscuit
 */
class ArmorStandHologram {

    /**
     * This is the minimum duration after which the {@link ArmorStandHologram} will expire.
     */
    private static final long EXPIRES_AFTER = TimeUnit.MINUTES.toMillis(10);

    /**
     * The {@link UUID} of the {@link ArmorStand}.
     */
    private final UUID uniqueId;

    /**
     * The timestamp of when the {@link ArmorStand} was last accessed.
     */
    private long lastAccess;

    /**
     * The label of this {@link ArmorStandHologram}.
     */
    private String label;

    /**
     * This creates a new {@link ArmorStandHologram} for the given {@link UUID}.
     *
     * @param uniqueId The {@link UUID} of the corresponding {@link ArmorStand}
     */
    ArmorStandHologram(@Nonnull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.lastAccess = System.currentTimeMillis();
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
     * This checks if the associated {@link ArmorStand} has despawned.
     *
     * @return Whether the {@link ArmorStand} despawned
     */
    boolean hasDespawned() {
        return getArmorStand() == null;
    }

    /**
     * This returns whether this {@link ArmorStandHologram} has expired.
     * The armorStand will expire if the last access has been more than 10
     * minutes ago.
     *
     * @return Whether this {@link ArmorStandHologram} has expired
     */
    boolean hasExpired() {
        return System.currentTimeMillis() - lastAccess > EXPIRES_AFTER;
    }

    /**
     * This method sets the label of this {@link ArmorStandHologram}.
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

    /**
     * This will remove the {@link ArmorStand} and expire this {@link ArmorStandHologram}.
     */
    void remove() {
        ArmorStand armorStand = getArmorStand();

        if (armorStand != null) {
            lastAccess = 0;
            armorStand.remove();
        }
    }
}
