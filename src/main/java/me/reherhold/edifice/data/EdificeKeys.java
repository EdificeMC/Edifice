package me.reherhold.edifice.data;

import com.google.common.reflect.TypeToken;
import me.reherhold.edifice.Structure;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.KeyFactory;
import org.spongepowered.api.data.value.mutable.Value;

public class EdificeKeys {

    private static final TypeToken<String> STRING_TOKEN = new TypeToken<String>() {

        private static final long serialVersionUID = -1;
    };

    private static final TypeToken<Value<String>> STRING_VALUE_TOKEN = new TypeToken<Value<String>>() {

        private static final long serialVersionUID = -1;
    };

    private static final TypeToken<Structure> STRUCTURE_TOKEN = new TypeToken<Structure>() {

        private static final long serialVersionUID = -1;
    };

    private static final TypeToken<Value<Structure>> STRUCTURE_VALUE_TOKEN = new TypeToken<Value<Structure>>() {

        private static final long serialVersionUID = -1;
    };

    public static final Key<Value<String>> BLUEPRINT = KeyFactory.makeSingleKey(STRING_TOKEN, STRING_VALUE_TOKEN,
            DataQuery.of("Blueprint"), "edifice:blueprint", "Blueprint");

    public static final Key<Value<Structure>> STRUCTURE = KeyFactory.makeSingleKey(STRUCTURE_TOKEN,
            STRUCTURE_VALUE_TOKEN, DataQuery.of("Structure"), "edifice:structure", "Structure");
}
