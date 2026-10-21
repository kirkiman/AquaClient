package dev.aqua.module;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.aqua.Tracker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ModuleManager {
    public static final List<Module> MODULES = new ArrayList<>();
    public static InspectModule inspect;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("aquaclient.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private ModuleManager() {}

    public static void init() {
        MODULES.add(new TextHud("watermark", "Watermark", "Aqua Client branding.", 0.01, 0.01, mc -> "Aqua Client").wave());
        MODULES.add(new ArmorModule());
        MODULES.add(new KeystrokesModule());
        MODULES.add(new TextHud("cps", "CPS", "Clicks per second (left | right).", 0.01, 0.80, mc ->
                dev.aqua.Input.cpsLeft() + " | " + dev.aqua.Input.cpsRight() + " CPS"));
        MODULES.add(new PotionModule());
        MODULES.add(new TextHud("coords", "Coordinates", "Your current XYZ position.", 0.01, 0.07, mc -> {
            if (mc.player == null) return null;
            return String.format(Locale.ROOT, "XYZ  %d  %d  %d",
                    (int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
        }));
        MODULES.add(new TextHud("fps", "FPS", "Frames per second.", 0.01, 0.13, mc -> mc.getCurrentFps() + " FPS"));
        MODULES.add(new TextHud("ping", "Ping", "Latency to the server.", 0.01, 0.19, mc -> {
            if (mc.player == null) return null;
            ClientPlayNetworkHandler nh = mc.getNetworkHandler();
            PlayerListEntry e = nh == null ? null : nh.getPlayerListEntry(mc.player.getUuid());
            return e == null ? null : e.getLatency() + " ms";
        }));
        MODULES.add(new TextHud("direction", "Direction", "Which way you are facing.", 0.01, 0.25, mc -> {
            if (mc.player == null) return null;
            String n = mc.player.getHorizontalFacing().asString();
            return "Facing " + Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }));
        MODULES.add(new TextHud("speed", "Speed", "Horizontal speed in blocks per second.", 0.12, 0.13, mc ->
                mc.player == null ? null : String.format(Locale.ROOT, "%.1f b/s", Tracker.speed)));
        MODULES.add(new TextHud("memory", "Memory", "Java memory usage.", 0.12, 0.19, mc -> {
            Runtime r = Runtime.getRuntime();
            long used = (r.totalMemory() - r.freeMemory()) >> 20, max = r.maxMemory() >> 20;
            return "RAM " + used + "/" + max + " MB";
        }));
        MODULES.add(new TextHud("durability", "Durability", "Remaining durability of the item in your main hand.", 0.12, 0.25, mc -> {
            if (mc.player == null) return null;
            ItemStack s = mc.player.getMainHandStack();
            if (s.isEmpty() || !s.isDamageable()) return null;
            return "Durability " + (s.getMaxDamage() - s.getDamage()) + "/" + s.getMaxDamage();
        }));
        MODULES.add(new TextHud("clock", "Clock", "Your real-world time.", 0.12, 0.07,
                mc -> LocalTime.now().format(CLOCK)));
        inspect = new InspectModule();
        MODULES.add(inspect);
        load();
    }

    public static void save() {
        Map<String, Map<String, Double>> out = new LinkedHashMap<>();
        for (Module m : MODULES) {
            Map<String, Double> o = new LinkedHashMap<>();
            o.put("enabled", m.enabled ? 1.0 : 0.0);
            if (m instanceof HudModule h) { o.put("x", h.x); o.put("y", h.y); }
            for (Setting s : m.settings) o.put("s_" + s.key, s.value);
            out.put(m.id, o);
        }
        try {
            Files.writeString(FILE, GSON.toJson(out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void load() {
        if (!Files.exists(FILE)) return;
        try {
            Type t = new TypeToken<Map<String, Map<String, Double>>>() {}.getType();
            Map<String, Map<String, Double>> in = GSON.fromJson(Files.readString(FILE), t);
            if (in == null) return;
            for (Module m : MODULES) {
                Map<String, Double> o = in.get(m.id);
                if (o == null) continue;
                if (o.containsKey("enabled")) m.enabled = o.get("enabled") >= 0.5;
                if (m instanceof HudModule h) {
                    if (o.containsKey("x")) h.x = o.get("x");
                    if (o.containsKey("y")) h.y = o.get("y");
                }
                for (Setting s : m.settings) {
                    Double v = o.get("s_" + s.key);
                    if (v != null) s.value = Math.max(s.min, Math.min(s.max, v));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
