package me.reherhold.edifice.data.structure;

import static me.reherhold.edifice.data.structure.StructureDataQueries.BLOCKS;
import static me.reherhold.edifice.data.structure.StructureDataQueries.DIRECTION;
import static me.reherhold.edifice.data.structure.StructureDataQueries.NAME;

import me.reherhold.edifice.Structure;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.util.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class StructureBuilder extends AbstractDataBuilder<Structure> {

    public StructureBuilder() {
        super(Structure.class, 1);
    }

    @Override
    protected Optional<Structure> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(NAME, BLOCKS, DIRECTION)) {
            HashMap<String, List<BlockSnapshot>> blockMap = new HashMap<>();
            DataView blockMapView = container.getView(BLOCKS).get();
            for (DataQuery key : blockMapView.getKeys(false)) {
                blockMap.put(key.toString(), blockMapView.getSerializableList(key, BlockSnapshot.class).get());
            }
            Structure structure =
                    new Structure(container.getString(NAME).get(), Direction.valueOf(container.getString(DIRECTION).get()), blockMap);
            return Optional.of(structure);
        }
        return Optional.empty();
    }
}
