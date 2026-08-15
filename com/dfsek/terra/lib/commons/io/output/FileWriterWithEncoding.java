package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.FileUtils;
import com.dfsek.terra.lib.commons.io.IOUtils;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Objects;

public class FileWriterWithEncoding extends ProxyWriter {
   public static FileWriterWithEncoding.Builder builder() {
      return new FileWriterWithEncoding.Builder();
   }

   private static OutputStreamWriter initWriter(File file, Object encoding, boolean append) throws IOException {
      Objects.requireNonNull(file, "file");
      OutputStream outputStream = null;
      boolean fileExistedAlready = file.exists();

      try {
         outputStream = FileUtils.newOutputStream(file, append);
         if (encoding == null || encoding instanceof Charset) {
            return new OutputStreamWriter(outputStream, Charsets.toCharset((Charset)encoding));
         } else {
            return encoding instanceof CharsetEncoder
               ? new OutputStreamWriter(outputStream, (CharsetEncoder)encoding)
               : new OutputStreamWriter(outputStream, (String)encoding);
         }
      } catch (IOException | RuntimeException ex) {
         try {
            IOUtils.close(outputStream);
         } catch (IOException e) {
            ex.addSuppressed(e);
         }

         if (!fileExistedAlready) {
            FileUtils.deleteQuietly(file);
         }

         throw ex;
      }
   }

   @Deprecated
   public FileWriterWithEncoding(File file, Charset charset) throws IOException {
      this(file, charset, false);
   }

   @Deprecated
   public FileWriterWithEncoding(File file, Charset encoding, boolean append) throws IOException {
      this(initWriter(file, encoding, append));
   }

   @Deprecated
   public FileWriterWithEncoding(File file, CharsetEncoder charsetEncoder) throws IOException {
      this(file, charsetEncoder, false);
   }

   @Deprecated
   public FileWriterWithEncoding(File file, CharsetEncoder charsetEncoder, boolean append) throws IOException {
      this(initWriter(file, charsetEncoder, append));
   }

   @Deprecated
   public FileWriterWithEncoding(File file, String charsetName) throws IOException {
      this(file, charsetName, false);
   }

   @Deprecated
   public FileWriterWithEncoding(File file, String charsetName, boolean append) throws IOException {
      this(initWriter(file, charsetName, append));
   }

   private FileWriterWithEncoding(OutputStreamWriter outputStreamWriter) {
      super(outputStreamWriter);
   }

   @Deprecated
   public FileWriterWithEncoding(String fileName, Charset charset) throws IOException {
      this(new File(fileName), charset, false);
   }

   @Deprecated
   public FileWriterWithEncoding(String fileName, Charset charset, boolean append) throws IOException {
      this(new File(fileName), charset, append);
   }

   @Deprecated
   public FileWriterWithEncoding(String fileName, CharsetEncoder encoding) throws IOException {
      this(new File(fileName), encoding, false);
   }

   @Deprecated
   public FileWriterWithEncoding(String fileName, CharsetEncoder charsetEncoder, boolean append) throws IOException {
      this(new File(fileName), charsetEncoder, append);
   }

   @Deprecated
   public FileWriterWithEncoding(String fileName, String charsetName) throws IOException {
      this(new File(fileName), charsetName, false);
   }

   @Deprecated
   public FileWriterWithEncoding(String fileName, String charsetName, boolean append) throws IOException {
      this(new File(fileName), charsetName, append);
   }

   public static class Builder extends AbstractStreamBuilder<FileWriterWithEncoding, FileWriterWithEncoding.Builder> {
      private boolean append;
      private CharsetEncoder charsetEncoder = super.getCharset().newEncoder();

      public FileWriterWithEncoding get() throws IOException {
         if (this.charsetEncoder != null && this.getCharset() != null && !this.charsetEncoder.charset().equals(this.getCharset())) {
            throw new IllegalStateException(String.format("Mismatched Charset(%s) and CharsetEncoder(%s)", this.getCharset(), this.charsetEncoder.charset()));
         }

         Object encoder = this.charsetEncoder != null ? this.charsetEncoder : this.getCharset();
         return new FileWriterWithEncoding(FileWriterWithEncoding.initWriter(this.checkOrigin().getFile(), encoder, this.append));
      }

      public FileWriterWithEncoding.Builder setAppend(boolean append) {
         this.append = append;
         return this;
      }

      public FileWriterWithEncoding.Builder setCharsetEncoder(CharsetEncoder charsetEncoder) {
         this.charsetEncoder = charsetEncoder;
         return this;
      }
   }
}
