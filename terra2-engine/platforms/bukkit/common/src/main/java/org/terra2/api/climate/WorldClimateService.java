package org.terra2.api.climate;

import java.util.Map;
import java.util.Optional;
import org.bukkit.World;

/** Obtain from Bukkit's ServicesManager. API 1 is optional and read-only. */
public interface WorldClimateService {
    int apiVersion();
    Optional<WorldClimateProfile> profile(World world);
    /** Classloader-neutral contract for optional adapters. Empty means no override. */
    Map<String, String> describe(World world);
}
