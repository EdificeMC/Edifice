package me.reherhold.edifice.command.executor;

import static me.reherhold.edifice.StructureJSONKeys.CREATOR_UUID;
import static me.reherhold.edifice.StructureJSONKeys.ID;
import static me.reherhold.edifice.StructureJSONKeys.NAME;
import static me.reherhold.edifice.StructureJSONKeys.WIDTH;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.body.RequestBodyEntity;
import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Direction.Division;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

public class SaveStructureExecutor implements CommandExecutor {

    private Edifice plugin;

    public SaveStructureExecutor(Edifice plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Constants.MUST_BE_PLAYER);
            return CommandResult.empty();
        }
        Player player = (Player) source;
        UUID uuid = player.getUniqueId();

        if (!this.plugin.getPlayerSelectedLocations().containsKey(uuid)
                || this.plugin.getPlayerSelectedLocations().get(uuid).getLeft() == null
                || this.plugin.getPlayerSelectedLocations().get(uuid).getRight() == null) {
            source.sendMessage(Constants.SELECT_LOCATIONS_FIRST);
            return CommandResult.empty();
        }

        World world = this.plugin.getPlayerSelectedLocations().get(uuid).getLeft().getExtent();
        Vector3i loc1 = this.plugin.getPlayerSelectedLocations().get(uuid).getLeft().getBlockPosition();
        Vector3i loc2 = this.plugin.getPlayerSelectedLocations().get(uuid).getRight().getBlockPosition();

        final String structureName = (String) args.getOne("name").get();

        int minX = Math.min(loc1.getX(), loc2.getX());
        int maxX = Math.max(loc1.getX(), loc2.getX());
        int minY = Math.min(loc1.getY(), loc2.getY());
        int maxY = Math.max(loc1.getY(), loc2.getY());
        int minZ = Math.min(loc1.getZ(), loc2.getZ());
        int maxZ = Math.max(loc1.getZ(), loc2.getZ());

        Direction direction = Direction.getClosestHorizontal(convertRotation(player.getRotation()), Division.CARDINAL);

        JSONObject structure = new JSONObject().put(NAME, structureName)
                .put(CREATOR_UUID, player.getUniqueId().toString()).put(WIDTH, maxX - minX + 1);
        // .put(LENGTH, maxZ - minZ + 1).put(HEIGHT, maxY - minY +
        // 1).put(BLOCKS, new JSONArray())

        player.sendMessage(Text.of(TextColors.GREEN, "Analyzing the structure..."));

        Vector3i bottomCorner = new Vector3i(0, 0, 0);
        switch (direction) {
            case NORTH:
                bottomCorner = new Vector3i(minX, minY, maxZ);
                break;
            case SOUTH:
                bottomCorner = new Vector3i(maxX, minY, minZ);
                break;
            case EAST:
                bottomCorner = new Vector3i(minX, minY, minZ);
                break;
            case WEST:
                bottomCorner = new Vector3i(maxX, minY, maxZ);
                break;
            default:
                player.sendMessage(Text.of(TextColors.RED, "Look in the direction facing the front of your structure."));
                return CommandResult.empty();
        }

        ArchetypeVolume volume = player.getWorld().createArchetypeVolume(new Vector3i(minX, minY, minZ),
                new Vector3i(maxX, maxY, maxZ), bottomCorner);

        Schematic schematic = Schematic.builder().volume(volume).metaValue(Schematic.METADATA_AUTHOR, player.getName())
                .metaValue(Schematic.METADATA_NAME, structureName).metaValue("Direction", direction.toString())
                .paletteType(BlockPaletteTypes.LOCAL).build();

        player.sendMessage(Text.of(TextColors.GREEN, "Uploading the structure..."));

        Sponge.getScheduler().createTaskBuilder().execute(new SaveStructureRunnable(player, structure, schematic))
                .async().name("Edifice - Submit Structure to REST API").submit(this.plugin);

        return CommandResult.success();
    }

    class SaveStructureRunnable implements Runnable {

        private Player player;
        private JSONObject structure;
        private Schematic schematic;

        public SaveStructureRunnable(Player player, JSONObject structure, Schematic schematic) {
            this.player = player;
            this.structure = structure;
            this.schematic = schematic;
        }

        @Override
        public void run() {
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out;

            try {
                out = new PipedOutputStream(in);
            } catch (IOException e2) {
                e2.printStackTrace();
                return;
            }

            new Thread(() -> {
                try {
                    DataFormats.NBT.writeTo(new GZIPOutputStream(out), DataTranslators.SCHEMATIC.translate(this.schematic));
                } catch (InvalidDataException | IOException e1) {
                    e1.printStackTrace();
                }
            }).start();

            // Upload the schematic
            HttpResponse<JsonNode> schematicResponse;
            try {
                schematicResponse = Unirest.post(Edifice.config.getRestURI().toString() + "/schematic")
                        .field("schematic", in, ContentType.APPLICATION_OCTET_STREAM, "schematic.dat")
                        .asJson();
            } catch (Exception e) {
                this.player.sendMessage(Text.of(TextColors.RED, "There was an error uploading your structure."));
                e.printStackTrace();
                return;
            }

            JSONObject schematicResponseBody = schematicResponse.getBody().getObject();
            if (schematicResponse.getStatus() != 201) {
                this.player.sendMessage(
                        Text.of(TextColors.RED, "There was an error uploading your structure. Received status code "
                                + schematicResponse.getStatus() + " and response body " + schematicResponseBody.toString()));
                return;
            }

            // Upload the structure w/ the newly uploaded schematic URL
            final String schematicUrl = schematicResponseBody.getString("url");
            structure.put("schematic", schematicUrl);
            System.out.println(structure);

            HttpResponse<JsonNode> structureResponse;
            try {
                RequestBodyEntity body = Unirest.post(Edifice.config.getRestURI().toString() + "/structures/")
                        .header("Content-Type", "application/json")
                        .body(structure);
                System.out.println("entity");
                System.out.println(body.getEntity());
                System.out.println("http req");
                System.out.println(body.getHttpRequest());
                System.out.println("body");
                System.out.println(body.getBody());
                structureResponse = body.asJson();
            } catch (Exception e) {
                this.player.sendMessage(Text.of(TextColors.RED, "There was an error uploading your structure."));
                e.printStackTrace();
                return;
            }
            
            JSONObject structureResponseBody = structureResponse.getBody().getObject();
            if (structureResponse.getStatus() != 201) {
                this.player.sendMessage(
                        Text.of(TextColors.RED, "There was an error uploading your structure. Received status code "
                                + structureResponse.getStatus() + " and response body " + structureResponseBody.toString()));
                return;
            }
            
            String structureID = structureResponseBody.getString(ID);
            this.player.sendMessage(Text.of(TextColors.GREEN, "You have successfully uploaded ", TextColors.GOLD,
                    structure.getString(NAME), TextColors.GREEN, "."));
            try {
                this.player.sendMessage(Text.of(TextColors.GREEN, "Click ",
                        Text.builder("here").color(TextColors.GOLD)
                                .onClick(TextActions.openUrl(
                                        new URL(Edifice.config.getWebURI().toString() + "/create/" + structureID)))
                                .build(),
                        TextColors.GREEN, " to finalize it with a screenshot."));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

    }

    private Vector3d convertRotation(Vector3d rotation) {
        double pitch = ((rotation.getX() + 90) * Math.PI) / 180;
        double yaw = ((rotation.getY() + 90) * Math.PI) / 180;
        Vector3d vector = new Vector3d(Math.sin(pitch) * Math.cos(yaw), 0, Math.sin(pitch) * Math.sin(yaw));
        return vector;
    }

}
