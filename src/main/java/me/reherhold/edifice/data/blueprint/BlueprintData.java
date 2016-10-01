package me.reherhold.edifice.data.blueprint;

import static me.reherhold.edifice.data.EdificeKeys.BLUEPRINT;

import com.google.common.base.Preconditions;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class BlueprintData extends AbstractSingleData<String, BlueprintData, ImmutableBlueprintData> {

    public BlueprintData(String value) {
        super(value, BLUEPRINT);
    }

    @Override
    public BlueprintData copy() {
        return new BlueprintData(this.getValue());
    }

    @Override
    public Optional<BlueprintData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
        BlueprintData warpData = Preconditions.checkNotNull(mergeFn).merge(copy(), dataHolder.get(BlueprintData.class).orElse(copy()));
        return Optional.of(set(BLUEPRINT, warpData.get(BLUEPRINT).get()));
    }

    @Override
    public Optional<BlueprintData> from(DataContainer container) {
        if (container.contains(BLUEPRINT.getQuery())) {
            return Optional.of(set(BLUEPRINT, container.getString(BLUEPRINT.getQuery()).orElse(getValue())));
        }
        return Optional.empty();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public ImmutableBlueprintData asImmutable() {
        return new ImmutableBlueprintData(this.getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(BLUEPRINT, getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(BLUEPRINT, getValue());
    }

}
