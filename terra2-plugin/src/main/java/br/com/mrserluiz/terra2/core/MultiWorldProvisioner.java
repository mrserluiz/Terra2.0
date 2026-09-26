package br.com.mrserluiz.terra2.core;

import br.com.mrserluiz.terra2.integration.ProvisioningResult;
import br.com.mrserluiz.terra2.integration.WorldProvisioningService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Processes every explicitly configured world without allowing one entry to authorize another. */
public final class MultiWorldProvisioner {
    private final WorldProvisioningService provisioningService;

    public MultiWorldProvisioner(WorldProvisioningService provisioningService) {
        this.provisioningService = provisioningService;
    }

    public Map<String, ProvisioningResult> provisionAll(
            GenerationSafetySettings settings,
            List<WorldDefinition> definitions,
            Set<String> primaryWorldNames
    ) {
        if (definitions == null || definitions.isEmpty()) return Map.of();

        Map<String, ProvisioningResult> results = new LinkedHashMap<>();
        for (WorldDefinition definition : definitions) {
            ProvisioningResult result = provisioningService.provision(settings, definition, primaryWorldNames);
            results.put(definition.worldName(), result);
        }
        return Map.copyOf(results);
    }
}
