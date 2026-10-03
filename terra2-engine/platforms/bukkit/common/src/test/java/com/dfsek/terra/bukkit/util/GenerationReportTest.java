package com.dfsek.terra.bukkit.util;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class GenerationReportTest {
    @TempDir Path folder;
    @Test void capturesWorkerStackWithoutAnException() throws Exception {
        GenerationReport.initialize(folder, "diagnostic test");
        assertTrue(Files.isDirectory(folder.resolve("reports")));
        var entered = new java.util.concurrent.CountDownLatch(1);
        var release = new java.util.concurrent.CountDownLatch(1);
        Thread worker = new Thread(() -> {
            entered.countDown();
            try { release.await(); } catch(InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "Terra2-test-blocked-worker");
        worker.start();
        try {
            assertTrue(entered.await(5, java.util.concurrent.TimeUnit.SECONDS));
            GenerationReport.stalledServer(15);
            try(var files = Files.list(folder.resolve("reports"))) {
                String report = Files.readString(files.findFirst().orElseThrow());
                assertTrue(report.contains("Terra2-test-blocked-worker"));
                assertTrue(report.contains("Heartbeat delay: 15"));
                assertTrue(report.contains("Recent Paper console log"));
            }
        } finally { release.countDown(); worker.join(5000); }
    }
    @Test void stallCaptureRemainsAvailableAfterFailureLimitAndUpdatesLatest() throws Exception {
        GenerationReport.initialize(folder, "test");
        for(int i = 0; i < 33; i++) GenerationReport.failure("test", "test", "test", new RuntimeException("failure"));
        GenerationReport.stalledServer(18);
        Path latest = folder.resolve("reports/stall-latest.txt");
        assertTrue(Files.readString(latest).contains("Heartbeat delay: 18"));
        GenerationReport.stalledServer(48);
        assertTrue(Files.readString(latest).contains("Heartbeat delay: 48"));
    }
    @Test void recordsContextAndNestedCauseWithBoundedOutput() throws Exception {
        GenerationReport.initialize(folder, "Paper 26.2 / Java 25");
        var error = new IllegalStateException("generation failed", new IllegalArgumentException("root cause"));
        for(int i = 0; i < 40; i++) GenerationReport.failure("noise 1,2", "test", "OVERWORLD", error);
        try(var files = Files.list(folder.resolve("reports"))) {
            var paths = files.toList();
            assertEquals(32, paths.size());
            String report = Files.readString(paths.getFirst());
            assertTrue(report.contains("World: test"));
            assertTrue(report.contains("Pack: OVERWORLD"));
            assertTrue(report.contains("Stage: noise 1,2"));
            assertTrue(report.contains("Caused by: java.lang.IllegalArgumentException: root cause"));
        }
    }
}
