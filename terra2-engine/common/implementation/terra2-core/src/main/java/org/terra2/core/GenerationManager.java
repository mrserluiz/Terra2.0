package org.terra2.core;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Default empty registry: no generation until a platform explicitly authorizes a target. */
public final class GenerationManager<B, M> {
    private static final Set<String> BUILTIN_DIMENSIONS = Set.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");
    private final Set<String> protectedWorlds;
    private final ConcurrentHashMap<String, Binding<B, M>> worlds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Shape> shapes = new ConcurrentHashMap<>();
    public record Binding<B, M>(WorldTarget target, GenerationPlan<B, M> plan) {}
    private record Shape(long seed, int minY, int maxY) {}
    public GenerationManager(Set<String> protectedWorlds) { this.protectedWorlds = Set.copyOf(protectedWorlds); }
    public synchronized void authorize(WorldTarget target, GenerationPlan<B, M> plan) {
        Objects.requireNonNull(plan); protect(target);
        if(target.dimensionKey() != null && worlds.values().stream().anyMatch(binding ->
            !binding.target().worldName().equals(target.worldName()) && target.dimensionKey().equals(binding.target().dimensionKey())))
            throw new IllegalStateException("Dimension already belongs to another world: " + target.dimensionKey());
        worlds.compute(target.worldName(), (name, previous) -> {
            if(previous != null && (!previous.plan().identity().equals(plan.identity()) ||
                !Objects.equals(previous.target().dimensionKey(), target.dimensionKey())))
                throw new IllegalStateException("Cannot replace an active generation binding: " + name);
            return previous == null ? new Binding<>(target, plan) : previous;
        });
    }
    public synchronized void bindDimension(String world, String dimension) {
        WorldTarget target = new WorldTarget(world, Objects.requireNonNull(dimension)); protect(target);
        worlds.compute(world, (name, previous) -> {
            if(previous == null) throw new IllegalStateException("World is not authorized: " + world);
            if(previous.target().dimensionKey() != null && !previous.target().dimensionKey().equals(dimension))
                throw new IllegalStateException("World dimension changed: " + world);
            if(worlds.values().stream().anyMatch(binding -> !binding.target().worldName().equals(world) &&
                dimension.equals(binding.target().dimensionKey())))
                throw new IllegalStateException("Dimension already belongs to another world: " + dimension);
            return new Binding<>(target, previous.plan());
        });
    }
    private void protect(WorldTarget target) {
        if(protectedWorlds.contains(target.worldName()) || (target.dimensionKey() != null && BUILTIN_DIMENSIONS.contains(target.dimensionKey())))
            throw new IllegalArgumentException("Protected generation target: " + target);
    }
    private GenerationProgram<B, M> resolve(GenerationContext context) {
        Binding<B, M> binding = worlds.get(context.target().worldName());
        if(binding == null || !binding.target().equals(context.target()) || binding.target().dimensionKey() == null)
            throw new IllegalStateException("Unbound generation target: " + context.target());
        Shape shape = new Shape(context.seed(), context.minY(), context.maxY());
        Shape previous = shapes.putIfAbsent(context.target().worldName(), shape);
        if(previous != null && !previous.equals(shape)) throw new IllegalStateException("Seed or height bounds changed for active world "
            + context.target().worldName() + ": expected " + previous + ", received " + shape);
        return binding.plan().program();
    }
    public Binding<B, M> binding(String world) {
        Binding<B, M> binding = worlds.get(world);
        if(binding == null) throw new IllegalStateException("World is not authorized: " + world);
        return binding;
    }
    public Map<String, Binding<B, M>> bindings() { return Map.copyOf(worlds); }
    public void generate(GenerationContext context, int x, int z, BlockVolume<B> output) {
        Objects.requireNonNull(output);
        if(output.minY() != context.minY() || output.maxY() != context.maxY())
            throw new IllegalArgumentException("Output height bounds differ from generation context");
        resolve(context).generate(context, x, z, output);
    }
    public void decorate(GenerationContext context, int x, int z, BlockVolume<B> output) {
        if(output.minY() != context.minY() || output.maxY() != context.maxY())
            throw new IllegalArgumentException("Decoration height bounds differ from context");
        resolve(context).decorate(context, x, z, output);
    }
    public B blockAt(GenerationContext context, int x, int y, int z) { return resolve(context).blockAt(context, x, y, z); }
    public M biomeAt(GenerationContext context, int x, int y, int z) { return resolve(context).biomeAt(context, x, y, z); }
}
