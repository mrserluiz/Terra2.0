package br.com.mrserluiz.terra2.core;

public record SafetyDecision(boolean allowed, Code code, String message) {
    public enum Code {
        ALLOWED_NEW_WORLD,
        ALLOWED_MANAGED_WORLD,
        GENERATION_DISABLED,
        WORLD_NOT_EXPLICITLY_ENABLED,
        MAIN_WORLD_BLOCKED,
        EXISTING_WORLD_BLOCKED,
        PACK_MISMATCH,
        MISSING_WORLD_CREATION_DISABLED,
        INVALID_REQUEST
    }

    public static SafetyDecision allow(Code code, String message) {
        return new SafetyDecision(true, code, message);
    }

    public static SafetyDecision deny(Code code, String message) {
        return new SafetyDecision(false, code, message);
    }
}
