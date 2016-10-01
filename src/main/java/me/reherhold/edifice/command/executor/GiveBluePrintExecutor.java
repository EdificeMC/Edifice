package me.reherhold.edifice.command.executor;

import static me.reherhold.edifice.StructureJSONKeys.CREATOR_UUID;
import static me.reherhold.edifice.StructureJSONKeys.HEIGHT;
import static me.reherhold.edifice.StructureJSONKeys.LENGTH;
import static me.reherhold.edifice.StructureJSONKeys.NAME;
import static me.reherhold.edifice.StructureJSONKeys.WIDTH;

import me.reherhold.edifice.Constants;
import me.reherhold.edifice.Edifice;
import me.reherhold.edifice.data.blueprint.BlueprintData;
import org.json.JSONObject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class GiveBluePrintExecutor implements CommandExecutor {

    private Edifice plugin;

    public GiveBluePrintExecutor(Edifice plugin) {
        this.plugin = plugin;
    }

    @Override
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

        @Override
        public void run() {
            Optional<JSONObject> structureOpt = Optional.empty();
            try {
                structureOpt = Edifice.structureCache.getById(this.structureID).get();
            } catch (InterruptedException | ExecutionException e1) {
                e1.printStackTrace();
            }

            if (!structureOpt.isPresent()) {
                this.player.sendMessage(Text.of(TextColors.RED, "Could not find a structure with ID " + this.structureID));
                return;
            }
            JSONObject structure = structureOpt.get();

            String creatorName;
            try {
                GameProfile creatorProfile = Sponge.getServer().getGameProfileManager().get(UUID.fromString(structure.getString(CREATOR_UUID))).get();
                creatorName = creatorProfile.getName().get();
            } catch (Exception e) {
                creatorName = "Anonymous";
            }

            ItemStack.Builder builder = Sponge.getRegistry().createBuilder(ItemStack.Builder.class);
            ItemStack blueprint = builder.itemType(ItemTypes.PAPER)
                    .quantity(1)
                    .itemData(new BlueprintData(this.structureID))
                    .add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, structure.getString(NAME), TextColors.GREEN, " Blueprint"))
                    .add(Keys.ITEM_LORE,
                            Arrays.asList(Text.of(TextColors.BLUE, "Creator: ", TextColors.GOLD, creatorName),
                                    Text.of(TextColors.GREEN, "To activate, place in an item frame in front"),
                                    Text.of(TextColors.GREEN, "in front of a cleared ", TextColors.GOLD, structure.getInt(WIDTH), TextColors.GREEN,
                                            " x ", TextColors.GOLD, structure.getInt(LENGTH), TextColors.GREEN, " x ", TextColors.GOLD,
                                            structure.getInt(HEIGHT), TextColors.GREEN, " area.")))
                    .build();
            this.player.getInventory().offer(blueprint);

        }

    }

}
