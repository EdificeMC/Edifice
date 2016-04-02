package me.reherhold.edifice.data.blueprint;

import static me.reherhold.edifice.data.EdificeKeys.BLUEPRINT;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class BlueprintDataManipulatorBuilder implements DataManipulatorBuilder<BlueprintData, ImmutableBlueprintData> {

    @Override
    public Optional<BlueprintData> build(DataView container) throws InvalidDataException {
        if (!container.contains(BLUEPRINT)) {
            return Optional.empty();
        }
        String blueprint = container.getString(BLUEPRINT.getQuery()).get();
        return Optional.of(new BlueprintData(blueprint));
    }

    @Override
    public BlueprintData create() {
        return new BlueprintData("");
    }

    @Override
    public Optional<BlueprintData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

}
