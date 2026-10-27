package io.github.ranafangirl.dalektopia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Optional;

public record DalekPartChoice(Identifier part, Optional<Identifier> texture, int weight) {
    private static final Codec<DalekPartChoice> OBJECT = RecordCodecBuilder.create(i -> i.group(Identifier.CODEC.fieldOf("part").forGetter(DalekPartChoice::part), Identifier.CODEC.optionalFieldOf("texture").forGetter(DalekPartChoice::texture), Codecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(DalekPartChoice::weight)).apply(i, DalekPartChoice::new));
    public static final Codec<DalekPartChoice> CODEC = Codec.withAlternative(OBJECT, Identifier.CODEC.xmap(id -> new DalekPartChoice(id, Optional.empty(), 1), DalekPartChoice::part));
    public static final Codec<List<DalekPartChoice>> LIST = Codec.withAlternative(CODEC.listOf(), CODEC.xmap(List::of, list -> list.get(0)));

    public DalekPart toPart() {
        return new DalekPart(part, texture.orElse(part));
    }
}
