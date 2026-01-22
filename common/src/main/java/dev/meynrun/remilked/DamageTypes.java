package dev.meynrun.remilked;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

import static dev.meynrun.remilked.Remilked.MOD_ID;

public interface DamageTypes {
    ResourceKey<DamageType> MILKED_SELF_TOO_MUCH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "milked_self_too_much")
    );

    ResourceKey<DamageType> MILKED_TOO_MUCH = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "milked_too_much")
    );

    static DamageSource of(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(key)
        );
    }
}