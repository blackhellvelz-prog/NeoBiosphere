package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.SphereMarker;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** Lets a NoiseChunk (and so its aquifer) know that it belongs to a biosphere generator. */
@Mixin(NoiseChunk.class)
public abstract class MixinNoiseChunk implements SphereMarker {
    @Unique
    private boolean neobiosphere$sphere;

    @Override
    public boolean neobiosphere$isSphere() {
        return this.neobiosphere$sphere;
    }

    @Override
    public void neobiosphere$setSphere(boolean sphere) {
        this.neobiosphere$sphere = sphere;
    }
}
