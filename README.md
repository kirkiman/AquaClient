# Aqua Client (Fabric, Minecraft 1.21.11)

Right Shift -> Aqua menu (rebind in Controls > Aqua Client)   |   Y -> inspect / karambit flip

## Modules
Watermark, **Armor HUD**, **Keystrokes**, CPS, Potion Effects, Coordinates, FPS, Ping, Direction,
Speed, Memory, Durability, Clock, Inspect animation. All draggable in the menu; config in `.minecraft/config/aquaclient.json`.

## Bundled optimization mods (Jar-in-Jar, unmodified)
Sodium 0.8.13, Lithium 0.21.2, ImmediatelyFast 1.14.1 - see their own licenses on Modrinth
(Lithium LGPL-3.0, ImmediatelyFast LGPL-3.0, Sodium PolyForm Shield 1.0.0 - check it fits how you distribute).
To add more (FerriteCore, EntityCulling, ModernFix...), copy the 2-line pattern in build.gradle with the
version ID from the mod's Modrinth version page ("Maven coordinates").

## Build the jar (needs internet)
Option A - GitHub (no local setup): push this folder to a GitHub repo, open Actions > build > run,
download the `aquaclient-jar` artifact.
Option B - local: install JDK 21, then
    gradle wrapper --gradle-version 9.2.0
    ./gradlew build
Jar: build/libs/aquaclient-1.0.0.jar (not the -sources jar) -> .minecraft/mods with Fabric API 0.141.1+1.21.11.
Do not also install standalone Sodium/Lithium/ImmediatelyFast - they are already inside.
