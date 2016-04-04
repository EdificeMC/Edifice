package me.reherhold.edifice;

import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.data.EdificeKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

public class WatchItemsRunnable implements Runnable {

    private static final int SEARCH_RADIUS = 1;

    @Override
    public void run() {
        for (World world : Sponge.getServer().getWorlds()) {
            if (!world.isLoaded()) {
                continue;
            }
            for (Entity e : world.getEntities((entity) -> {
                return entity instanceof Item;
            })) {
                Item item = (Item) e;
                ItemStackSnapshot itemStack = item.item().get();
                Optional<BlockType> blockTypeOpt = itemStack.getType().getBlock();
                if (!blockTypeOpt.isPresent()) {
                    continue;
                }

                // Normally I would use a Priority R-tree, but it is somewhat
                // unpractical to maintain a list of regions surrounding blocks
                // with StructureData
                Vector3i itemBlockPos = item.getLocation().getBlockPosition();
                for (int x = itemBlockPos.getX() - SEARCH_RADIUS; x <= itemBlockPos.getX() + SEARCH_RADIUS; x++) {
                    for (int y = itemBlockPos.getY() - SEARCH_RADIUS; y <= itemBlockPos.getY() + SEARCH_RADIUS; y++) {
                        for (int z = itemBlockPos.getZ() - SEARCH_RADIUS; z <= itemBlockPos.getZ() + SEARCH_RADIUS; z++) {
                            Location<World> loc = new Location<World>(item.getLocation().getExtent(), x, y, z);
                            if (!loc.get(EdificeKeys.STRUCTURE).isPresent()) {
                                continue;
                            }
                            Structure structure = loc.get(EdificeKeys.STRUCTURE).get();
                            String itemName = itemStack.getType().getTranslation().get().replace('.', '-');
                            if (!structure.getRemainingBlocks().containsKey(itemName)) {
                                continue;
                            }
                            List<BlockSnapshot> blocks = structure.getRemainingBlocks().get(itemName);
                            int itemsFromStackUsed = 0;
                            for (int i = 0; i < itemStack.getCount(); i++) {
                                if (blocks.size() > 0) {
                                    BlockSnapshot block = blocks.get(0);
                                    block.restore(true, false);
                                    blocks.remove(block);
                                    itemsFromStackUsed++;
                                } else {
                                    structure.getRemainingBlocks().remove(itemName);
                                    break;
                                }
                            }
                            loc.offer(EdificeKeys.STRUCTURE, structure);
                            int itemsLeft = itemStack.getCount() - itemsFromStackUsed;
                            if (itemsLeft > 0) {
                                ItemStack finalStack = ItemStack.builder().fromSnapshot(itemStack).quantity(itemsLeft).build();
                                item.getLocation().getExtent().createEntity(finalStack.toContainer(), item.getLocation().getPosition());
                            }
                            item.remove();
                        }
                    }
                }
            }
        }
    }
}
