package dev.aqua;

import dev.aqua.module.InspectModule;
import dev.aqua.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

/** Time-based karambit-style flip applied to the first-person main hand. */
public final class InspectAnimation {
    private static boolean active;
    private static long startMs;
    private static int startSlot;

    private InspectAnimation() {}

    public static boolean isActive() { return active; }

    public static void start(MinecraftClient mc) {
        InspectModule m = ModuleManager.inspect;
        if (m == null || !m.enabled || active || mc.player == null) return;
        active = true;
        startMs = Util.getMeasuringTimeMs();
        startSlot = mc.player.getInventory().getSelectedSlot();
    }

    public static void tick(MinecraftClient mc) {
        if (!active) return;
        if (mc.player == null || mc.currentScreen != null
                || mc.player.getInventory().getSelectedSlot() != startSlot
                || mc.player.handSwinging
                || progress() >= 1f) {
            active = false;
        }
    }

    private static float progress() {
        double dur = 1900.0 / ModuleManager.inspect.get("speed");
        return (float) Math.max(0, Math.min(1, (Util.getMeasuringTimeMs() - startMs) / dur));
    }

    private static float smooth(float a, float b, float v) {
        float t = Math.max(0, Math.min(1, (v - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    private static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    /** Called at the head of HeldItemRenderer#renderFirstPersonItem, inside a pushed matrix. */
    public static void apply(MatrixStack m, Arm arm) {
        InspectModule cfg = ModuleManager.inspect;
        float t = progress();
        float side = arm == Arm.RIGHT ? 1f : -1f;

        float lift = smooth(0f, 0.16f, t) - smooth(0.90f, 1f, t);
        float spinT = Math.max(0, Math.min(1, (t - 0.14f) / 0.72f));
        float spin = easeInOutCubic(spinT) * 360f * (float) cfg.get("spins");
        float tilt = (float) cfg.get("tilt");
        float wobble = (float) Math.sin(Math.toRadians(spin)) * 10f * lift;

        float hx = 0.56f * side, hy = -0.52f, hz = -0.72f;

        m.translate(hx, hy, hz);
        m.translate(-0.30f * lift * side, 0.34f * lift, 0.12f * lift);
        m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-tilt * lift));
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(tilt * 0.8f * lift * side));
        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-spin * side));
        m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(wobble));
        m.translate(-hx, -hy, -hz);
    }
}
