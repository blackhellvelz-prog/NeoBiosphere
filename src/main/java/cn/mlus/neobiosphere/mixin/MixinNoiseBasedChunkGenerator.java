package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.BiosphereChunkGenerator;
import cn.mlus.neobiosphere.world.SphereMarker;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Marks the NoiseChunks a biosphere generator creates (chunk filling and height queries). */
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class MixinNoiseBasedChunkGenerator {
    @Inject(method = "createNoiseChunk", at = @At("RETURN"))
    private void neobiosphere$markChunk(CallbackInfoReturnable<NoiseChunk> cir) {
        if ((Object) this instanceof BiosphereChunkGenerator) {
            ((SphereMarker) cir.getReturnValue()).neobiosphere$setSphere(true);
        }
    }

    @ModifyVariable(method = "iterateNoiseColumn", at = @At("STORE"))
    private NoiseChunk neobiosphere$markColumn(NoiseChunk chunk) {
        if ((Object) this instanceof BiosphereChunkGenerator) {
            ((SphereMarker) chunk).neobiosphere$setSphere(true);
        }
        return chunk;
    }
}
