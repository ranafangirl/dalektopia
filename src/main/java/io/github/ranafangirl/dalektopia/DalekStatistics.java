package io.github.ranafangirl.dalektopia;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DalekStatistics(double maxHealth, double speed, double scale, float width, float height) {
    public static final DalekStatistics DEFAULT = new DalekStatistics(20.0, 0.2, 1.0, 0.9f, 1.6f);

    public static final Codec<DalekStatistics> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.doubleRange(1.0, 1024.0).optionalFieldOf("max_health", DEFAULT.maxHealth()).forGetter(DalekStatistics::maxHealth),
            Codec.doubleRange(0.01, 2.0).optionalFieldOf("speed", DEFAULT.speed()).forGetter(DalekStatistics::speed),
            Codec.doubleRange(0.01, 2.0).optionalFieldOf("scale", DEFAULT.scale()).forGetter(DalekStatistics::scale),
            Codec.floatRange(0.2f, 8.0f).optionalFieldOf("width", DEFAULT.width()).forGetter(DalekStatistics::width),
            Codec.floatRange(0.2f, 8.0f).optionalFieldOf("height", DEFAULT.height()).forGetter(DalekStatistics::height)
    ).apply(i, DalekStatistics::new));
}
