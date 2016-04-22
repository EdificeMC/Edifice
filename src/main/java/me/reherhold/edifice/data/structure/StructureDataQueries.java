package me.reherhold.edifice.data.structure;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataQuery;

public class StructureDataQueries {

    public static final DataQuery NAME = of("name");
    public static final DataQuery CREATOR_UUID = of("creator-uuid");
    public static final DataQuery OWNER_UUID = of("owner-uuid");
    public static final DataQuery DIRECTION = of("direction");
    public static final DataQuery BLOCKS = of("blocks");

}