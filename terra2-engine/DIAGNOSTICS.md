# Diagnostics (7.0.7-BETA)

`plugins/Terra2/reports/` is created during enable. Generation exceptions produce `generation-*.txt`.
An independent daemon samples all thread stacks and lock ownership after the server heartbeat stops for at least 15 seconds. It saves `stall-*.txt` with the last 256 KiB of `logs/latest.log`, up to three stall snapshots per server session, spaced at least 30 seconds apart.
Snapshots describe observed waits and work; they do not automatically establish the cause. Startup work after enabling can also produce a stall snapshot. The monitor does not cancel world generation or modify chunks.
A hard process kill, disk failure, or JVM failure can still prevent a report. Console lines absent from Paper's file log cannot be reconstructed. Reports stay on the server; no automatic upload occurs.
Authorization reload remains `/terra2 reload`; installing this JAR requires one complete restart.
