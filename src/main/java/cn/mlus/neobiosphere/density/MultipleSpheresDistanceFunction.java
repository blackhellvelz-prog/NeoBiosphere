package cn.mlus.neobiosphere.density;

import cn.mlus.neobiosphere.world.SphereGeometry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.levelgen.DensityFunction;

/**
 * 0 at a sphere centre, 1 on its shell, &gt;1 outside. The spacing/radius/center_y fields are kept for datapack
 * compatibility with the original mod; the live values come from the config (as in the original).
 */
public record MultipleSpheresDistanceFunction(int spacing, int radius, int centerY) implements DensityFunction.SimpleFunction {
    public static final MapCodec<MultipleSpheresDistanceFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("spacing").forGetter(MultipleSpheresDistanceFunction::spacing),
            Codec.INT.fieldOf("radius").forGetter(MultipleSpheresDistanceFunction::radius),
            Codec.INT.fieldOf("center_y").forGetter(MultipleSpheresDistanceFunction::centerY)
    ).apply(instance, MultipleSpheresDistanceFunction::new));
    public static final KeyDispatchDataCodec<MultipleSpheresDistanceFunction> KEY_CODEC = KeyDispatchDataCodec.of(CODEC);

    @Override
    public double compute(FunctionContext context) {
        return SphereGeometry.normalizedDistanceSq(context.blockX(), context.blockY(), context.blockZ());
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 2.0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return KEY_CODEC;
    }
}
