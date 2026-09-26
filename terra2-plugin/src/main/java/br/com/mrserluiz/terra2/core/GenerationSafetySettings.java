package br.com.mrserluiz.terra2.core;

/**
 * Global safety switches. Defaults are deliberately restrictive.
 */
public record GenerationSafetySettings(
        boolean enabled,
        boolean allowMainWorld,
        boolean affectExistingWorlds,
        boolean generateNewChunksOnly,
        boolean requireExplicitWorldSelection
) {
    public static GenerationSafetySettings safeDefaults() {
        return new GenerationSafetySettings(false, false, false, true, true);
    }
}
