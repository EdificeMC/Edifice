package me.reherhold.edifice;

import me.reherhold.edifice.data.structure.StructureDataQueries;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.util.Direction;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Structure implements DataSerializable {

    private String name;
    private UUID creatorUUID;
    private UUID ownerUUID;
    private Direction direction;
    Map<String, List<BlockSnapshot>> remainingBlocks;

    public Structure(String name, UUID creatorUUID, UUID ownerUUID, Direction direction, Map<String, List<BlockSnapshot>> remainingBlocks) {
        this.name = name;
        this.creatorUUID = creatorUUID;
        this.ownerUUID = ownerUUID;
        this.direction = direction;
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

    public Direction getDirection() {
        return this.direction;
    }

    public Map<String, List<BlockSnapshot>> getRemainingBlocks() {
        return this.remainingBlocks;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(StructureDataQueries.NAME, getName())
                .set(StructureDataQueries.CREATOR_UUID, getCreatorUUID().toString())
                .set(StructureDataQueries.OWNER_UUID, getOwnerUUID().toString())
                .set(StructureDataQueries.DIRECTION, getDirection().toString())
                .set(StructureDataQueries.BLOCKS, getRemainingBlocks());
    }

}
