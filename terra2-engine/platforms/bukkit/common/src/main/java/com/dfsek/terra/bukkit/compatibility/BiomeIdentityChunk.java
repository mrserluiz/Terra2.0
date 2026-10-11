package com.dfsek.terra.bukkit.compatibility;

import java.io.*;
import java.util.*;
import java.util.zip.CRC32;
import org.terra2.api.biome.BiomeIdentity;

/** Versioned bounded palette; 4x4x4 biome cells, including negative heights. */
public final class BiomeIdentityChunk {
    public static final int MAX_BYTES = 1_048_576;
    private static final int MAGIC = 0x54324249;
    public final UUID world;
    public final long seed;
    public final int x, z, minY, height;
    public final String pack, fingerprint;
    private final List<BiomeIdentity> palette;
    private final int[] cells;
    public BiomeIdentityChunk(UUID world, long seed, int x, int z, int minY, int height,
            String pack, String fingerprint, List<BiomeIdentity> palette, int[] cells) {
        if(height <= 0 || height > 4096 || (height & 3) != 0 || (minY & 3) != 0
                || palette.isEmpty() || palette.size() > 16384 || cells.length != height * 4)
            throw new IllegalArgumentException("Invalid biome identity dimensions/palette");
        for(int cell : cells) if(cell < 0 || cell >= palette.size()) throw new IllegalArgumentException("Invalid palette index");
        this.world = Objects.requireNonNull(world); this.seed = seed; this.x=x; this.z=z; this.minY=minY; this.height=height;
        this.pack=Objects.requireNonNull(pack); this.fingerprint=Objects.requireNonNull(fingerprint);
        this.palette=List.copyOf(palette); this.cells=cells.clone();
    }
    public Optional<BiomeIdentity> at(int bx, int by, int bz) {
        if((bx >> 4) != x || (bz >> 4) != z || by < minY || by >= minY + height) return Optional.empty();
        int index = ((by - minY) >> 2) * 16 + ((bz & 15) >> 2) * 4 + ((bx & 15) >> 2);
        return Optional.of(palette.get(cells[index]));
    }
    public byte[] encode() throws IOException {
        var bytes = new ByteArrayOutputStream();
        try(var out = new DataOutputStream(bytes)) {
            out.writeInt(MAGIC); out.writeInt(1); out.writeLong(world.getMostSignificantBits()); out.writeLong(world.getLeastSignificantBits());
            out.writeLong(seed); out.writeInt(x); out.writeInt(z); out.writeInt(minY); out.writeInt(height);
            out.writeUTF(pack); out.writeUTF(fingerprint); out.writeInt(palette.size());
            for(var identity : palette) { out.writeUTF(identity.terraId()); out.writeUTF(identity.registeredId()); out.writeUTF(identity.vanillaReference()); }
            for(int cell : cells) out.writeShort(cell);
            out.flush(); var crc = new CRC32(); crc.update(bytes.toByteArray()); out.writeLong(crc.getValue());
        }
        if(bytes.size() > MAX_BYTES) throw new IOException("Biome identity payload too large");
        return bytes.toByteArray();
    }
    public static BiomeIdentityChunk decode(byte[] data) throws IOException {
        if(data == null || data.length < 64 || data.length > MAX_BYTES) throw new IOException("Invalid biome identity payload length");
        var crc = new CRC32(); crc.update(data, 0, data.length - 8);
        try(var in = new DataInputStream(new ByteArrayInputStream(data))) {
            if(in.readInt()!=MAGIC || in.readInt()!=1) throw new IOException("Unknown biome identity schema");
            UUID world = new UUID(in.readLong(), in.readLong()); long seed = in.readLong();
            int x=in.readInt(), z=in.readInt(), min=in.readInt(), height=in.readInt();
            String pack=in.readUTF(), fingerprint=in.readUTF(); int count=in.readInt();
            if(height<=0 || height>4096 || (height&3)!=0 || (min&3)!=0 || count<1 || count>16384)
                throw new IOException("Invalid biome identity dimensions/palette");
            var palette = new ArrayList<BiomeIdentity>(count);
            for(int i=0;i<count;i++) palette.add(new BiomeIdentity(in.readUTF(),in.readUTF(),in.readUTF()));
            int[] cells = new int[height*4];
            for(int i=0;i<cells.length;i++) { cells[i]=in.readUnsignedShort(); if(cells[i]>=count) throw new IOException("Invalid palette index"); }
            if(in.readLong()!=crc.getValue() || in.available()!=0) throw new IOException("Biome identity checksum/trailing data mismatch");
            return new BiomeIdentityChunk(world,seed,x,z,min,height,pack,fingerprint,palette,cells);
        }
    }
}
