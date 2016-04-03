package me.reherhold.edifice.eventhandler;

import com.google.common.collect.Lists;

import static me.reherhold.edifice.StructureJSONKeys.BLOCKS;
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
import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.Structure;
import me.reherhold.edifice.data.EdificeKeys;
import me.reherhold.edifice.data.structure.StructureData;
import me.reherhold.edifice.data.structure.StructureDataManipulatorBuilder;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InteractEntityEventHandler {

    private Edifice plugin;

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
        // not been cancelled,
        // so it is almost certain that whatever item that is being placed will
        // end up in the frame

        Optional<ItemStack> itemOpt = player.getItemInHand();
        if (!itemOpt.isPresent()) {
            return;
        }
        ItemStack item = itemOpt.get();
        Optional<String> blueprintDataOpt = item.get(EdificeKeys.BLUEPRINT);
        if (!blueprintDataOpt.isPresent()) {
            return;
        }
        JSONObject structureJson = new JSONObject(blueprintDataOpt.get());
        int width = structureJson.getInt(WIDTH);
        int length = structureJson.getInt(LENGTH);
        int height = structureJson.getInt(HEIGHT);
        Location<World> itemFrameLoc = itemFrame.getLocation();
        Direction direction = itemFrame.direction().get();
        // Vector offset to be added to the item frame location to result in the
        // location of the block containing the StructureData
        Vector3i structureBaseOffset = new Vector3i(0, 0, 0);
        // Vector offset to be added to the item frame location to result in the
        // bottom corner of the structure
        Vector3i offset = new Vector3i(0, 0, 0);
        // Vector offset to be added to the bottom corner to result in the
        // top opposite corner of the structure
        Vector3i endingLocationOffset = new Vector3i(0, 0, 0);
        // For each direction, we will put the block in the opposite direction
        // as the item frame, since the way the item frame "faces" is the side
        // away from the block it is on
        switch (direction) {
            case NORTH: // Towards negative z
                structureBaseOffset = new Vector3i(0, 0, 1);
                offset = new Vector3i(0, 0, 2);
                endingLocationOffset = new Vector3i(-width, height, length);
                break;
            case EAST: // Towards positive x
                structureBaseOffset = new Vector3i(-1, 0, 0);
                offset = new Vector3i(-2, 0, 0);
                endingLocationOffset = new Vector3i(-width, height, -length);
                break;
            case SOUTH: // Towards positive z
                structureBaseOffset = new Vector3i(0, 0, -1);
                offset = new Vector3i(0, 0, -2);
                endingLocationOffset = new Vector3i(width, height, -length);
                break;
            case WEST: // Towards negative x
                structureBaseOffset = new Vector3i(1, 0, 0);
                offset = new Vector3i(2, 0, 0);
                endingLocationOffset = new Vector3i(width, height, length);
                break;
        }
        Location<World> structureBlock = new Location<World>(itemFrameLoc.getExtent(), itemFrameLoc.getBlockPosition().add(structureBaseOffset));

        if (structureBlock.getBlockType() != BlockTypes.CHEST) {
            return;
        }
        
        if(structureBlock.get(EdificeKeys.STRUCTURE).isPresent()) {
            player.sendMessage(Text.of(TextColors.RED, "There is already a structure in progress here!"));
            return;
        }

        Vector3i originLocation = itemFrameLoc.getBlockPosition().add(offset);
        Vector3i endLocation = originLocation.add(endingLocationOffset);

        // TODO check if the area is clear based on config value

        List<BlockSnapshot> deserializedStructureBlocks = new ArrayList<BlockSnapshot>();
        // Take all of the blocks and add the origin location to restore their
        // locations and world UUID
        structureJson.getJSONArray(BLOCKS).forEach((obj) -> {
            JSONObject block = (JSONObject) obj;
            JSONObject pos = block.getJSONObject(POSITION);
            pos.put(POSITION_X, pos.getInt(POSITION_X) + originLocation.getX());
            pos.put(POSITION_Y, pos.getInt(POSITION_Y) + originLocation.getY());
            pos.put(POSITION_Z, pos.getInt(POSITION_Z) + originLocation.getZ());
            block.put(WORLD_UUID, itemFrameLoc.getExtent().getUniqueId().toString());

            // Deserialize the JSON block into a BlockSnapshot
                BufferedReader reader = new BufferedReader(new StringReader(block.toString()));
                JSONConfigurationLoader loader = JSONConfigurationLoader.builder().setSource(() -> {
                    return reader;
                }).build();
                ConfigurationNode node = null;
                try {
                    node = loader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Optional<BlockSnapshot> blockSnapOpt =
                        Sponge.getDataManager().deserialize(BlockSnapshot.class, ConfigurateTranslator.instance().translateFrom(node));
                if (!blockSnapOpt.isPresent()) {
                    plugin.getLogger()
                            .error("There was an error deserializing to BlockSnapshot for structure with ID " + structureJson.getString(ID));
                    player.sendMessage(Text.of(TextColors.RED, "There was an error deserializing the structure. Cannot continue"));
                    return;
                }
//                deserializedStructureBlocks.add(blockSnapOpt.get());
            });
        Structure structure = new Structure(structureJson.getString(NAME), deserializedStructureBlocks);

        StructureDataManipulatorBuilder builder =
                (StructureDataManipulatorBuilder) Sponge.getDataManager().getManipulatorBuilder(StructureData.class).get();
        StructureData data = builder.createFrom(structure);

        DataTransactionResult result = structureBlock.offer(data);
        if (!result.isSuccessful()) {
            player.sendMessage(Text.of(TextColors.RED, "There was an error starting the construction."));
            return;
        }

        player.sendMessage(Text.of(TextColors.GREEN, "You have started the construction of ", TextColors.GOLD, structure.getName(), TextColors.GREEN,
                "."));
        player.sendMessage(Text.of(TextColors.GREEN,
                "You can right click the crafting table to see your progress. To begin, throw the necessary materials near the crafting table."));

        new Location<World>(itemFrameLoc.getExtent(), originLocation).setBlockType(BlockTypes.GOLD_BLOCK);
        new Location<World>(itemFrameLoc.getExtent(), endLocation).setBlockType(BlockTypes.DIAMOND_BLOCK);
    }
}
