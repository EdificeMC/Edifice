package me.reherhold.edifice.data.structure;

import static me.reherhold.edifice.data.structure.StructureDataQueries.BLOCKS;
import static me.reherhold.edifice.data.structure.StructureDataQueries.NAME;

import com.google.common.collect.Lists;
import me.reherhold.edifice.Structure;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

public class StructureBuilder extends AbstractDataBuilder<Structure> {

    public StructureBuilder() {
        super(Structure.class, 1);
    }

    @Override
    protected Optional<Structure> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(NAME, BLOCKS)) {
            Structure structure =
                    new Structure(container.getString(NAME).get(), container.getSerializableList(BLOCKS, BlockSnapshot.class).orElse(
                            Lists.newArrayList()));
            return Optional.of(structure);
        }
        return Optional.empty();
    }
}
