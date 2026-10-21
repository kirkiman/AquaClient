package dev.aqua.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Base class for every draggable HUD element. Subclasses describe their content;
 * this class handles position, scale, background, color and the edit-mode outline.
 */
public abstract class HudModule extends Module {
    /** Aqua first, then a few accents. */
    public static final int[] COLORS = {0x4DE8FF, 0xFFFFFF, 0xFF6B81, 0xFFC857, 0x7BED9F, 0xB388FF};

    /** Position as a fraction of the screen so it survives GUI scale / resolution changes. */
    public double x, y;
    /** Last drawn bounds in screen pixels, used for drag hit-testing. */
    public int bx, by, bw, bh;

    protected HudModule(String id, String name, String desc, double x, double y) {
        super(id, name, desc);
        this.x = x; this.y = y;
        settings.add(Setting.number("scale", "Scale", 1.0, 0.6, 2.0, 0.1));
        settings.add(Setting.number("color", "Color", 0, 0, COLORS.length - 1, 1));
        settings.add(Setting.toggle("background", "Background", true));
    }

    /** Refresh state for this frame. Return false to draw nothing (in edit mode, return true with placeholder data). */
    protected abstract boolean prepare(MinecraftClient mc, boolean editing);
    protected abstract int contentWidth(MinecraftClient mc);
    protected abstract int contentHeight(MinecraftClient mc);
    protected abstract void drawContent(DrawContext ctx, MinecraftClient mc, int rgb);

    public final void render(DrawContext ctx, MinecraftClient mc, int sw, int sh, boolean editing, boolean highlight) {
        if (!prepare(mc, editing)) { bw = 0; return; }

        float sc = (float) get("scale");
        int pad = 4;
        int iw = contentWidth(mc) + pad * 2 + 2;
        int ih = contentHeight(mc) + pad * 2;
        bw = (int) Math.ceil(iw * sc);
        bh = (int) Math.ceil(ih * sc);
        bx = Math.max(0, Math.min((int) (x * sw), Math.max(0, sw - bw)));
        by = Math.max(0, Math.min((int) (y * sh), Math.max(0, sh - bh)));

        int rgb = 0xFF000000 | COLORS[(int) get("color")];
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate((float) bx, (float) by);
        m.scale(sc, sc);
        if (on("background")) {
            ctx.fill(0, 0, iw, ih, 0x90000000);
            ctx.fill(0, 0, 2, ih, rgb);
        }
        m.pushMatrix();
        m.translate((float) (pad + 2), (float) pad);
        drawContent(ctx, mc, rgb);
        m.popMatrix();
        if (editing) {
            int c = highlight ? 0xFFFFFFFF : 0x55FFFFFF;
            ctx.fill(0, 0, iw, 1, c);
            ctx.fill(0, ih - 1, iw, ih, c);
            ctx.fill(0, 0, 1, ih, c);
            ctx.fill(iw - 1, 0, iw, ih, c);
        }
        m.popMatrix();
    }

    public boolean contains(double mx, double my) {
        return bw > 0 && mx >= bx && mx < bx + bw && my >= by && my < by + bh;
    }

    public void moveTo(int px, int py, int sw, int sh) {
        x = Math.max(0, Math.min(1, px / (double) sw));
        y = Math.max(0, Math.min(1, py / (double) sh));
    }
}
