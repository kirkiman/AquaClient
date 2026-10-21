package dev.aqua.gui;

import dev.aqua.AquaClient;
import dev.aqua.module.HudModule;
import dev.aqua.module.Module;
import dev.aqua.module.ModuleManager;
import dev.aqua.module.Setting;
import dev.aqua.module.TextHud;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Aqua menu. Mouse/keyboard are polled through GLFW inside render() instead of overriding
 * the input callbacks, so the screen is immune to input-API changes between versions.
 */
public class MenuScreen extends Screen {
    private static final int W = 470, H = 310;
    private static final int LIST_X = 14, LIST_Y = 60, LIST_W = 150, ROW_H = 20;
    private static final int PANE_X = 182, PANE_W = 274, SET_Y = 130, SET_H = 22;

    private final long opened = Util.getMeasuringTimeMs();
    private Module selected = ModuleManager.MODULES.get(0);
    private HudModule dragging;
    private int dragDX, dragDY, scroll;
    private boolean armed, wasDown;

    public MenuScreen() { super(Text.literal("Aqua Client")); }

    // ---------- helpers ----------
    private float openProgress() {
        float t = Math.min(1f, (Util.getMeasuringTimeMs() - opened) / 240f);
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }
    private int wx() { return (width - W) / 2; }
    private int wy() { return (height - H) / 2 + (int) ((1f - openProgress()) * 18); }
    private int visibleRows() { return (H - LIST_Y - 30) / ROW_H; }

    private int accent() {
        double k = (Math.sin(Util.getMeasuringTimeMs() / 900.0) + 1) / 2;
        return TextHud.lerp(0xFF2DE2E6, 0xFF4D9BFF, k);
    }
    private static int argb(int a, int rgb) { return (a << 24) | (rgb & 0xFFFFFF); }
    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Rectangle with notched corners (cheap rounded look). */
    private void box(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x + 1, y, x + w - 1, y + h, color);
        c.fill(x, y + 1, x + w, y + h - 1, color);
    }

    private void txt(DrawContext c, String s, int x, int y, int color, float sc) {
        var m = c.getMatrices();
        m.pushMatrix();
        m.translate((float) x, (float) y);
        m.scale(sc, sc);
        c.drawText(textRenderer, s, 0, 0, color, false);
        m.popMatrix();
    }
    private int tw(String s, float sc) { return (int) (textRenderer.getWidth(s) * sc); }

    private void gradientText(DrawContext c, String s, int x, int y, float sc) {
        double t = Util.getMeasuringTimeMs() / 500.0;
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            int col = TextHud.lerp(0xFF4DE8FF, 0xFF3D7BFF, (Math.sin(t + i * 0.6) + 1) / 2);
            txt(c, ch, cx, y, col, sc);
            cx += tw(ch, sc) + 1;
        }
    }

    private void pill(DrawContext c, int x, int y, boolean on, int ac) {
        box(c, x, y, 24, 10, on ? argb(0xC0, ac) : 0xFF2A3440);
        c.fill(x + (on ? 14 : 2), y + 2, x + (on ? 22 : 10), y + 8, 0xFFFFFFFF);
    }
    private void click() {
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    // ---------- render ----------
    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long win = client.getWindow().getHandle();
        boolean shift = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        if (!shift) armed = true; else if (armed) { close(); return; }

        boolean down = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        if (down && !wasDown) onPress(mx, my);
        if (down && dragging != null) dragging.moveTo(mx - dragDX, my - dragDY, width, height);
        if (!down && wasDown && dragging != null) { dragging = null; ModuleManager.save(); }
        wasDown = down;

        float a = openProgress();
        int ac = accent();
        long now = Util.getMeasuringTimeMs();

        ctx.fillGradient(0, 0, width, height, argb((int) (0xC0 * a), 0x04101C), argb((int) (0xE0 * a), 0x000308));

        // rising bubbles
        for (int i = 0; i < 18; i++) {
            int bx = (i * 97 + 31) % Math.max(1, width);
            double speed = 0.015 + (i % 5) * 0.006;
            int by = height - (int) ((now * speed + i * 53) % (height + 20));
            int r = 2 + i % 3;
            ctx.fill(bx, by, bx + r, by + r, argb((int) (0x38 * a), 0x4DE8FF));
        }

        // draggable HUD modules over the dim
        for (Module m : ModuleManager.MODULES) {
            if (m.enabled && m instanceof HudModule h) h.render(ctx, client, width, height, true, h == selected || h == dragging);
        }

        int x = wx(), y = wy();
        box(ctx, x - 2, y - 2, W + 4, H + 4, argb(0x60, ac));
        box(ctx, x, y, W, H, 0xF00A121C);
        ctx.fillGradient(x, y, x + W, y + 46, argb(0xFF, 0x0E3350), argb(0x00, 0x0A121C));

        gradientText(ctx, "AQUA", x + 16, y + 9, 2.4f);
        txt(ctx, "CLIENT", x + 16 + tw("AQUA", 2.4f) + 14, y + 17, 0xFFBFEFFF, 1.1f);
        String ver = "v" + AquaClient.VERSION + "  -  1.21.11";
        txt(ctx, ver, x + W - 14 - tw(ver, 1f), y + 18, 0xFF6F93A8, 1f);
        ctx.fill(x + 14, y + 44, x + W - 14, y + 45, argb(0x90, ac));
        ctx.fill(x + PANE_X - 9, y + 54, x + PANE_X - 8, y + H - 30, 0x22FFFFFF);

        // module list (scrollable)
        List<Module> mods = ModuleManager.MODULES;
        int rows = visibleRows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, mods.size() - rows)));
        for (int r = 0; r < rows && r + scroll < mods.size(); r++) {
            Module m = mods.get(r + scroll);
            int rx = x + LIST_X, ry = y + LIST_Y + r * ROW_H;
            boolean hov = in(mx, my, rx, ry, LIST_W, ROW_H - 2), sel = m == selected;
            box(ctx, rx, ry, LIST_W, ROW_H - 2, sel ? argb(0x45, ac) : hov ? 0x26FFFFFF : 0x0CFFFFFF);
            if (sel) ctx.fill(rx, ry + 1, rx + 2, ry + ROW_H - 3, ac);
            txt(ctx, m.name, rx + 9, ry + 5, m.enabled ? 0xFFFFFFFF : 0xFF60768A, 1.0f);
            pill(ctx, rx + LIST_W - 30, ry + 4, m.enabled, ac);
        }
        if (mods.size() > rows) {
            int th = rows * ROW_H, bh = Math.max(12, th * rows / mods.size());
            int by = y + LIST_Y + (th - bh) * scroll / Math.max(1, mods.size() - rows);
            ctx.fill(x + LIST_X + LIST_W + 3, by, x + LIST_X + LIST_W + 5, by + bh, argb(0x90, ac));
        }

        // settings pane
        if (selected != null) {
            int px = x + PANE_X, py = y + LIST_Y;
            txt(ctx, selected.name, px, py - 4, 0xFFFFFFFF, 1.7f);
            txt(ctx, selected.enabled ? "ENABLED" : "DISABLED", px, py + 14, selected.enabled ? ac : 0xFF60768A, 0.9f);
            List<OrderedText> lines = textRenderer.wrapLines(Text.literal(selected.description), PANE_W);
            for (int i = 0; i < Math.min(3, lines.size()); i++)
                ctx.drawText(textRenderer, lines.get(i), px, py + 28 + i * 10, 0xFF8FA6B8, false);
            int right = px + PANE_W;
            for (int j = 0; j < selected.settings.size(); j++) {
                Setting s = selected.settings.get(j);
                int sy = y + SET_Y + j * SET_H;
                box(ctx, px, sy, PANE_W, SET_H - 3, 0x16FFFFFF);
                txt(ctx, s.label, px + 8, sy + 5, 0xFFE0F0FA, 1.0f);
                if (s.toggle) {
                    pill(ctx, right - 34, sy + 4, s.on(), ac);
                } else {
                    boolean hl = in(mx, my, right - 100, sy, 16, SET_H - 3), hr = in(mx, my, right - 20, sy, 16, SET_H - 3);
                    txt(ctx, "<", right - 96, sy + 4, hl ? ac : 0xFF7C93A6, 1.3f);
                    txt(ctx, ">", right - 16, sy + 4, hr ? ac : 0xFF7C93A6, 1.3f);
                    int cx = right - 58;
                    if (s.key.equals("color")) {
                        ctx.fill(cx - 10, sy + 5, cx + 10, sy + 13, 0xFF000000 | HudModule.COLORS[(int) s.value]);
                    } else {
                        String v = s.display();
                        txt(ctx, v, cx - tw(v, 1f) / 2, sy + 5, 0xFFFFFFFF, 1.0f);
                    }
                }
            }
        }
        txt(ctx, "drag modules to move them  -  scroll the list  -  right shift to close", x + 14, y + H - 20, 0xFF58778C, 0.9f);
    }

    // ---------- input ----------
    private void onPress(int mx, int my) {
        int x = wx(), y = wy();
        List<Module> mods = ModuleManager.MODULES;
        if (in(mx, my, x, y, W, H)) {
            for (int r = 0; r < visibleRows() && r + scroll < mods.size(); r++) {
                int rx = x + LIST_X, ry = y + LIST_Y + r * ROW_H;
                if (in(mx, my, rx, ry, LIST_W, ROW_H - 2)) {
                    Module m = mods.get(r + scroll);
                    if (mx >= rx + LIST_W - 34) m.enabled = !m.enabled; else selected = m;
                    click();
                    return;
                }
            }
            if (selected != null) {
                int right = x + PANE_X + PANE_W;
                for (int j = 0; j < selected.settings.size(); j++) {
                    Setting s = selected.settings.get(j);
                    int sy = y + SET_Y + j * SET_H;
                    if (s.toggle) {
                        if (in(mx, my, right - 36, sy, 30, SET_H - 3)) { s.bump(0); click(); return; }
                    } else if (in(mx, my, right - 100, sy, 20, SET_H - 3)) { s.bump(-1); click(); return; }
                    else if (in(mx, my, right - 22, sy, 20, SET_H - 3)) { s.bump(1); click(); return; }
                }
            }
            return;
        }
        for (int i = mods.size() - 1; i >= 0; i--) {
            if (mods.get(i) instanceof HudModule h && h.enabled && h.contains(mx, my)) {
                dragging = h; selected = h;
                dragDX = mx - h.bx; dragDY = my - h.by;
                return;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
        scroll -= (int) Math.signum(vertical);
        return true;
    }

    @Override
    public void removed() {
        ModuleManager.save();
        super.removed();
    }

    @Override
    public boolean shouldPause() { return false; }
}
