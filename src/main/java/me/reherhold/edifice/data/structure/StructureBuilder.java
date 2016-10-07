package me.reherhold.edifice.data.structure;

import static me.reherhold.edifice.data.structure.StructureDataQueries.AUTHOR_UUID;
import static me.reherhold.edifice.data.structure.StructureDataQueries.BLOCKS;
import static me.reherhold.edifice.data.structure.StructureDataQueries.ORIGIN;
import static me.reherhold.edifice.data.structure.StructureDataQueries.NAME;
import static me.reherhold.edifice.data.structure.StructureDataQueries.OWNER_UUID;

import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.Structure;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StructureBuilder extends AbstractDataBuilder<Structure> {

    public StructureBuilder() {
        super(Structure.class, 1);
    }

    @Override
    protected Optional<Structure> buildContent(DataView container) throws InvalidDataException {
        if (container.contains(NAME, AUTHOR_UUID, OWNER_UUID, BLOCKS)) {
            HashMap<BlockState, List<Vector3i>> blockMap = new HashMap<>();
            DataView blockMapView = container.getView(BLOCKS).get();
            for (DataQuery key : blockMapView.getKeys(false)) {
                BlockState blockState = Sponge.getRegistry().getType(BlockState.class, key.toString()).get();
                List<Vector3i> vectorList = blockMapView.getObjectList(key, Vector3i.class).get();
                blockMap.put(blockState, vectorList);
            }
            Structure structure =
                    new Structure(container.getString(NAME).get(), UUID.fromString(container.getString(AUTHOR_UUID).get()),
                            UUID.fromString(container.getString(OWNER_UUID).get()), container.getObject(ORIGIN, Vector3i.class).get(), blockMap);
            return Optional.of(structure);
        }
        return Optional.empty();
    }
}
