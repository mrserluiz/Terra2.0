package org.terra2.pack;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.terra2.core.*;

/** One terrain/biome base plus ordered additive decoration stages. No last-writer terrain replacement. */
public final class PlanComposition {
    private PlanComposition() {}
    public static <B, M> GenerationPlan<B, M> compose(String id, String version, GenerationPlan<B, M> base,
        List<SimpleBlockFeature> features, Function<String, B> blocks, Predicate<B> air) {
        List<B> states = features.stream().map(feature -> blocks.apply(feature.block())).toList();
        return new GenerationPlan<>(id, "terrapack-composition", version, new GenerationProgram<>() {
            public void generate(GenerationContext context, int x, int z, BlockVolume<B> output) { base.program().generate(context, x, z, output); }
            public B blockAt(GenerationContext context, int x, int y, int z) { return base.program().blockAt(context, x, y, z); }
            public M biomeAt(GenerationContext context, int x, int y, int z) { return base.program().biomeAt(context, x, y, z); }
            public void decorate(GenerationContext context, int x, int z, BlockVolume<B> output) {
                base.program().decorate(context, x, z, output);
                for(int index = 0; index < features.size(); index++) features.get(index).decorate(context, x, z, output, states.get(index), air);
            }
        });
    }
}
