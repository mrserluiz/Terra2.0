package br.com.mrserluiz.terra2.integration;

import java.util.Set;

public record PackValidation(boolean valid, Set<String> missingAddons, String message) {
    public PackValidation {
        missingAddons = Set.copyOf(missingAddons);
    }
}
