package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.SphereGeometry;
import cn.mlus.neobiosphere.world.SphereMarker;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No sea water outside the spheres (and in their shell zone) - only for a biosphere generator's chunks. */
@Mixin(Aquifer.NoiseBasedAquifer.class)
public abstract class MixinNoiseBasedAquifer {
    @Shadow
    @Final
    private NoiseChunk noiseChunk;

    @Inject(method = "computeSubstance", at = @At("HEAD"), cancellable = true)
    private void neobiosphere$dryOutsideSpheres(DensityFunction.FunctionContext context, double substance,
                                                CallbackInfoReturnable<BlockState> cir) {
        if (!((SphereMarker) this.noiseChunk).neobiosphere$isSphere()) {
            return;
        }
        int x = context.blockX();
        int y = context.blockY();
        int z = context.blockZ();
        int spacing = SphereGeometry.spacing();
        int r = SphereGeometry.radiusAt(x, z);
        double gx = Math.round((float) x / spacing) * (double) spacing;
        double gz = Math.round((float) z / spacing) * (double) spacing;
        double dx = x - gx;
        double dy = y - SphereGeometry.centerY();
        double dz = z - gz;
        double d = dx * dx + dy * dy + dz * dz;
        if (d - (double) r * r >= -r * 2.0) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
