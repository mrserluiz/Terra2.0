package com.dfsek.terra.api.block.state.properties;

import com.dfsek.terra.api.registry.key.StringIdentifiable;
import java.util.Collection;

public interface Property<T> extends StringIdentifiable {
   Collection<T> values();

   Class<T> getType();
}
