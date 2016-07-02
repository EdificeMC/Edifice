package me.reherhold.edifice;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

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

import com.google.inject.Inject;

import me.reherhold.edifice.command.executor.EdificeWandExecutor;
import me.reherhold.edifice.command.executor.GiveBluePrintExecutor;
import me.reherhold.edifice.command.executor.SaveStructureExecutor;
import me.reherhold.edifice.data.blueprint.BlueprintData;
import me.reherhold.edifice.data.blueprint.BlueprintDataManipulatorBuilder;
import me.reherhold.edifice.data.blueprint.ImmutableBlueprintData;
import me.reherhold.edifice.data.structure.ImmutableStructureData;
import me.reherhold.edifice.data.structure.StructureBuilder;
import me.reherhold.edifice.data.structure.StructureData;
import me.reherhold.edifice.data.structure.StructureDataManipulatorBuilder;
import me.reherhold.edifice.eventhandler.ChangeBlockEventHandler;
import me.reherhold.edifice.eventhandler.InteractBlockEventHandler;
import me.reherhold.edifice.eventhandler.InteractEntityEventHandler;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class Edifice {

    @Inject @DefaultConfig(sharedRoot = false) private File configFile;
    @Inject @DefaultConfig(sharedRoot = false) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject private Logger logger;
    private EdificeConfiguration config;
    private HashMap<UUID, Boolean> playerWandActivationStates;
    private HashMap<UUID, Pair<Location<World>, Location<World>>> playerSelectedLocations;
    private Client client;

    @Listener
    public void preInit(GamePreInitializationEvent event) {
        this.playerWandActivationStates = new HashMap<UUID, Boolean>();
        this.playerSelectedLocations = new HashMap<UUID, Pair<Location<World>, Location<World>>>();
        try {
			setupRestClient();
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			logger.error("Could not initialize the REST client with TLS enabled. Shutting down.");
			e.printStackTrace();
			Sponge.getServer().shutdown();
		}
        registerEventListeners();
        registerCommands();
        setupConfig();
        registerData();
        startItemEntityListener();
    }
    
    private void setupRestClient() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslCxt = SSLContext.getInstance("TLSv1");
		System.setProperty("https.protocols", "TLSv1");

		TrustManager[] trustAllCerts = { new InsecureTrustManager() };
		sslCxt.init(null, trustAllCerts, new java.security.SecureRandom());
		HostnameVerifier allHostsValid = new InsecureHostnameVerifier();

		this.client = ClientBuilder.newBuilder().sslContext(sslCxt).hostnameVerifier(allHostsValid).build();
    }

    private void registerEventListeners() {
        // Watch for players left/right-clicking blocks
        Sponge.getEventManager().registerListeners(this, new InteractBlockEventHandler(this));
        // Watch for players destroying blocks
        Sponge.getEventManager().registerListeners(this, new ChangeBlockEventHandler(this));
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
        Sponge.getDataManager().register(StructureData.class, ImmutableStructureData.class, new StructureDataManipulatorBuilder());
        Sponge.getDataManager().registerBuilder(Structure.class, new StructureBuilder());
    }

    // I really dislike checking every few ticks for items on the ground, but
    // with no events implemented for Players throwing items or implementation
    // for checking Chest inventories, this is what I have to do until they are
    // implemented
    private void startItemEntityListener() {
        Sponge.getScheduler().createTaskBuilder().intervalTicks(10)
                .name("Edifice - Check for items on the ground to be used in the construction of a structure").execute(new WatchItemsRunnable())
                .submit(this);
    }

    public EdificeConfiguration getConfig() {
        return this.config;
    }
    
    public Client getClient() {
    	return this.client;
    }

    public Logger getLogger() {
        return this.logger;
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
