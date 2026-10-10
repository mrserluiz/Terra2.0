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
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ConfigRegistry.class);
    private volatile List<CommunityPackDiscovery.Source> discovered = List.of();
    public List<CommunityPackDiscovery.Source> discoveredSources() { return discovered; }
    public synchronized List<CommunityPackDiscovery.Source> discoverForReload(Platform platform) throws IOException {
        var scanned = scanSources(platform.getDataFolder().toPath().resolve("packs"));
        List<CommunityPackDiscovery.Source> refreshed = new ArrayList<>();
        for(var source : scanned) {
            var previous = discovered.stream().filter(old -> old.path().equals(source.path()) &&
                (java.util.Objects.equals(old.manifestId(), source.manifestId()) && old.version().equals(source.version())
                    || source.key() == null && sourceAdapters.stream().anyMatch(adapter -> adapter.supports(source.path())))).findFirst();
            boolean adapted = sourceAdapters.stream().anyMatch(adapter -> adapter.supports(source.path()));
            var next = (source.failure() == null || adapted) && previous.isPresent() && !previous.get().status().equals("RESTART_REQUIRED")
                ? previous.get() : (source.failure() == null || adapted) ? source.state("RESTART_REQUIRED", null) : source;
            refreshed.add(next);
            LOGGER.info("{}", next.diagnostic());
        }
        discovered = List.copyOf(refreshed);
        return discovered;
    }
    private List<CommunityPackDiscovery.Source> scanSources(Path directory) throws IOException {
        var sources = new ArrayList<>(CommunityPackDiscovery.scan(directory));
        try(var paths = Files.list(directory)) {
            for(var path : paths.sorted().toList()) {
                if(sources.stream().anyMatch(source -> source.path().equals(path))) continue;
                if(sourceAdapters.stream().anyMatch(adapter -> adapter.supports(path)))
                    sources.add(new CommunityPackDiscovery.Source(path, null, null, "unknown", "DISCOVERED", null));
            }
        }
        sources.sort(java.util.Comparator.comparing(source -> source.path().toString()));
        return List.copyOf(sources);
    }
    public String discoveryDiagnostics() {
        return discovered.stream().map(CommunityPackDiscovery.Source::diagnostic).collect(java.util.stream.Collectors.joining("\n"));
    }
    public void assertSourceAvailable(String id) {
        var matches = discovered.stream().filter(source -> source.matches(id) || id.equals(source.manifestId())).toList();
        if(matches.size() > 1) throw new IllegalArgumentException("Ambiguous Community Pack ID: " + id + "\n" + discoveryDiagnostics());
        if(!matches.isEmpty() && !matches.getFirst().status().equals("REGISTERED")) {
            var source = matches.getFirst();
            throw new IllegalArgumentException("Community Pack '" + id + "' was found but is not registered; server restart required after repairing any loader failure. " + source.diagnostic(), source.failure());
        }
    }

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
        var scanned = scanSources(packsDirectory);
        List<Exception> failedLoads = new ArrayList<>();
        List<CommunityPackDiscovery.Source> results = new ArrayList<>();
        List<PackSourceAdapter> adapters = List.copyOf(sourceAdapters);
        // Registration is sequential: duplicate checking and insertion must be deterministic.
        for(var source : scanned) {
            LOGGER.info("Community Pack discovered: {} ({})", source.manifestId(), source.path());
            try {
                boolean adapted = adapters.stream().anyMatch(adapter -> adapter.supports(source.path()));
                if(source.status().equals("DUPLICATE") || source.failure() != null && !adapted) throw new IOException(source.diagnostic(), source.failure());
                ConfigPack pack = loadSource(source.path(), platform, adapters);
                if(source.key() != null && !source.key().equals(pack.getRegistryKey()))
                    throw new IOException("Loaded pack ID differs from pack.yml: " + source.manifestId());
                registerChecked(pack.getRegistryKey(), pack);
                results.add(new CommunityPackDiscovery.Source(source.path(), source.manifestId() == null ? pack.getRegistryKey().toString() : source.manifestId(), pack.getRegistryKey(),
                    source.version(), "REGISTERED", null));
                LOGGER.info("Community Pack registered: {} ({})", pack.getRegistryKey(), source.path());
            } catch(Exception error) {
                var failed = source.state("LOAD_FAILED", error); results.add(failed);
                LOGGER.error("Community Pack rejected: {}", failed.diagnostic(), error);
                failedLoads.add(new IOException("Failed to load pack source " + source.path(), error));
            }
        }
        discovered = List.copyOf(results);
        if(!failedLoads.isEmpty()) throw new PackLoadFailuresException(failedLoads);
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
