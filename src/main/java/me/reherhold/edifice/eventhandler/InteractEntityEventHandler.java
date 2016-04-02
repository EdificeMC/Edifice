package me.reherhold.edifice.eventhandler;

import static me.reherhold.edifice.StructureJSONKeys.HEIGHT;
import static me.reherhold.edifice.StructureJSONKeys.LENGTH;
import static me.reherhold.edifice.StructureJSONKeys.WIDTH;

import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.data.EdificeKeys;
import org.json.JSONObject;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
        JSONObject structure = new JSONObject(blueprintDataOpt.get());
        int width = structure.getInt(WIDTH);
        int length = structure.getInt(LENGTH);
        int height = structure.getInt(HEIGHT);
        Location<World> itemFrameLoc = itemFrame.getLocation();
        Direction direction = itemFrame.direction().get();
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
                offset = new Vector3i(0, 0, 2);
                endingLocationOffset = new Vector3i(-width, height, length);
                break;
            case EAST: // Towards positive x
                offset = new Vector3i(-2, 0, 0);
                endingLocationOffset = new Vector3i(-width, height, -length);
                break;
            case SOUTH: // Towards positive z
                offset = new Vector3i(0, 0, -2);
                endingLocationOffset = new Vector3i(width, height, -length);
                break;
            case WEST: // Towards negative x
                offset = new Vector3i(2, 0, 0);
                endingLocationOffset = new Vector3i(width, height, length);
                break;
        }
        Vector3i originLocation = itemFrameLoc.getBlockPosition().add(offset);
        Vector3i endLocation = originLocation.add(endingLocationOffset);
        new Location<World>(itemFrameLoc.getExtent(), originLocation).setBlockType(BlockTypes.GOLD_BLOCK);
        new Location<World>(itemFrameLoc.getExtent(), endLocation).setBlockType(BlockTypes.DIAMOND_BLOCK);
    }
}
