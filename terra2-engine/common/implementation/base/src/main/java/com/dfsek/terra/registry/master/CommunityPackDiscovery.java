package com.dfsek.terra.registry.master;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipFile;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import com.dfsek.terra.api.registry.key.RegistryKey;

/** Metadata discovery only: never creates a world or calls generation code. */
public final class CommunityPackDiscovery {
    public record Source(Path path, String manifestId, RegistryKey key, String version, String status, Throwable failure) {
        public Source state(String status, Throwable failure) { return new Source(path, manifestId, key, version, status, failure); }
        public boolean matches(String id) { return key != null && (key.toString().equals(id) || key.getID().equals(id)); }
        public String diagnostic() {
            String result = "Community Pack Path: " + path + "; Manifest ID: " + manifestId + "; Registration Status: " + status;
            if(failure != null) {
                Throwable cause = failure;
                for(int i = 0; i < 16 && cause.getCause() != null && cause.getCause() != cause; i++) cause = cause.getCause();
                result += "; Load Status: " + cause;
            }
            return result;
        }
    }
    private CommunityPackDiscovery() {}
    public static List<Source> scan(Path directory) throws IOException {
        Files.createDirectories(directory);
        List<Source> sources = new ArrayList<>();
        try(var files = Files.list(directory)) {
            for(Path path : files.sorted().toList()) {
                if(!Files.isDirectory(path) && !path.getFileName().toString().endsWith(".zip")) continue;
                String id = null;
                try {
                    if(Files.isSymbolicLink(path)) throw new IOException("Pack source must not be a symbolic link");
                    byte[] bytes;
                    if(Files.isDirectory(path)) {
                        try(var input = Files.newInputStream(path.resolve("pack.yml"))) { bytes = manifest(input); }
                    } else try(var zip = new ZipFile(path.toFile())) {
                        var entry = zip.getEntry("pack.yml");
                        if(entry == null) throw new IOException("No root pack.yml; install the Community Pack archive, not a repository wrapper ZIP");
                        try(var input = zip.getInputStream(entry)) { bytes = manifest(input); }
                    }
                    LoaderOptions options = new LoaderOptions();
                    options.setAllowDuplicateKeys(false); options.setMaxAliasesForCollections(50); options.setCodePointLimit(262144);
                    Object document = new Yaml(new SafeConstructor(options)).load(new ByteArrayInputStream(bytes));
                    if(!(document instanceof Map<?, ?> map)) throw new IOException("pack.yml must be a mapping");
                    if(!(map.get("id") instanceof String value)) throw new IOException("pack.yml requires a string id");
                    id = value;
                    if(!id.matches("[A-Za-z0-9_-]+(:[A-Za-z0-9_-]+)?")) throw new IOException("Invalid manifest ID: " + id);
                    var key = id.contains(":") ? RegistryKey.parse(id) : RegistryKey.of(id, id);
                    sources.add(new Source(path, id, key, Objects.toString(map.get("version"), "unknown"), "DISCOVERED", null));
                } catch(Exception failure) { sources.add(new Source(path, id, null, "unknown", "INVALID", failure)); }
            }
        }
        Map<RegistryKey, Integer> counts = new HashMap<>();
        sources.forEach(source -> { if(source.key != null) counts.merge(source.key, 1, Integer::sum); });
        return sources.stream().map(source -> source.key != null && counts.get(source.key) > 1
            ? source.state("DUPLICATE", new IOException("Duplicate Community Pack manifest ID: " + source.manifestId)) : source).toList();
    }
    private static byte[] manifest(InputStream input) throws IOException {
        byte[] bytes = input.readNBytes(262145);
        if(bytes.length > 262144) throw new IOException("pack.yml exceeds 256 KiB");
        return bytes;
    }
}
