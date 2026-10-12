package cn.mlus.neobiosphere.world;

import cn.mlus.neobiosphere.density.MultipleSpheresDistanceFunction;
import cn.mlus.neobiosphere.world.SphereMarker;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * The "Biosphere" world type's overworld generator ("neobiosphere:biosphere"). It is a normal noise generator for
 * the given settings (usually minecraft:overworld, i.e. whatever Terralith and other datapacks made of it) whose
 * final density is wrapped so terrain only exists inside the spheres. Its settings holder stays the real one, so mods
 * that look for minecraft:overworld (surface rules, structure checks) still recognise it; only the RandomState that
 * ChunkMap builds for this generator uses {@link #sphereSettings()} (see MixinChunkMap).
 * <p>
 * Everything sphere-specific (shells, bridges, void biome, dry void, heightmaps) is tied to this generator, so normal
 * worlds and other dimensions are untouched - unlike the original mod, which changed the overworld of every world.
 */
public class BiosphereChunkGenerator extends NoiseBasedChunkGenerator {
    public static final MapCodec<BiosphereChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings)
    ).apply(instance, instance.stable(BiosphereChunkGenerator::new)));

    private volatile NoiseGeneratorSettings sphereSettings;

    public BiosphereChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
        if (biomeSource instanceof SphereMarker marker) {
            marker.neobiosphere$setSphere(true);
        }
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    /** The real settings with final density = original inside a sphere, solid air outside (as the original mod). */
    public NoiseGeneratorSettings sphereSettings() {
        NoiseGeneratorSettings s = this.sphereSettings;
        if (s == null) {
            NoiseGeneratorSettings base = this.generatorSettings().value();
            NoiseRouter r = base.noiseRouter();
            NoiseRouter router = new NoiseRouter(r.barrierNoise(), r.fluidLevelFloodednessNoise(), r.fluidLevelSpreadNoise(),
                    r.lavaNoise(), r.temperature(), r.vegetation(), r.continents(), r.erosion(), r.depth(), r.ridges(),
                    r.preliminarySurfaceLevel(),
                    DensityFunctions.rangeChoice(new MultipleSpheresDistanceFunction(SphereGeometry.spacing(), 0,
                            SphereGeometry.centerY()), 0.0, 1.0, r.finalDensity(), DensityFunctions.constant(-1000000.0)),
                    r.veinToggle(), r.veinRidged(), r.veinGap());
            s = new NoiseGeneratorSettings(base.noiseSettings(), base.defaultBlock(), base.defaultFluid(), router,
                    base.surfaceRule(), base.spawnTarget(), base.seaLevel(), base.disableMobGeneration(),
                    base.aquifersEnabled(), base.oreVeinsEnabled(), base.useLegacyRandomSource());
            this.sphereSettings = s;
        }
        return s;
    }

    /** Like the original mod: no vanilla carvers in a biosphere world (caves would breach the shells); shells and bridges instead. */
    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk) {
        SphereCarving.carve(chunk);
    }
}
