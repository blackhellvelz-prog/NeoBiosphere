package cn.mlus.neobiosphere.event;

import cn.mlus.neobiosphere.world.BiosphereWorlds;
import cn.mlus.neobiosphere.world.SphereGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * If a player joins or respawns inside a sphere shell, move them onto solid ground inside the sphere.
 * Port: runs on the server thread (the original searched the world from a new thread) and only in biosphere worlds.
 */
@EventBusSubscriber
public class PlayerSpawnHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        schedule(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        schedule(event.getEntity());
    }

    private static void schedule(net.minecraft.world.entity.player.Player entity) {
        if (entity instanceof ServerPlayer player && player.level() instanceof ServerLevel level && BiosphereWorlds.isBiosphere(level)) {
            level.getServer().execute(() -> teleportIntoSphere(level, player));
        }
    }

    private static void teleportIntoSphere(ServerLevel level, ServerPlayer player) {
        BlockPos current = player.blockPosition();
        if (!SphereGeometry.isSphereBlock(level.getBlockState(current))) {
            return;
        }
        BlockPos target = findValidPosition(level, current, SphereGeometry.radiusAt(current.getX(), current.getZ()));
        if (target != null) {
            player.teleportTo(target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5);
        }
    }

    private static BlockPos findValidPosition(Level level, BlockPos center, int radius) {
        int minY = level.getMinY();
        int maxY = level.getMaxY() - 1;
        for (int y = center.getY(); y >= minY; y--) {
            BlockPos p = new BlockPos(center.getX(), y, center.getZ());
            if (isGround(level, p) && hasEnoughSpace(level, p.above())) {
                return p.above();
            }
        }
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                for (int y = maxY; y >= minY; y--) {
                    BlockPos p = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
                    if (isGround(level, p) && hasEnoughSpace(level, p.above())) {
                        return p.above();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isGround(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolid() && state.isCollisionShapeFullBlock(level, pos) && !state.isAir() && !SphereGeometry.isSphereBlock(state);
    }

    private static boolean hasEnoughSpace(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }
}
