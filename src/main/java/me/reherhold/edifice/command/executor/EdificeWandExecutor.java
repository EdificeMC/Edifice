package me.reherhold.edifice.command.executor;

import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;

public class EdificeWandExecutor implements CommandExecutor {

    private Edifice plugin;

    public EdificeWandExecutor(Edifice plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext arg1) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Constants.MUST_BE_PLAYER);
            return CommandResult.empty();
        }
        Player player = (Player) source;
        if (this.plugin.isWandActivated(player.getUniqueId())) {
            player.sendMessage(Constants.STOPPED_MARKING_REGION);
            this.plugin.getPlayerWandActivationStates().put(player.getUniqueId(), false);
        } else {
            player.sendMessage(Constants.CAN_MARK_REGION);
            this.plugin.getPlayerWandActivationStates().put(player.getUniqueId(), true);
        }

        return CommandResult.success();
    }

}
