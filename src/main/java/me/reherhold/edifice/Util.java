package me.reherhold.edifice;

import com.google.common.collect.Maps;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.property.block.HeldItemProperty;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;

import java.util.Map;
import java.util.Optional;

public class Util {

    // All of the BlockTypes for which HeldItemProperty does not exist, mapped
    // to what it should be
    private static final Map<BlockType, ItemType> BLOCK_ITEM_MAP = Maps.newHashMap();

    static {
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

    public static Optional<ItemType> resolveItemType(BlockState block) {
        Optional<HeldItemProperty> itemEquivalentOpt = block.getProperty(HeldItemProperty.class);
        if (itemEquivalentOpt.isPresent()) {
            return Optional.of(itemEquivalentOpt.get().getValue());
        } else if (BLOCK_ITEM_MAP.containsKey(block.getType())) {
            return Optional.of(BLOCK_ITEM_MAP.get(block.getType()));
        } else {
            return Optional.empty();
        }
    }

}
