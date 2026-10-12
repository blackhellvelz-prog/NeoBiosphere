package cn.mlus.neobiosphere.world;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Sphere shells and bridges for one chunk (the original SphereCarver / SphereBridgeCarver algorithms, called only by
 * {@link BiosphereChunkGenerator} instead of being registered as carvers for every biome of every world).
 */
public final class SphereCarving {
    private static final int FLAGS = 0;

    private SphereCarving() {}

    public static void carve(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int startX = pos.getMinBlockX();
        int startZ = pos.getMinBlockZ();
        int endX = startX + 15;
        int endZ = startZ + 15;
        int spacing = SphereGeometry.spacing();
        int gx0 = (int) Math.floor((double) startX / spacing);
        int gx1 = (int) Math.ceil((double) endX / spacing);
        int gz0 = (int) Math.floor((double) startZ / spacing);
        int gz1 = (int) Math.ceil((double) endZ / spacing);
        for (int gx = gx0; gx <= gx1; gx++) {
            for (int gz = gz0; gz <= gz1; gz++) {
                shell(chunk, gx, gz, startX, endX, startZ, endZ);
            }
        }
        if (SphereGeometry.bridges()) {
            for (int gx = gx0; gx <= gx1; gx++) {
                for (int gz = gz0; gz <= gz1; gz++) {
                    double cx = gx * (double) spacing;
                    double cz = gz * (double) spacing;
                    int cy = SphereGeometry.centerY();
                    bridge(chunk, cx, cz, cy, cx + spacing, cz, cy, startX, endX, startZ, endZ);
                    bridge(chunk, cx, cz, cy, cx, cz + spacing, cy, startX, endX, startZ, endZ);
                }
            }
        }
    }

    private static void shell(ChunkAccess chunk, int gridX, int gridZ, int startX, int endX, int startZ, int endZ) {
        int spacing = SphereGeometry.spacing();
        int centerY = SphereGeometry.centerY();
        boolean upperOnly = SphereGeometry.upperHemisphereOnly();
        double centerX = gridX * (double) spacing;
        double centerZ = gridZ * (double) spacing;
        int radius = SphereGeometry.radius(gridX, gridZ);
        if (upperOnly) {
            radius--;
        }
        BlockState block = SphereGeometry.sphereBlock(gridX, gridZ);
        int minY = Math.max(chunk.getMinY(), centerY - radius - 1);
        int maxY = Math.min(chunk.getMaxY(), centerY + radius + 1);
        double inner = (radius - 2.0) * (radius - 2.0);
        double outer = (double) radius * radius;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = startX; x <= endX; x++) {
            double dx2 = (x - centerX) * (x - centerX);
            if (dx2 > outer) {
                continue;
            }
            for (int z = startZ; z <= endZ; z++) {
                double h = dx2 + (z - centerZ) * (z - centerZ);
                if (h > outer) {
                    continue;
                }
                for (int y = minY; y <= maxY; y++) {
                    double d = h + (y - centerY) * (double) (y - centerY);
                    if (d < inner || d > outer) {
                        continue;
                    }
                    p.set(x, y, z);
                    BlockState here = chunk.getBlockState(p);
                    if (upperOnly) {
                        if (!(here.is(BlockTags.AIR) || here.is(Blocks.WATER))) {
                            continue;
                        }
                        if (y < 55 && !here.is(Blocks.WATER)) {
                            continue;
                        }
                    }
                    chunk.setBlockState(p, block, FLAGS);
                }
            }
        }
    }

    private static void bridge(ChunkAccess chunk, double x1, double z1, double y1, double x2, double z2, double y2,
                               int startX, int endX, int startZ, int endZ) {
        double dx = x2 - x1;
        double dz = z2 - z1;
        double dy = y2 - y1;
        double length = Math.sqrt(dx * dx + dz * dz + dy * dy);
        if (length == 0) {
            return;
        }
        int r = SphereGeometry.bridgeRadius();
        BlockState block = SphereGeometry.bridgeBlock();
        double nx = dx / length;
        double nz = dz / length;
        double ny = dy / length;
        double rSq = (double) r * r;
        double rInnerSq = (r - 1.0) * (r - 1.0);
        int minX = Math.max(startX, (int) Math.min(x1, x2) - r);
        int maxX = Math.min(endX, (int) Math.max(x1, x2) + r);
        int minY = Math.max(chunk.getMinY(), (int) Math.min(y1, y2) - r);
        int maxY = Math.min(chunk.getMaxY(), (int) Math.max(y1, y2) + r);
        int minZ = Math.max(startZ, (int) Math.min(z1, z2) - r);
        int maxZ = Math.min(endZ, (int) Math.max(z1, z2) + r);
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    double ax = x - x1;
                    double az = z - z1;
                    double ay = y - y1;
                    double t = ax * nx + az * nz + ay * ny;
                    if (t < 0 || t > length) {
                        continue;
                    }
                    double ex = x - (x1 + t * nx);
                    double ey = y - (y1 + t * ny);
                    double ez = z - (z1 + t * nz);
                    double distSq = ex * ex + ey * ey + ez * ez;
                    if (distSq > rSq || distSq < rInnerSq || SphereGeometry.isInsideAnySphere(x, y, z)) {
                        continue;
                    }
                    p.set(x, y, z);
                    if (chunk.getBlockState(p).isAir()) {
                        chunk.setBlockState(p, block, FLAGS);
                    }
                }
            }
        }
    }
}
