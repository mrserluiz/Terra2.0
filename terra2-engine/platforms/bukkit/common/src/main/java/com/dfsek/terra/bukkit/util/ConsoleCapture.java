package com.dfsek.terra.bukkit.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.LoggerFactory;

/** Independent disk tailer: keeps flushing even while the server thread is blocked. */
public final class ConsoleCapture implements AutoCloseable {
    private static final long LIMIT = 16L * 1024 * 1024;
    private final Path directory;
    private final Path log;
    private final String environment;
    private final ScheduledExecutorService worker;
    private FileChannel output;
    private FileChannel latest;
    private Path report;
    private long position;
    private long written;
    private long deadline;

    public ConsoleCapture(Path directory, Path log, String environment) {
        this(directory, log, environment, true);
    }
    ConsoleCapture(Path directory, Path log, String environment, boolean scheduled) {
        this.directory = directory; this.log = log; this.environment = environment;
        worker = Executors.newSingleThreadScheduledExecutor(task -> {
            Thread thread = new Thread(task, "Terra2-console-capture"); thread.setDaemon(true); return thread;
        });
        if(scheduled) worker.scheduleWithFixedDelay(this::pollSafely, 1, 1, TimeUnit.SECONDS);
    }
    public synchronized Path start(String command, String sender) throws IOException {
        deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(10);
        if(output != null) {
            write("\n[Terra2 command " + Instant.now() + "] " + clean(sender) + ": " + clean(command) + "\n");
            flush(); return report;
        }
        Files.createDirectories(directory);
        try(var files = Files.list(directory)) {
            var previous = files.filter(path -> path.getFileName().toString().matches("console-[0-9]+\\.log"))
                .sorted(java.util.Comparator.comparingLong((Path path) -> path.toFile().lastModified()).reversed()).toList();
            for(int i = 7; i < previous.size(); i++) Files.deleteIfExists(previous.get(i));
        }
        report = Files.createTempFile(directory, "console-", ".log");
        try {
            output = FileChannel.open(report, StandardOpenOption.WRITE);
            latest = FileChannel.open(directory.resolve("console-latest.txt"), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            written = 0;
            position = Files.isRegularFile(log) ? Math.max(0, Files.size(log) - 65536) : 0;
            write("Terra2 console capture started at " + Instant.now() + "\n" + environment + "\n"
                + "Sender: " + clean(sender) + "\nCommand: " + clean(command) + "\n"
                + "Includes up to 64 KiB before the command; polls every second; limit 10 minutes / 16 MiB.\n"
                + "Source: " + log + "\n\n");
            flush();
        } catch(IOException error) { closeFiles(); throw error; }
        return report;
    }
    private static String clean(String text) { return text.replace('\n', ' ').replace('\r', ' '); }
    public synchronized String status() { return output == null ? "Captura inativa." : "Captura ativa: " + report; }
    private void pollSafely() {
        try { poll(); }
        catch(Exception error) {
            // Stop once; do not fill latest.log with an error every second.
            synchronized(this) { closeFiles(); }
            LoggerFactory.getLogger(ConsoleCapture.class).error("Terra2 console capture failed", error);
        }
    }
    synchronized void poll() throws IOException {
        if(output == null) return;
        if(Files.isRegularFile(log)) {
            try(var source = FileChannel.open(log, StandardOpenOption.READ)) {
                if(source.size() < position) { position = 0; write("\n[Terra2: console log truncated; reading from start]\n"); }
                source.position(position);
                var buffer = ByteBuffer.allocate(65536);
                long budget = Math.min(1024 * 1024, LIMIT - written);
                while(budget > 0) {
                    buffer.clear(); buffer.limit((int) Math.min(buffer.capacity(), budget));
                    int count = source.read(buffer);
                    if(count <= 0) break;
                    buffer.flip(); write(buffer); position += count; budget -= count;
                }
            }
        }
        flush();
        if(written >= LIMIT || System.nanoTime() >= deadline) finish("Limite de captura atingido");
    }
    private void write(String text) throws IOException { write(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8))); }
    private void write(ByteBuffer buffer) throws IOException {
        int bytes = buffer.remaining();
        var copy = buffer.duplicate();
        while(buffer.hasRemaining()) output.write(buffer);
        while(copy.hasRemaining()) latest.write(copy);
        written += bytes;
    }
    private void flush() throws IOException { output.force(false); latest.force(false); }
    public synchronized Path stop(String reason) throws IOException {
        poll();
        return finish(reason);
    }
    private Path finish(String reason) throws IOException {
        if(output != null) {
            try { write("\n[Terra2 capture stopped " + Instant.now() + "] " + clean(reason) + "\n"); flush(); }
            finally { closeFiles(); }
        }
        return report;
    }
    private void closeFiles() {
        if(output != null) { try { output.close(); } catch(IOException ignored) {} output = null; }
        if(latest != null) { try { latest.close(); } catch(IOException ignored) {} latest = null; }
    }
    @Override public void close() {
        worker.shutdownNow();
        synchronized(this) {
            try { poll(); stop("Plugin desativado"); }
            catch(IOException error) { closeFiles(); }
        }
    }
}
