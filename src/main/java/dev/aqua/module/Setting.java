package dev.aqua.module;

import java.util.Locale;

public class Setting {
    public final String key, label;
    public final double min, max, step;
    public final boolean toggle;
    public final String[] labels;
    public double value;

    private Setting(String key, String label, double def, double min, double max, double step, boolean toggle, String[] labels) {
        this.key = key; this.label = label; this.value = def;
        this.min = min; this.max = max; this.step = step; this.toggle = toggle; this.labels = labels;
    }

    public static Setting number(String key, String label, double def, double min, double max, double step) {
        return new Setting(key, label, def, min, max, step, false, null);
    }

    public static Setting toggle(String key, String label, boolean def) {
        return new Setting(key, label, def ? 1 : 0, 0, 1, 1, true, null);
    }

    public static Setting choice(String key, String label, int def, String... labels) {
        return new Setting(key, label, def, 0, labels.length - 1, 1, false, labels);
    }

    public boolean on() { return value >= 0.5; }

    public void bump(int dir) {
        if (toggle) { value = on() ? 0 : 1; return; }
        double v = Math.round((value + dir * step) / step) * step;
        value = Math.round(Math.max(min, Math.min(max, v)) * 1000.0) / 1000.0;
    }

    public String display() {
        if (toggle) return on() ? "on" : "off";
        if (labels != null) return labels[(int) Math.max(0, Math.min(labels.length - 1, value))];
        return step >= 1 ? String.valueOf((int) value) : String.format(Locale.ROOT, "%.2f", value);
    }
}
