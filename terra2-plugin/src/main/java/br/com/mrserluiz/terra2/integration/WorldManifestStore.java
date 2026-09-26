package br.com.mrserluiz.terra2.integration;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

public final class WorldManifestStore {
    private static final String DIRECTORY = ".terra2";
    private static final String FILE = "manifest.properties";
    private final Path worldsRoot;

    public WorldManifestStore(Path worldsRoot) {
        this.worldsRoot = worldsRoot.toAbsolutePath().normalize();
    }

    public Optional<WorldManifest> read(String worldName) throws IOException {
        Path path = manifestPath(worldName);
        if (!Files.isRegularFile(path)) return Optional.empty();
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) { properties.load(input); }
        return Optional.of(new WorldManifest(
                properties.getProperty("world"),
                properties.getProperty("pack"),
                properties.getProperty("pack-version"),
                Long.parseLong(properties.getProperty("created-at"))
        ));
    }

    public void write(WorldManifest manifest) throws IOException {
        Path destination = manifestPath(manifest.worldName());
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(FILE + ".tmp");
        Properties properties = new Properties();
        properties.setProperty("format", "1");
        properties.setProperty("world", manifest.worldName());
        properties.setProperty("pack", manifest.packId());
        properties.setProperty("pack-version", manifest.packVersion());
        properties.setProperty("created-at", Long.toString(manifest.createdAtEpochMillis()));
        try (OutputStream output = Files.newOutputStream(temporary)) {
            properties.store(output, "Terra 2.0 managed world - do not edit while the server is running");
        }
        try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path worldPath(String worldName) {
        Path candidate = worldsRoot.resolve(worldName).normalize();
        if (!candidate.getParent().equals(worldsRoot)) {
            throw new IllegalArgumentException("Invalid world name path: " + worldName);
        }
        return candidate;
    }

    private Path manifestPath(String worldName) {
        return worldPath(worldName).resolve(DIRECTORY).resolve(FILE);
    }
}
