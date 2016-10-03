package me.reherhold.edifice.eventhandler;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.data.EdificeKeys;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.DiscreteTransform3;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.schematic.Schematic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InteractEntityEventHandler {

    private Edifice plugin;
    // Specifically arranged in clockwise direction
    private static final List<Direction> CARDINAL_SET = Lists.newArrayList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    // All of the BlockTypes for which HeldItemProperty does not exist, mapped
    // to what it should be
    private static final Map<BlockType, ItemType> BLOCK_ITEM_MAP = Maps.newHashMap();

    public InteractEntityEventHandler(Edifice plugin) {
        this.plugin = plugin;
        BLOCK_ITEM_MAP.put(BlockTypes.ACACIA_DOOR, ItemTypes.ACACIA_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.BED, ItemTypes.BED);
        BLOCK_ITEM_MAP.put(BlockTypes.BEETROOTS, ItemTypes.BEETROOT_SEEDS);
        BLOCK_ITEM_MAP.put(BlockTypes.BIRCH_DOOR, ItemTypes.BIRCH_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.BREWING_STAND, ItemTypes.BREWING_STAND);
        BLOCK_ITEM_MAP.put(BlockTypes.CAKE, ItemTypes.CAKE);
        BLOCK_ITEM_MAP.put(BlockTypes.CARROTS, ItemTypes.CARROT);
        BLOCK_ITEM_MAP.put(BlockTypes.CAULDRON, ItemTypes.CAULDRON);
        // TODO No ItemType for cocoa?
//        BLOCK_ITEM_MAP.put(BlockTypes.COCOA, ItemTypes.)
        BLOCK_ITEM_MAP.put(BlockTypes.DARK_OAK_DOOR, ItemTypes.DARK_OAK_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.DAYLIGHT_DETECTOR_INVERTED, ItemTypes.DAYLIGHT_DETECTOR);
        // TODO Not double... needs to be fixed
        BLOCK_ITEM_MAP.put(BlockTypes.DOUBLE_STONE_SLAB, ItemTypes.STONE_SLAB);
        // TODO Not double... needs to be fixed
        BLOCK_ITEM_MAP.put(BlockTypes.DOUBLE_STONE_SLAB2, ItemTypes.STONE_SLAB2);
        // TODO figure out how this works w/ different wood types
        BLOCK_ITEM_MAP.put(BlockTypes.DOUBLE_WOODEN_SLAB, ItemTypes.WOODEN_SLAB);
        // TODO figure out how to deal w/ this
        BLOCK_ITEM_MAP.put(BlockTypes.FIRE, ItemTypes.NONE);
        BLOCK_ITEM_MAP.put(BlockTypes.FLOWER_POT, ItemTypes.FLOWER_POT);
        BLOCK_ITEM_MAP.put(BlockTypes.IRON_DOOR, ItemTypes.IRON_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.JUNGLE_DOOR, ItemTypes.JUNGLE_DOOR);
        // TODO make sure the player gets the bucket back
        BLOCK_ITEM_MAP.put(BlockTypes.LAVA, ItemTypes.LAVA_BUCKET);
        BLOCK_ITEM_MAP.put(BlockTypes.LIT_REDSTONE_LAMP, ItemTypes.REDSTONE_LAMP);
        BLOCK_ITEM_MAP.put(BlockTypes.LIT_REDSTONE_ORE, ItemTypes.REDSTONE_ORE);
        BLOCK_ITEM_MAP.put(BlockTypes.MELON_STEM, ItemTypes.MELON_SEEDS);
        BLOCK_ITEM_MAP.put(BlockTypes.NETHER_WART, ItemTypes.NETHER_WART);
        BLOCK_ITEM_MAP.put(BlockTypes.POTATOES, ItemTypes.POTATO);
        BLOCK_ITEM_MAP.put(BlockTypes.POWERED_COMPARATOR, ItemTypes.COMPARATOR);
        BLOCK_ITEM_MAP.put(BlockTypes.POWERED_REPEATER, ItemTypes.REPEATER);
        BLOCK_ITEM_MAP.put(BlockTypes.PUMPKIN_STEM, ItemTypes.PUMPKIN_SEEDS);
        BLOCK_ITEM_MAP.put(BlockTypes.PURPUR_DOUBLE_SLAB, ItemTypes.PURPUR_BLOCK);
        BLOCK_ITEM_MAP.put(BlockTypes.REDSTONE_WIRE, ItemTypes.REDSTONE);
        BLOCK_ITEM_MAP.put(BlockTypes.REEDS, ItemTypes.REEDS);
        BLOCK_ITEM_MAP.put(BlockTypes.SKULL, ItemTypes.SKULL);
        BLOCK_ITEM_MAP.put(BlockTypes.SPRUCE_DOOR, ItemTypes.SPRUCE_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.STANDING_BANNER, ItemTypes.BANNER);
        BLOCK_ITEM_MAP.put(BlockTypes.STANDING_SIGN, ItemTypes.SIGN);
        BLOCK_ITEM_MAP.put(BlockTypes.TRIPWIRE, ItemTypes.STRING);
        BLOCK_ITEM_MAP.put(BlockTypes.UNLIT_REDSTONE_TORCH, ItemTypes.REDSTONE_TORCH);
        BLOCK_ITEM_MAP.put(BlockTypes.UNPOWERED_COMPARATOR, ItemTypes.COMPARATOR);
        BLOCK_ITEM_MAP.put(BlockTypes.UNPOWERED_REPEATER, ItemTypes.REPEATER);
        BLOCK_ITEM_MAP.put(BlockTypes.WALL_BANNER, ItemTypes.BANNER);
        BLOCK_ITEM_MAP.put(BlockTypes.WALL_SIGN, ItemTypes.SIGN);
        BLOCK_ITEM_MAP.put(BlockTypes.WATER, ItemTypes.WATER_BUCKET);
        BLOCK_ITEM_MAP.put(BlockTypes.WHEAT, ItemTypes.WHEAT_SEEDS);
        BLOCK_ITEM_MAP.put(BlockTypes.WOODEN_DOOR, ItemTypes.WOODEN_DOOR);
    }

    @SuppressWarnings("incomplete-switch")
    @Listener(order = Order.LAST)
    public void interactEntity(InteractEntityEvent.Secondary event, @Root Player player) {
        System.out.println(1);
        if (event.isCancelled()) {
            return;
        }
        System.out.println(2);
        Entity entity = event.getTargetEntity();
        if (!(entity instanceof ItemFrame)) {
            return;
        }
        System.out.println(3);
        ItemFrame itemFrame = (ItemFrame) entity;
        Optional<ItemStackSnapshot> itemStackSnapshotOpt = itemFrame.get(Keys.REPRESENTED_ITEM);
        if (itemStackSnapshotOpt.isPresent()) {
            return;
        }
        System.out.println(4);
        // At this point, there is nothing in the item frame and the event has
        // not been cancelled,
        // so it is almost certain that whatever item that is being placed will
        // end up in the frame

        Optional<ItemStack> itemOpt = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemOpt.isPresent()) {
            return;
        }
        System.out.println(5);
        ItemStack item = itemOpt.get();
        Optional<String> structureIdOpt = item.get(EdificeKeys.BLUEPRINT);
        if (!structureIdOpt.isPresent()) {
            return;
        }
        System.out.println(6);
        Edifice.schematicCache.get(structureIdOpt.get()).thenAcceptAsync((optSchematic) -> {
            if (!optSchematic.isPresent()) {
                return;
            }
            System.out.println(7);
            Schematic schematic = optSchematic.get();

            Location<World> itemFrameLoc = itemFrame.getLocation();
            Direction itemFrameDirection = itemFrame.direction().get();
            // Vector offset to be added to the item frame location to result in
            // the
            // location of the block containing the StructureData
            Vector3i structureBaseOffset = new Vector3i(0, 0, 0);
            // Vector offset to be added to the item frame location to result in
            // the
            // bottom corner of the structure
            Vector3i offset = new Vector3i(0, 0, 0);
            // For each direction, we will put the block in the opposite
            // direction
            // as the item frame, since the way the item frame "faces" is the
            // side
            // away from the block it is on
            switch (itemFrameDirection) {
                case NORTH: // Towards negative z
                    structureBaseOffset = new Vector3i(0, 0, 1);
                    offset = new Vector3i(0, 0, 2);
                    break;
                case EAST: // Towards positive x
                    structureBaseOffset = new Vector3i(-1, 0, 0);
                    offset = new Vector3i(-2, 0, 0);
                    break;
                case SOUTH: // Towards positive z
                    structureBaseOffset = new Vector3i(0, 0, -1);
                    offset = new Vector3i(0, 0, -2);
                    break;
                case WEST: // Towards negative x
                    structureBaseOffset = new Vector3i(1, 0, 0);
                    offset = new Vector3i(2, 0, 0);
                    break;
            }
            Location<World> structureBlock = new Location<World>(itemFrameLoc.getExtent(), itemFrameLoc.getBlockPosition().add(structureBaseOffset));

            if (structureBlock.getBlockType() != BlockTypes.CHEST) {
                return;
            }
            System.out.println(8);
            if (structureBlock.get(EdificeKeys.STRUCTURE).isPresent()) {
                player.sendMessage(Text.of(TextColors.RED, "There is already a structure in progress here!"));
                event.setCancelled(true);
                return;
            }
            System.out.println(9);
            DataView metadata = schematic.getMetadata().getView(DataQuery.of(".")).get();

            // Difference in indices between the opposite of the way the item
            // frame is facing (the intuitive direction) and the structure
            // direction
            int directionIndexDifference = CARDINAL_SET.indexOf(itemFrameDirection.getOpposite())
                    - CARDINAL_SET.indexOf(Direction.valueOf(metadata.getString(DataQuery.of("Direction")).get()));
            int quarterTurns = directionIndexDifference < 0 ? directionIndexDifference + 4 : directionIndexDifference;

            System.out.println("Rotation iterations: " + quarterTurns);

            // TODO check if the area is clear based on config value

            ArchetypeVolume archetypeVolume = schematic;
            if (quarterTurns != 0) {
                System.out.println(10);
                Vector3i size = schematic.getBlockMax().sub(schematic.getBlockMin()).add(Vector3i.ONE);
                if (quarterTurns == 1 || quarterTurns == 3) {
                    size = new Vector3i(size.getZ(), size.getY(), size.getX());
                }
                System.out.println(11);

//                final ArchetypeVolume constructedVolume = Sponge.getRegistry().getExtentBufferFactory().createArchetypeVolume(size);
                System.out.println(12);
                DiscreteTransform3 rotationTransform = DiscreteTransform3.fromRotation(quarterTurns, Axis.Y);
                MutableBlockVolume rotatedVolume = schematic.getBlockView(rotationTransform);
                System.out.println(schematic);
                System.out.println(rotatedVolume);
//                DiscreteTransform3.fromRotation(quarterTurns, axis, point, blockCorner) TODO do this
//                schematic.getRelativeBlockView().getBlockWorker(Cause.source(this).build()).iterate(new BlockVolumeVisitor() {
//
//                    @Override
//                    public void visit(BlockVolume volume, int x, int y, int z) {
//                        try {
//                            System.out.println(x);
//                            System.out.println(y);
//                            System.out.println(z);
//                            Vector3i destination = new Vector3i(z, y, x);
////                            for (int i = 0; i < rotationIterations; i++) {
////                                destination = new Vector3i(destination.getZ(), destination.getY(), -destination.getX());
////                            }
////                            if (rotationIterations == 1 || rotationIterations == 3) {
////                                destination.mul(-1, 1, -1);
////                            }
//
//                            constructedVolume.setBlock(destination, volume.getBlock(x, y, z), Cause.source(this).build());
//                        } catch (Exception e) {
//                            System.out.println(e);
//                            e.printStackTrace(System.out);
//                        }
//
//                    }
//                });
                System.out.println(13);
//                archetypeVolume = constructedVolume;
            }

            final ArchetypeVolume archetypeVolumeRef = archetypeVolume;
            System.out.println("scheduling");

            Vector3i originLocation = itemFrameLoc.getBlockPosition().add(offset);
            Sponge.getScheduler().createTaskBuilder()
                    .execute(() -> archetypeVolumeRef.apply(new Location<World>(itemFrameLoc.getExtent(), originLocation), BlockChangeFlag.ALL,
                            Cause.source(Edifice.getContainer()).build()))
                    .submit(plugin);

//            Map<String, Integer> blockTypeCount = new HashMap<>();
//            archetypeVolume.getBlockWorker(Cause.source(this).build()).iterate(new BlockVolumeVisitor<ArchetypeVolume>() {
//                @Override
//                public void visit(ArchetypeVolume volume, int x, int y, int z) {
//                    BlockState block = volume.getBlock(x, y, z);
//                    
//                }
//            });
//
//            Map<String, List<BlockSnapshot>> deserializedStructureBlocks = new HashMap<String, List<BlockSnapshot>>();
//            // Take all of the blocks and add the origin location to restore
//            // their
//            // locations and world UUID
//            structureJson.getJSONArray(BLOCKS).forEach((obj) -> {
//                JSONObject blockJson = (JSONObject) obj;
//                rotateBlockPosition(blockJson, rotationIterations);
//                restoreBlockLocation(blockJson, originLocation, itemFrameLoc.getExtent().getUniqueId());
//
//                // Deserialize the JSON block into a BlockSnapshot
//                Optional<BlockSnapshot> blockSnapOpt = deserializeBlock(blockJson);
//
//                if (!blockSnapOpt.isPresent()) {
//                    plugin.getLogger().error("There was an error deserializing to BlockSnapshot for structure with ID "
//                            + structureJson.getString(ID));
//                    player.sendMessage(
//                            Text.of(TextColors.RED, "There was an error deserializing the structure. Cannot continue"));
//                    return;
//                }
//                BlockSnapshot block = blockSnapOpt.get();
//
//                block = rotateBlockDirectionData(block, rotationIterations);
//
//                Optional<HeldItemProperty> itemEquivalentOpt = block.getProperty(HeldItemProperty.class);
//                ItemType itemType = null;
//                if (itemEquivalentOpt.isPresent()) {
//                    itemType = itemEquivalentOpt.get().getValue();
//                } else if (BLOCK_ITEM_MAP.containsKey(block.getState().getType())) {
//                    itemType = BLOCK_ITEM_MAP.get(block.getState().getType());
//                } else {
//                    return;
//                }
//                String itemId = itemType.getId();
//                if (deserializedStructureBlocks.containsKey(itemId)) {
//                    deserializedStructureBlocks.get(itemId).add(block);
//                } else {
//                    ArrayList<BlockSnapshot> newList = new ArrayList<BlockSnapshot>();
//                    newList.add(block);
//                    deserializedStructureBlocks.put(itemId, newList);
//                }
//            });
//            Structure structure =
//                    new Structure(structureJson.getString(NAME), UUID.fromString(structureJson.getString(CREATOR_UUID)), player.getUniqueId(),
//                            Direction.valueOf(structureJson
//                                    .getString(DIRECTION)),
//                            deserializedStructureBlocks);
//
//            StructureDataManipulatorBuilder builder =
//                    (StructureDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(StructureData.class).get();
//            StructureData data = builder.createFrom(structure);
//
//            DataTransactionResult result = structureBlock.offer(data);
//            if (!result.isSuccessful()) {
//                player.sendMessage(Text.of(TextColors.RED, "There was an error starting the construction."));
//                return;
//            }
//
//            Text msg1 = Text.of(TextColors.GREEN, "You have started the construction of ", TextColors.GOLD, structure.getName());
//            Text msg2 = Text.of(TextColors.GREEN, ".");
//
//            // If possible, get the creator's name
//            try {
//                GameProfile creatorProfile = Sponge.getServer().getGameProfileManager().get(structure.getCreatorUUID()).get();
//                msg2 = Text.of(TextColors.GREEN, " by ", TextColors.GOLD, creatorProfile.getName().get());
//            } catch (Exception e) {
//            }
//
//            player.sendMessage(Text.of(msg1, msg2));
//            player.sendMessage(Text.of(TextColors.GREEN,
//                    "You can right click the chest to see your progress. To begin, throw the necessary materials near the chest."));
        });
    }

    private BlockSnapshot rotateBlockDirectionData(BlockSnapshot block, int rotationIterations) {
        if (!block.get(Keys.DIRECTION).isPresent()) {
            return block;
        }
        Direction orig = block.get(Keys.DIRECTION).get();
        int index = (CARDINAL_SET.indexOf(orig) + rotationIterations) % CARDINAL_SET.size();
        Direction newDir = CARDINAL_SET.get(index);
        return block.with(Keys.DIRECTION, newDir).get();
    }
}
