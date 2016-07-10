package me.reherhold.edifice.eventhandler;

import static me.reherhold.edifice.StructureJSONKeys.BLOCKS;
import static me.reherhold.edifice.StructureJSONKeys.CREATOR_UUID;
import static me.reherhold.edifice.StructureJSONKeys.DIRECTION;
import static me.reherhold.edifice.StructureJSONKeys.HEIGHT;
import static me.reherhold.edifice.StructureJSONKeys.ID;
import static me.reherhold.edifice.StructureJSONKeys.LENGTH;
import static me.reherhold.edifice.StructureJSONKeys.NAME;
import static me.reherhold.edifice.StructureJSONKeys.POSITION;
import static me.reherhold.edifice.StructureJSONKeys.POSITION_X;
import static me.reherhold.edifice.StructureJSONKeys.POSITION_Y;
import static me.reherhold.edifice.StructureJSONKeys.POSITION_Z;
import static me.reherhold.edifice.StructureJSONKeys.WIDTH;
import static me.reherhold.edifice.StructureJSONKeys.WORLD_UUID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.HeldItemProperty;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.Structure;
import me.reherhold.edifice.data.EdificeKeys;
import me.reherhold.edifice.data.structure.StructureData;
import me.reherhold.edifice.data.structure.StructureDataManipulatorBuilder;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.json.JSONConfigurationLoader;

public class InteractEntityEventHandler {

    private Edifice plugin;
    // Specifically arranged in clockwise direction
    private static final List<Direction> CARDINAL_SET = Lists.newArrayList(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    // All of the BlockTypes for which HeldItemProperty does not exist, mapped to what it should be
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
//        BLOCK_ITEM_MAP.put(BlockTypes.COCOA, ItemTypes.) // TODO No ItemType for cocoa?
        BLOCK_ITEM_MAP.put(BlockTypes.DARK_OAK_DOOR, ItemTypes.DARK_OAK_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.DAYLIGHT_DETECTOR_INVERTED, ItemTypes.DAYLIGHT_DETECTOR);
        BLOCK_ITEM_MAP.put(BlockTypes.DOUBLE_STONE_SLAB, ItemTypes.STONE_SLAB); // TODO Not double... needs to be fixed
        BLOCK_ITEM_MAP.put(BlockTypes.DOUBLE_STONE_SLAB2, ItemTypes.STONE_SLAB2); // TODO Not double... needs to be fixed
        BLOCK_ITEM_MAP.put(BlockTypes.DOUBLE_WOODEN_SLAB, ItemTypes.WOODEN_SLAB); // TODO figure out how this works w/ different wood types
        BLOCK_ITEM_MAP.put(BlockTypes.FIRE, ItemTypes.NONE); // TODO figure out how to deal w/ this
        BLOCK_ITEM_MAP.put(BlockTypes.FLOWER_POT, ItemTypes.FLOWER_POT);
        BLOCK_ITEM_MAP.put(BlockTypes.IRON_DOOR, ItemTypes.IRON_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.JUNGLE_DOOR, ItemTypes.JUNGLE_DOOR);
        BLOCK_ITEM_MAP.put(BlockTypes.LAVA, ItemTypes.LAVA_BUCKET); // TODO make sure the player gets the bucket back
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
        // not been cancelled,
        // so it is almost certain that whatever item that is being placed will
        // end up in the frame

        Optional<ItemStack> itemOpt = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemOpt.isPresent()) {
            return;
        }
        ItemStack item = itemOpt.get();
        Optional<String> blueprintDataOpt = item.get(EdificeKeys.BLUEPRINT);
        if (!blueprintDataOpt.isPresent()) {
            return;
        }
        Edifice.structureCache.getById(blueprintDataOpt.get()).thenAcceptAsync((optStructure) -> {
        	if(!optStructure.isPresent()) {
        		return;
        	}
        	JSONObject structureJson = optStructure.get();
        	
        	int width = structureJson.getInt(WIDTH);
            int length = structureJson.getInt(LENGTH);
            int height = structureJson.getInt(HEIGHT);
            Location<World> itemFrameLoc = itemFrame.getLocation();
            Direction itemFrameDirection = itemFrame.direction().get();
            // Vector offset to be added to the item frame location to result in the
            // location of the block containing the StructureData
            Vector3i structureBaseOffset = new Vector3i(0, 0, 0);
            // Vector offset to be added to the item frame location to result in the
            // bottom corner of the structure
            Vector3i offset = new Vector3i(0, 0, 0);
            // For each direction, we will put the block in the opposite direction
            // as the item frame, since the way the item frame "faces" is the side
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

            if (structureBlock.get(EdificeKeys.STRUCTURE).isPresent()) {
                player.sendMessage(Text.of(TextColors.RED, "There is already a structure in progress here!"));
                event.setCancelled(true);
                return;
            }

            // Difference in indices between the opposite of the way the item frame is facing (the intuitive direction) and the structure direction
            int directionIndexDifference = CARDINAL_SET.indexOf(itemFrameDirection.getOpposite()) - CARDINAL_SET.indexOf(Direction.valueOf(structureJson.getString(DIRECTION)));
            int rotationIterations = directionIndexDifference < 0 ? directionIndexDifference + 4 : directionIndexDifference;

            Vector3i originLocation = itemFrameLoc.getBlockPosition().add(offset);

            // TODO check if the area is clear based on config value

            Map<String, List<BlockSnapshot>> deserializedStructureBlocks = new HashMap<String, List<BlockSnapshot>>();
            // Take all of the blocks and add the origin location to restore their
            // locations and world UUID
            structureJson.getJSONArray(BLOCKS).forEach((obj) -> {
                JSONObject blockJson = (JSONObject) obj;
                rotateBlockPosition(blockJson, rotationIterations);
                restoreBlockLocation(blockJson, originLocation, itemFrameLoc.getExtent().getUniqueId());
                
                System.out.println(blockJson.toString());
                
                // Deserialize the JSON block into a BlockSnapshot
                Optional<BlockSnapshot> blockSnapOpt = deserializeBlock(blockJson);
                
				if (!blockSnapOpt.isPresent()) {
					plugin.getLogger().error("There was an error deserializing to BlockSnapshot for structure with ID "
							+ structureJson.getString(ID));
					player.sendMessage(
							Text.of(TextColors.RED, "There was an error deserializing the structure. Cannot continue"));
					return;
				}
				BlockSnapshot block = blockSnapOpt.get();
				
				System.out.println("Deserialized block");
				System.out.println(block.toString());
				
				block = rotateBlockDirectionData(block, rotationIterations);
				
				System.out.println("Rotated block");
				System.out.println(block.toString());
				
				Optional<HeldItemProperty> itemEquivalentOpt = block.getProperty(HeldItemProperty.class);
				ItemType itemType = null;
				if (itemEquivalentOpt.isPresent()) {
					itemType = itemEquivalentOpt.get().getValue();
				} else if (BLOCK_ITEM_MAP.containsKey(block.getState().getType())) {
					itemType = BLOCK_ITEM_MAP.get(block.getState().getType());
				} else {
					return;
				}
				String itemId = itemType.getId();
				if (deserializedStructureBlocks.containsKey(itemId)) {
					deserializedStructureBlocks.get(itemId).add(block);
				} else {
					ArrayList<BlockSnapshot> newList = new ArrayList<BlockSnapshot>();
					newList.add(block);
					deserializedStructureBlocks.put(itemId, newList);
                }
            });
            System.out.println("Done");
            Structure structure =
                    new Structure(structureJson.getString(NAME), UUID.fromString(structureJson.getString(CREATOR_UUID)), player.getUniqueId(),
                            Direction.valueOf(structureJson
                                    .getString(DIRECTION)), deserializedStructureBlocks);

            StructureDataManipulatorBuilder builder =
                    (StructureDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(StructureData.class).get();
            StructureData data = builder.createFrom(structure);

            System.out.println(1);
            DataTransactionResult result = structureBlock.offer(data);
            if (!result.isSuccessful()) {
                player.sendMessage(Text.of(TextColors.RED, "There was an error starting the construction."));
                return;
            }

            System.out.println(2);
            Text msg1 = Text.of(TextColors.GREEN, "You have started the construction of ", TextColors.GOLD, structure.getName());
            Text msg2 = Text.of(TextColors.GREEN, ".");

            // If possible, get the creator's name
            try {
                GameProfile creatorProfile = Sponge.getServer().getGameProfileManager().get(structure.getCreatorUUID()).get();
                msg2 = Text.of(TextColors.GREEN, " by ", TextColors.GOLD, creatorProfile.getName().get());
            } catch (Exception e) {
            }

            System.out.println(3);
            player.sendMessage(Text.of(msg1, msg2));
            player.sendMessage(Text.of(TextColors.GREEN,
                    "You can right click the chest to see your progress. To begin, throw the necessary materials near the chest."));
        });
    }

    private void rotateBlockPosition(JSONObject block, int iterations) {
        JSONObject pos = block.getJSONObject(POSITION);
        for (int i = 0; i < iterations; i++) {
            int tmpX = pos.getInt(POSITION_X);
            pos.put(POSITION_X, pos.getInt(POSITION_Z));
            pos.put(POSITION_Z, -tmpX);
        }
        if (iterations == 1 || iterations == 3) {
            pos.put(POSITION_Z, -pos.getInt(POSITION_Z));
            pos.put(POSITION_X, -pos.getInt(POSITION_X));
        }

    }
    
    private void restoreBlockLocation(JSONObject block, Vector3i origin, UUID worldUUID) {
    	JSONObject pos = block.getJSONObject(POSITION);
        pos.put(POSITION_X, pos.getInt(POSITION_X) + origin.getX());
        pos.put(POSITION_Y, pos.getInt(POSITION_Y) + origin.getY());
        pos.put(POSITION_Z, pos.getInt(POSITION_Z) + origin.getZ());
        block.put(WORLD_UUID, worldUUID.toString());
    }
    
    private Optional<BlockSnapshot> deserializeBlock(JSONObject blockJson) {
    	BufferedReader reader = new BufferedReader(new StringReader(blockJson.toString()));
        JSONConfigurationLoader loader = JSONConfigurationLoader.builder().setSource(() -> {
            return reader;
        }).build();
        ConfigurationNode node = null;
        try {
            node = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Sponge.getDataManager().deserialize(BlockSnapshot.class, ConfigurateTranslator.instance().translateFrom(node));
    }
    
    private BlockSnapshot rotateBlockDirectionData(BlockSnapshot block, int rotationIterations) {
    	if(!block.get(Keys.DIRECTION).isPresent()) {
    		System.out.println("No direction");
    		return block;
    	}
    	Direction orig = block.get(Keys.DIRECTION).get();
    	System.out.println("Original");
    	System.out.println(orig);
		int index = -1;
		if (CARDINAL_SET.indexOf(orig) + rotationIterations >= 4) {
			index = CARDINAL_SET.size() - CARDINAL_SET.indexOf(orig) - rotationIterations;
		} else {
			index = CARDINAL_SET.indexOf(orig) + rotationIterations;
		}
		System.out.println("Index");
		System.out.println(index);
		Direction newDir = CARDINAL_SET.get(index);
		System.out.println("newDir");
		System.out.println(newDir);
		return block.with(Keys.DIRECTION, newDir).get();
	}
}
