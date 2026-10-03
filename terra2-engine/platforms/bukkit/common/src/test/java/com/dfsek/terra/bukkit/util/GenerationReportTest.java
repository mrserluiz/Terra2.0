package com.dfsek.terra.bukkit.util;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class GenerationReportTest {
    @TempDir Path folder;
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
