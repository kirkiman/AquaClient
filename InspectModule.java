package dev.aqua.module;

/** No HUD output. Holds the settings for the karambit-style inspect animation. */
public class InspectModule extends Module {
    public InspectModule() {
        super("inspect", "Inspect",
                "Press your Inspect key (Controls > Aqua Client, default Y) and your hand pops up and flips the item like a karambit.");
        settings.add(Setting.number("speed", "Speed", 1.0, 0.5, 2.0, 0.25));
        settings.add(Setting.number("spins", "Spins", 3, 1, 6, 1));
        settings.add(Setting.number("tilt", "Tilt", 20, 0, 40, 5));
    }
}
