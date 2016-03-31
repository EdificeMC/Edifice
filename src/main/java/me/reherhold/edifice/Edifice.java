package me.reherhold.edifice;

import com.google.inject.Inject;
import me.reherhold.edifice.command.executor.EdificeWandExecutor;
import me.reherhold.edifice.command.executor.SaveStructureExecutor;
import me.reherhold.edifice.eventhandler.InteractBlockEventHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Plugin(id = "edifice", name = "Edifice", version = "0.0.1")
public class Edifice {

    @Inject @DefaultConfig(sharedRoot = false) private File configFile;
    @Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private HashMap<UUID, Boolean> playerWandActivationStates;
    private HashMap<UUID, Pair<Location<World>, Location<World>>> playerSelectedLocations;

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        playerWandActivationStates = new HashMap<UUID, Boolean>();
        playerSelectedLocations = new HashMap<UUID, Pair<Location<World>, Location<World>>>();
        // Watch for players right-clicking blocks
        Sponge.getEventManager().registerListeners(this, new InteractBlockEventHandler(this));
        registerCommands();
    }

    private void registerCommands() {
        HashMap<List<String>, CommandSpec> subCommands = new HashMap<List<String>, CommandSpec>();

        CommandSpec activateWandSpec =
                CommandSpec.builder().description(Text.of("Allows the player to start marking corners"))
                        .executor(new EdificeWandExecutor(this))
                        .build();
        subCommands.put(Arrays.asList("wand"), activateWandSpec);

        CommandSpec saveStructureSpec =
                CommandSpec.builder().description(Text.of("Saves the structure within the marked corners")).executor(new SaveStructureExecutor(this))
                        .arguments(GenericArguments.string(Text.of("name")))
                        .build();
        subCommands.put(Arrays.asList("save"), saveStructureSpec);

        CommandSpec mainSpec = CommandSpec.builder().children(subCommands).build();
        Sponge.getCommandManager().register(this, mainSpec, "edifice");
    }

    public HashMap<UUID, Boolean> getPlayerWandActivationStates() {
        return playerWandActivationStates;
    }

    public HashMap<UUID, Pair<Location<World>, Location<World>>> getPlayerSelectedLocations() {
        return playerSelectedLocations;
    }

    public File getConfigFile() {
        return configFile;
    }

    public ConfigurationLoader<CommentedConfigurationNode> getConfigLoader() {
        return configLoader;
    }

    public boolean isWandActivated(UUID uuid) {
        if (playerWandActivationStates.containsKey(uuid)) {
            return playerWandActivationStates.get(uuid);
        }
        return false;
    }

}
