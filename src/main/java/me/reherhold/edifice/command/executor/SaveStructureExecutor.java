package me.reherhold.edifice.command.executor;

import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SaveStructureExecutor implements CommandExecutor {

    private Edifice plugin;

    public SaveStructureExecutor(Edifice plugin) {
        this.plugin = plugin;
    }

    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Constants.MUST_BE_PLAYER);
            return CommandResult.empty();
        }
        Player player = (Player) source;
        UUID uuid = player.getUniqueId();

        if (!plugin.getPlayerSelectedLocations().containsKey(uuid) || plugin.getPlayerSelectedLocations().get(uuid).getLeft() == null
                || plugin.getPlayerSelectedLocations().get(uuid).getRight() == null) {
            source.sendMessage(Constants.SELECT_LOCATIONS_FIRST);
            return CommandResult.empty();
        }

        World w = plugin.getPlayerSelectedLocations().get(uuid).getLeft().getExtent();
        Vector3i loc1 = plugin.getPlayerSelectedLocations().get(uuid).getLeft().getBlockPosition();
        Vector3i loc2 = plugin.getPlayerSelectedLocations().get(uuid).getRight().getBlockPosition();

        Sponge.getScheduler().createTaskBuilder().execute(new SaveStructureRunnable(player, (String) args.getOne("name").get(), w, loc1, loc2))
                .async().name("Edifice - Submit Structure to REST API").submit(plugin);

        return CommandResult.success();
    }

    class SaveStructureRunnable implements Runnable {

        private Player player;
        private String structureName;
        private World world;
        private Vector3i loc1;
        private Vector3i loc2;

        public SaveStructureRunnable(Player player, String structureName, World world, Vector3i loc1, Vector3i loc2) {
            this.player = player;
            this.structureName = structureName;
            this.world = world;
            this.loc1 = loc1;
            this.loc2 = loc2;
        }

        public void run() {
            int minX = Math.min(loc1.getX(), loc2.getX());
            int maxX = Math.max(loc1.getX(), loc2.getX());
            int minY = Math.min(loc1.getY(), loc2.getY());
            int maxY = Math.max(loc1.getY(), loc2.getY());
            int minZ = Math.min(loc1.getZ(), loc2.getZ());
            int maxZ = Math.max(loc1.getZ(), loc2.getZ());

            JSONObject structure = new JSONObject()
                    .put("name", structureName)
                    .put("creatorUUID", player.getUniqueId().toString())
                    .put("blocks", new JSONArray());

            for (int i = minX; i < maxX; i++) {
                for (int j = minY; j < maxY; j++) {
                    for (int k = minZ; k < maxZ; k++) {
                        StringWriter writer = new StringWriter();
                        BlockSnapshot block = world.createSnapshot(new Vector3i(i, j, k));
                        if (block.getState().getType() == BlockTypes.AIR) {
                            continue;
                        }
                        ConfigurationNode node = SimpleConfigurationNode.root();
                        ConfigurateTranslator.instance().translateContainerToData(node, block.toContainer());
                        try {
                            JSONConfigurationLoader.builder().build().saveInternal(node, writer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        JSONObject jsonBlock = new JSONObject(writer.toString());
                        jsonBlock.remove("WorldUuid");
                        jsonBlock.getJSONObject("Position").put("X", i - minX).put("Y", j - minY).put("Z", k - minZ);
                        structure.getJSONArray("blocks").put(jsonBlock);
                    }
                }
            }

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:3000/structures");
            Response response = target.request().post(Entity.entity(structure.toString(), MediaType.APPLICATION_JSON_TYPE));
        }

    }
}
