# Engine provenance

The `terra2-engine` tree was imported from the official Terra repository as the
starting point for the independent Terra 2.0 engine.

- Upstream: <https://github.com/PolyhedralDev/Terra>
- Imported commit: `25d510156de02cb77f8fb6ffde276417eb19aea4`
- Import date: 2026-10-03
- Terra 2.0 repository: <https://github.com/mrserluiz/Terra2.0>

The import is intentionally source-based. Terra 2.0 does not depend on an
installed legacy Terra plugin or redistribute a decompiled server artifact.
Original copyright notices, contributor history in the upstream repository,
and per-module license files are retained. The platform implementations are
covered by GPL-3.0-or-later; API, core addons, and other modules keep the
licenses declared in their nearest license files.

## Compatibility policy

During the recovery phase, Java package names, configuration IDs, addon entry
points, and pack schemas remain compatible with Terra. Public plugin and build
artifact names use `Terra2`. Changing internal Java namespaces is deferred
until a compatibility bridge exists because an immediate rename would break
community addons without improving runtime safety.

## First modernization baseline

- Java 25
- Paper 26.2 as the production target
- Paper 26.3 reserved for compatibility validation
- Mojang-mapped Paper runtime, as required for Paper 26.1 and later

This import is a recovery milestone, not yet a production release. Native NMS
adapters must compile and pass generation tests before a Terra 2.0 engine JAR
is marked usable.
