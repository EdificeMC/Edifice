package me.reherhold.edifice.eventhandler;

import me.reherhold.edifice.Edifice;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class ChangeBlockEventHandler {

    private Edifice plugin;

    public ChangeBlockEventHandler(Edifice plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void breakBlock(ChangeBlockEvent.Break event, @Root Player player) {
        // Check if the player is in wand mode
        if (this.plugin.getPlayerWandActivationStates().containsKey(player.getUniqueId())
                && this.plugin.getPlayerWandActivationStates().get(player.getUniqueId())) {
            // Check if the player is in creative and not sneaking
            if (player.get(Keys.GAME_MODE).get() == GameModes.CREATIVE && !player.get(Keys.IS_SNEAKING).get()) {
                event.setCancelled(true);
            }
        }
    }
}
