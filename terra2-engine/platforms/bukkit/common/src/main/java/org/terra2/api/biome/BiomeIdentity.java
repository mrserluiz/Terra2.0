package org.terra2.api.biome;
/** Original engine identity plus the vanilla reference written at generation time. */
public record BiomeIdentity(String terraId, String registeredId, String vanillaReference) { }
