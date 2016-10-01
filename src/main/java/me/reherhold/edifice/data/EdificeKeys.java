package me.reherhold.edifice.data;

import me.reherhold.edifice.Structure;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

public class EdificeKeys {

    public static final Key<Value<String>> BLUEPRINT = KeyFactory.makeSingleKey(String.class, Value.class, DataQuery.of("BLUEPRINT"));
    public static final Key<Value<Structure>> STRUCTURE = KeyFactory.makeSingleKey(Structure.class, Value.class, DataQuery.of("STRUCTURE"));
}
