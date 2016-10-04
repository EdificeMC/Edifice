package me.reherhold.edifice;

import com.flowpowered.math.vector.Vector3i;
import me.reherhold.edifice.data.structure.StructureDataQueries;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Structure implements DataSerializable {

    private String name;
    private UUID creatorUUID;
    private UUID ownerUUID;
    private Map<BlockState, List<Vector3i>> remainingBlocks;

    public Structure(String name, UUID creatorUUID, UUID ownerUUID, Map<BlockState, List<Vector3i>> remainingBlocks) {
        this.name = name;
        this.creatorUUID = creatorUUID;
        this.ownerUUID = ownerUUID;
        this.remainingBlocks = remainingBlocks;
    }

    public String getName() {
        return this.name;
    }

    public UUID getCreatorUUID() {
        return this.creatorUUID;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public Map<BlockState, List<Vector3i>> getRemainingBlocks() {
        return this.remainingBlocks;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(StructureDataQueries.NAME, getName())
                .set(StructureDataQueries.AUTHOR_UUID, getCreatorUUID().toString())
                .set(StructureDataQueries.OWNER_UUID, getOwnerUUID().toString())
                .set(StructureDataQueries.BLOCKS, getRemainingBlocks());
    }

}
