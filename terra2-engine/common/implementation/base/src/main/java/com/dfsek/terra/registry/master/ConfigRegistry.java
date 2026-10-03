/*
 * This file is part of Terra.
 *
 * Terra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Terra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Terra.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dfsek.terra.registry.master;

import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.config.PackSourceAdapter;
import com.dfsek.terra.api.util.reflection.TypeKey;
import com.dfsek.terra.config.pack.ConfigPackImpl;
import com.dfsek.terra.registry.OpenRegistryImpl;


/**
 * Class to hold config packs
 */
public class ConfigRegistry extends OpenRegistryImpl<ConfigPack> {
    private final List<PackSourceAdapter> sourceAdapters = new ArrayList<>();

    public ConfigRegistry() {
        super(TypeKey.of(ConfigPack.class));
        ServiceLoader.load(PackSourceAdapter.class, ConfigRegistry.class.getClassLoader()).forEach(this::registerSourceAdapter);
    }

    public synchronized void registerSourceAdapter(PackSourceAdapter adapter) {
        if(adapter == null || adapter.id() == null || adapter.id().isBlank()) {
            throw new IllegalArgumentException("Pack adapter must have an ID");
        }
        if(sourceAdapters.stream().anyMatch(existing -> existing.id().equals(adapter.id()))) {
            throw new IllegalArgumentException("Duplicate pack adapter: " + adapter.id());
        }
        sourceAdapters.add(adapter);
    }

    private ConfigPack loadSource(Path path, Platform platform, List<PackSourceAdapter> adapters) throws Exception {
        List<PackSourceAdapter> matches = adapters.stream().filter(adapter -> adapter.supports(path)).toList();
        if(matches.size() > 1) throw new IOException("Ambiguous pack adapters for " + path);
        // Keep the original loader, addon checks and generation contracts for native packs.
        ConfigPack pack = matches.isEmpty() ? new ConfigPackImpl(path, platform) : matches.getFirst().load(path, platform);
        if(pack == null) throw new IOException("Pack adapter returned no pack for " + path);
        return pack;
    }

    public synchronized void loadAll(Platform platform) throws IOException, PackLoadFailuresException {
        Path packsDirectory = platform.getDataFolder().toPath().resolve("packs");
        Files.createDirectories(packsDirectory);
        List<Exception> failedLoads = new CopyOnWriteArrayList<>();
        List<PackSourceAdapter> adapters = List.copyOf(sourceAdapters);
        try(Stream<Path> packs = Files.list(packsDirectory)) {
            packs.parallel().forEach(path -> {
                try {
                    ConfigPack pack = loadSource(path, platform, adapters);
                    registerChecked(pack.getRegistryKey(), pack);
                } catch(Exception e) {
                    failedLoads.add(new IOException("Failed to load pack source " + path, e));
                }
            });
        }
        if(!failedLoads.isEmpty()) {
            throw new PackLoadFailuresException(failedLoads);
        }
    }

    public static class PackLoadFailuresException extends Exception {
        @Serial
        private static final long serialVersionUID = 538998844645186306L;

        private final List<Throwable> exceptions;

        public PackLoadFailuresException(List<? extends Throwable> exceptions) {
            this.exceptions = (List<Throwable>) exceptions;
        }

        public List<Throwable> getExceptions() {
            return exceptions;
        }
    }
}
