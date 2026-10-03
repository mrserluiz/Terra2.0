package com.dfsek.terra.api.config;

import java.nio.file.Path;
import com.dfsek.terra.api.Platform;

/**
 * Extension boundary for external pack formats, including future vanilla datapacks.
 * Implementations must validate/translate the source before returning a native ConfigPack.
 * Loading a pack must not create worlds, attach generators, or rewrite existing saves.
 * The caller remains responsible for addon validation and explicit world authorization.
 */
public interface PackSourceAdapter {
    /** Stable adapter identifier, for example terra2:vanilla_datapack. */
    String id();

    /** Pure capability check. Must not mutate the source or platform. */
    boolean supports(Path source);

    /**
     * Load a source into Terra's native pack contracts. Unsupported vanilla features
     * must raise an error instead of silently producing a different world.
     * Native Community Packs continue to use their existing loader unchanged.
     */
    ConfigPack load(Path source, Platform platform) throws Exception;
}
