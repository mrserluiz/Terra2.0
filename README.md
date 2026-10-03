# Terra 2.0

Terra 2.0 is an independent, safety-first continuation of the Terra world
generation engine for Paper. It is being modernized for Java 25 and Paper 26.2
while retaining compatibility with Terra Community Packs.

The project currently contains:

- `terra2-engine/`: clean upstream engine source under active modernization;
- `terra2-plugin/`: the existing safe world-selection and manifest prototype;
- `ENGINE-MIGRATION.md`: architecture, safety guarantees, and delivery phases.

Generation remains disabled by default. Terra 2.0 must never attach to the
primary world automatically, and only explicitly authorized worlds may use a
configured pack.

This repository is under active recovery. The current scaffold JAR is not yet
a replacement for the full Terra generator; consult `ENGINE-MIGRATION.md` for
the exact status.
