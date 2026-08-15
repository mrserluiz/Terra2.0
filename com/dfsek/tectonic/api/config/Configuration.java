package com.dfsek.tectonic.api.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Configuration {
   @Nullable
   Object get(@NotNull String var1);

   boolean contains(@NotNull String var1);

   @Nullable
   String getName();
}
