package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.Dalektopia;
import net.minecraft.util.Identifier;

import java.util.List;

public record DalekPart(Identifier model, Identifier texture, String point, boolean aims, List<DalekPart> parts) {

    public static DalekPart of(String part, String point, List<DalekPart> parts) {
        return new DalekPart(id("geo/entity/dalek/" + part + ".geo.json"), id("textures/entity/dalek/" + part + ".png"), point, false, parts);
    }

    public static DalekPart aiming(String part, String point, List<DalekPart> parts) {
        return new DalekPart(id("geo/entity/dalek/" + part + ".geo.json"), id("textures/entity/dalek/" + part + ".png"), point, true, parts);
    }

    private static Identifier id(String path) {
        return Identifier.of(Dalektopia.MOD_ID, path);
    }
}