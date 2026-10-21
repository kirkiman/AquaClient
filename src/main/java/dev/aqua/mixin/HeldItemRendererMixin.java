package dev.aqua.mixin;

import dev.aqua.InspectAnimation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NOTE: this signature targets 1.21.11 (render-command-queue era). The mixin config is
 * marked non-required, so if it ever fails to apply only the Inspect animation is lost.
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void aqua$begin(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                            float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                            OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        matrices.push(); // always push so the RETURN pop stays balanced for both hands
        if (hand == Hand.MAIN_HAND && InspectAnimation.isActive()) {
            InspectAnimation.apply(matrices, player.getMainArm());
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void aqua$end(AbstractClientPlayerEntity player, float tickProgress, float pitch, Hand hand,
                          float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                          OrderedRenderCommandQueue queue, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
