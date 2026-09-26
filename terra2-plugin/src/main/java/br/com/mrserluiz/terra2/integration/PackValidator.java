package br.com.mrserluiz.terra2.integration;

import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class PackValidator {
    public PackValidation validate(PackDescriptor pack, String requestedId, Set<String> installedAddons) {
        if (!pack.id().equalsIgnoreCase(requestedId)) {
            return new PackValidation(false, Set.of(), "The requested pack ID does not match pack.yml.");
        }
        Set<String> normalizedInstalled = installedAddons.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        Set<String> missing = pack.requiredAddons().stream()
                .filter(addon -> !normalizedInstalled.contains(addon.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)));
        if (!missing.isEmpty()) {
            return new PackValidation(false, missing, "Required Terra addons are missing.");
        }
        return new PackValidation(true, Set.of(), "Pack manifest and addon requirements are valid.");
    }
}
