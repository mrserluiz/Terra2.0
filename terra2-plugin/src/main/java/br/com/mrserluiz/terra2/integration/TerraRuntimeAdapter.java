package br.com.mrserluiz.terra2.integration;

import br.com.mrserluiz.terra2.core.WorldDefinition;
import java.util.Set;

/** Boundary implemented by the Paper/Terra platform adapter. */
public interface TerraRuntimeAdapter {
    boolean terraAvailable();
    Set<String> installedAddonIds();
    boolean packLoaded(String packId);
    boolean createWorld(WorldDefinition definition, PackDescriptor pack) throws Exception;
}
