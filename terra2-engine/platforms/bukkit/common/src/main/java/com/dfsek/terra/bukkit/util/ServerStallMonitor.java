package com.dfsek.terra.bukkit.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Observes progress from a separate daemon; never calls the Bukkit world API. */
public final class ServerStallMonitor implements AutoCloseable {
    private volatile long heartbeat = System.nanoTime();
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "Terra2-stall-monitor");
        thread.setDaemon(true);
        return thread;
    });
    private long lastReport;
    private int reports;
    public ServerStallMonitor() {
        worker.scheduleWithFixedDelay(this::check, 5, 5, TimeUnit.SECONDS);
    }
    public void heartbeat() { heartbeat = System.nanoTime(); }
    private void check() {
        long now = System.nanoTime();
        long delay = TimeUnit.NANOSECONDS.toSeconds(now - heartbeat);
        if(delay >= 15 && reports < 3 && (lastReport == 0 || now - lastReport >= TimeUnit.SECONDS.toNanos(30))) {
            lastReport = now;
            reports++;
            GenerationReport.stalledServer(delay);
        }
    }
    @Override public void close() { worker.shutdownNow(); }
}
