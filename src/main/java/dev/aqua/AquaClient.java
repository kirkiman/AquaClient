package dev.aqua;

import dev.aqua.gui.MenuScreen;
import dev.aqua.module.HudModule;
import dev.aqua.module.Module;
import dev.aqua.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class AquaClient implements ClientModInitializer {
    public static final String ID = "aquaclient";
    public static final String VERSION = "1.0.0";

    public static KeyBinding menuKey;
    public static KeyBinding inspectKey;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID, "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aquaclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        inspectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aquaclient.inspect", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_Y, category));

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            Input.poll(mc);
            if (mc.player == null || mc.options.hudHidden || mc.currentScreen instanceof MenuScreen) return;
            int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
            for (Module m : ModuleManager.MODULES) {
                if (m.enabled && m instanceof HudModule h) h.render(ctx, mc, sw, sh, false, false);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            Tracker.tick(mc);
            while (menuKey.wasPressed()) {
                if (mc.currentScreen == null && mc.world != null) mc.setScreen(new MenuScreen());
            }
            while (inspectKey.wasPressed()) {
                if (mc.currentScreen == null) InspectAnimation.start(mc);
            }
            InspectAnimation.tick(mc);
        });
    }
}
