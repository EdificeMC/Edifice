package me.reherhold.edifice.command.executor;

import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import org.apache.commons.lang3.tuple.MutablePair;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

public class EdificeExpandWandExecutor implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Constants.MUST_BE_PLAYER);
            return CommandResult.empty();
        }
        Player player = (Player) source;
        UUID playerUUID = player.getUniqueId();

        if (!Edifice.getPlayerSelectedLocations().containsKey(playerUUID)
                || Edifice.getPlayerSelectedLocations().get(playerUUID).getLeft() == null
                || Edifice.getPlayerSelectedLocations().get(playerUUID).getRight() == null) {
            source.sendMessage(Constants.SELECT_LOCATIONS_FIRST);
            return CommandResult.empty();
        }

        Location<World> loc1 = Edifice.getPlayerSelectedLocations().get(playerUUID).getLeft();
        Location<World> loc2 = Edifice.getPlayerSelectedLocations().get(playerUUID).getRight();
        World world = loc1.getExtent();

        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = minY;
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

        int worldCap = world.getBlockMax().getY();
        yLoop: for (int y = minY; y <= worldCap; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState block = world.getBlock(x, y, z);
                    if (!block.getType().equals(BlockTypes.AIR)) {
                        maxY = y;
                        continue yLoop;
                    }
                }
            }
        }

        Location<World> newMin = new Location<World>(world, minX, minY, minZ);
        Location<World> newMax = new Location<World>(world, maxX, maxY, maxZ);

        Edifice.getPlayerSelectedLocations().put(player.getUniqueId(), new MutablePair<>(newMin, newMax));
        player.sendMessage(Text.of(Constants.SET_FIRST_LOC, TextColors.GOLD, newMin.getPosition()));
        player.sendMessage(Text.of(Constants.SET_SECOND_LOC, TextColors.GOLD, newMax.getPosition()));

        return CommandResult.success();
    }

}
