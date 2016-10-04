package me.reherhold.edifice.eventhandler;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.Structure;
import me.reherhold.edifice.data.EdificeKeys;
import me.reherhold.edifice.data.structure.StructureData;
import me.reherhold.edifice.data.structure.StructureDataManipulatorBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.BlockVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeVisitor;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InteractEntityEventHandler {

    private Edifice plugin;
    // Specifically arranged in clockwise direction
    private static final List<Direction> CARDINAL_SET = Lists.newArrayList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    public InteractEntityEventHandler(Edifice plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("incomplete-switch")
    @Listener(order = Order.LAST)
    public void interactEntity(InteractEntityEvent.Secondary event, @Root Player player) {
        if (event.isCancelled()) {
            return;
        }
        Entity entity = event.getTargetEntity();
        if (!(entity instanceof ItemFrame)) {
            return;
        }
        ItemFrame itemFrame = (ItemFrame) entity;
        Optional<ItemStackSnapshot> itemStackSnapshotOpt = itemFrame.get(Keys.REPRESENTED_ITEM);
        if (itemStackSnapshotOpt.isPresent()) {
            return;
        }
        // At this point, there is nothing in the item frame and the event has
        // not been cancelled, so it is almost certain that whatever item that
        // is being placed will end up in the frame

        Optional<ItemStack> itemOpt = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemOpt.isPresent()) {
            return;
        }
        ItemStack item = itemOpt.get();
        Optional<String> structureIdOpt = item.get(EdificeKeys.BLUEPRINT);
        if (!structureIdOpt.isPresent()) {
            return;
        }
        Edifice.schematicCache.get(structureIdOpt.get()).thenAcceptAsync((optSchematic) -> {
            if (!optSchematic.isPresent()) {
                return;
            }
            Schematic schematic = optSchematic.get();

            Location<World> itemFrameLoc = itemFrame.getLocation();
            Direction itemFrameDirection = itemFrame.direction().get();
            // Vector offset to be added to the item frame location to result in
            // the location of the block containing the StructureData
            Vector3i chestTranslation = new Vector3i(0, 0, 0);
            // Vector offset to be added to the item frame location to result in
            // the bottom corner of the structure
            Vector3i structureOriginTranslation = new Vector3i(0, 0, 0);
            // For each direction, we will put the block in the opposite
            // direction as the item frame, since the way the item frame "faces"
            // is the side away from the block it is on
            switch (itemFrameDirection) {
                case NORTH: // Towards negative z
                    chestTranslation = new Vector3i(0, 0, 1);
                    structureOriginTranslation = new Vector3i(0, 0, 2);
                    break;
                case EAST: // Towards positive x
                    chestTranslation = new Vector3i(-1, 0, 0);
                    structureOriginTranslation = new Vector3i(-2, 0, 0);
                    break;
                case SOUTH: // Towards positive z
                    chestTranslation = new Vector3i(0, 0, -1);
                    structureOriginTranslation = new Vector3i(0, 0, -2);
                    break;
                case WEST: // Towards negative x
                    chestTranslation = new Vector3i(1, 0, 0);
                    structureOriginTranslation = new Vector3i(2, 0, 0);
                    break;
            }
            Location<World> structureBlock = new Location<World>(itemFrameLoc.getExtent(), itemFrameLoc.getBlockPosition().add(chestTranslation));

            if (structureBlock.getBlockType() != BlockTypes.CHEST) {
                return;
            }
            if (structureBlock.get(EdificeKeys.STRUCTURE).isPresent()) {
                player.sendMessage(Text.of(TextColors.RED, "There is already a structure in progress here!"));
                event.setCancelled(true);
                return;
            }
            DataView metadata = schematic.getMetadata().getView(DataQuery.of(".")).get();

            // Difference in indices between the opposite of the way the item
            // frame is facing (the intuitive direction) and the structure
            // direction
            int directionIndexDifference = CARDINAL_SET.indexOf(Direction.valueOf(metadata.getString(DataQuery.of("Direction")).get()))
                    - CARDINAL_SET.indexOf(itemFrameDirection.getOpposite());
            int quarterTurns = directionIndexDifference < 0 ? directionIndexDifference + 4 : directionIndexDifference;

            // TODO check if the area is clear based on config value

            MutableBlockVolume volume = schematic;
            if (quarterTurns != 0) {
                DiscreteTransform3 rotationTransform = DiscreteTransform3.fromRotation(quarterTurns, Axis.Y);
                volume = schematic.getBlockView(rotationTransform);
            }
            Vector3i size = volume.getBlockMax().sub(volume.getBlockMin()).add(Vector3i.ONE);
            switch (itemFrameDirection.getOpposite()) {
                case NORTH:
                    structureOriginTranslation = structureOriginTranslation.add(0, 0, -size.getZ() + 1);
                    break;
                case EAST:
                    structureOriginTranslation = structureOriginTranslation.add(0, 0, 0);
                    break;
                case SOUTH:
                    structureOriginTranslation = structureOriginTranslation.add(-size.getX() + 1, 0, 0);
                    break;
                case WEST:
                    structureOriginTranslation = structureOriginTranslation.add(-size.getX() + 1, 0, -size.getZ() + 1);
                    break;
            }

            final boolean buildInstantly = player.get(Keys.GAME_MODE).get().equals(GameModes.CREATIVE);

            Map<BlockState, List<Vector3i>> structureBlocks = new HashMap<>();
            final ArchetypeVolume archetypeVolume = Sponge.getRegistry().getExtentBufferFactory().createArchetypeVolume(size);
            volume.getRelativeBlockView().getBlockWorker(Cause.source(this).build()).iterate(new BlockVolumeVisitor() {

                @Override
                public void visit(BlockVolume volume, int x, int y, int z) {
                    Vector3i pos = new Vector3i(x, y, z);
                    BlockState block = volume.getBlock(pos);
                    archetypeVolume.setBlock(pos, block, Cause.source(this).build());

                    if (!buildInstantly) {
                        if (block.getType().equals(BlockTypes.AIR)) {
                            return;
                        }

                        if (structureBlocks.containsKey(block)) {
                            ((List<Vector3i>) structureBlocks.get(block)).add(pos);
                        } else {
                            structureBlocks.put(block, Lists.newArrayList(pos));
                        }
                    }
                }
            });

            Vector3i originLocation = itemFrameLoc.getBlockPosition().add(structureOriginTranslation);

            if (buildInstantly) {
                Sponge.getScheduler().createTaskBuilder()
                        .execute(() -> archetypeVolume.apply(new Location<World>(itemFrameLoc.getExtent(), originLocation), BlockChangeFlag.ALL,
                                Cause.source(Edifice.getContainer()).build()))
                        .submit(plugin);
                return;
            }

            Structure structure =
                    new Structure(metadata.getString(DataQuery.of(Schematic.METADATA_NAME)).get(),
                            UUID.fromString(metadata.getString(DataQuery.of(Schematic.METADATA_AUTHOR)).get()), player.getUniqueId(),
                            structureBlocks);

            StructureDataManipulatorBuilder builder =
                    (StructureDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(StructureData.class).get();
            StructureData data = builder.createFrom(structure);

            DataTransactionResult result = structureBlock.offer(data);
            if (!result.isSuccessful()) {
                player.sendMessage(Text.of(TextColors.RED, "There was an error starting the construction."));
                return;
            }

            Text msg1 = Text.of(TextColors.GREEN, "You have started the construction of ", TextColors.GOLD, structure.getName());
            Text msg2 = Text.of(TextColors.GREEN, ".");

            // If possible, get the creator's name
            try {
                GameProfile creatorProfile = Sponge.getServer().getGameProfileManager().get(structure.getCreatorUUID()).get();
                msg2 = Text.of(TextColors.GREEN, " by ", TextColors.GOLD, creatorProfile.getName().get());
            } catch (Exception e) {
            }

            player.sendMessage(Text.of(msg1, msg2));
            player.sendMessage(Text.of(TextColors.GREEN,
                    "You can right click the chest to see your progress. To begin, throw the necessary materials near the chest."));
        });
    }

}
