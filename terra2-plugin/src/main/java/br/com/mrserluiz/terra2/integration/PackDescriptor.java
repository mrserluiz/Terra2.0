package br.com.mrserluiz.terra2.integration;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

public record PackDescriptor(String id, String version, Set<String> requiredAddons, Path source) {
    public PackDescriptor {
        id = requireText(id, "id");
        version = requireText(version, "version");
        requiredAddons = Set.copyOf(Objects.requireNonNull(requiredAddons, "requiredAddons"));
        source = Objects.requireNonNull(source, "source").toAbsolutePath().normalize();
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " cannot be blank");
        return normalized;
    }
}
