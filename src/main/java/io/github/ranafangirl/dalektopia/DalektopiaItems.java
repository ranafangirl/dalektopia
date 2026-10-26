package io.github.ranafangirl.dalektopia;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class DalektopiaItems {
    public static final Item DALEK_SPAWN_EGG = Registry.register(
            Registries.ITEM,
            Identifier.of(Dalektopia.MOD_ID, "dalek_spawn_egg"),
            new SpawnEggItem(DalektopiaEntities.DALEK, 0xC3CBD3, 0x7DCBE1, new Item.Settings())
    );

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
                .register(entries -> entries.add(DALEK_SPAWN_EGG));
    }
}
