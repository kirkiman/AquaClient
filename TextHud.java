package dev.aqua.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.util.function.Function;

/** Single-line text readout (FPS, coords, ping, ...). */
public class TextHud extends HudModule {
    private final Function<MinecraftClient, String> source;
    private String text = "";

    public TextHud(String id, String name, String desc, double x, double y, Function<MinecraftClient, String> source) {
        super(id, name, desc, x, y);
        this.source = source;
    }

    /** Adds an animated aqua wave to the text. */
    public TextHud wave() {
        settings.add(Setting.toggle("wave", "Wave animation", true));
        return this;
    }

    @Override
    protected boolean prepare(MinecraftClient mc, boolean editing) {
        String s = source.apply(mc);
        if (s == null) {
            if (!editing) return false;
            s = name;
        }
        text = s;
        return true;
    }

    @Override protected int contentWidth(MinecraftClient mc) { return mc.textRenderer.getWidth(text); }
    @Override protected int contentHeight(MinecraftClient mc) { return mc.textRenderer.fontHeight; }

    @Override
    protected void drawContent(DrawContext ctx, MinecraftClient mc, int rgb) {
        if (setting("wave") != null && on("wave")) {
            double t = Util.getMeasuringTimeMs() / 450.0;
            int cx = 0;
            for (int i = 0; i < text.length(); i++) {
                String ch = String.valueOf(text.charAt(i));
                double k = (Math.sin(t + i * 0.45) + 1) / 2;
                ctx.drawText(mc.textRenderer, ch, cx, 0, lerp(rgb, 0xFF3D7BFF, k), true);
                cx += mc.textRenderer.getWidth(ch);
            }
        } else {
            ctx.drawText(mc.textRenderer, text, 0, 0, rgb, true);
        }
    }

    public static int lerp(int a, int b, double t) {
        int ar = a >> 16 & 255, ag = a >> 8 & 255, ab = a & 255;
        int br = b >> 16 & 255, bg = b >> 8 & 255, bb = b & 255;
        return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }
}
