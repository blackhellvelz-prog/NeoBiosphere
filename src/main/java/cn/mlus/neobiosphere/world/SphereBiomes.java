package cn.mlus.neobiosphere.world;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/** The void biome used outside the spheres (biome lookup captured from the overworld parameter list, as in the original). */
public final class SphereBiomes {
    private static volatile HolderGetter<Biome> lookup;
    private static volatile Holder<Biome> voidBiome;

    private SphereBiomes() {}

    public static void capture(HolderGetter<Biome> getter) {
        lookup = getter;
        voidBiome = null;
    }

    public static Holder<Biome> voidBiome() {
        Holder<Biome> v = voidBiome;
        if (v == null && lookup != null) {
            v = lookup.get(Biomes.THE_VOID).orElse(null);
            voidBiome = v;
        }
        return v;
    }
}
