package me.reherhold.edifice;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class Constants {

    public static final Text MUST_BE_PLAYER = Text.of(TextColors.RED, "You must be a player to run that command!");
    public static final Text CAN_MARK_REGION = Text.of(TextColors.GREEN, "You may now mark a region.");
    public static final Text STOPPED_MARKING_REGION = Text.of(TextColors.GREEN, "You have stopped marking the region.");
    public static final Text SET_FIRST_LOC = Text.of(TextColors.GREEN, "You have set the first location to: ");
    public static final Text SET_SECOND_LOC = Text.of(TextColors.GREEN, "You have set the second location to: ");
    public static final Text SELECT_LOCATIONS_FIRST = Text.of(TextColors.RED, "You must select two locations first!");

}
