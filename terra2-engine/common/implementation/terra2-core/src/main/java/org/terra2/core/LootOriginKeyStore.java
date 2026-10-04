package org.terra2.core;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

/** A missing key is created once; a damaged key is never silently replaced. */
public final class LootOriginKeyStore {
    private LootOriginKeyStore() {}
    public static LootOrigin load(Path file) throws IOException {
        if(Files.isSymbolicLink(file) || Files.isSymbolicLink(file.getParent())) throw new IOException("Loot key cannot be a symbolic link");
        Files.createDirectories(file.getParent());
        if(!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            byte[] secret = new byte[32]; new SecureRandom().nextBytes(secret);
            try(var channel = FileChannel.open(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                var buffer = ByteBuffer.wrap(secret); while(buffer.hasRemaining()) channel.write(buffer); channel.force(true);
                try { Files.setPosixFilePermissions(file, java.nio.file.attribute.PosixFilePermissions.fromString("rw-------")); }
                catch(UnsupportedOperationException ignored) { }
            } catch(FileAlreadyExistsException raced) { /* Another startup created it; use that key. */ }
        }
        if(!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS) || Files.size(file) != 32) throw new IOException("Invalid loot origin key; restore the original key");
        byte[] secret = Files.readAllBytes(file);
        if(secret.length != 32) throw new IOException("Loot origin key changed while being read");
        return new LootOrigin(secret);
    }
}
