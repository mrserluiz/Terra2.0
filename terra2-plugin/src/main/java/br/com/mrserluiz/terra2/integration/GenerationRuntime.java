package br.com.mrserluiz.terra2.integration;

import br.com.mrserluiz.terra2.core.WorldDefinition;
import java.util.Set;

/** Boundary implemented by Terra 2.0 generation engines. */
public interface GenerationRuntime {
    boolean engineAvailable();
    Set<String> installedAddonIds();
    boolean packLoaded(String packId);
    boolean createWorld(WorldDefinition definition, PackDescriptor pack) throws Exception;
}
