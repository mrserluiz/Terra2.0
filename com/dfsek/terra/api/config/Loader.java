package com.dfsek.terra.api.config;

import com.dfsek.tectonic.api.exception.ConfigException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;

public interface Loader {
   Loader thenNames(Consumer<List<String>> var1) throws ConfigException;

   Loader thenEntries(Consumer<Set<Entry<String, InputStream>>> var1) throws ConfigException;

   InputStream get(String var1) throws IOException;

   Loader open(String var1, String var2);

   Loader close();
}
