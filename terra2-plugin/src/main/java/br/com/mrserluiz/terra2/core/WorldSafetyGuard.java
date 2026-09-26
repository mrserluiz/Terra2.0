package br.com.mrserluiz.terra2.core;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Pure safety policy. This class performs no Bukkit, filesystem or Terra calls.
 */
public final class WorldSafetyGuard {
    private static final Set<String> DEFAULT_WORLD_NAMES = Set.of(
            "world", "world_nether", "world_the_end"
    );

    public SafetyDecision evaluate(
            GenerationSafetySettings settings,
            WorldDefinition definition,
            WorldInspection inspection
    ) {
        if (settings == null || definition == null || inspection == null || inspection.state() == null) {
            return SafetyDecision.deny(SafetyDecision.Code.INVALID_REQUEST, "Incomplete safety request.");
        }

        if (!settings.enabled()) {
            return SafetyDecision.deny(SafetyDecision.Code.GENERATION_DISABLED,
                    "World generation is disabled globally.");
        }

        if (!definition.enabled()) {
            return SafetyDecision.deny(SafetyDecision.Code.WORLD_NOT_EXPLICITLY_ENABLED,
                    "The world is not explicitly enabled.");
        }

        if (!same(definition.worldName(), inspection.worldName())) {
            return SafetyDecision.deny(SafetyDecision.Code.INVALID_REQUEST,
                    "Configured and inspected world names do not match.");
        }

        boolean protectedMainWorld = inspection.primaryWorld()
                || DEFAULT_WORLD_NAMES.contains(normalize(definition.worldName()));
        if (protectedMainWorld) {
            return SafetyDecision.deny(SafetyDecision.Code.MAIN_WORLD_BLOCKED,
                    "Main worlds are permanently protected by Terra 2.0.");
        }

        return switch (inspection.state()) {
            case MISSING -> evaluateMissing(definition);
            case EXISTING_UNMANAGED -> SafetyDecision.deny(SafetyDecision.Code.EXISTING_WORLD_BLOCKED,
                    "Terra 2.0 will not attach to an unmanaged existing world.");
            case EXISTING_MANAGED_BY_TERRA2 -> evaluateManaged(definition, inspection);
        };
    }

    private SafetyDecision evaluateMissing(WorldDefinition definition) {
        if (!definition.createIfMissing()) {
            return SafetyDecision.deny(SafetyDecision.Code.MISSING_WORLD_CREATION_DISABLED,
                    "The world does not exist and create-if-missing is disabled.");
        }
        return SafetyDecision.allow(SafetyDecision.Code.ALLOWED_NEW_WORLD,
                "A new isolated world may be created with the selected Terra pack.");
    }

    private SafetyDecision evaluateManaged(WorldDefinition definition, WorldInspection inspection) {
        if (!same(definition.packId(), inspection.managedPackId())) {
            return SafetyDecision.deny(SafetyDecision.Code.PACK_MISMATCH,
                    "The configured pack differs from the world's Terra 2.0 manifest.");
        }
        return SafetyDecision.allow(SafetyDecision.Code.ALLOWED_MANAGED_WORLD,
                "The existing world is already managed by Terra 2.0 with the same pack.");
    }

    private static boolean same(String first, String second) {
        return Objects.equals(normalize(first), normalize(second));
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
