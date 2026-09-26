package br.com.mrserluiz.terra2.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldSafetyGuardTest {
    private final WorldSafetyGuard guard = new WorldSafetyGuard();

    @Test
    void safeDefaultsNeverGenerate() {
        SafetyDecision decision = guard.evaluate(
                GenerationSafetySettings.safeDefaults(),
                enabledWorld("ether_expansion"),
                missing("ether_expansion")
        );

        assertFalse(decision.allowed());
        assertEquals(SafetyDecision.Code.GENERATION_DISABLED, decision.code());
    }

    @Test
    void blocksMainWorldEvenWhenGenerationIsEnabled() {
        SafetyDecision decision = guard.evaluate(
                enabledSafeSettings(),
                enabledWorld("world"),
                new WorldInspection("world", WorldState.MISSING, true, null)
        );

        assertFalse(decision.allowed());
        assertEquals(SafetyDecision.Code.MAIN_WORLD_BLOCKED, decision.code());
    }

    @Test
    void allowsExplicitNewIsolatedWorld() {
        SafetyDecision decision = guard.evaluate(
                enabledSafeSettings(),
                enabledWorld("ether_expansion"),
                missing("ether_expansion")
        );

        assertTrue(decision.allowed());
        assertEquals(SafetyDecision.Code.ALLOWED_NEW_WORLD, decision.code());
    }

    @Test
    void refusesToTakeOverExistingUnmanagedWorld() {
        SafetyDecision decision = guard.evaluate(
                enabledSafeSettings(),
                enabledWorld("survival_old"),
                new WorldInspection("survival_old", WorldState.EXISTING_UNMANAGED, false, null)
        );

        assertFalse(decision.allowed());
        assertEquals(SafetyDecision.Code.EXISTING_WORLD_BLOCKED, decision.code());
    }

    @Test
    void reloadsManagedWorldOnlyWithSamePack() {
        SafetyDecision accepted = guard.evaluate(
                enabledSafeSettings(),
                enabledWorld("ether_expansion"),
                new WorldInspection("ether_expansion", WorldState.EXISTING_MANAGED_BY_TERRA2,
                        false, "OVERWORLD")
        );
        SafetyDecision rejected = guard.evaluate(
                enabledSafeSettings(),
                enabledWorld("ether_expansion"),
                new WorldInspection("ether_expansion", WorldState.EXISTING_MANAGED_BY_TERRA2,
                        false, "SKYLANDS")
        );

        assertTrue(accepted.allowed());
        assertFalse(rejected.allowed());
        assertEquals(SafetyDecision.Code.PACK_MISMATCH, rejected.code());
    }

    private static GenerationSafetySettings enabledSafeSettings() {
        return new GenerationSafetySettings(true, false, true, true);
    }

    private static WorldDefinition enabledWorld(String name) {
        return new WorldDefinition(name, true, true, "OVERWORLD", "NORMAL", 128734921L);
    }

    private static WorldInspection missing(String name) {
        return new WorldInspection(name, WorldState.MISSING, false, null);
    }
}
