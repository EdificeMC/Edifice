package me.reherhold.edifice.data.blueprint;

import static me.reherhold.edifice.data.EdificeKeys.BLUEPRINT;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

public class ImmutableBlueprintData extends AbstractImmutableSingleData<String, ImmutableBlueprintData, BlueprintData> {

    protected ImmutableBlueprintData(String value) {
        super(value, BLUEPRINT);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public BlueprintData asMutable() {
        return new BlueprintData(this.getValue());
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(BLUEPRINT, getValue()).asImmutable();
    }

}
