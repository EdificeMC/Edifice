package me.reherhold.edifice;

import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.data.EdificeKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

                if (!item.getCreator().isPresent()) {
                    continue;
                }

                UUID ownerUUID = item.getCreator().get();

                // Normally I would use a Priority R-tree, but it is somewhat
                // unpractical to maintain a list of regions surrounding blocks
                // with StructureData
                Vector3i itemBlockPos = item.getLocation().getBlockPosition();
                for (int x = itemBlockPos.getX() - SEARCH_RADIUS; x <= itemBlockPos.getX() + SEARCH_RADIUS; x++) {
                    for (int y = itemBlockPos.getY() - SEARCH_RADIUS; y <= itemBlockPos.getY() + SEARCH_RADIUS; y++) {
                        for (int z = itemBlockPos.getZ() - SEARCH_RADIUS; z <= itemBlockPos.getZ()
                                + SEARCH_RADIUS; z++) {
                            Location<World> loc = new Location<>(item.getLocation().getExtent(), x, y, z);
                            if (!loc.get(EdificeKeys.STRUCTURE).isPresent()) {
                                continue;
                            }
                            Structure structure = loc.get(EdificeKeys.STRUCTURE).get();
                            // Check that the person who threw the items is also
                            // creating the structure
                            if (!structure.getOwnerUUID().equals(ownerUUID)) {
                                continue;
                            }

                            Optional<BlockState> matchingBlockStateOpt = Optional.empty();
                            for (BlockState blockState : structure.getRemainingBlocks().keySet()) {
                                Optional<ItemType> itemTypeOpt = Util.resolveItemType(blockState);
                                if (itemTypeOpt.isPresent() && itemTypeOpt.get().equals(item.getItemType())) {
                                    matchingBlockStateOpt = Optional.of(blockState);
                                    break;
                                }
                            }

                            if (!matchingBlockStateOpt.isPresent()) {
                                continue;
                            }

                            BlockState blockState = matchingBlockStateOpt.get();
                            List<Vector3i> blockPositions = structure.getRemainingBlocks().get(blockState);
                            int itemsFromStackUsed = 0;
                            for (int i = 0; i < itemStack.getCount(); i++) {
                                if (blockPositions.size() > 0) {
                                    Location<World> location = new Location<World>(world, structure.getOrigin().add(blockPositions.get(0)));
                                    location.setBlock(blockState, Cause.source(Edifice.getContainer()).build());
                                    blockPositions.remove(0);
                                    itemsFromStackUsed++;
                                    if (blockPositions.size() == 0) {
                                        structure.getRemainingBlocks().remove(blockState);
                                        break;
                                    }
                                }
                            }

                            int itemsLeft = itemStack.getCount() - itemsFromStackUsed;
                            if (itemsLeft > 0) {
                                ItemStack finalStack = ItemStack.builder().fromSnapshot(itemStack).quantity(itemsLeft)
                                        .build();
                                Entity itemToBeSpawned = item.getLocation().getExtent().createEntity(EntityTypes.ITEM,
                                        item.getLocation().getPosition());
                                itemToBeSpawned.offer(Keys.REPRESENTED_ITEM, finalStack.createSnapshot());
                                item.getLocation().getExtent().spawnEntity(itemToBeSpawned,
                                        Cause.source(EntitySpawnCause.builder().entity(itemToBeSpawned)
                                                .type(SpawnTypes.PLUGIN).build()).build());
                            }
                            item.remove();

                            if (structure.getRemainingBlocks().keySet().isEmpty()) {
                                // If there are no more blocks to be placed,
                                // remove the StructureData
                                loc.remove(EdificeKeys.STRUCTURE);
                            } else {
                                loc.offer(EdificeKeys.STRUCTURE, structure);
                            }
                        }
                    }
                }
            }
        }
    }
}
