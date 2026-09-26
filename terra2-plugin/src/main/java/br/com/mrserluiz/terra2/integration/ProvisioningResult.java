package br.com.mrserluiz.terra2.integration;

import br.com.mrserluiz.terra2.core.SafetyDecision;
import java.util.Set;

public record ProvisioningResult(Status status, String message, SafetyDecision safetyDecision, Set<String> missingAddons) {
    public enum Status { CREATED, ALREADY_MANAGED, BLOCKED, PACK_NOT_FOUND, PACK_INVALID, TERRA_UNAVAILABLE, FAILED }

    public ProvisioningResult {
        missingAddons = Set.copyOf(missingAddons);
    }
}
