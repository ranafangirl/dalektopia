package io.github.ranafangirl.dalektopia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dalektopia implements ModInitializer {
    public static final String MOD_ID = "dalektopia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        DalektopiaEntities.register();
        DalektopiaItems.register();
        DynamicRegistries.registerSynced(DalekType.KEY, DalekType.CODEC);

        LOGGER.info("Dalektopia started!");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Registry<DalekType> types = server.getRegistryManager().get(DalekType.KEY);
            types.getEntrySet().forEach(e -> LOGGER.info("[type] {} -> base={}, points={}",
                    e.getKey().getValue(), e.getValue().base(), e.getValue().parts().keySet()));
        });
    }
}