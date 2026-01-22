package dev.meynrun.remilked;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import static dev.meynrun.remilked.Remilked.MOD_ID;


public class Sounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(MOD_ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> MILKED = SOUNDS.register(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "milked"),
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "milked"))
    );

    public static final RegistrySupplier<SoundEvent> SELF_MILKED = SOUNDS.register(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "self_milked"),
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "self_milked"))
    );

    public static void init() {
        SOUNDS.register();
    }
}
