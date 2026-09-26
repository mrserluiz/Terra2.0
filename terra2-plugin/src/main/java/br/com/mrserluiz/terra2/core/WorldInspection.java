package br.com.mrserluiz.terra2.core;

/** Read-only facts supplied by the future Paper adapter. */
public record WorldInspection(
        String worldName,
        WorldState state,
        boolean primaryWorld,
        String managedPackId
) {
}
