package io.github.ranafangirl.dalektopia.client;

import io.github.ranafangirl.dalektopia.DalektopiaEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class DalektopiaClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(DalektopiaEntities.DALEK, DalekRenderer::new);
    }
}
