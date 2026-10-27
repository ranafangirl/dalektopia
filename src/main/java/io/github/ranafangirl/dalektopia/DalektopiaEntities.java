package io.github.ranafangirl.dalektopia;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DalektopiaEntities {
    public static final EntityType<DalekEntity> DALEK = Registry.register(Registries.ENTITY_TYPE, Identifier.of(Dalektopia.MOD_ID, "dalek"), EntityType.Builder.create(DalekEntity::new, SpawnGroup.CREATURE).dimensions(0.6f, 1.2f).build("dalek"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(DALEK, DalekEntity.createDalekAttributes());
    }
}