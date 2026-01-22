package dev.meynrun.remilked.mixin;

import dev.meynrun.remilked.Remilked;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    /**
     * Registers custom synced entity data.
     * This is called during player entity initialization.
     */
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    protected void remilked$defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        // Initialize milk level with the maximum value
        builder.define(Remilked.MILK_LEVEL, Remilked.MAX_MILK_LEVEL);
    }

    /**
     * Writes custom player data to NBT.
     * Called when the player is saved (world save, logout, etc.).
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void remilked$writeCustomDataToNbt(CompoundTag nbt, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Persist current milk level
        nbt.putInt("remilked:milk_level", player.getEntityData().get(Remilked.MILK_LEVEL));
    }

    /**
     * Reads custom player data from NBT.
     * Called when the player is loaded back into the world.
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void remilked$readCustomDataFromNbt(CompoundTag nbt, CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Restore milk level if present
        if (nbt.contains("remilked:milk_level")) {
            player.getEntityData().set(Remilked.MILK_LEVEL, nbt.getInt("remilked:milk_level"));
        }
    }
}