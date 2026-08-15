package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.FileSystem;
import com.dfsek.terra.lib.commons.io.StandardLineSeparator;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.function.IOIterable;
import com.dfsek.terra.lib.commons.io.function.IOIterator;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ReversedLinesFileReader implements Closeable, IOIterable<String> {
   private static final String EMPTY_STRING = "";
   private static final int DEFAULT_BLOCK_SIZE = FileSystem.getCurrent().getBlockSize();
   private final int blockSize;
   private final Charset charset;
   private final SeekableByteChannel channel;
   private final long totalByteLength;
   private final long totalBlockCount;
   private final byte[][] newLineSequences;
   private final int avoidNewlineSplitBufferSize;
   private final int byteDecrement;
   private ReversedLinesFileReader.FilePart currentFilePart;
   private boolean trailingNewlineOfFileSkipped;

   public static ReversedLinesFileReader.Builder builder() {
      return new ReversedLinesFileReader.Builder();
   }

   @Deprecated
   public ReversedLinesFileReader(File file) throws IOException {
      this(file, DEFAULT_BLOCK_SIZE, Charset.defaultCharset());
   }

   @Deprecated
   public ReversedLinesFileReader(File file, Charset charset) throws IOException {
      this(file.toPath(), charset);
   }

   @Deprecated
   public ReversedLinesFileReader(File file, int blockSize, Charset charset) throws IOException {
      this(file.toPath(), blockSize, charset);
   }

   @Deprecated
   public ReversedLinesFileReader(File file, int blockSize, String charsetName) throws IOException {
      this(file.toPath(), blockSize, charsetName);
   }

   @Deprecated
   public ReversedLinesFileReader(Path file, Charset charset) throws IOException {
      this(file, DEFAULT_BLOCK_SIZE, charset);
   }

   @Deprecated
   public ReversedLinesFileReader(Path file, int blockSize, Charset charset) throws IOException {
      this.blockSize = blockSize;
      this.charset = Charsets.toCharset(charset);
      CharsetEncoder charsetEncoder = this.charset.newEncoder();
      float maxBytesPerChar = charsetEncoder.maxBytesPerChar();
      if (maxBytesPerChar != 1.0F && this.charset != StandardCharsets.UTF_8) {
         if (this.charset != Charset.forName("Shift_JIS")
            && this.charset != Charset.forName("windows-31j")
            && this.charset != Charset.forName("x-windows-949")
            && this.charset != Charset.forName("gbk")
            && this.charset != Charset.forName("x-windows-950")) {
            if (this.charset != StandardCharsets.UTF_16BE && this.charset != StandardCharsets.UTF_16LE) {
               if (this.charset == StandardCharsets.UTF_16) {
                  throw new UnsupportedEncodingException("For UTF-16, you need to specify the byte order (use UTF-16BE or UTF-16LE)");
               }

               throw new UnsupportedEncodingException("Encoding " + charset + " is not supported yet (feel free to submit a patch)");
            }

            this.byteDecrement = 2;
         } else {
            this.byteDecrement = 1;
         }
      } else {
         this.byteDecrement = 1;
      }

      this.newLineSequences = new byte[][]{
         StandardLineSeparator.CRLF.getBytes(this.charset), StandardLineSeparator.LF.getBytes(this.charset), StandardLineSeparator.CR.getBytes(this.charset)
      };
      this.avoidNewlineSplitBufferSize = this.newLineSequences[0].length;
      this.channel = Files.newByteChannel(file, StandardOpenOption.READ);
      this.totalByteLength = this.channel.size();
      int lastBlockLength = (int)(this.totalByteLength % blockSize);
      if (lastBlockLength > 0) {
         this.totalBlockCount = this.totalByteLength / blockSize + 1L;
      } else {
         this.totalBlockCount = this.totalByteLength / blockSize;
         if (this.totalByteLength > 0L) {
            lastBlockLength = blockSize;
         }
      }

      this.currentFilePart = new ReversedLinesFileReader.FilePart(this.totalBlockCount, lastBlockLength, null);
   }

   @Deprecated
   public ReversedLinesFileReader(Path file, int blockSize, String charsetName) throws IOException {
      this(file, blockSize, Charsets.toCharset(charsetName));
   }

   @Override
   public void close() throws IOException {
      this.channel.close();
   }

   @Override
   public IOIterator<String> iterator() {
      return new IOIterator<String>() {
         private String next;

         @Override
         public boolean hasNext() throws IOException {
            if (this.next == null) {
               this.next = ReversedLinesFileReader.this.readLine();
            }

            return this.next != null;
         }

         public String next() throws IOException {
            if (this.next == null) {
               this.next = ReversedLinesFileReader.this.readLine();
            }

            String tmp = this.next;
            this.next = null;
            return tmp;
         }

         @Override
         public Iterator<String> unwrap() {
            return null;
         }
      };
   }

   public String readLine() throws IOException {
      String line;
      for (line = this.currentFilePart.readLine(); line == null; line = this.currentFilePart.readLine()) {
         this.currentFilePart = this.currentFilePart.rollOver();
         if (this.currentFilePart == null) {
            break;
         }
      }

      if ("".equals(line) && !this.trailingNewlineOfFileSkipped) {
         this.trailingNewlineOfFileSkipped = true;
         line = this.readLine();
      }

      return line;
   }

   public List<String> readLines(int lineCount) throws IOException {
      if (lineCount < 0) {
         throw new IllegalArgumentException("lineCount < 0");
      }

      ArrayList<String> arrayList = new ArrayList<>(lineCount);

      for (int i = 0; i < lineCount; i++) {
         String line = this.readLine();
         if (line == null) {
            return arrayList;
         }

         arrayList.add(line);
      }

      return arrayList;
   }

   public String toString(int lineCount) throws IOException {
      List<String> lines = this.readLines(lineCount);
      Collections.reverse(lines);
      return lines.isEmpty() ? "" : String.join(System.lineSeparator(), lines) + System.lineSeparator();
   }

   @Override
   public Iterable<String> unwrap() {
      return null;
   }

   public static class Builder extends AbstractStreamBuilder<ReversedLinesFileReader, ReversedLinesFileReader.Builder> {
      public Builder() {
         this.setBufferSizeDefault(ReversedLinesFileReader.DEFAULT_BLOCK_SIZE);
         this.setBufferSize(ReversedLinesFileReader.DEFAULT_BLOCK_SIZE);
      }

      public ReversedLinesFileReader get() throws IOException {
         return new ReversedLinesFileReader(this.getPath(), this.getBufferSize(), this.getCharset());
      }
   }

   private final class FilePart {
      private final long partNumber;
      private final byte[] data;
      private byte[] leftOver;
      private int currentLastBytePos;

      private FilePart(final long partNumber, final int length, final byte[] leftOverOfLastFilePart) throws IOException {
         this.partNumber = partNumber;
         int dataLength = length + (leftOverOfLastFilePart != null ? leftOverOfLastFilePart.length : 0);
         this.data = new byte[dataLength];
         long off = (partNumber - 1L) * ReversedLinesFileReader.this.blockSize;
         if (partNumber > 0L) {
            ReversedLinesFileReader.this.channel.position(off);
            int countRead = ReversedLinesFileReader.this.channel.read(ByteBuffer.wrap(this.data, 0, length));
            if (countRead != length) {
               throw new IllegalStateException("Count of requested bytes and actually read bytes don't match");
            }
         }

         if (leftOverOfLastFilePart != null) {
            System.arraycopy(leftOverOfLastFilePart, 0, this.data, length, leftOverOfLastFilePart.length);
         }

         this.currentLastBytePos = this.data.length - 1;
         this.leftOver = null;
      }

      private void createLeftOver() {
         int lineLengthBytes = this.currentLastBytePos + 1;
         if (lineLengthBytes > 0) {
            this.leftOver = Arrays.copyOf(this.data, lineLengthBytes);
         } else {
            this.leftOver = null;
         }

         this.currentLastBytePos = -1;
      }

      private int getNewLineMatchByteCount(byte[] data, int i) {
         for (byte[] newLineSequence : ReversedLinesFileReader.this.newLineSequences) {
            boolean match = true;

            for (int j = newLineSequence.length - 1; j >= 0; j--) {
               int k = i + j - (newLineSequence.length - 1);
               match &= k >= 0 && data[k] == newLineSequence[j];
            }

            if (match) {
               return newLineSequence.length;
            }
         }

         return 0;
      }

      private String readLine() {
         String line = null;
         boolean isLastFilePart = this.partNumber == 1L;
         int i = this.currentLastBytePos;

         while (i > -1) {
            if (!isLastFilePart && i < ReversedLinesFileReader.this.avoidNewlineSplitBufferSize) {
               this.createLeftOver();
               break;
            }

            int newLineMatchByteCount;
            if ((newLineMatchByteCount = this.getNewLineMatchByteCount(this.data, i)) > 0) {
               int lineStart = i + 1;
               int lineLengthBytes = this.currentLastBytePos - lineStart + 1;
               if (lineLengthBytes < 0) {
                  throw new IllegalStateException("Unexpected negative line length=" + lineLengthBytes);
               }

               byte[] lineData = Arrays.copyOfRange(this.data, lineStart, lineStart + lineLengthBytes);
               line = new String(lineData, ReversedLinesFileReader.this.charset);
               this.currentLastBytePos = i - newLineMatchByteCount;
               break;
            }

            i -= ReversedLinesFileReader.this.byteDecrement;
            if (i < 0) {
               this.createLeftOver();
               break;
            }
         }

         if (isLastFilePart && this.leftOver != null) {
            line = new String(this.leftOver, ReversedLinesFileReader.this.charset);
            this.leftOver = null;
         }

         return line;
      }

      private ReversedLinesFileReader.FilePart rollOver() throws IOException {
         if (this.currentLastBytePos > -1) {
            throw new IllegalStateException(
               "Current currentLastCharPos unexpectedly positive... last readLine() should have returned something! currentLastCharPos="
                  + this.currentLastBytePos
            );
         } else if (this.partNumber > 1L) {
            return ReversedLinesFileReader.this.new FilePart(this.partNumber - 1L, ReversedLinesFileReader.this.blockSize, this.leftOver);
         } else if (this.leftOver != null) {
            throw new IllegalStateException(
               "Unexpected leftover of the last block: leftOverOfThisFilePart=" + new String(this.leftOver, ReversedLinesFileReader.this.charset)
            );
         } else {
            return null;
         }
      }
   }
}
