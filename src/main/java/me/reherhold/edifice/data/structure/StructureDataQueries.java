package me.reherhold.edifice.data.structure;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataQuery;

public class StructureDataQueries {

    public static final DataQuery NAME = of("Name");
    public static final DataQuery AUTHOR_UUID = of("AuthorUuid");
    public static final DataQuery OWNER_UUID = of("OwnerUuid");
    public static final DataQuery BLOCKS = of("Blocks");

}
