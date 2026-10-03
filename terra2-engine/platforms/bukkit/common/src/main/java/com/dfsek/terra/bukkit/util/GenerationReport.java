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
    private GenerationReport() {}
    public static void initialize(Path dataFolder, String details) {
        directory = dataFolder.resolve("reports");
        environment = details;
        count.set(0);
        try { Files.createDirectories(directory); }
        catch(java.io.IOException e) { LoggerFactory.getLogger(GenerationReport.class).error("Could not create reports directory", e); }
    }
    public static synchronized void stalledServer(long seconds) {
        if(directory == null || count.incrementAndGet() > 32) return;
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
