package me.reherhold.edifice.data.structure;

import static me.reherhold.edifice.data.EdificeKeys.STRUCTURE;
import com.google.common.base.Preconditions;
import me.reherhold.edifice.Structure;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.mutable.Value;

import java.util.Optional;

public class StructureData extends AbstractSingleData<Structure, StructureData, ImmutableStructureData> {

    protected StructureData(Structure value) {
        super(value, STRUCTURE);
    }

    @Override
    public StructureData copy() {
        return new StructureData(this.getValue());
    }

    @Override
    public Optional<StructureData> fill(DataHolder dataHolder, MergeFunction mergeFn) {
        StructureData warpData = Preconditions.checkNotNull(mergeFn).merge(copy(), dataHolder.get(StructureData.class).orElse(copy()));
        return Optional.of(set(STRUCTURE, warpData.get(STRUCTURE).get()));
    }

    @Override
    public Optional<StructureData> from(DataContainer container) {
        if (container.contains(STRUCTURE.getQuery())) {
            return Optional.of(set(STRUCTURE, container.getSerializable(STRUCTURE.getQuery(), Structure.class).orElse(getValue())));
        }
        return Optional.empty();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public ImmutableStructureData asImmutable() {
        return new ImmutableStructureData(this.getValue());
    }

    @Override
    public int compareTo(StructureData arg0) {
        return 0;
    }

    @Override
    protected Value<Structure> getValueGetter() {
        return Sponge.getRegistry().getValueFactory().createValue(STRUCTURE, getValue(), getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer().set(STRUCTURE, getValue());
    }
}
