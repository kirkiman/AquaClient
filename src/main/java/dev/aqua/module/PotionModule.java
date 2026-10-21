package dev.aqua.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

/** Active status effects with level and remaining time. */
public class PotionModule extends HudModule {
    private static final String[] ROMAN = {"", "", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};
    private final List<String> lines = new ArrayList<>();

    public PotionModule() {
        super("potions", "Potion Effects", "Lists your active potion effects and how long they last.", 0.01, 0.30);
    }

    @Override
    protected boolean prepare(MinecraftClient mc, boolean editing) {
        lines.clear();
        if (mc.player != null) {
            for (StatusEffectInstance e : mc.player.getStatusEffects()) {
                int amp = e.getAmplifier();
                String lvl = amp < ROMAN.length ? ROMAN[amp] : " " + (amp + 1);
                String time;
                if (e.isInfinite()) time = "**:**";
                else {
                    int s = e.getDuration() / 20;
                    time = String.format("%d:%02d", s / 60, s % 60);
                }
                lines.add(e.getEffectType().value().getName().getString() + lvl + "  " + time);
            }
        }
        if (lines.isEmpty()) {
            if (!editing) return false;
            lines.add("Speed II  1:30");
            lines.add("Strength  0:45");
        }
        return true;
    }

    @Override protected int contentWidth(MinecraftClient mc) {
        int w = 0;
        for (String s : lines) w = Math.max(w, mc.textRenderer.getWidth(s));
        return w;
    }
    @Override protected int contentHeight(MinecraftClient mc) { return lines.size() * 10 - 1; }

    @Override
    protected void drawContent(DrawContext ctx, MinecraftClient mc, int rgb) {
        for (int i = 0; i < lines.size(); i++) ctx.drawText(mc.textRenderer, lines.get(i), 0, i * 10, rgb, true);
    }
}
