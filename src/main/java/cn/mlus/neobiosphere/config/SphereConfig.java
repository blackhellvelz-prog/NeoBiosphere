package cn.mlus.neobiosphere.config;

import cn.mlus.neobiosphere.world.SphereGeometry;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SphereConfig {
    private static final ModConfigSpec SPEC;
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Number> RADIUS;
    public static final ModConfigSpec.ConfigValue<Number> MIN_RADIUS;
    public static final ModConfigSpec.ConfigValue<Number> SPACING;
    public static final ModConfigSpec.ConfigValue<Number> CENTER_Y;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SPHERE_BLOCK;
    public static final ModConfigSpec.ConfigValue<Boolean> ONLY_UPPER_HEMISPHERE;
    public static final ModConfigSpec.ConfigValue<Boolean> GENERATE_BRIDGE;
    public static final ModConfigSpec.ConfigValue<String> BRIDGE_BLOCK;
    public static final ModConfigSpec.ConfigValue<Number> BRIDGE_RADIUS;

    static {
        BUILDER.comment("Only worlds created with the \"Biosphere\" world type use these settings.").push("Settings");
        RADIUS = BUILDER.comment("Max radius of the spheres").define("radius", 128);
        MIN_RADIUS = BUILDER.comment("Min radius of the spheres (same as radius for fixed size)").define("min_radius", 128);
        SPACING = BUILDER.comment("Spacing between the spheres").define("spacing", 500);
        CENTER_Y = BUILDER.comment("Center Y level for sphere generation").define("center_y", 62);
        SPHERE_BLOCK = BUILDER.comment("Blocks used for the spheres, randomly assigned per sphere").defineListAllowEmpty("sphere_block", List.of("minecraft:glass"), SphereConfig::validateItemName);
        ONLY_UPPER_HEMISPHERE = BUILDER.comment("Generate only the upper hemisphere of the spheres").define("only_upper_hemisphere", false);
        GENERATE_BRIDGE = BUILDER.comment("Generate bridges between spheres").define("generate_bridge", true);
        BRIDGE_BLOCK = BUILDER.comment("Block used for the bridges").define("bridge_block", "minecraft:oak_planks", SphereConfig::validateItemName);
        BRIDGE_RADIUS = BUILDER.comment("Radius of the bridges").define("bridge_radius", 1);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private static boolean validateItemName(final Object obj) {
        if (!(obj instanceof String itemName)) {
            return false;
        }
        Identifier id = Identifier.tryParse(itemName);
        return id != null && BuiltInRegistries.ITEM.containsKey(id);
    }

    public static void setup(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, SPEC, "NeoBiosphere.toml");
    }

    /** Worldgen reads the values on every block, so they are copied into plain fields whenever the file (re)loads. */
    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            SphereGeometry.reload();
        }
    }
}
