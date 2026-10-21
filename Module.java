package dev.aqua.module;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String id, name, description;
    public boolean enabled = true;
    public final List<Setting> settings = new ArrayList<>();

    protected Module(String id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }

    public Setting setting(String key) {
        for (Setting s : settings) if (s.key.equals(key)) return s;
        return null;
    }

    public double get(String key) {
        Setting s = setting(key);
        return s == null ? 0 : s.value;
    }

    public boolean on(String key) { return get(key) >= 0.5; }
}
