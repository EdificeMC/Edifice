package me.reherhold.edifice;

import me.reherhold.edifice.data.blueprint.BlueprintDataManipulatorBuilder;

import com.google.inject.Inject;
import me.reherhold.edifice.command.executor.EdificeWandExecutor;
import me.reherhold.edifice.command.executor.GiveBluePrintExecutor;
import me.reherhold.edifice.command.executor.SaveStructureExecutor;
import me.reherhold.edifice.data.blueprint.BlueprintData;
import me.reherhold.edifice.data.blueprint.ImmutableBlueprintData;
import me.reherhold.edifice.eventhandler.InteractBlockEventHandler;
import me.reherhold.edifice.eventhandler.InteractEntityEventHandler;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
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
import org.spongepowered.api.world.extent.StorageType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Plugin(id = "edifice", name = "Edifice", version = "0.0.1")
public class Edifice {

    @Inject @DefaultConfig(sharedRoot = false) private File configFile;
    @Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject private Logger logger;
    private EdificeConfiguration config;
    private HashMap<UUID, Boolean> playerWandActivationStates;
    private HashMap<UUID, Pair<Location<World>, Location<World>>> playerSelectedLocations;

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        this.playerWandActivationStates = new HashMap<UUID, Boolean>();
        this.playerSelectedLocations = new HashMap<UUID, Pair<Location<World>, Location<World>>>();
        registerEventListeners();
        registerCommands();
        setupConfig();
        registerData();
    }

    private void registerEventListeners() {
        // Watch for players left/right-clicking blocks
        Sponge.getEventManager().registerListeners(this, new InteractBlockEventHandler(this));
        // Watch for players putting blueprints in item frames
        Sponge.getEventManager().registerListeners(this, new InteractEntityEventHandler(this));
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

        CommandSpec createStructureSpec =
                CommandSpec.builder().description(Text.of("Starts the process of creating a structure")).executor(new GiveBluePrintExecutor(this))
                        .arguments(GenericArguments.string(Text.of("id")))
                        .build();
        subCommands.put(Arrays.asList("create"), createStructureSpec);

        CommandSpec mainSpec = CommandSpec.builder().children(subCommands).build();
        Sponge.getCommandManager().register(this, mainSpec, "edifice");
    }

    private void setupConfig() {
        if (!this.configFile.exists()) {
            saveDefaultConfig();
        } else {
            loadConfig();
        }
    }

    /**
     * Reads in config values supplied from the ConfigManager. Falls back on the
     * default configuration values in Settings
     */
    private void loadConfig() {
        ConfigurationNode rawConfig = null;
        try {
            rawConfig = this.configLoader.load();
            this.config = EdificeConfiguration.MAPPER.bindToNew().populate(rawConfig);
        } catch (IOException e) {
            this.logger.warn("The configuration could not be loaded! Using the default configuration");
        } catch (IllegalArgumentException e) {
            // Everything after this is only for stringifying the array of all
            // StorageType values
            StringBuilder sb = new StringBuilder();
            StorageType[] storageTypes = StorageType.values();
            for (int i = 0; i < storageTypes.length; i++) {
                sb.append(storageTypes[i].toString());
                if (i + 1 != storageTypes.length) {
                    sb.append(", ");
                }
            }
            this.logger.warn("The specified storage type could not be found. Reverting to flatfile storage. Try: " + sb.toString());
        } catch (ObjectMappingException e) {
            this.logger.warn("There was an error loading the configuration." + e.getStackTrace());
        }
    }

    /**
     * Saves a config file with default values if it does not already exist
     *
     * @return true if default config was successfully created, false if the
     *         file was not created
     */
    private void saveDefaultConfig() {
        try {
            this.logger.info("Generating config file...");
            this.configFile.getParentFile().mkdirs();
            this.configFile.createNewFile();
            CommentedConfigurationNode rawConfig = this.configLoader.load();

            try {
                // Populate config with default values
                this.config = EdificeConfiguration.MAPPER.bindToNew().populate(rawConfig);
                EdificeConfiguration.MAPPER.bind(this.config).serialize(rawConfig);
            } catch (ObjectMappingException e) {
                e.printStackTrace();
            }

            this.configLoader.save(rawConfig);
            this.logger.info("Config file successfully generated.");
        } catch (IOException exception) {
            this.logger.warn("The default configuration could not be created!");
        }
    }
    
    private void registerData() {
        Sponge.getDataManager().register(BlueprintData.class, ImmutableBlueprintData.class, new BlueprintDataManipulatorBuilder());
    }

    public EdificeConfiguration getConfig() {
        return this.config;
    }

    public HashMap<UUID, Boolean> getPlayerWandActivationStates() {
        return this.playerWandActivationStates;
    }

    public HashMap<UUID, Pair<Location<World>, Location<World>>> getPlayerSelectedLocations() {
        return this.playerSelectedLocations;
    }

    public boolean isWandActivated(UUID uuid) {
        if (this.playerWandActivationStates.containsKey(uuid)) {
            return this.playerWandActivationStates.get(uuid);
        }
        return false;
    }

}
