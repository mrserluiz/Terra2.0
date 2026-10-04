package org.terra2.pack;

import java.util.List;
import org.terra2.adapter.vanilla.FlatDefinition;

/** A runnable compiled pack can provide terrain, additive features, or both. */
public record TerraPack(String id, String fingerprint, FlatDefinition terrain, List<SimpleBlockFeature> features) {
    public TerraPack { ResourceBundle.fileName(id); features = List.copyOf(features); }
}
