package org.terra2.core;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Server-issued provenance, independent of item spawning or moving between dimensions. */
public final class LootOrigin {
    public record Receipt(int version, String world, String dimension, String pack, String table, String nonce, String signature) {}
    private final byte[] secret;
    public LootOrigin(byte[] secret) {
        if(secret.length != 32) throw new IllegalArgumentException("Loot origin key must contain 32 bytes");
        this.secret = secret.clone();
    }
    public Receipt issue(WorldTarget target, String pack, String table) {
        Objects.requireNonNull(target.dimensionKey(), "World must have an actual dimension binding");
        return sign(new Receipt(1, target.worldName(), target.dimensionKey(), required(pack), required(table), UUID.randomUUID().toString(), ""));
    }
    public boolean authentic(Receipt receipt) {
        if(receipt == null || receipt.version() != 1) return false;
        try {
            byte[] given = HexFormat.of().parseHex(receipt.signature());
            return MessageDigest.isEqual(given, mac(receipt));
        } catch(RuntimeException error) { return false; }
    }
    private Receipt sign(Receipt receipt) {
        return new Receipt(receipt.version(), receipt.world(), receipt.dimension(), receipt.pack(), receipt.table(), receipt.nonce(), HexFormat.of().formatHex(mac(receipt)));
    }
    private byte[] mac(Receipt receipt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            for(String value : List.of(Integer.toString(receipt.version()), receipt.world(), receipt.dimension(), receipt.pack(), receipt.table(), receipt.nonce())) {
                byte[] bytes = required(value).getBytes(StandardCharsets.UTF_8);
                mac.update(java.nio.ByteBuffer.allocate(4).putInt(bytes.length).array()); mac.update(bytes);
            }
            return mac.doFinal();
        } catch(GeneralSecurityException error) { throw new IllegalStateException(error); }
    }
    private static String required(String value) {
        if(value == null || value.isBlank() || value.length() > 4096) throw new IllegalArgumentException("Invalid loot origin field"); return value;
    }
}
