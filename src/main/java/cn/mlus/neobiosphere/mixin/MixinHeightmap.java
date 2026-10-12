package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.BiosphereWorlds;
import cn.mlus.neobiosphere.world.SphereGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * The upper half of a sphere shell does not count for heightmaps (trees, rain and sky light reach the ground inside
 * the dome) - only in biosphere worlds; a glass roof in a normal world stays a roof.
 */
@Mixin(Heightmap.class)
public class MixinHeightmap {
    @Shadow
    @Final
    private ChunkAccess chunk;

    @ModifyVariable(method = "update", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BlockState neobiosphere$ignoreShell(BlockState state, int x, int y, int z) {
        if (y > SphereGeometry.centerY() && SphereGeometry.isSphereBlock(state) && BiosphereWorlds.isBiosphere(this.chunk)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Redirect(method = "primeHeightmaps", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/chunk/ChunkAccess;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState neobiosphere$ignoreShellPrime(ChunkAccess instance, BlockPos pos) {
        BlockState original = instance.getBlockState(pos);
        if (pos.getY() > SphereGeometry.centerY() && SphereGeometry.isSphereBlock(original) && BiosphereWorlds.isBiosphere(instance)) {
            return Blocks.AIR.defaultBlockState();
        }
        return original;
    }
}
