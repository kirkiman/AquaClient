package dev.aqua.module;

import dev.aqua.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;

/** WASD + mouse buttons + space, with CPS on the mouse keys. */
public class KeystrokesModule extends HudModule {
    private static final int K = 20, G = 2;

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", "Shows WASD, jump and mouse buttons as you press them, with CPS.", 0.01, 0.55);
        settings.add(Setting.toggle("mouse", "Mouse buttons", true));
        settings.add(Setting.toggle("space", "Space bar", true));
    }

    @Override protected boolean prepare(MinecraftClient mc, boolean editing) { return mc.player != null || editing; }
    @Override protected int contentWidth(MinecraftClient mc) { return K * 3 + G * 2; }
    @Override protected int contentHeight(MinecraftClient mc) {
        int h = K * 2 + G;
        if (on("mouse")) h += G + K;
        if (on("space")) h += G + 8;
        return h;
    }

    private void key(DrawContext ctx, MinecraftClient mc, int x, int y, int w, int h, String label, boolean down, int rgb) {
        ctx.fill(x, y, x + w, y + h, down ? (0xB0000000 | (rgb & 0xFFFFFF)) : 0x70000000);
        int tw = mc.textRenderer.getWidth(label);
        ctx.drawText(mc.textRenderer, label, x + (w - tw) / 2, y + (h - 8) / 2, down ? 0xFF001018 : 0xFFFFFFFF, false);
    }

    private void mouse(DrawContext ctx, MinecraftClient mc, int x, int y, int w, String label, boolean down, int cps, int rgb) {
        ctx.fill(x, y, x + w, y + K, down ? (0xB0000000 | (rgb & 0xFFFFFF)) : 0x70000000);
        int c = down ? 0xFF001018 : 0xFFFFFFFF;
        ctx.drawText(mc.textRenderer, label, x + (w - mc.textRenderer.getWidth(label)) / 2, y + 2, c, false);
        var m = ctx.getMatrices();
        String s = cps + " CPS";
        float sw = mc.textRenderer.getWidth(s) * 0.7f;
        m.pushMatrix();
        m.translate(x + (w - sw) / 2f, (float) (y + 11));
        m.scale(0.7f, 0.7f);
        ctx.drawText(mc.textRenderer, s, 0, 0, c, false);
        m.popMatrix();
    }

    @Override
    protected void drawContent(DrawContext ctx, MinecraftClient mc, int rgb) {
        var o = mc.options;
        int w3 = K * 3 + G * 2;
        key(ctx, mc, K + G, 0, K, K, "W", o.forwardKey.isPressed(), rgb);
        key(ctx, mc, 0, K + G, K, K, "A", o.leftKey.isPressed(), rgb);
        key(ctx, mc, K + G, K + G, K, K, "S", o.backKey.isPressed(), rgb);
        key(ctx, mc, (K + G) * 2, K + G, K, K, "D", o.rightKey.isPressed(), rgb);
        int y = K * 2 + G * 2;
        if (on("mouse")) {
            int half = (w3 - G) / 2;
            mouse(ctx, mc, 0, y, half, "LMB", Input.leftDown, Input.cpsLeft(), rgb);
            mouse(ctx, mc, half + G, y, w3 - half - G, "RMB", Input.rightDown, Input.cpsRight(), rgb);
            y += K + G;
        }
        if (on("space")) {
            boolean d = o.jumpKey.isPressed();
            ctx.fill(0, y, w3, y + 8, d ? (0xB0000000 | (rgb & 0xFFFFFF)) : 0x70000000);
            ctx.fill(w3 / 2 - 8, y + 3, w3 / 2 + 8, y + 5, d ? 0xFF001018 : 0xFFFFFFFF);
        }
    }
}
