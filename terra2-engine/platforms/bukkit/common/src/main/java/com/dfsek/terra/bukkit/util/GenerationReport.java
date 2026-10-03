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
