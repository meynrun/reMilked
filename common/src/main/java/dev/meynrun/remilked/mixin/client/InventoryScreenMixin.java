package dev.meynrun.remilked.mixin.client;

import dev.meynrun.remilked.Remilked;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    /**
     * Renders the milk meter overlay on the inventory screen.
     * Injected at the end of InventoryScreen#render.
     */
    @Inject(method = "render", at = @At("TAIL"))
    private void remilked$renderMilkMeter(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        // Player may be null during early screen initialization
        if (player == null) return;

        int milkLevel = player.getEntityData().get(Remilked.MILK_LEVEL);

        // Bar dimensions
        int barHeight = 60;
        int barWidth = 8;

        // Bar position (relative to the screen center)
        int barX = (graphics.guiWidth() / 2) - 120; // Slightly shifted to the left
        int barY = (graphics.guiHeight() / 2) - 30;

        // Calculate fill amount based on current milk level
        float fillRatio = (float) milkLevel / Remilked.MAX_MILK_LEVEL;
        int filledHeight = (int) (barHeight * fillRatio);

        // Draw background (semi-transparent black)
        graphics.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0xFF000000);

        // Draw filled portion of the bar (white)
        int top = barY + (barHeight - filledHeight);
        graphics.fill(barX, top, barX + barWidth, barY + barHeight, 0xFFFFFFFF);

        // Draw a simple border around the bar
        this.remilked$drawSimpleBorder(graphics, barX, barY, barWidth, barHeight);
    }

    /**
     * Draws a simple 1-pixel border around a rectangle.
     */
    @Unique
    private void remilked$drawSimpleBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        int borderColor = 0xFF646464;

        // Top border
        graphics.fill(x - 1, y - 1, x + width + 1, y, borderColor);
        // Bottom border
        graphics.fill(x - 1, y + height, x + width + 1, y + height + 1, borderColor);
        // Left border
        graphics.fill(x - 1, y, x, y + height, borderColor);
        // Right border
        graphics.fill(x + width, y, x + width + 1, y + height, borderColor);
    }
}