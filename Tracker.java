package dev.aqua;

import net.minecraft.client.MinecraftClient;

/** Per-tick data such as horizontal speed in blocks/second. */
public final class Tracker {
    public static double speed;
    private static double lx, lz;
    private static boolean has;

    private Tracker() {}

    public static void tick(MinecraftClient mc) {
        if (mc.player == null) { has = false; speed = 0; return; }
        double x = mc.player.getX(), z = mc.player.getZ();
        if (has) {
            double s = Math.hypot(x - lx, z - lz) * 20.0;
            speed = speed * 0.6 + s * 0.4;
        }
        lx = x; lz = z; has = true;
    }
}
