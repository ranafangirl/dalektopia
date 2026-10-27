package io.github.ranafangirl.dalektopia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public record DalekPart(Identifier id, Identifier skin) {
    public static final Codec<DalekPart> CODEC = RecordCodecBuilder.create(i -> i.group(Identifier.CODEC.fieldOf("id").forGetter(DalekPart::id), Identifier.CODEC.fieldOf("skin").forGetter(DalekPart::skin)).apply(i, DalekPart::new));

    public static DalekPart of(Identifier id) {
        return new DalekPart(id, id);
    }

    public Identifier model() {
        return Identifier.of(id.getNamespace(), "geo/entity/dalek/" + id.getPath() + ".geo.json");
    }

    public Identifier texture() {
        return Identifier.of(skin.getNamespace(), "textures/entity/dalek/" + skin.getPath() + ".png");
    }
}