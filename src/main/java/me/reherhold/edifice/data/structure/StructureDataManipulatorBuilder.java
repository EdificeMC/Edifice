package me.reherhold.edifice.data.structure;

import jersey.repackaged.com.google.common.collect.Lists;

import static me.reherhold.edifice.data.EdificeKeys.STRUCTURE;
import me.reherhold.edifice.Structure;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class StructureDataManipulatorBuilder implements DataManipulatorBuilder<StructureData, ImmutableStructureData> {

    @Override
    public Optional<StructureData> build(DataView container) throws InvalidDataException {
        if (!container.contains(STRUCTURE.getQuery())) {
            return Optional.empty();
        }
        Structure warp = container.getSerializable(STRUCTURE.getQuery(), Structure.class).get();
        return Optional.of(new StructureData(warp));
    }

    @Override
    public StructureData create() {
        return new StructureData(new Structure("", Lists.newArrayList()));
    }

    @Override
    public Optional<StructureData> createFrom(DataHolder dataHolder) {
        return create().fill(dataHolder);
    }

    public StructureData createFrom(Structure structure) {
        return new StructureData(structure);
    }

}
