package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.BiosphereChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * ChunkMap builds the level's RandomState (noise router) from the generator's settings. For a biosphere generator
 * it gets the sphere-wrapped settings instead; the generator's own settings holder stays minecraft:overworld.
 */
@Mixin(ChunkMap.class)
public abstract class MixinChunkMap {
    @Redirect(method = "<init>", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;generatorSettings()Lnet/minecraft/core/Holder;"))
    private Holder<NoiseGeneratorSettings> neobiosphere$sphereRouter(NoiseBasedChunkGenerator generator) {
        if (generator instanceof BiosphereChunkGenerator biosphere) {
            return Holder.direct(biosphere.sphereSettings());
        }
        return generator.generatorSettings();
    }
}
