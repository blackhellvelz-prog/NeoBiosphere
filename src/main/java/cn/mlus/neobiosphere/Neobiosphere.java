package cn.mlus.neobiosphere;

import cn.mlus.neobiosphere.config.SphereConfig;
import cn.mlus.neobiosphere.density.MultipleSpheresDistanceFunction;
import cn.mlus.neobiosphere.world.BiosphereChunkGenerator;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoBiosphere for MC 26.2 ("дум" modpack port of mlus-asuka/NeoBiosphere 1.0.6, GPL-3.0).
 * Port changes: spheres are a separate "Biosphere" world type (generator neobiosphere:biosphere) instead of replacing
 * the overworld of every world; the sphere terrain wraps the live minecraft:overworld settings (Terralith etc.).
 */
@Mod(Neobiosphere.MODID)
public class Neobiosphere {
    public static final String MODID = "neobiosphere";

    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTION_TYPES =
            DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, MODID);
    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS =
            DeferredRegister.create(Registries.CHUNK_GENERATOR, MODID);

    static {
        DENSITY_FUNCTION_TYPES.register("multiple_spheres", () -> MultipleSpheresDistanceFunction.CODEC);
        CHUNK_GENERATORS.register("biosphere", () -> BiosphereChunkGenerator.CODEC);
    }

    public Neobiosphere(IEventBus modEventBus, ModContainer modContainer) {
        SphereConfig.setup(modContainer);
        DENSITY_FUNCTION_TYPES.register(modEventBus);
        CHUNK_GENERATORS.register(modEventBus);
        modEventBus.addListener(ModConfigEvent.Loading.class, SphereConfig::onConfigChanged);
        modEventBus.addListener(ModConfigEvent.Reloading.class, SphereConfig::onConfigChanged);
    }

    public static Identifier prefix(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
