package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.ByteOrderMark;
import com.dfsek.terra.lib.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BOMInputStream extends ProxyInputStream {
   private static final Comparator<ByteOrderMark> ByteOrderMarkLengthComparator = Comparator.comparing(ByteOrderMark::length).reversed();
   private final List<ByteOrderMark> bomList;
   private ByteOrderMark byteOrderMark;
   private int fbIndex;
   private int fbLength;
   private int[] firstBytes;
   private final boolean include;
   private boolean markedAtStart;
   private int markFbIndex;

   public static BOMInputStream.Builder builder() {
      return new BOMInputStream.Builder();
   }

   private BOMInputStream(BOMInputStream.Builder builder) throws IOException {
      super(builder);
      if (IOUtils.length(builder.byteOrderMarks) == 0) {
         throw new IllegalArgumentException("No ByteOrderMark specified.");
      }

      this.include = builder.include;
      List<ByteOrderMark> list = Arrays.asList(builder.byteOrderMarks);
      list.sort(ByteOrderMarkLengthComparator);
      this.bomList = list;
   }

   @Deprecated
   public BOMInputStream(InputStream delegate) {
      this(delegate, false, BOMInputStream.Builder.DEFAULT);
   }

   @Deprecated
   public BOMInputStream(InputStream delegate, boolean include) {
      this(delegate, include, BOMInputStream.Builder.DEFAULT);
   }

   @Deprecated
   public BOMInputStream(InputStream delegate, boolean include, ByteOrderMark... boms) {
      super(delegate);
      if (IOUtils.length(boms) == 0) {
         throw new IllegalArgumentException("No BOMs specified");
      }

      this.include = include;
      List<ByteOrderMark> list = Arrays.asList(boms);
      list.sort(ByteOrderMarkLengthComparator);
      this.bomList = list;
   }

   @Deprecated
   public BOMInputStream(InputStream delegate, ByteOrderMark... boms) {
      this(delegate, false, boms);
   }

   private ByteOrderMark find() {
      return this.bomList.stream().filter(this::matches).findFirst().orElse(null);
   }

   public ByteOrderMark getBOM() throws IOException {
      if (this.firstBytes == null) {
         this.byteOrderMark = this.readBom();
      }

      return this.byteOrderMark;
   }

   public String getBOMCharsetName() throws IOException {
      this.getBOM();
      return this.byteOrderMark == null ? null : this.byteOrderMark.getCharsetName();
   }

   public boolean hasBOM() throws IOException {
      return this.getBOM() != null;
   }

   public boolean hasBOM(ByteOrderMark bom) throws IOException {
      if (!this.bomList.contains(bom)) {
         throw new IllegalArgumentException("Stream not configured to detect " + bom);
      } else {
         return Objects.equals(this.getBOM(), bom);
      }
   }

   @Override
   public synchronized void mark(int readLimit) {
      this.markFbIndex = this.fbIndex;
      this.markedAtStart = this.firstBytes == null;
      this.in.mark(readLimit);
   }

   private boolean matches(ByteOrderMark bom) {
      return bom.matches(this.firstBytes);
   }

   @Override
   public int read() throws IOException {
      this.checkOpen();
      int b = this.readFirstBytes();
      return b >= 0 ? b : this.in.read();
   }

   @Override
   public int read(byte[] buf) throws IOException {
      return this.read(buf, 0, buf.length);
   }

   @Override
   public int read(byte[] buf, int off, int len) throws IOException {
      int firstCount = 0;
      int b = 0;

      while (len > 0 && b >= 0) {
         b = this.readFirstBytes();
         if (b >= 0) {
            buf[off++] = (byte)(b & 0xFF);
            len--;
            firstCount++;
         }
      }

      int secondCount = this.in.read(buf, off, len);
      this.afterRead(secondCount);
      return secondCount < 0 ? (firstCount > 0 ? firstCount : -1) : firstCount + secondCount;
   }

   private ByteOrderMark readBom() throws IOException {
      this.fbLength = 0;
      int maxBomSize = this.bomList.get(0).length();
      this.firstBytes = new int[maxBomSize];

      for (int i = 0; i < this.firstBytes.length; i++) {
         this.firstBytes[i] = this.in.read();
         this.afterRead(this.firstBytes[i]);
         this.fbLength++;
         if (this.firstBytes[i] < 0) {
            break;
         }
      }

      ByteOrderMark bom = this.find();
      if (bom != null && !this.include) {
         if (bom.length() < this.firstBytes.length) {
            this.fbIndex = bom.length();
         } else {
            this.fbLength = 0;
         }
      }

      return bom;
   }

   private int readFirstBytes() throws IOException {
      this.getBOM();
      return this.fbIndex < this.fbLength ? this.firstBytes[this.fbIndex++] : -1;
   }

   @Override
   public synchronized void reset() throws IOException {
      this.fbIndex = this.markFbIndex;
      if (this.markedAtStart) {
         this.firstBytes = null;
      }

      this.in.reset();
   }

   @Override
   public long skip(long n) throws IOException {
      int skipped = 0;

      while (n > skipped && this.readFirstBytes() >= 0) {
         skipped++;
      }

      return this.in.skip(n - skipped) + skipped;
   }

   public static class Builder extends ProxyInputStream.AbstractBuilder<BOMInputStream, BOMInputStream.Builder> {
      private static final ByteOrderMark[] DEFAULT = new ByteOrderMark[]{ByteOrderMark.UTF_8};
      private ByteOrderMark[] byteOrderMarks = DEFAULT;
      private boolean include;

      static ByteOrderMark getDefaultByteOrderMark() {
         return DEFAULT[0];
      }

      public BOMInputStream get() throws IOException {
         return new BOMInputStream(this);
      }

      public BOMInputStream.Builder setByteOrderMarks(ByteOrderMark... byteOrderMarks) {
         this.byteOrderMarks = byteOrderMarks != null ? (ByteOrderMark[])byteOrderMarks.clone() : DEFAULT;
         return this;
      }

      public BOMInputStream.Builder setInclude(boolean include) {
         this.include = include;
         return this;
      }
   }
}
