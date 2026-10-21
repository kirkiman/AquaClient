package dev.aqua.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Armor HUD: equipped armor (and optionally hand items) with durability bars / percentages. */
public class ArmorModule extends HudModule {
    private static final int ICON = 16, GAP = 3;
    private final List<ItemStack> stacks = new ArrayList<>();

    public ArmorModule() {
        super("armor", "Armor HUD", "Shows your equipped armor with durability bars and percentages.", 0.88, 0.80);
        settings.add(Setting.choice("layout", "Layout", 0, "Horizontal", "Vertical"));
        settings.add(Setting.toggle("bar", "Durability bar", true));
        settings.add(Setting.toggle("percent", "Percentage", true));
        settings.add(Setting.toggle("hands", "Hand items", false));
    }

    @Override
    protected boolean prepare(MinecraftClient mc, boolean editing) {
        stacks.clear();
        if (mc.player != null) {
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot s : slots) {
                ItemStack st = mc.player.getEquippedStack(s);
                if (!st.isEmpty()) stacks.add(st);
            }
            if (on("hands")) {
                if (!mc.player.getMainHandStack().isEmpty()) stacks.add(mc.player.getMainHandStack());
                if (!mc.player.getOffHandStack().isEmpty()) stacks.add(mc.player.getOffHandStack());
            }
        }
        if (stacks.isEmpty()) {
            if (!editing) return false;
            for (int i = 0; i < 4; i++) stacks.add(ItemStack.EMPTY); // placeholder boxes while positioning
        }
        return true;
    }

    private boolean vertical() { return on("layout"); }

    private int cellH() {
        if (vertical()) return ICON + 2;
        return ICON + (on("bar") ? 3 : 0) + (on("percent") ? 8 : 0);
    }

    private int cellW(MinecraftClient mc) {
        int pct = mc.textRenderer.getWidth("100%");
        if (vertical()) return ICON + (on("percent") ? 4 + pct : 0);
        return Math.max(ICON, on("percent") ? pct * 3 / 4 + 1 : 0);
    }

    @Override protected int contentWidth(MinecraftClient mc) {
        int n = stacks.size();
        return vertical() ? cellW(mc) : n * cellW(mc) + (n - 1) * GAP;
    }

    @Override protected int contentHeight(MinecraftClient mc) {
        int n = stacks.size();
        return vertical() ? n * cellH() : cellH();
    }

    @Override
    protected void drawContent(DrawContext ctx, MinecraftClient mc, int rgb) {
        int cw = cellW(mc), ch = cellH();
        var m = ctx.getMatrices();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack st = stacks.get(i);
            int cx = vertical() ? 0 : i * (cw + GAP);
            int cy = vertical() ? i * ch : 0;
            int ix = cx + (vertical() ? 0 : (cw - ICON) / 2);

            if (st.isEmpty()) { // placeholder
                ctx.fill(ix, cy, ix + ICON, cy + ICON, 0x30FFFFFF);
                continue;
            }
            ctx.drawItem(st, ix, cy);

            boolean dmg = st.isDamageable();
            int barColor = 0xFF000000 | st.getItemBarColor();
            if (dmg && on("bar")) {
                int by = cy + ICON; // thin bar directly under the icon
                ctx.fill(ix + 1, by, ix + 14, by + 2, 0xFF000000);
                ctx.fill(ix + 1, by, ix + 1 + st.getItemBarStep(), by + 1, barColor);
            }
            if (on("percent")) {
                String p = dmg ? ((st.getMaxDamage() - st.getDamage()) * 100 / st.getMaxDamage()) + "%"
                               : (st.getCount() > 1 ? "x" + st.getCount() : "");
                if (!p.isEmpty()) {
                    int color = dmg ? barColor : 0xFFFFFFFF;
                    m.pushMatrix();
                    if (vertical()) {
                        m.translate((float) (cx + ICON + 4), (float) (cy + 4));
                    } else {
                        float w = mc.textRenderer.getWidth(p) * 0.75f;
                        m.translate(cx + (cw - w) / 2f, (float) (cy + ICON + (on("bar") ? 3 : 1)));
                        m.scale(0.75f, 0.75f);
                    }
                    ctx.drawText(mc.textRenderer, p, 0, 0, color, true);
                    m.popMatrix();
                }
            }
        }
    }
}
