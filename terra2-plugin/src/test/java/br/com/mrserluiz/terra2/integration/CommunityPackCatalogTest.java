package br.com.mrserluiz.terra2.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityPackCatalogTest {
    @TempDir Path temporary;

    @Test
    void discoversFolderAndZipPacks() throws IOException {
        Path folder = Files.createDirectories(temporary.resolve("Overworld"));
        Files.writeString(folder.resolve("pack.yml"), "id: OVERWORLD\nversion: 1.0.0\naddons:\n  language-yaml: '1.+'\n");
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(temporary.resolve("skylands.zip")))) {
            zip.putNextEntry(new ZipEntry("pack.yml"));
            zip.write("id: SKYLANDS\nversion: 2.0.0\naddons:\n  chunk-generator-noise-3d: '1.+'\n".getBytes());
            zip.closeEntry();
        }

        var packs = new CommunityPackCatalog(temporary).discover();
        assertEquals(2, packs.size());
        assertTrue(packs.stream().anyMatch(pack -> pack.id().equals("OVERWORLD")));
        assertEquals(Set.of("chunk-generator-noise-3d"), packs.stream()
                .filter(pack -> pack.id().equals("SKYLANDS")).findFirst().orElseThrow().requiredAddons());
    }
}
