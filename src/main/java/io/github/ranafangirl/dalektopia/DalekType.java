package io.github.ranafangirl.dalektopia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DalekType(List<DalekPartChoice> base, DalekStatistics stats, Map<String, List<DalekPartChoice>> parts) {
    public static final RegistryKey<Registry<DalekType>> KEY = RegistryKey.ofRegistry(Identifier.of(Dalektopia.MOD_ID, "dalek"));
    public static final Codec<DalekType> CODEC = RecordCodecBuilder.create(i -> i.group(
            DalekPartChoice.LIST.fieldOf("base").forGetter(DalekType::base),
            DalekStatistics.CODEC.optionalFieldOf("stats", DalekStatistics.DEFAULT).forGetter(DalekType::stats),
            Codec.unboundedMap(Codec.STRING, DalekPartChoice.LIST).fieldOf("parts").forGetter(DalekType::parts)
    ).apply(i, DalekType::new));

    public DalekBuild resolve(Random random) {
        DalekPart chosenBase = base.isEmpty() ? DalekBuild.DEFAULT_CODEC.base() : pick(base, random);
        Map<String, DalekPart> chosen = new LinkedHashMap<>();
        parts.forEach((point, choices) -> {
            if (!choices.isEmpty()) chosen.put(point, pick(choices, random));
        });
        return new DalekBuild(chosenBase, chosen, stats);
    }

    private static DalekPart pick(List<DalekPartChoice> choices, Random random) {
        int roll = random.nextInt(choices.stream().mapToInt(DalekPartChoice::weight).sum());
        for (DalekPartChoice choice : choices) {
            roll -= choice.weight();
            if (roll < 0) return choice.toPart();
        }
        return choices.get(0).toPart();
    }
}
