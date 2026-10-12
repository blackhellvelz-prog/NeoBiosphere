package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.BiosphereWorlds;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Exposes the level a chunk belongs to (a ServerLevel for both proto and full chunks on the server). */
@Mixin(ChunkAccess.class)
public abstract class MixinChunkAccess implements BiosphereWorlds.ChunkLevelAccess {
    @Shadow
    @Final
    protected LevelHeightAccessor levelHeightAccessor;

    @Override
    public LevelHeightAccessor neobiosphere$levelHeightAccessor() {
        return this.levelHeightAccessor;
    }
}
