package org.terra2.pack;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Local conversion workspace and immutable pack artifacts. Never touches server/world datapack registries. */
public final class TerraPackStore {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path input, output, reports;
    private NativePackBackend nativeBackend;
    public synchronized void nativeBackend(NativePackBackend backend) { nativeBackend = Objects.requireNonNull(backend); }
    public record Conversion(String id, String status, Path output, Path report, int blockers, int features, boolean terrain) {}
    public TerraPackStore(Path pluginDirectory) throws IOException {
        input = pluginDirectory.resolve("conversion/input"); output = pluginDirectory.resolve("terrapacks"); reports = pluginDirectory.resolve("conversion/reports");
        for(Path path : List.of(pluginDirectory.resolve("conversion"), input, output, reports)) {
            if(Files.isSymbolicLink(path)) throw new IOException("Conversion directory cannot be a symbolic link");
            Files.createDirectories(path);
        }
    }
    public synchronized Conversion convert(String id, List<String> sourceNames, String dimension) throws IOException {
        return convert(id, sourceNames, dimension, null);
    }
    public synchronized Conversion convert(String id, List<String> sourceNames, String dimension, TemplateMigration.Backend migrationBackend) throws IOException {
        return convert(id, sourceNames, dimension, migrationBackend, PackCompiler.Profile.FULL);
    }
    public synchronized Conversion convert(String id, List<String> sourceNames, String dimension, TemplateMigration.Backend migrationBackend, PackCompiler.Profile profile) throws IOException {
        ResourceBundle.fileName(id);
        if(sourceNames.isEmpty() || sourceNames.size() > 8 || new HashSet<>(sourceNames).size() != sourceNames.size())
            throw new IllegalArgumentException("Select 1..8 distinct input sources");
        Path destination = output.resolve(id + ".terrapack");
        if(Files.exists(destination, LinkOption.NOFOLLOW_LINKS) || Files.exists(reports.resolve(id + ".json"), LinkOption.NOFOLLOW_LINKS)) throw new IOException("TerraPack already exists; use a new ID: " + id);
        List<ResourceBundle> sources = new ArrayList<>();
        JsonArray provenance = new JsonArray();
        for(String name : sourceNames) {
            ResourceBundle.fileName(name);
            var archive = ResourceBundle.read(input.resolve(name));
            var source = archive.datapackRoot(); sources.add(source);
            if(sources.stream().mapToLong(ResourceBundle::sizeBytes).sum() > 128L * 1024 * 1024)
                throw new IOException("Combined conversion inputs exceed 128 MiB");
            var entry = new JsonObject(); entry.addProperty("input", name); entry.addProperty("sha256", source.fingerprint());
            entry.addProperty("archiveSha256", archive.fingerprint());
            if(source.contains("pack.mcmeta")) entry.add("originalMetadata", JsonParser.parseString(source.text("pack.mcmeta")));
            provenance.add(entry);
        }
        ResourceBundle merged = ResourceBundle.merge(sources);
        var migration = TemplateMigration.run(merged, migrationBackend);
        var compiled = PackCompiler.compile(merged, dimension, profile);
        var blockers = new ArrayList<>(compiled.blockers());
        if(migration.report().status().equals("FAILED")) blockers.addAll(migration.report().errors());
        for(int index = 0; index < sources.size(); index++) for(String issue : PackCompiler.metadataIssues(sources.get(index))) {
            String message = sourceNames.get(index) + ": " + issue;
            if(!blockers.contains(issue)) blockers.add(message);
        }
        compiled = new PackCompiler.Result(compiled.terrain(), compiled.features(), blockers, compiled.notes(), compiled.resourceKinds());
        String status = compiled.ready() ? "READY" : "BLOCKED";
        var manifest = new JsonObject(); manifest.addProperty("schema", 1); manifest.addProperty("compiler", "terra2-vanilla-1");
        manifest.addProperty("id", id); manifest.addProperty("status", status); manifest.addProperty("target", "paper-26.2");
        manifest.addProperty("profile", profile.name());
        if(compiled.terrain() != null) manifest.addProperty("sourceDimension", compiled.terrain().dimension());
        else if(dimension != null) manifest.addProperty("sourceDimension", dimension);
        manifest.addProperty("resourceSha256", merged.fingerprint()); manifest.add("sources", provenance);
        manifest.add("compiled", JSON.toJsonTree(compiled));
        // Decoded structure inventory is diagnostic, not authorization to execute unfinished stages.
        manifest.add("structureMigration", JSON.toJsonTree(StructureCatalog.audit(merged)));
        manifest.add("nativeTemplateMigration", JSON.toJsonTree(migration.report()));
        if(nativeBackend != null) manifest.add("nativeResourceValidation", JSON.toJsonTree(nativeBackend.validate(merged)));
        // Readiness always comes from the executable compiler; MIGRATED only describes NBT.
        Path temp = Files.createTempFile(output, ".conversion-", ".tmp");
        Path reportTemp = Files.createTempFile(reports, ".conversion-", ".tmp");
        try {
            try(var zip = new ZipOutputStream(Files.newOutputStream(temp))) {
                write(zip, "terrapack.json", JSON.toJson(manifest).getBytes(StandardCharsets.UTF_8));
                for(String path : merged.paths()) write(zip, "resources/" + path, merged.bytes(path));
                for(var entry : migration.templates().entrySet()) write(zip, "native-templates/" + entry.getKey(), entry.getValue());
                for(int index = 0; index < sources.size(); index++) for(String path : sources.get(index).paths()) {
                    if(!path.startsWith("data/") && !path.equals("pack.png"))
                        write(zip, "provenance/" + index + "/" + path, sources.get(index).bytes(path));
                }
            }
            // Report failure must not leave a pack that appears to have finished conversion.
            Path report = reports.resolve(id + ".json");
            Files.writeString(reportTemp, JSON.toJson(manifest) + "\n");
            Files.move(reportTemp, report);
            try { Files.move(temp, destination); }
            catch(IOException error) { Files.deleteIfExists(report); throw error; }
        } finally { Files.deleteIfExists(temp); Files.deleteIfExists(reportTemp); }
        Path report = reports.resolve(id + ".json");
        return new Conversion(id, status, destination, report, compiled.blockers().size(), compiled.features().size(), compiled.terrain() != null);
    }
    private static void write(ZipOutputStream zip, String path, byte[] bytes) throws IOException {
        zip.putNextEntry(new ZipEntry(path)); zip.write(bytes); zip.closeEntry();
    }
    public boolean contains(String id) {
        if(id == null || !id.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*")) return false;
        return Files.exists(output.resolve(id + ".terrapack"), LinkOption.NOFOLLOW_LINKS);
    }
    public List<String> list() throws IOException {
        try(var paths = Files.list(output)) {
            return paths.filter(path -> !Files.isSymbolicLink(path) && path.getFileName().toString().endsWith(".terrapack"))
                .map(path -> path.getFileName().toString().replaceFirst("\\.terrapack$", "")).sorted().toList();
        }
    }
    public JsonObject inspect(String id) throws IOException {
        ResourceBundle.fileName(id);
        Path file = output.resolve(id + ".terrapack");
        if(Files.isSymbolicLink(file)) throw new IOException("TerraPack cannot be a symbolic link");
        try(var zip = new ZipFile(file.toFile())) {
            var entry = zip.getEntry("terrapack.json");
            if(entry == null) throw new IllegalArgumentException("Missing TerraPack manifest: " + id);
            try(var stream = zip.getInputStream(entry)) {
                byte[] bytes = stream.readNBytes(8 * 1024 * 1024 + 1);
                if(bytes.length > 8 * 1024 * 1024) throw new IOException("TerraPack manifest exceeds 8 MiB");
                return PackCompiler.object(new String(bytes, StandardCharsets.UTF_8));
            }
        }
    }
    private record Snapshot(JsonObject manifest, ResourceBundle archive) {}
    private Snapshot snapshot(String id) throws IOException {
        ResourceBundle.fileName(id);
        var archive = ResourceBundle.read(output.resolve(id + ".terrapack"));
        var manifest = PackCompiler.object(archive.text("terrapack.json"));
        if(!manifest.has("schema") || manifest.get("schema").getAsInt() != 1 ||
            !id.equals(PackCompiler.string(manifest, "id")) || !"terra2-vanilla-1".equals(PackCompiler.string(manifest, "compiler")))
            throw new IllegalArgumentException("Unknown TerraPack schema/compiler/ID: " + id);
        return new Snapshot(manifest, archive);
    }
    public TerraPack load(String id) throws IOException {
        var preview = inspect(id);
        if(!"READY".equals(PackCompiler.string(preview, "status")))
            throw new IllegalArgumentException("TerraPack " + id + " is BLOCKED; inspect conversion/reports/" + id + ".json");
        var snapshot = snapshot(id); var manifest = snapshot.manifest;
        var source = snapshot.archive.subtree("resources/");
        if(!source.fingerprint().equals(PackCompiler.string(manifest, "resourceSha256"))) throw new IllegalArgumentException("TerraPack resources were modified: " + id);
        var profile = manifest.has("profile") ? PackCompiler.Profile.valueOf(PackCompiler.string(manifest, "profile")) : PackCompiler.Profile.FULL;
        var compiled = PackCompiler.compile(source, manifest.has("sourceDimension") ? PackCompiler.string(manifest, "sourceDimension") : null, profile);
        if(!compiled.ready() || !JSON.toJsonTree(compiled).equals(manifest.get("compiled")))
            throw new IllegalArgumentException("TerraPack compiled IR failed validation: " + id);
        return new TerraPack(id, snapshot.archive.fingerprint(), compiled.terrain(), compiled.features());
    }
    public static List<SimpleBlockFeature> features(List<TerraPack> packs) {
        Set<String> seen = new HashSet<>(); List<SimpleBlockFeature> result = new ArrayList<>();
        for(TerraPack pack : packs) for(var feature : pack.features()) {
            if(!seen.add(feature.id())) throw new IllegalArgumentException("Conflicting feature ID: " + feature.id());
            result.add(feature);
        }
        return List.copyOf(result);
    }
    public static void validateComposition(List<TerraPack> packs, boolean legacyTerrain) {
        Set<String> ids = new HashSet<>(); int terrain = legacyTerrain ? 1 : 0;
        for(TerraPack pack : packs) {
            if(!ids.add(pack.id())) throw new IllegalArgumentException("Repeated pack: " + pack.id());
            if(pack.terrain() != null) terrain++;
        }
        if(terrain != 1) throw new IllegalArgumentException("Composition requires exactly one terrain base; found " + terrain);
        var features = features(packs);
        if(features.size() > 32 || features.stream().mapToInt(SimpleBlockFeature::count).sum() > 128)
            throw new IllegalArgumentException("Composition exceeds feature budget: 32 features / 128 attempts per chunk");
    }
}
