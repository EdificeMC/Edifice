package me.reherhold.edifice;

import me.reherhold.edifice.data.structure.StructureDataQueries;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;

import java.util.List;

public class Structure implements DataSerializable {

    private String name;
    List<BlockSnapshot> remainingBlocks;
    
    public Structure(String name, List<BlockSnapshot> remainingBlocks) {
        this.name = name;
        this.remainingBlocks = remainingBlocks;
    } 

    public String getName() {
        return name;
    }

    public List<BlockSnapshot> getRemainingBlocks() {
        return remainingBlocks;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(StructureDataQueries.NAME, getName())
                .set(StructureDataQueries.BLOCKS, getRemainingBlocks());
    }

}
