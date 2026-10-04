package org.terra2.adapter.vanilla.paper;

import java.io.*;
import java.util.zip.GZIPOutputStream;
import org.bukkit.Bukkit;
import org.terra2.pack.TemplateMigration;

/** Paper's structure reader runs Minecraft's structure DataFixer before loading the template. */
public final class PaperTemplateMigration implements TemplateMigration.Backend {
    private final int targetDataVersion;
    public PaperTemplateMigration() { targetDataVersion = Bukkit.getUnsafe().getDataVersion(); }
    @Override public int targetDataVersion() { return targetDataVersion; }
    @Override public byte[] migrate(byte[] source) throws IOException {
        if(source.length < 2 || (source[0] & 255) != 0x1f || (source[1] & 255) != 0x8b) {
            // The neutral reader accepts raw Java NBT; Paper's stream loader expects gzip.
            var compressed = new ByteArrayOutputStream();
            try(var gzip = new GZIPOutputStream(compressed)) { gzip.write(source); }
            source = compressed.toByteArray();
        }
        // loadStructure(InputStream) does not register the result or write into a world.
        var manager = Bukkit.getStructureManager();
        var structure = manager.loadStructure(new ByteArrayInputStream(source));
        var output = new ByteArrayOutputStream();
        manager.saveStructure(output, structure);
        return output.toByteArray();
    }
}
