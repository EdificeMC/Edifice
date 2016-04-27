package me.reherhold.edifice.eventhandler;

import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.Structure;
import me.reherhold.edifice.data.EdificeKeys;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class InteractBlockEventHandler {

    private Edifice plugin;

    public InteractBlockEventHandler(Edifice plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void interactBlock(InteractBlockEvent event, @Root Player player) {
        Optional<Location<World>> lOpt = event.getTargetBlock().getLocation();
        if (!lOpt.isPresent()) {
            return;
        }
        Location<World> loc = lOpt.get();

        if (event instanceof InteractBlockEvent.Secondary && loc.get(EdificeKeys.STRUCTURE).isPresent()) {
            handleStructureInfo((InteractBlockEvent.Secondary) event, player, loc);
            return;
        }
        if (!this.plugin.isWandActivated(player.getUniqueId())) {
            return;
        }

        if (!this.plugin.getPlayerSelectedLocations().containsKey(player.getUniqueId())) {
            this.plugin.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<Location<World>, Location<World>>(null, null));
        }

        if (event instanceof InteractBlockEvent.Primary) {
            Location<World> existingRight = this.plugin.getPlayerSelectedLocations().get(player.getUniqueId()).getRight();
            this.plugin.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<Location<World>, Location<World>>(loc, existingRight));
            player.sendMessage(Text.of(Constants.SET_FIRST_LOC, TextColors.GOLD, loc.getPosition()));
        } else if (event instanceof InteractBlockEvent.Secondary) {
            Location<World> existingLeft = this.plugin.getPlayerSelectedLocations().get(player.getUniqueId()).getLeft();
            this.plugin.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<Location<World>, Location<World>>(existingLeft, loc));
            player.sendMessage(Text.of(Constants.SET_SECOND_LOC, TextColors.GOLD, loc.getPosition()));
        }
    }

    private void handleStructureInfo(InteractBlockEvent.Secondary event, Player player, Location<World> location) {
        Structure structure = location.get(EdificeKeys.STRUCTURE).get();
        List<Text> materialsNeeded = new ArrayList<Text>();
        for (String itemId : structure.getRemainingBlocks().keySet()) {
            ItemType itemType = Sponge.getRegistry().getType(ItemType.class, itemId).get();
            materialsNeeded.add(Text.of(TextColors.GREEN, structure.getRemainingBlocks().get(itemId).size(), " ", TextColors.GOLD, itemType
                    .getTranslation().get()));
        }

        Text title = Text.of("Constructing ", TextColors.GOLD, structure.getName());
        // If possible, get the creator's name
        try {
            GameProfile creatorProfile = Sponge.getServer().getGameProfileManager().get(structure.getCreatorUUID()).get();
            title = Text.of(title, TextColors.GREEN, " by ", TextColors.GOLD, creatorProfile.getName().get());
        } catch (Exception e) {
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder builder = paginationService.builder();
        builder.title(title).contents(materialsNeeded)
                .header(Text.of(TextColors.GREEN, "Materials needed to complete")).sendTo(player);
        event.setCancelled(true);
    }
}
