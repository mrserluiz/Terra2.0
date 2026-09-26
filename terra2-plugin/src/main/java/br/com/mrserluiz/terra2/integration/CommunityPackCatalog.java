package br.com.mrserluiz.terra2.integration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Discovers Terra config packs installed as folders or zip archives. */
public final class CommunityPackCatalog {
    private final Path packsDirectory;
    private final PackManifestParser parser = new PackManifestParser();

    public CommunityPackCatalog(Path packsDirectory) {
        this.packsDirectory = packsDirectory.toAbsolutePath().normalize();
    }

    public List<PackDescriptor> discover() throws IOException {
        if (!Files.isDirectory(packsDirectory)) return List.of();
        List<PackDescriptor> packs = new ArrayList<>();
        try (var children = Files.list(packsDirectory)) {
            for (Path child : children.sorted().toList()) {
                Optional<PackDescriptor> descriptor = read(child);
                descriptor.ifPresent(packs::add);
            }
        }
        packs.sort(Comparator.comparing(PackDescriptor::id, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(packs);
    }

    public Optional<PackDescriptor> findById(String id) throws IOException {
        return discover().stream().filter(pack -> pack.id().equalsIgnoreCase(id)).findFirst();
    }

    private Optional<PackDescriptor> read(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            Path manifest = source.resolve("pack.yml");
            if (!Files.isRegularFile(manifest)) return Optional.empty();
            try (Reader reader = Files.newBufferedReader(manifest, StandardCharsets.UTF_8)) {
                return Optional.of(parser.parse(reader, source));
            }
        }

        if (!source.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")) return Optional.empty();
        try (ZipFile zip = new ZipFile(source.toFile())) {
            ZipEntry manifest = zip.getEntry("pack.yml");
            if (manifest == null) return Optional.empty();
            try (Reader reader = new InputStreamReader(zip.getInputStream(manifest), StandardCharsets.UTF_8)) {
                return Optional.of(parser.parse(reader, source));
            }
        }
    }
}
