package com.dfsek.terra.bukkit.util;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class ConsoleCaptureTest {
    @TempDir Path folder;
    @Test void capturesCommandBeforeExecutionAndFlushesNewErrorsWithoutServerTicks() throws Exception {
        Path log = folder.resolve("latest.log"); Files.writeString(log, "before command\n");
        try(var capture = new ConsoleCapture(folder.resolve("reports"), log, "7.0.10", false)) {
            Path report = capture.start("/mv create terra2_teste normal --generator Terra2:OVERWORLD", "OP");
            assertTrue(Files.readString(report).contains("/mv create terra2_teste"));
            Files.writeString(log, "before command\nERROR actual generation failure\n");
            capture.poll();
            assertTrue(Files.readString(report).contains("actual generation failure"));
            assertEquals(Files.readString(report), Files.readString(folder.resolve("reports/console-latest.txt")));
            Files.writeString(log, "before command\nERROR actual generation failure\nlast line\n");
            capture.stop("manual");
            assertTrue(Files.readString(report).contains("last line"));
        }
    }
    @Test void handlesMissingAndTruncatedConsoleAndKeepsSameSessionForMvConfirm() throws Exception {
        Path log = folder.resolve("latest.log");
        try(var capture = new ConsoleCapture(folder.resolve("reports"), log, "test", false)) {
            Path report = capture.start("mv create test normal", "Console"); capture.poll();
            Files.writeString(log, "long original content\n"); capture.poll();
            assertEquals(report, capture.start("mv confirm 1234", "Console"));
            Files.writeString(log, "short\n"); capture.poll(); capture.stop("test");
            String text = Files.readString(report);
            assertTrue(text.contains("mv confirm 1234"));
            assertTrue(text.contains("log truncated")); assertTrue(text.contains("short"));
        }
    }
    @Test void limitsRetainedSessionsAndPreservesPreviouslyClosedReports() throws Exception {
        Path reports = folder.resolve("reports");
        try(var capture = new ConsoleCapture(reports, folder.resolve("missing.log"), "test", false)) {
            for(int i = 0; i < 10; i++) { capture.start("test " + i, "Console"); capture.stop("test"); }
            try(var files = Files.list(reports)) {
                assertEquals(8, files.filter(path -> path.toString().endsWith(".log")).count());
            }
            assertTrue(Files.readString(reports.resolve("console-latest.txt")).contains("Command: test 9"));
        }
    }
}
