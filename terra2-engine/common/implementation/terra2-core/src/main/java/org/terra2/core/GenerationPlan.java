package org.terra2.core;
import java.util.Objects;
/** Immutable routing identity. Source version prevents silent upgrades of an active world. */
public record GenerationPlan<B, M>(String id, String sourceFormat, String sourceVersion, GenerationProgram<B, M> program) {
    public GenerationPlan {
        Objects.requireNonNull(id); Objects.requireNonNull(sourceFormat);
        Objects.requireNonNull(sourceVersion); Objects.requireNonNull(program);
        if(id.isBlank() || sourceFormat.isBlank() || sourceVersion.isBlank())
            throw new IllegalArgumentException("Incomplete generation plan identity");
    }
    public String identity() { return sourceFormat + ":" + id + "@" + sourceVersion; }
}
