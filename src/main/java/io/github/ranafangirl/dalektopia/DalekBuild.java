package io.github.ranafangirl.dalektopia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

import java.util.Map;

public record DalekBuild(DalekPart base, Map<String, DalekPart> parts) {
    public static final Codec<DalekBuild> BUILD_CODEC = RecordCodecBuilder.create(i -> i.group(DalekPart.CODEC.fieldOf("base").forGetter(DalekBuild::base), Codec.unboundedMap(Codec.STRING, DalekPart.CODEC).fieldOf("parts").forGetter(DalekBuild::parts)).apply(i, DalekBuild::new));
    public static final DalekBuild DEFAULT_CODEC = new DalekBuild(DalekPart.of(Identifier.of(Dalektopia.MOD_ID, "default_dalek_skirt")), Map.of());
}
