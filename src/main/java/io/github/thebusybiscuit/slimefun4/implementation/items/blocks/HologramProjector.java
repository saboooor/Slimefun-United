package io.github.thebusybiscuit.slimefun4.implementation.items.blocks;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.bakedlibs.dough.common.ChatColors;
import io.github.bakedlibs.dough.items.ItemStackFactory;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.HologramOwner;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler;
import io.github.thebusybiscuit.slimefun4.core.services.holograms.ArmorStandHologramsService;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.NumberUtils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * The {@link HologramProjector} is a very simple block which allows the {@link Player}
 * to create a floating text that is completely configurable.
 *
 * @author TheBusyBiscuit
 * @author Kry-Vosa
 * @author SoSeDiK
 *
 * @see HologramOwner
 * @see ArmorStandHologramsService
 *
 */
public class HologramProjector extends SlimefunItem implements HologramOwner {

    private static final String OFFSET_PARAMETER = "offset";

    @ParametersAreNonnullByDefault
    public HologramProjector(
            ItemGroup itemGroup,
            SlimefunItemStack item,
            RecipeType recipeType,
            ItemStack[] recipe,
            ItemStack recipeOutput) {
        super(itemGroup, item, recipeType, recipe, recipeOutput);

        addItemHandler(onPlace(), onRightClick(), onBreak());
    }

    private @Nonnull BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(false) {

            @Override
            public void onPlayerPlace(BlockPlaceEvent e) {
                Block b = e.getBlockPlaced();
                var blockData = StorageCacheUtils.getBlock(b.getLocation());
                blockData.setData("text", "Use the projector to edit this text");
                blockData.setData(OFFSET_PARAMETER, "0.5");
                blockData.setData("owner", e.getPlayer().getUniqueId().toString());

                updateHologram(b, null);
            }
        };
    }

    private @Nonnull BlockBreakHandler onBreak() {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                removeHologram(b);
            }
        };
    }

    public @Nonnull BlockUseHandler onRightClick() {
        return e -> {
            e.cancel();

            var p = e.getPlayer();
            var b = e.getClickedBlock().get();
            var data = StorageCacheUtils.getBlock(b.getLocation());

            if (data != null && !data.isDataLoaded()) {
                StorageCacheUtils.requestLoad(data);
                return;
            }

            if (p.getUniqueId().toString().equals(StorageCacheUtils.getData(b.getLocation(), "owner"))) {
                openEditor(p, b);
            }
        };
    }

    private void openEditor(@Nonnull Player p, @Nonnull Block projector) {
        ChestMenu menu =
                new ChestMenu(Slimefun.getLocalization().getMessage(p, "machines.HOLOGRAM_PROJECTOR.inventory-title"));

        menu.addItem(
                0,
        ItemStackFactory.create(
            Material.NAME_TAG,
            "&7Displayed Text &e(Click to edit)",
            "",
            "&f" + ChatColors.color(StorageCacheUtils.getData(projector.getLocation(), "text"))));
        menu.addMenuClickHandler(0, (pl, slot, item, action) -> {
            pl.closeInventory();
            Slimefun.getLocalization().sendMessage(pl, "machines.HOLOGRAM_PROJECTOR.enter-text", true);

            ChatUtils.awaitInput(pl, message -> {
                // Fixes #3445 - Make sure the projector is not broken
                if (!StorageCacheUtils.isBlock(projector.getLocation(), getId())) {
                    // Hologram projector no longer exists.
                    // TODO: Add a chat message informing the player that their message was ignored.
                    return;
                }

                updateHologram(projector, ChatColors.color(message));
                StorageCacheUtils.setData(projector.getLocation(), "text", message);
                openEditor(pl, projector);
            });

            return false;
        });

        menu.addItem(
                1,
                ItemStackFactory.create(
                        Material.CLOCK,
            "&7Height: &e"
                                + NumberUtils.reparseDouble(Double.parseDouble(
                                                StorageCacheUtils.getData(projector.getLocation(), OFFSET_PARAMETER))
                                        + 1.0D),
                        "",
            "&fLeft Click: &7+0.1",
            "&fRight Click: &7-0.1"));
        menu.addMenuClickHandler(1, (pl, slot, item, action) -> {
            var blockData = StorageCacheUtils.getBlock(projector.getLocation());
            double offset = NumberUtils.reparseDouble(
                    Double.parseDouble(blockData.getData(OFFSET_PARAMETER)) + (action.isRightClicked() ? -0.1F : 0.1F));

            updateHologramOffset(projector, offset);

            openEditor(pl, projector);
            return false;
        });

        menu.open(p);
    }

    public void updateHologram(@Nonnull Block projector, @Nullable String text) {
        Location loc = projector.getLocation();
        var blockData = StorageCacheUtils.getBlock(loc);
        String blockDataText = blockData.getData("text");
        Vector offset = new Vector(0.5, Double.parseDouble(blockData.getData(OFFSET_PARAMETER)), 0.5);

        Slimefun.getHologramsService()
                .setHologramLabel(loc.add(offset), ChatColors.color(text != null ? text : blockDataText));
    }

    public void updateHologramOffset(@Nonnull Block projector, double newOffset) {
        Location loc = projector.getLocation();
        var blockData = StorageCacheUtils.getBlock(loc);
        Vector offsetVector = new Vector(0.5, Double.parseDouble(blockData.getData(OFFSET_PARAMETER)), 0.5);
        Vector newOffsetVector = new Vector(0.5, newOffset, 0.5);
        blockData.setData(OFFSET_PARAMETER, String.valueOf(newOffset));

        Slimefun.getHologramsService().setHologramLocation(loc.add(offsetVector), loc.add(newOffsetVector));
    }
}
