package org.terra2.pack;

import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import java.util.zip.*;

/** Bounded read-only source snapshot. Conversion never extracts or executes source files. */
public final class ResourceBundle {
    private static final int FILE_LIMIT = 8 * 1024 * 1024, TOTAL_LIMIT = 128 * 1024 * 1024, ENTRY_LIMIT = 20000;
    private final SortedMap<String, byte[]> files = new TreeMap<>();
    private long total;
    public static ResourceBundle read(Path path) throws IOException {
        if(Files.isSymbolicLink(path)) throw new IOException("Symbolic link refused: " + path.getFileName());
        ResourceBundle result = new ResourceBundle();
        if(Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try(var paths = Files.walk(path)) {
                var iterator = paths.iterator(); int count = 0;
                while(iterator.hasNext()) {
                    Path file = iterator.next();
                    if(++count > ENTRY_LIMIT) throw new IOException("Source exceeds 20000 entries");
                    if(Files.isSymbolicLink(file)) throw new IOException("Symbolic link refused in source");
                    if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) continue;
                    try(var stream = Files.newInputStream(file)) { result.add(path.relativize(file).toString().replace('\\', '/'), stream); }
                }
            }
        } else try(var zip = new ZipFile(path.toFile())) {
            var entries = zip.entries(); int count = 0;
            while(entries.hasMoreElements()) {
                var entry = entries.nextElement();
                if(++count > ENTRY_LIMIT) throw new IOException("Archive exceeds 20000 entries");
                checkPath(entry.getName().replaceFirst("/$", ""));
                if(!entry.isDirectory()) try(var stream = zip.getInputStream(entry)) { result.add(entry.getName(), stream); }
            }
        }
        return result;
    }
    public static ResourceBundle merge(List<ResourceBundle> sources) throws IOException {
        ResourceBundle result = new ResourceBundle();
        for(var source : sources) for(var entry : source.files.entrySet()) {
            byte[] previous = result.files.get(entry.getKey());
            if(previous != null) {
                if(entry.getKey().equals("pack.mcmeta") || entry.getKey().equals("pack.png") ||
                    !entry.getKey().startsWith("data/")) continue; // Original metadata/licenses retained separately in output.
                if(!Arrays.equals(previous, entry.getValue())) throw new IllegalArgumentException("Conflicting resource: " + entry.getKey());
            } else result.add(entry.getKey(), new ByteArrayInputStream(entry.getValue()));
        }
        return result;
    }
    private void add(String name, InputStream stream) throws IOException {
        checkPath(name);
        if(files.containsKey(name)) throw new IOException("Duplicate resource: " + name);
        byte[] bytes = stream.readNBytes(FILE_LIMIT + 1);
        total += bytes.length;
        if(bytes.length > FILE_LIMIT || total > TOTAL_LIMIT || files.size() >= ENTRY_LIMIT)
            throw new IOException("Source limits exceeded (8 MiB/file, 128 MiB total, 20000 files)");
        files.put(name, bytes);
    }
    public ResourceBundle subtree(String prefix) throws IOException {
        ResourceBundle result = new ResourceBundle();
        for(var entry : files.entrySet()) if(entry.getKey().startsWith(prefix))
            result.add(entry.getKey().substring(prefix.length()), new ByteArrayInputStream(entry.getValue()));
        return result;
    }
    public long sizeBytes() { return total; }
    public Set<String> paths() { return Collections.unmodifiableSet(files.keySet()); }
    public byte[] bytes(String path) {
        byte[] result = files.get(path);
        if(result == null) throw new IllegalArgumentException("Missing resource: " + path);
        return result.clone();
    }
    public boolean contains(String path) { return files.containsKey(path); }
    public String text(String path) { return new String(bytes(path), StandardCharsets.UTF_8); }
    public String fingerprint() {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            files.forEach((key, value) -> { digest.update(key.getBytes(StandardCharsets.UTF_8)); digest.update((byte) 0); digest.update(value); });
            return HexFormat.of().formatHex(digest.digest());
        } catch(NoSuchAlgorithmException error) { throw new IllegalStateException(error); }
    }
    public static void checkPath(String name) {
        if(name.isBlank() || name.startsWith("/") || name.contains("\\") || name.contains(":"))
            throw new IllegalArgumentException("Invalid archive resource path: " + name);
        for(String part : name.split("/", -1)) if(part.isBlank() || part.equals(".") || part.equals(".."))
            throw new IllegalArgumentException("Invalid archive resource path: " + name);
    }
    public static String fileName(String value) {
        if(value == null || !value.matches("[A-Za-z0-9_-][A-Za-z0-9_.-]*") || value.equals(".."))
            throw new IllegalArgumentException("Use a filename without folders or spaces: " + value);
        return value;
    }
    public static String identifier(String value) {
        if(value == null || !value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid resource ID: " + value);
        for(String part : value.split("[:/]", -1)) if(part.isBlank() || part.equals("..") || part.equals("."))
            throw new IllegalArgumentException("Invalid resource ID: " + value);
        return value;
    }
}
