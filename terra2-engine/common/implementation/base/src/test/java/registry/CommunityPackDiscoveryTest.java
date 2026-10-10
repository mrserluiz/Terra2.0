package registry;

import java.nio.file.*;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.dfsek.terra.registry.master.CommunityPackDiscovery;
import static org.junit.jupiter.api.Assertions.*;

class CommunityPackDiscoveryTest {
    @TempDir Path directory;
    private void zip(String file, String entry, String yaml) throws Exception {
        try(var zip = new ZipOutputStream(Files.newOutputStream(directory.resolve(file)))) {
            zip.putNextEntry(new ZipEntry(entry)); zip.write(yaml.getBytes(java.nio.charset.StandardCharsets.UTF_8)); zip.closeEntry();
        }
    }
    @Test void zipUsesManifestIdInsteadOfFilename() throws Exception {
        zip("renamed.zip", "pack.yml", "id: ARBITRARY\nversion: 1.0.0\n");
        var source = CommunityPackDiscovery.scan(directory).getFirst();
        assertEquals("ARBITRARY", source.manifestId()); assertTrue(source.matches("ARBITRARY"));
        assertTrue(source.matches("ARBITRARY:ARBITRARY")); assertNull(source.failure());
    }
    @Test void unpackedPackAndNewPackAreDiscovered() throws Exception {
        var path = Files.createDirectories(directory.resolve("arbitrary-folder"));
        Files.writeString(path.resolve("pack.yml"), "id: FIRST\nversion: 1.0.0\n");
        assertEquals(1, CommunityPackDiscovery.scan(directory).size());
        zip("new.zip", "pack.yml", "id: SECOND\n");
        assertEquals(2, CommunityPackDiscovery.scan(directory).size());
    }
    @Test void missingManifestAndRepositoryWrapperAreExplicitlyRejected() throws Exception {
        zip("wrapped.zip", "repo/pack.yml", "id: EXAMPLE\n");
        var source = CommunityPackDiscovery.scan(directory).getFirst();
        assertNotNull(source.failure()); assertTrue(source.diagnostic().contains("root pack.yml"));
    }
    @Test void missingInvalidOrDuplicateManifestKeysAreRejected() throws Exception {
        for(String yaml : new String[]{"version: 1\n", "id: bad/id\n", "id: FIRST\nid: SECOND\n", "id: [broken"}) {
            zip("invalid.zip", "pack.yml", yaml);
            assertNotNull(CommunityPackDiscovery.scan(directory).getFirst().failure());
        }
    }
    @Test void duplicateIdsAreDetectedBeforeRegistration() throws Exception {
        zip("a.zip", "pack.yml", "id: SAME\n"); zip("b.zip", "pack.yml", "id: SAME\n");
        assertTrue(CommunityPackDiscovery.scan(directory).stream().allMatch(source -> source.status().equals("DUPLICATE")));
    }
}
