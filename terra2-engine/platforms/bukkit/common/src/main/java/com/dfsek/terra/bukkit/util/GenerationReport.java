package com.dfsek.terra.bukkit.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.LoggerFactory;

/** Bounded, independent diagnostics. Never replaces or suppresses the original failure. */
public final class GenerationReport {
    private static volatile Path directory;
    private static volatile String environment;
    private static final AtomicInteger count = new AtomicInteger();
    private static volatile java.util.function.Supplier<String> packDiagnostics = () -> "Pack discovery unavailable";
    public static void packDiagnostics(java.util.function.Supplier<String> supplier) { packDiagnostics = supplier; }
    private GenerationReport() {}
    public static void initialize(Path dataFolder, String details) {
        directory = dataFolder.resolve("reports");
        environment = details;
        count.set(0);
        try { Files.createDirectories(directory); }
        catch(java.io.IOException e) { LoggerFactory.getLogger(GenerationReport.class).error("Could not create reports directory", e); }
    }
    public static synchronized void stalledServer(long seconds) {
        if(directory == null) return;
        try {
            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            writer.println("Terra2 server stall snapshot at " + Instant.now());
            writer.println(environment);
            writer.println("Heartbeat delay: " + seconds + " seconds; this snapshot does not establish the cause.");
            var bean = java.lang.management.ManagementFactory.getThreadMXBean();
            for(var thread : bean.dumpAllThreads(true, true)) {
                writer.println("Thread: " + thread.getThreadName() + " id=" + thread.getThreadId() + " state=" + thread.getThreadState());
                writer.println("Waiting on: " + thread.getLockInfo() + "; owner=" + thread.getLockOwnerName());
                for(var frame : thread.getStackTrace()) writer.println("    at " + frame);
                for(var lock : thread.getLockedMonitors()) writer.println("    locked monitor " + lock);
                for(var lock : thread.getLockedSynchronizers()) writer.println("    locked synchronizer " + lock);
            }
            writer.println("\nRecent Paper console log (last 256 KiB):");
            Path log = Path.of("logs", "latest.log");
            if(Files.isRegularFile(log)) {
                try(var input = new java.io.RandomAccessFile(log.toFile(), "r")) {
                    long size = input.length();
                    input.seek(Math.max(0, size - 262144));
                    byte[] tail = new byte[(int) Math.min(size, 262144)];
                    input.readFully(tail);
                    writer.println(new String(tail, java.nio.charset.StandardCharsets.UTF_8));
                } catch(java.io.IOException e) { writer.println("Unable to read log tail: " + e); }
            } else writer.println("logs/latest.log not available.");
            Files.createDirectories(directory);
            Path file = Files.createTempFile(directory, "stall-", ".txt");
            Files.writeString(file, buffer.toString());
            Path latest = directory.resolve("stall-latest.txt");
            Path replacement = Files.createTempFile(directory, "latest-", ".tmp");
            try {
                Files.copy(file, replacement, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                try {
                    Files.move(replacement, latest, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
                } catch(java.nio.file.AtomicMoveNotSupportedException e) {
                    Files.move(replacement, latest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            } finally { Files.deleteIfExists(replacement); }
            // Keep a rolling history, rather than exhausting diagnostics for this session.
            try(var files = Files.list(directory)) {
                var snapshots = files.filter(path -> path.getFileName().toString().matches("stall-[0-9]+\\.txt"))
                    .sorted(java.util.Comparator.comparingLong((Path path) -> path.toFile().lastModified()).reversed())
                    .toList();
                for(int i = 8; i < snapshots.size(); i++) Files.deleteIfExists(snapshots.get(i));
            }
            LoggerFactory.getLogger(GenerationReport.class).error("Terra2 stalled-server report saved: {}", file);
        } catch(Exception e) {
            LoggerFactory.getLogger(GenerationReport.class).error("Could not save stall snapshot", e);
        }
    }

    public static synchronized void failure(String stage, String world, String pack, Throwable error) {
        if(directory == null || count.incrementAndGet() > 32) return;
        try {
            Files.createDirectories(directory);
            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            writer.println("Terra2 generation failure at " + Instant.now());
            writer.println(environment);
            writer.println("Stage: " + stage);
            writer.println("World: " + world);
            writer.println("Pack: " + pack);
            writer.println("Requested Pack ID: " + pack);
            try { writer.println(packDiagnostics.get()); }
            catch(Exception diagnosticFailure) { writer.println("Resolution diagnostics unavailable: " + diagnosticFailure); }
            writer.println("Thread: " + Thread.currentThread().getName());
            error.printStackTrace(writer);
            Path file = Files.createTempFile(directory, "generation-", ".txt");
            Files.writeString(file, buffer.toString());
            LoggerFactory.getLogger(GenerationReport.class).error("Terra2 failure report saved: {}", file);
        } catch(Exception reportingError) {
            LoggerFactory.getLogger(GenerationReport.class).error("Could not save Terra2 failure report", reportingError);
        }
    }
}
