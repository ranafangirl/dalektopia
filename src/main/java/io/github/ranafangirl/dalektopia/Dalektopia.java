package io.github.ranafangirl.dalektopia;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.boss.dragon.EnderDragonPart;

public class Dalektopia implements ModInitializer {
    public static final String MOD_ID = "dalektopia";

    @Override
    public void onInitialize() {
        DalektopiaEntities.register();
        DalektopiaItems.register();
    }
}