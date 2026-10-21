package dev.aqua;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;

/** Polls raw mouse state (version-proof) and tracks clicks-per-second. */
public final class Input {
    private static final ArrayDeque<Long> LEFT = new ArrayDeque<>(), RIGHT = new ArrayDeque<>();
    public static boolean leftDown, rightDown;

    private Input() {}

    public static void poll(MinecraftClient mc) {
        long h = mc.getWindow().getHandle();
        long now = System.currentTimeMillis();
        boolean l = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(h, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        if (mc.currentScreen == null) {
            if (l && !leftDown) LEFT.add(now);
            if (r && !rightDown) RIGHT.add(now);
        }
        leftDown = l;
        rightDown = r;
    }

    private static int count(ArrayDeque<Long> q) {
        long cutoff = System.currentTimeMillis() - 1000;
        while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
        return q.size();
    }

    public static int cpsLeft() { return count(LEFT); }
    public static int cpsRight() { return count(RIGHT); }
}
