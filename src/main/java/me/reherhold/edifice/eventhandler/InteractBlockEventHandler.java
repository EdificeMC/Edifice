package me.reherhold.edifice.eventhandler;

import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class InteractBlockEventHandler {

    private Edifice plugin;

    public InteractBlockEventHandler(Edifice plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void interactBlock(InteractBlockEvent event, @Root Player player) {
        if (!plugin.isWandActivated(player.getUniqueId())) {
            return;
        }

        Optional<Location<World>> lOpt = event.getTargetBlock().getLocation();
        if (!lOpt.isPresent()) {
            return;
        }
        Location<World> loc = lOpt.get();

        if (!plugin.getPlayerSelectedLocations().containsKey(player.getUniqueId())) {
            plugin.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<Location<World>, Location<World>>(null, null));
        }

        if (event instanceof InteractBlockEvent.Primary) {
            Location<World> existingRight = plugin.getPlayerSelectedLocations().get(player.getUniqueId()).getRight();
            plugin.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<Location<World>, Location<World>>(loc, existingRight));
            player.sendMessage(Text.of(Constants.SET_FIRST_LOC, loc));
        } else if (event instanceof InteractBlockEvent.Secondary) {
            Location<World> existingLeft = plugin.getPlayerSelectedLocations().get(player.getUniqueId()).getLeft();
            plugin.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<Location<World>, Location<World>>(existingLeft, loc));
            player.sendMessage(Text.of(Constants.SET_SECOND_LOC, loc));
        }
    }
}
