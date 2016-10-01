package me.reherhold.edifice.data.structure;

import static me.reherhold.edifice.data.EdificeKeys.STRUCTURE;

import me.reherhold.edifice.Structure;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;

import java.util.Optional;

public class ImmutableStructureData extends AbstractImmutableSingleData<Structure, ImmutableStructureData, StructureData> {

    protected ImmutableStructureData(Structure value) {
        super(value, STRUCTURE);
    }

    @Override
    public <E> Optional<ImmutableStructureData> with(Key<? extends BaseValue<E>> key, E value) {
        if (this.supports(key)) {
            return Optional.of(asMutable().set(key, value).asImmutable());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(STRUCTURE, getValue()).asImmutable();
    }

    @Override
    public StructureData asMutable() {
        return new StructureData(this.getValue());
    }
}
