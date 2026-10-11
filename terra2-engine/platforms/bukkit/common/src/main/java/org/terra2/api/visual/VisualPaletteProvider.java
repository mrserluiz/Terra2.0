package org.terra2.api.visual;

import java.util.Collection;

/** Register with Bukkit ServicesManager. Catalog only: no world/chunk writes or runtime projection. */
public interface VisualPaletteProvider {
    int apiVersion();
    Collection<VisualProfile> profiles();
}
