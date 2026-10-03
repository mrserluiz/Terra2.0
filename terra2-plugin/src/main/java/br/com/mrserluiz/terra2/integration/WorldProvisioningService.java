package br.com.mrserluiz.terra2.integration;

import br.com.mrserluiz.terra2.core.GenerationSafetySettings;
import br.com.mrserluiz.terra2.core.SafetyDecision;
import br.com.mrserluiz.terra2.core.WorldDefinition;
import br.com.mrserluiz.terra2.core.WorldInspection;
import br.com.mrserluiz.terra2.core.WorldSafetyGuard;
import br.com.mrserluiz.terra2.core.WorldState;
import java.nio.file.Files;
import java.time.Clock;
import java.util.Optional;
import java.util.Set;

public final class WorldProvisioningService {
    private final CommunityPackCatalog catalog;
    private final PackValidator validator;
    private final WorldManifestStore manifests;
    private final GenerationRuntime runtime;
    private final WorldSafetyGuard safetyGuard;
    private final Clock clock;

    public WorldProvisioningService(CommunityPackCatalog catalog, PackValidator validator,
                                    WorldManifestStore manifests, GenerationRuntime runtime,
                                    WorldSafetyGuard safetyGuard, Clock clock) {
        this.catalog = catalog;
        this.validator = validator;
        this.manifests = manifests;
        this.runtime = runtime;
        this.safetyGuard = safetyGuard;
        this.clock = clock;
    }

    public ProvisioningResult provision(GenerationSafetySettings settings, WorldDefinition definition,
                                        Set<String> primaryWorldNames) {
        try {
            Optional<WorldManifest> manifest = manifests.read(definition.worldName());
            boolean exists = Files.exists(manifests.worldPath(definition.worldName()));
            WorldState state = !exists ? WorldState.MISSING
                    : manifest.isPresent() ? WorldState.EXISTING_MANAGED_BY_TERRA2 : WorldState.EXISTING_UNMANAGED;
            WorldInspection inspection = new WorldInspection(definition.worldName(), state,
                    primaryWorldNames.stream().anyMatch(name -> name.equalsIgnoreCase(definition.worldName())),
                    manifest.map(WorldManifest::packId).orElse(null));
            SafetyDecision safety = safetyGuard.evaluate(settings, definition, inspection);
            if (!safety.allowed()) {
                return result(ProvisioningResult.Status.BLOCKED, safety.message(), safety, Set.of());
            }

            if (!runtime.engineAvailable()) {
                return result(ProvisioningResult.Status.ENGINE_UNAVAILABLE,
                        "The standalone Terra 2.0 generation engine is not available yet. No world was touched.",
                        safety, Set.of());
            }
            Optional<PackDescriptor> found = catalog.findById(definition.packId());
            if (found.isEmpty()) {
                return result(ProvisioningResult.Status.PACK_NOT_FOUND, "The requested Community Pack is not installed.", safety, Set.of());
            }
            PackDescriptor pack = found.get();
            PackValidation validation = validator.validate(pack, definition.packId(), runtime.installedAddonIds());
            if (!validation.valid() || !runtime.packLoaded(pack.id())) {
                String message = validation.valid() ? "Terra 2.0 did not load the requested pack." : validation.message();
                return result(ProvisioningResult.Status.PACK_INVALID, message, safety, validation.missingAddons());
            }

            if (state == WorldState.EXISTING_MANAGED_BY_TERRA2) {
                return result(ProvisioningResult.Status.ALREADY_MANAGED, safety.message(), safety, Set.of());
            }
            if (!runtime.createWorld(definition, pack)) {
                return result(ProvisioningResult.Status.FAILED, "Paper did not create the world.", safety, Set.of());
            }
            manifests.write(new WorldManifest(definition.worldName(), pack.id(), pack.version(), clock.millis()));
            return result(ProvisioningResult.Status.CREATED, "World created and registered as Terra 2.0 managed.", safety, Set.of());
        } catch (Exception error) {
            return result(ProvisioningResult.Status.FAILED, error.getClass().getSimpleName() + ": " + error.getMessage(), null, Set.of());
        }
    }

    private static ProvisioningResult result(ProvisioningResult.Status status, String message,
                                              SafetyDecision safety, Set<String> missingAddons) {
        return new ProvisioningResult(status, message, safety, missingAddons);
    }
}
