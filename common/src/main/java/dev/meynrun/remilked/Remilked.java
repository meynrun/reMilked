package dev.meynrun.remilked;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class Remilked {
    public static final String MOD_ID = "remilked";

    public static final EntityDataAccessor<Integer> MILK_LEVEL = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);

    // Maximum amount of milk a player can have
    public static final int MAX_MILK_LEVEL = 10;

    // Cooldown (in ticks) between milking actions
    public static final int COOLDOWN_TICKS = 10;

    // Interval (in ticks) for milk regeneration
    public static final int REGENERATION_TICKS = 600;

    public static void init() {
        Sounds.init();
        registerEvents();
    }

    // Registers all gameplay-related events.
    private static void registerEvents() {

        // Reset milk level on player respawn
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, removalReason) -> player.getEntityData().set(MILK_LEVEL, MAX_MILK_LEVEL));

        // Handle self-milking on right click
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            // If player is sneaking, looking down, and holding an empty bucket
            if (player.isShiftKeyDown() && player.getXRot() >= 45.0F && stack.is(Items.BUCKET)) {
                if (!player.getCooldowns().isOnCooldown(Items.BUCKET)) {
                    InteractionResult result = handleMilking(player, player, player.level(), hand, true);
                    if (result.consumesAction()) {
                        return CompoundEventResult.interruptTrue(player.getItemInHand(hand));
                    }
                }
            }
            return CompoundEventResult.pass();
        });

        // Handle other player milking
        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (entity instanceof Player target) {
                InteractionResult result = handleMilking(player, target, player.level(), hand, false);
                if (result.consumesAction()) {
                    return EventResult.interruptTrue();
                }
            }
            return EventResult.pass();
        });

        // Milk regeneration
        TickEvent.SERVER_POST.register(server -> {
            if (server.getTickCount() % REGENERATION_TICKS == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    int currentLevel = player.getEntityData().get(MILK_LEVEL);
                    if (currentLevel < MAX_MILK_LEVEL) {
                        player.getEntityData().set(MILK_LEVEL, currentLevel + 1);
                    }
                }
            }
        });
    }

    /**
     * @param source Player performing the action
     * @param target Player being milked
     * @param level  Current world
     * @param hand   Used interaction hand
     * @param isSelf Whether the player is milking themselves
     */
    private static InteractionResult handleMilking(Player source, Player target, Level level, InteractionHand hand, boolean isSelf) {
        ItemStack bucketStack = source.getItemInHand(hand);

        // Basic validation checks
        if (!bucketStack.is(Items.BUCKET) || source.getCooldowns().isOnCooldown(Items.BUCKET) || target.getAbilities().invulnerable) {
            return InteractionResult.PASS;
        }

        int milkLevel = target.getEntityData().get(MILK_LEVEL);
        if (milkLevel <= 0) return InteractionResult.PASS;

        // Play sound and spawn particles
        var sound = isSelf ? Sounds.SELF_MILKED : Sounds.MILKED;
        level.playSound(null, source.getX(), source.getY(), source.getZ(), sound.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        // Particles are spawned client-side only
        if (level.isClientSide) {
            spawnParticles(level, target);
        }

        // Create a milk bucket with a custom name
        ItemStack milkBucket = new ItemStack(Items.MILK_BUCKET);
        milkBucket.set(DataComponents.ITEM_NAME, Component.translatable("item.remilked.player_milk", target.getName().getString()));

        // Try to insert into inventory, otherwise drop it
        if (!source.getInventory().add(milkBucket)) {
            source.drop(milkBucket, false);
        }

        // Decrease target's milk level
        int nextLevel = milkLevel - 1;
        target.getEntityData().set(MILK_LEVEL, nextLevel);

        // Consume bucket and apply cooldown (unless in creative mode)
        if (!source.getAbilities().instabuild) {
            bucketStack.shrink(1);
            source.getCooldowns().addCooldown(Items.BUCKET, COOLDOWN_TICKS);
        }

        // Damage logic when milk is fully depleted
        // NOTE: This is a workaround.
        // The goal here is to reliably kill the player with a custom damage message
        // without dealing an absurdly large amount of damage that would instantly
        // break all armor durability.
        //
        // By first reducing health to a tiny value and then applying a normal damage
        // source, we ensure (almost certainly) a death event while keeping armor intact.
        if (nextLevel <= 0) {
            ResourceKey<DamageType> damageType = isSelf ? DamageTypes.MILKED_SELF_TOO_MUCH : DamageTypes.MILKED_TOO_MUCH;
            target.removeAllEffects();
            target.setHealth(0.001f);
            target.hurt(DamageTypes.of(level, damageType), target.getMaxHealth());
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Spawns visual particles around the target player.
     * @param level  Current world
     * @param target Player being milked
     */
    private static void spawnParticles(Level level, Player target) {
        for (int i = 0; i < 5; i++) {
            double vx = (level.random.nextDouble() - 0.5) * 0.4;
            double vz = (level.random.nextDouble() - 0.5) * 0.4;
            level.addParticle(ParticleTypes.POOF, target.getX(), target.getY() + 1.0, target.getZ(), vx, -0.2, vz);
        }
    }
}