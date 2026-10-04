# Diagnostics (7.0.7-BETA)

`plugins/Terra2/reports/` is created during enable. Generation exceptions produce `generation-*.txt`.
An independent daemon samples all thread stacks and lock ownership after the server heartbeat stops for at least 15 seconds. It saves `stall-*.txt` with the last 256 KiB of `logs/latest.log`, up to three stall snapshots per server session, spaced at least 30 seconds apart.
Snapshots describe observed waits and work; they do not automatically establish the cause. Startup work after enabling can also produce a stall snapshot. The monitor does not cancel world generation or modify chunks.
A hard process kill, disk failure, or JVM failure can still prevent a report. Console lines absent from Paper's file log cannot be reconstructed. Reports stay on the server; no automatic upload occurs.
Authorization reload remains `/terra2 reload`; installing this JAR requires one complete restart.

## 7.0.8: recurring diagnostics and spawn initialization

The monitor captures at 15 seconds and then every 30 seconds, up to three snapshots
per uninterrupted stall. A resumed heartbeat resets that allowance.
`reports/stall-latest.txt` is atomically replaced on every capture; the last eight
numbered stall snapshots are retained. Generation exception limits do not suppress
stall snapshots.

Two 7.0.7 snapshots at 2026-10-03 21:37:07Z and 21:37:38Z show Multiverse world
creation waiting for spawn chunks while `generateRingPositions` repeatedly queries
the Terra biome pipeline through `CustomWorldChunkManager`. On native Paper bindings,
the initial Bukkit biome provider now remains vanilla for structure placement state.
The WorldInit hook installs the native Terra biome source before generating chunks.
Terrain and generated chunk biomes still use the chosen community pack; vanilla
stronghold ring selection now uses vanilla biomes, so positions may differ from 7.0.7
for newly created worlds. Bukkit-only fallback retains its pack biome provider.
This addresses the observed expensive initialization path; server reproduction
is still required to establish whether other generation stalls remain.

## 7.0.10: console capture before Multiverse commands

`/terra2reportlog` (or `/terra2reportlog start`) opens a capture; `stop` flushes
and closes it; `status` reports whether it is active. Permission:
`terra2.diagnostics.capture`, OP by default. Console/RCON may use the same command.
Player, console and RCON commands with root label `mv` or `multiverse` start the
same capture automatically, before execution. Namespaced root labels are accepted.
Subsequent commands append markers and renew the ten-minute window.

The independent daemon reads `logs/latest.log` every second and forces report writes
to disk. It includes up to 64 KiB of preceding context, writes `console-latest.txt`
and a numbered `console-*.log`, stops at ten minutes or about 16 MiB, and retains
eight numbered sessions. These reports complement stall thread snapshots. Files
remain available after failure/restart; a new capture replaces only the latest alias.

This captures the console file, not GUI chat feedback that a plugin never logs.
An abrupt process kill can lose lines written since the last poll, or console lines
not yet flushed by Paper. Commands invoked by another plugin via dispatchCommand
may bypass Bukkit command events; manual start covers that case. No remote upload
or global server reload is performed.

## 7.0.11: correct NMS height-bound conversion

The server crash report `crash-2026-10-04_01.26.41-server.txt` shows the core
rejecting structure height queries with `Seed or height bounds changed for active
world`, via `NMSChunkGeneratorDelegate.getBaseHeight`. The NMS adapter passed
Minecraft's inclusive `getMaxY` as Terra's exclusive max height. It now computes
`getMinY + getHeight`, matching Bukkit and core contracts, while retaining the
seed/bounds protection. NMS tests verify Overworld and alternate dimension heights.
Future mismatches include world name and expected/received values in the exception.
