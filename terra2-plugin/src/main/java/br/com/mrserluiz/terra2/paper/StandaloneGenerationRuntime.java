package br.com.mrserluiz.terra2.paper;

import br.com.mrserluiz.terra2.core.WorldDefinition;
import br.com.mrserluiz.terra2.integration.GenerationRuntime;
import br.com.mrserluiz.terra2.integration.PackDescriptor;
import java.util.Set;

/**
 * Safe bootstrap for the native Terra 2.0 engine.
 *
 * <p>The first standalone milestone deliberately reports the engine as unavailable. This lets
 * the Paper plugin load with an inert configuration without delegating to the legacy Terra plugin
 * or creating a world before the native engine exists.</p>
 */
final class StandaloneGenerationRuntime implements GenerationRuntime {
    @Override
    public boolean engineAvailable() {
        return false;
    }

    @Override
    public Set<String> installedAddonIds() {
        return Set.of();
    }

    @Override
    public boolean packLoaded(String packId) {
        return false;
    }

    @Override
    public boolean createWorld(WorldDefinition definition, PackDescriptor pack) {
        throw new IllegalStateException(
                "The standalone Terra 2.0 generation engine is not implemented yet.");
    }
}
