package me.reherhold.edifice.command.executor;

import java.util.Arrays;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class GiveBluePrintExecutor implements CommandExecutor {

    private Edifice plugin;

    public GiveBluePrintExecutor(Edifice plugin) {
        this.plugin = plugin;
    }

    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        if (!(source instanceof Player)) {
            source.sendMessage(Constants.MUST_BE_PLAYER);
            return CommandResult.empty();
        }
        Player player = (Player) source;
        // Already know this will be present since the argument is required
        String structureID = (String) args.getOne("id").get();

        Sponge.getScheduler().createTaskBuilder().execute(new GiveBluePrintRunnable(player, structureID))
                .async().name("Edifice - Fetch Structure from REST API").submit(this.plugin);

        return CommandResult.success();
    }

    class GiveBluePrintRunnable implements Runnable {

        private Player player;
        private String structureID;

        public GiveBluePrintRunnable(Player player, String structureID) {
            this.player = player;
            this.structureID = structureID;
        }

        public void run() {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(GiveBluePrintExecutor.this.plugin.getConfig().getRestURI().toString() + "/structures/" + structureID);
            Response response = target.request().get();
            if (response.getStatus() != 200) {
                player.sendMessage(Text.of(TextColors.RED, "Could not find a structure with ID " + structureID));
                return;
            }
            JSONObject structure = new JSONObject(response.readEntity(String.class));
            ItemStack.Builder builder = Sponge.getRegistry().createBuilder(ItemStack.Builder.class);
            ItemStack blueprint = builder.itemType(ItemTypes.PAPER)
                    .quantity(1)
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, structure.getString("name"), TextColors.GREEN, " Blueprint"))
                    .add(Keys.ITEM_LORE,
                            Arrays.asList(
                                    Text.of(TextColors.GREEN, "To activate, place in an item frame in front"),
                                    Text.of(TextColors.GREEN, "in front of a cleared ", TextColors.GOLD, structure.getInt("width"), TextColors.GREEN,
                                            " x ", TextColors.GOLD, structure.getInt("length"), TextColors.GREEN, " x ", TextColors.GOLD,
                                            structure.getInt("height"), TextColors.GREEN, " area.")))
                    .build();
            player.getInventory().offer(blueprint);

        }

    }

}
