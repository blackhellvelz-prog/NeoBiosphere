package cn.mlus.neobiosphere.mixin;

import cn.mlus.neobiosphere.world.SphereBiomes;
import cn.mlus.neobiosphere.world.SphereGeometry;
import cn.mlus.neobiosphere.world.SphereMarker;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Outside the spheres the biome is minecraft:the_void - only for the biome source of a biosphere generator. */
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MixinMultiNoiseBiomeSource implements SphereMarker {
    @Unique
    private boolean neobiosphere$sphere;

    @Shadow
    public abstract Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint);

    @Override
    public boolean neobiosphere$isSphere() {
        return this.neobiosphere$sphere;
    }

    @Override
    public void neobiosphere$setSphere(boolean sphere) {
        this.neobiosphere$sphere = sphere;
    }

    @Inject(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at = @At("HEAD"), cancellable = true)
    private void neobiosphere$voidOutsideSpheres(int x, int y, int z, Climate.Sampler sampler, CallbackInfoReturnable<Holder<Biome>> cir) {
        if (!this.neobiosphere$sphere) {
            return;
        }
        int bx = QuartPos.toBlock(x);
        int by = QuartPos.toBlock(y);
        int bz = QuartPos.toBlock(z);
        int spacing = SphereGeometry.spacing();
        int gx = Math.round((float) bx / spacing) * spacing;
        int gz = Math.round((float) bz / spacing) * spacing;
        double dx = bx - gx;
        double dy = by - SphereGeometry.centerY();
        double dz = bz - gz;
        int r = SphereGeometry.radiusAt(bx, bz) + 2;
        if (dx * dx + dy * dy + dz * dz <= (double) r * r) {
            return;
        }
        Holder<Biome> biome = this.getNoiseBiome(sampler.sample(x, y, z));
        if (biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END)) {
            return;
        }
        Holder<Biome> voidBiome = SphereBiomes.voidBiome();
        if (voidBiome != null && voidBiome.isBound()) {
            cir.setReturnValue(voidBiome);
        }
    }
}
