package org.terra2.adapter.vanilla.paper;

import java.io.*;
import org.bukkit.Bukkit;
import org.terra2.pack.TemplateMigration;

/** Paper's structure reader runs Minecraft's structure DataFixer before loading the template. */
public final class PaperTemplateMigration implements TemplateMigration.Backend {
    private final int targetDataVersion;
    public PaperTemplateMigration() { targetDataVersion = Bukkit.getUnsafe().getDataVersion(); }
    @Override public int targetDataVersion() { return targetDataVersion; }
    @Override public byte[] migrate(byte[] source) throws IOException {
        // loadStructure(InputStream) does not register the result or write into a world.
        var manager = Bukkit.getStructureManager();
        var structure = manager.loadStructure(new ByteArrayInputStream(source));
        var output = new ByteArrayOutputStream();
        manager.saveStructure(output, structure);
        return output.toByteArray();
    }
}
