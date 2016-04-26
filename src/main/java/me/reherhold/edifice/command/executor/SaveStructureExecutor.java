package me.reherhold.edifice.command.executor;

import com.flowpowered.math.vector.Vector3d;

import static me.reherhold.edifice.StructureJSONKeys.BLOCKS;
import static me.reherhold.edifice.StructureJSONKeys.CREATOR_UUID;
import static me.reherhold.edifice.StructureJSONKeys.DIRECTION;
import static me.reherhold.edifice.StructureJSONKeys.HEIGHT;
import static me.reherhold.edifice.StructureJSONKeys.ID;
import static me.reherhold.edifice.StructureJSONKeys.LENGTH;
import static me.reherhold.edifice.StructureJSONKeys.NAME;
import static me.reherhold.edifice.StructureJSONKeys.POSITION;
import static me.reherhold.edifice.StructureJSONKeys.POSITION_X;
import static me.reherhold.edifice.StructureJSONKeys.POSITION_Y;
import static me.reherhold.edifice.StructureJSONKeys.POSITION_Z;
import static me.reherhold.edifice.StructureJSONKeys.WIDTH;
import static me.reherhold.edifice.StructureJSONKeys.WORLD_UUID;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Direction.Division;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
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

        if (!this.plugin.getPlayerSelectedLocations().containsKey(uuid) || this.plugin.getPlayerSelectedLocations().get(uuid).getLeft() == null
                || this.plugin.getPlayerSelectedLocations().get(uuid).getRight() == null) {
            source.sendMessage(Constants.SELECT_LOCATIONS_FIRST);
            return CommandResult.empty();
        }

        World w = this.plugin.getPlayerSelectedLocations().get(uuid).getLeft().getExtent();
        Vector3i loc1 = this.plugin.getPlayerSelectedLocations().get(uuid).getLeft().getBlockPosition();
        Vector3i loc2 = this.plugin.getPlayerSelectedLocations().get(uuid).getRight().getBlockPosition();

        Sponge.getScheduler().createTaskBuilder().execute(new SaveStructureRunnable(player, (String) args.getOne("name").get(), w, loc1, loc2))
                .async().name("Edifice - Submit Structure to REST API").submit(this.plugin);

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
            int minX = Math.min(this.loc1.getX(), this.loc2.getX());
            int maxX = Math.max(this.loc1.getX(), this.loc2.getX());
            int minY = Math.min(this.loc1.getY(), this.loc2.getY());
            int maxY = Math.max(this.loc1.getY(), this.loc2.getY());
            int minZ = Math.min(this.loc1.getZ(), this.loc2.getZ());
            int maxZ = Math.max(this.loc1.getZ(), this.loc2.getZ());

            Direction direction = Direction.getClosestHorizontal(convertRotation(player.getRotation()), Division.CARDINAL);

            JSONObject structure = new JSONObject()
                    .put(NAME, this.structureName)
                    .put(CREATOR_UUID, this.player.getUniqueId().toString())
                    .put(WIDTH, maxX - minX + 1)
                    .put(LENGTH, maxZ - minZ + 1)
                    .put(HEIGHT, maxY - minY + 1)
                    .put(BLOCKS, new JSONArray())
                    .put(DIRECTION, direction.toString());

            player.sendMessage(Text.of(TextColors.GREEN, "Analyzing the structure..."));

            Vector3i bottomCorner = new Vector3i(0, 0, 0);
//            Vector3i topCorner = new Vector3i(0, 0, 0);
            switch (direction) {
                case NORTH:
                    bottomCorner = new Vector3i(minX, minY, maxZ);
//                    topCorner = new Vector3i(maxX, maxY, minZ);
                    break;
                case SOUTH:
                    bottomCorner = new Vector3i(maxX, minY, minZ);
//                    topCorner = new Vector3i(minX, maxY, maxZ);
                    break;
                case EAST:
                    bottomCorner = new Vector3i(minX, minY, minZ);
//                    topCorner = new Vector3i(maxX, maxY, maxZ);
                    break;
                case WEST:
                    bottomCorner = new Vector3i(maxX, minY, maxZ);
//                    topCorner = new Vector3i(minX, maxY, minZ);
                    break;
                default:
                    player.sendMessage(Text.of(TextColors.RED, "Look in the direction facing the front of your structure."));
                    return;
            }

            for (int i = minX; i <= maxX; i++) {
                for (int j = minY; j <= maxY; j++) {
                    for (int k = minZ; k <= maxZ; k++) {
                        StringWriter writer = new StringWriter();
                        BlockSnapshot block = this.world.createSnapshot(new Vector3i(i, j, k));
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
                        jsonBlock.remove(WORLD_UUID);
                        jsonBlock.getJSONObject(POSITION).put(POSITION_X, i - bottomCorner.getX())
                                .put(POSITION_Y, j - bottomCorner.getY()).put(POSITION_Z, k - bottomCorner.getZ());
                        structure.getJSONArray(BLOCKS).put(jsonBlock);
                    }
                }
            }

            player.sendMessage(Text.of(TextColors.GREEN, "Uploading the structure..."));

            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(SaveStructureExecutor.this.plugin.getConfig().getRestURI().toString() + "/structures");
            Response response;
            try {
                response = target.request().post(Entity.entity(structure.toString(), MediaType.APPLICATION_JSON_TYPE));
            } catch (Exception e) {
                this.player.sendMessage(Text.of(TextColors.RED,
                        "There was an error uploading your structure."));
                return;
            }

            if (response.getStatus() == 201) {
                JSONObject responseBody = new JSONObject(response.readEntity(String.class));
                String structureID = responseBody.getString(ID);
                this.player.sendMessage(Text.of(TextColors.GREEN, "You have successfully uploaded ", TextColors.GOLD, this.structureName,
                        TextColors.GREEN,
                        "."));
                try {
                    this.player.sendMessage(Text.of(
                            TextColors.GREEN,
                            "Click ",
                            Text.builder("here")
                                    .color(TextColors.GOLD)
                                    .onClick(
                                            TextActions.openUrl(new URL(SaveStructureExecutor.this.plugin.getConfig().getWebURI().toString()
                                                    + "/#/create/" + structureID))).build(), TextColors.GREEN,
                            " to finalize it with images."));

                } catch (MalformedURLException e) {
                }
            } else {
                this.player.sendMessage(Text.of(TextColors.RED,
                        "There was an error uploading your structure. Received status code " + response.getStatus() + " and response body "
                                + response.getEntity()));
            }
        }

        private Vector3d convertRotation(Vector3d rotation) {
            double pitch = ((rotation.getX() + 90) * Math.PI) / 180;
            double yaw = ((rotation.getY() + 90) * Math.PI) / 180;
            Vector3d vector = new Vector3d(Math.sin(pitch) * Math.cos(yaw), 0, Math.sin(pitch) * Math.sin(yaw));
            return vector;
        }
    }

}
