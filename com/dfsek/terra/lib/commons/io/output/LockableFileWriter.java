package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.FileUtils;
import com.dfsek.terra.lib.commons.io.build.AbstractOrigin;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;

public class LockableFileWriter extends Writer {
   private static final String LCK = ".lck";
   private final Writer out;
   private final File lockFile;

   public static LockableFileWriter.Builder builder() {
      return new LockableFileWriter.Builder();
   }

   @Deprecated
   public LockableFileWriter(File file) throws IOException {
      this(file, false, null);
   }

   @Deprecated
   public LockableFileWriter(File file, boolean append) throws IOException {
      this(file, append, null);
   }

   @Deprecated
   public LockableFileWriter(File file, boolean append, String lockDir) throws IOException {
      this(file, Charset.defaultCharset(), append, lockDir);
   }

   @Deprecated
   public LockableFileWriter(File file, Charset charset) throws IOException {
      this(file, charset, false, null);
   }

   @Deprecated
   public LockableFileWriter(File file, Charset charset, boolean append, String lockDir) throws IOException {
      File absFile = Objects.requireNonNull(file, "file").getAbsoluteFile();
      if (absFile.getParentFile() != null) {
         FileUtils.forceMkdir(absFile.getParentFile());
      }

      if (absFile.isDirectory()) {
         throw new IOException("File specified is a directory");
      }

      File lockDirFile = new File(lockDir != null ? lockDir : FileUtils.getTempDirectoryPath());
      FileUtils.forceMkdir(lockDirFile);
      this.testLockDir(lockDirFile);
      this.lockFile = new File(lockDirFile, absFile.getName() + ".lck");
      this.createLock();
      this.out = this.initWriter(absFile, charset, append);
   }

   @Deprecated
   public LockableFileWriter(File file, String charsetName) throws IOException {
      this(file, charsetName, false, null);
   }

   @Deprecated
   public LockableFileWriter(File file, String charsetName, boolean append, String lockDir) throws IOException {
      this(file, Charsets.toCharset(charsetName), append, lockDir);
   }

   @Deprecated
   public LockableFileWriter(String fileName) throws IOException {
      this(fileName, false, null);
   }

   @Deprecated
   public LockableFileWriter(String fileName, boolean append) throws IOException {
      this(fileName, append, null);
   }

   @Deprecated
   public LockableFileWriter(String fileName, boolean append, String lockDir) throws IOException {
      this(new File(fileName), append, lockDir);
   }

   @Override
   public void close() throws IOException {
      try {
         this.out.close();
      } finally {
         FileUtils.delete(this.lockFile);
      }
   }

   private void createLock() throws IOException {
      synchronized (LockableFileWriter.class) {
         if (!this.lockFile.createNewFile()) {
            throw new IOException("Can't write file, lock " + this.lockFile.getAbsolutePath() + " exists");
         }

         this.lockFile.deleteOnExit();
      }
   }

   @Override
   public void flush() throws IOException {
      this.out.flush();
   }

   private Writer initWriter(File file, Charset charset, boolean append) throws IOException {
      boolean fileExistedAlready = file.exists();

      try {
         return new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath(), append), Charsets.toCharset(charset));
      } catch (IOException | RuntimeException ex) {
         FileUtils.deleteQuietly(this.lockFile);
         if (!fileExistedAlready) {
            FileUtils.deleteQuietly(file);
         }

         throw ex;
      }
   }

   private void testLockDir(File lockDir) throws IOException {
      if (!lockDir.exists()) {
         throw new IOException("Could not find lockDir: " + lockDir.getAbsolutePath());
      }

      if (!lockDir.canWrite()) {
         throw new IOException("Could not write to lockDir: " + lockDir.getAbsolutePath());
      }
   }

   @Override
   public void write(char[] cbuf) throws IOException {
      this.out.write(cbuf);
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException {
      this.out.write(cbuf, off, len);
   }

   @Override
   public void write(int c) throws IOException {
      this.out.write(c);
   }

   @Override
   public void write(String str) throws IOException {
      this.out.write(str);
   }

   @Override
   public void write(String str, int off, int len) throws IOException {
      this.out.write(str, off, len);
   }

   public static class Builder extends AbstractStreamBuilder<LockableFileWriter, LockableFileWriter.Builder> {
      private boolean append;
      private AbstractOrigin<?, ?> lockDirectory = newFileOrigin(FileUtils.getTempDirectoryPath());

      public Builder() {
         this.setBufferSizeDefault(1024);
         this.setBufferSize(1024);
      }

      public LockableFileWriter get() throws IOException {
         return new LockableFileWriter(this.checkOrigin().getFile(), this.getCharset(), this.append, this.lockDirectory.getFile().toString());
      }

      public LockableFileWriter.Builder setAppend(boolean append) {
         this.append = append;
         return this;
      }

      public LockableFileWriter.Builder setLockDirectory(File lockDirectory) {
         this.lockDirectory = newFileOrigin(lockDirectory != null ? lockDirectory : FileUtils.getTempDirectory());
         return this;
      }

      public LockableFileWriter.Builder setLockDirectory(String lockDirectory) {
         this.lockDirectory = newFileOrigin(lockDirectory != null ? lockDirectory : FileUtils.getTempDirectoryPath());
         return this;
      }
   }
}
