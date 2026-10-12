package cn.mlus.neobiosphere.world;

import cn.mlus.neobiosphere.config.SphereConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sphere grid maths (from the original SphereCarver / SphereUtil). Config values are cached in plain fields because
 * the density function, aquifer and biome hooks call this for every block / biome sample.
 */
public final class SphereGeometry {
    private static volatile int spacing = 500;
    private static volatile int centerY = 62;
    private static volatile int minRadius = 128;
    private static volatile int maxRadius = 128;
    private static volatile boolean upperHemisphereOnly;
    private static volatile boolean bridges = true;
    private static volatile int bridgeRadius = 1;
    private static volatile List<BlockState> sphereBlocks = List.of(Blocks.GLASS.defaultBlockState());
    private static volatile List<Block> sphereBlockTypes = List.of(Blocks.GLASS);
    private static volatile BlockState bridgeBlock = Blocks.OAK_PLANKS.defaultBlockState();

    private SphereGeometry() {}

    public static void reload() {
        spacing = Math.max(1, SphereConfig.SPACING.get().intValue());
        centerY = SphereConfig.CENTER_Y.get().intValue();
        minRadius = SphereConfig.MIN_RADIUS.get().intValue();
        maxRadius = SphereConfig.RADIUS.get().intValue();
        upperHemisphereOnly = SphereConfig.ONLY_UPPER_HEMISPHERE.get();
        bridges = SphereConfig.GENERATE_BRIDGE.get();
        bridgeRadius = SphereConfig.BRIDGE_RADIUS.get().intValue();
        List<BlockState> states = new ArrayList<>();
        List<Block> types = new ArrayList<>();
        for (String id : SphereConfig.SPHERE_BLOCK.get()) {
            Block block = block(id);
            states.add(block.defaultBlockState());
            types.add(block);
        }
        sphereBlocks = List.copyOf(states);
        sphereBlockTypes = List.copyOf(types);
        bridgeBlock = block(SphereConfig.BRIDGE_BLOCK.get()).defaultBlockState();
    }

    private static Block block(String id) {
        Identifier key = Identifier.tryParse(id);
        return key == null ? Blocks.AIR : BuiltInRegistries.BLOCK.getValue(key);
    }

    public static int spacing() {
        return spacing;
    }

    public static int centerY() {
        return centerY;
    }

    public static boolean upperHemisphereOnly() {
        return upperHemisphereOnly;
    }

    public static boolean bridges() {
        return bridges;
    }

    public static int bridgeRadius() {
        return bridgeRadius;
    }

    public static BlockState bridgeBlock() {
        return bridgeBlock;
    }

    public static int gridX(double x) {
        return (int) Math.round(x / spacing);
    }

    public static int gridZ(double z) {
        return (int) Math.round(z / spacing);
    }

    public static int radius(int gridX, int gridZ) {
        int min = minRadius;
        int max = maxRadius;
        if (min >= max) {
            return max;
        }
        int seed = gridX * 31 + gridZ;
        int hash = seed * 1664525 + 1013904223;
        return min + Math.abs(hash) % (max - min + 1);
    }

    public static int radiusAt(double x, double z) {
        return radius(gridX(x), gridZ(z));
    }

    public static BlockState sphereBlock(int gridX, int gridZ) {
        List<BlockState> blocks = sphereBlocks;
        if (blocks.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        return blocks.get(Math.abs(gridX * 31 + gridZ) % blocks.size());
    }

    public static boolean isSphereBlock(BlockState state) {
        for (Block block : sphereBlockTypes) {
            if (state.is(block)) {
                return true;
            }
        }
        return false;
    }

    /** Squared distance to the nearest grid centre divided by that sphere's radius squared (0 = centre, 1 = shell). */
    public static double normalizedDistanceSq(int x, int y, int z) {
        int s = spacing;
        double gx = Math.round((double) x / s) * (double) s;
        double gz = Math.round((double) z / s) * (double) s;
        double dx = x - gx;
        double dy = y - centerY;
        double dz = z - gz;
        int r = radiusAt(x, z);
        return (dx * dx + dy * dy + dz * dz) / ((double) r * r);
    }

    public static boolean isInsideAnySphere(double x, double y, double z) {
        int gx0 = gridX(x);
        int gz0 = gridZ(z);
        for (int gx = gx0 - 1; gx <= gx0 + 1; gx++) {
            for (int gz = gz0 - 1; gz <= gz0 + 1; gz++) {
                double dx = x - gx * (double) spacing;
                double dy = y - centerY;
                double dz = z - gz * (double) spacing;
                int r = radius(gx, gz);
                double inner = (r - 2.0) * (r - 2.0);
                if (dx * dx + dy * dy + dz * dz < inner) {
                    return true;
                }
            }
        }
        return false;
    }
}
