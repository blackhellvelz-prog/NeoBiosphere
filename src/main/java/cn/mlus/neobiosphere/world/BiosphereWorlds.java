package cn.mlus.neobiosphere.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;

/** "Does this level / chunk use the biosphere generator?" for the hooks that only have a level or chunk at hand. */
public final class BiosphereWorlds {
    private BiosphereWorlds() {}

    public static boolean isBiosphere(LevelHeightAccessor level) {
        return level instanceof ServerLevel server && server.getChunkSource().getGenerator() instanceof BiosphereChunkGenerator;
    }

    public static boolean isBiosphere(ChunkAccess chunk) {
        return chunk instanceof ChunkLevelAccess access && isBiosphere(access.neobiosphere$levelHeightAccessor());
    }

    /** Implemented on ChunkAccess by a mixin accessor. */
    public interface ChunkLevelAccess {
        LevelHeightAccessor neobiosphere$levelHeightAccessor();
    }
}
