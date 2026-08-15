package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.IOUtils;
import com.dfsek.terra.lib.commons.io.ThreadUtils;
import com.dfsek.terra.lib.commons.io.build.AbstractOrigin;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import com.dfsek.terra.lib.commons.io.file.attribute.FileTimes;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Tailer implements Runnable, AutoCloseable {
   private static final int DEFAULT_DELAY_MILLIS = 1000;
   private static final String RAF_READ_ONLY_MODE = "r";
   private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
   private final byte[] inbuf;
   private final Tailer.Tailable tailable;
   private final Charset charset;
   private final Duration delayDuration;
   private final boolean tailAtEnd;
   private final TailerListener listener;
   private final boolean reOpen;
   private volatile boolean run = true;

   public static Tailer.Builder builder() {
      return new Tailer.Builder();
   }

   @Deprecated
   public static Tailer create(File file, Charset charset, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufferSize) {
      return builder()
         .setFile(file)
         .setTailerListener(listener)
         .setCharset(charset)
         .setDelayDuration(Duration.ofMillis(delayMillis))
         .setTailFromEnd(end)
         .setReOpen(reOpen)
         .setBufferSize(bufferSize)
         .get();
   }

   @Deprecated
   public static Tailer create(File file, TailerListener listener) {
      return builder().setFile(file).setTailerListener(listener).get();
   }

   @Deprecated
   public static Tailer create(File file, TailerListener listener, long delayMillis) {
      return builder().setFile(file).setTailerListener(listener).setDelayDuration(Duration.ofMillis(delayMillis)).get();
   }

   @Deprecated
   public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end) {
      return builder().setFile(file).setTailerListener(listener).setDelayDuration(Duration.ofMillis(delayMillis)).setTailFromEnd(end).get();
   }

   @Deprecated
   public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen) {
      return builder().setFile(file).setTailerListener(listener).setDelayDuration(Duration.ofMillis(delayMillis)).setTailFromEnd(end).setReOpen(reOpen).get();
   }

   @Deprecated
   public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufferSize) {
      return builder()
         .setFile(file)
         .setTailerListener(listener)
         .setDelayDuration(Duration.ofMillis(delayMillis))
         .setTailFromEnd(end)
         .setReOpen(reOpen)
         .setBufferSize(bufferSize)
         .get();
   }

   @Deprecated
   public static Tailer create(File file, TailerListener listener, long delayMillis, boolean end, int bufferSize) {
      return builder()
         .setFile(file)
         .setTailerListener(listener)
         .setDelayDuration(Duration.ofMillis(delayMillis))
         .setTailFromEnd(end)
         .setBufferSize(bufferSize)
         .get();
   }

   @Deprecated
   public Tailer(File file, Charset charset, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufSize) {
      this(new Tailer.TailablePath(file.toPath(), new LinkOption[0]), charset, listener, Duration.ofMillis(delayMillis), end, reOpen, bufSize);
   }

   @Deprecated
   public Tailer(File file, TailerListener listener) {
      this(file, listener, 1000L);
   }

   @Deprecated
   public Tailer(File file, TailerListener listener, long delayMillis) {
      this(file, listener, delayMillis, false);
   }

   @Deprecated
   public Tailer(File file, TailerListener listener, long delayMillis, boolean end) {
      this(file, listener, delayMillis, end, 8192);
   }

   @Deprecated
   public Tailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen) {
      this(file, listener, delayMillis, end, reOpen, 8192);
   }

   @Deprecated
   public Tailer(File file, TailerListener listener, long delayMillis, boolean end, boolean reOpen, int bufferSize) {
      this(file, DEFAULT_CHARSET, listener, delayMillis, end, reOpen, bufferSize);
   }

   @Deprecated
   public Tailer(File file, TailerListener listener, long delayMillis, boolean end, int bufferSize) {
      this(file, listener, delayMillis, end, false, bufferSize);
   }

   private Tailer(Tailer.Tailable tailable, Charset charset, TailerListener listener, Duration delayDuration, boolean end, boolean reOpen, int bufferSize) {
      this.tailable = Objects.requireNonNull(tailable, "tailable");
      this.listener = Objects.requireNonNull(listener, "listener");
      this.delayDuration = delayDuration;
      this.tailAtEnd = end;
      this.inbuf = IOUtils.byteArray(bufferSize);
      listener.init(this);
      this.reOpen = reOpen;
      this.charset = charset;
   }

   @Override
   public void close() {
      this.run = false;
   }

   @Deprecated
   public long getDelay() {
      return this.delayDuration.toMillis();
   }

   public Duration getDelayDuration() {
      return this.delayDuration;
   }

   public File getFile() {
      if (this.tailable instanceof Tailer.TailablePath) {
         return ((Tailer.TailablePath)this.tailable).getPath().toFile();
      } else {
         throw new IllegalStateException("Cannot extract java.io.File from " + this.tailable.getClass().getName());
      }
   }

   protected boolean getRun() {
      return this.run;
   }

   public Tailer.Tailable getTailable() {
      return this.tailable;
   }

   private long readLines(Tailer.RandomAccessResourceBridge reader) throws IOException {
      ByteArrayOutputStream lineBuf = new ByteArrayOutputStream(64);

      long var13;
      try {
         long pos = reader.getPointer();
         long rePos = pos;
         boolean seenCR = false;

         int num;
         while (this.getRun() && (num = reader.read(this.inbuf)) != -1) {
            for (int i = 0; i < num; i++) {
               byte ch = this.inbuf[i];
               switch (ch) {
                  case 10:
                     seenCR = false;
                     this.listener.handle(new String(lineBuf.toByteArray(), this.charset));
                     lineBuf.reset();
                     rePos = pos + i + 1L;
                     break;
                  case 13:
                     if (seenCR) {
                        lineBuf.write(13);
                     }

                     seenCR = true;
                     break;
                  default:
                     if (seenCR) {
                        seenCR = false;
                        this.listener.handle(new String(lineBuf.toByteArray(), this.charset));
                        lineBuf.reset();
                        rePos = pos + i + 1L;
                     }

                     lineBuf.write(ch);
               }
            }

            pos = reader.getPointer();
         }

         reader.seek(rePos);
         if (this.listener instanceof TailerListenerAdapter) {
            ((TailerListenerAdapter)this.listener).endOfFileReached();
         }

         var13 = rePos;
      } catch (Throwable var12) {
         try {
            lineBuf.close();
         } catch (Throwable var11) {
            var12.addSuppressed(var11);
         }

         throw var12;
      }

      lineBuf.close();
      return var13;
   }

   @Override
   public void run() {
      Tailer.RandomAccessResourceBridge reader = null;

      try {
         FileTime last = FileTimes.EPOCH;
         long position = 0L;

         while (this.getRun() && reader == null) {
            try {
               reader = this.tailable.getRandomAccess("r");
            } catch (FileNotFoundException e) {
               this.listener.fileNotFound();
            }

            if (reader == null) {
               ThreadUtils.sleep(this.delayDuration);
            } else {
               position = this.tailAtEnd ? this.tailable.size() : 0L;
               last = this.tailable.lastModifiedFileTime();
               reader.seek(position);
            }
         }

         while (this.getRun()) {
            boolean newer = this.tailable.isNewer(last);
            long length = this.tailable.size();
            if (length < position) {
               this.listener.fileRotated();

               try {
                  Tailer.RandomAccessResourceBridge save = reader;

                  try {
                     reader = this.tailable.getRandomAccess("r");

                     try {
                        this.readLines(save);
                     } catch (IOException ioe) {
                        this.listener.handle(ioe);
                     }

                     position = 0L;
                  } catch (Throwable var29) {
                     if (save != null) {
                        try {
                           save.close();
                        } catch (Throwable var26) {
                           var29.addSuppressed(var26);
                        }
                     }

                     throw var29;
                  }

                  if (save != null) {
                     save.close();
                  }
               } catch (FileNotFoundException e) {
                  this.listener.fileNotFound();
                  ThreadUtils.sleep(this.delayDuration);
               }
            } else {
               if (length > position) {
                  position = this.readLines(reader);
                  last = this.tailable.lastModifiedFileTime();
               } else if (newer) {
                  position = 0L;
                  reader.seek(position);
                  position = this.readLines(reader);
                  last = this.tailable.lastModifiedFileTime();
               }

               if (this.reOpen && reader != null) {
                  reader.close();
               }

               ThreadUtils.sleep(this.delayDuration);
               if (this.getRun() && this.reOpen) {
                  reader = this.tailable.getRandomAccess("r");
                  reader.seek(position);
               }
            }
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         this.listener.handle(e);
      } catch (Exception e) {
         this.listener.handle(e);
      } finally {
         try {
            IOUtils.close(reader);
         } catch (IOException e) {
            this.listener.handle(e);
         }

         this.close();
      }
   }

   @Deprecated
   public void stop() {
      this.close();
   }

   public static class Builder extends AbstractStreamBuilder<Tailer, Tailer.Builder> {
      private static final Duration DEFAULT_DELAY_DURATION = Duration.ofMillis(1000L);
      private Tailer.Tailable tailable;
      private TailerListener tailerListener;
      private Duration delayDuration = DEFAULT_DELAY_DURATION;
      private boolean tailFromEnd;
      private boolean reOpen;
      private boolean startThread = true;
      private ExecutorService executorService = Executors.newSingleThreadExecutor(Tailer.Builder::newDaemonThread);

      private static Thread newDaemonThread(Runnable runnable) {
         Thread thread = new Thread(runnable, "commons-io-tailer");
         thread.setDaemon(true);
         return thread;
      }

      public Tailer get() {
         Tailer tailer = new Tailer(
            this.tailable, this.getCharset(), this.tailerListener, this.delayDuration, this.tailFromEnd, this.reOpen, this.getBufferSize()
         );
         if (this.startThread) {
            this.executorService.submit(tailer);
         }

         return tailer;
      }

      public Tailer.Builder setDelayDuration(Duration delayDuration) {
         this.delayDuration = delayDuration != null ? delayDuration : DEFAULT_DELAY_DURATION;
         return this;
      }

      public Tailer.Builder setExecutorService(ExecutorService executorService) {
         this.executorService = Objects.requireNonNull(executorService, "executorService");
         return this;
      }

      protected Tailer.Builder setOrigin(AbstractOrigin<?, ?> origin) {
         this.setTailable(new Tailer.TailablePath(origin.getPath(), new LinkOption[0]));
         return (Tailer.Builder)super.setOrigin(origin);
      }

      public Tailer.Builder setReOpen(boolean reOpen) {
         this.reOpen = reOpen;
         return this;
      }

      public Tailer.Builder setStartThread(boolean startThread) {
         this.startThread = startThread;
         return this;
      }

      public Tailer.Builder setTailable(Tailer.Tailable tailable) {
         this.tailable = Objects.requireNonNull(tailable, "tailable");
         return this;
      }

      public Tailer.Builder setTailerListener(TailerListener tailerListener) {
         this.tailerListener = Objects.requireNonNull(tailerListener, "tailerListener");
         return this;
      }

      public Tailer.Builder setTailFromEnd(boolean end) {
         this.tailFromEnd = end;
         return this;
      }
   }

   private static final class RandomAccessFileBridge implements Tailer.RandomAccessResourceBridge {
      private final RandomAccessFile randomAccessFile;

      private RandomAccessFileBridge(File file, String mode) throws FileNotFoundException {
         this.randomAccessFile = new RandomAccessFile(file, mode);
      }

      @Override
      public void close() throws IOException {
         this.randomAccessFile.close();
      }

      @Override
      public long getPointer() throws IOException {
         return this.randomAccessFile.getFilePointer();
      }

      @Override
      public int read(byte[] b) throws IOException {
         return this.randomAccessFile.read(b);
      }

      @Override
      public void seek(long position) throws IOException {
         this.randomAccessFile.seek(position);
      }
   }

   public interface RandomAccessResourceBridge extends Closeable {
      long getPointer() throws IOException;

      int read(byte[] var1) throws IOException;

      void seek(long var1) throws IOException;
   }

   public interface Tailable {
      Tailer.RandomAccessResourceBridge getRandomAccess(String var1) throws FileNotFoundException;

      boolean isNewer(FileTime var1) throws IOException;

      FileTime lastModifiedFileTime() throws IOException;

      long size() throws IOException;
   }

   private static final class TailablePath implements Tailer.Tailable {
      private final Path path;
      private final LinkOption[] linkOptions;

      private TailablePath(Path path, LinkOption... linkOptions) {
         this.path = Objects.requireNonNull(path, "path");
         this.linkOptions = linkOptions;
      }

      Path getPath() {
         return this.path;
      }

      @Override
      public Tailer.RandomAccessResourceBridge getRandomAccess(String mode) throws FileNotFoundException {
         return new Tailer.RandomAccessFileBridge(this.path.toFile(), mode);
      }

      @Override
      public boolean isNewer(FileTime fileTime) throws IOException {
         return PathUtils.isNewer(this.path, fileTime, this.linkOptions);
      }

      @Override
      public FileTime lastModifiedFileTime() throws IOException {
         return Files.getLastModifiedTime(this.path, this.linkOptions);
      }

      @Override
      public long size() throws IOException {
         return Files.size(this.path);
      }

      @Override
      public String toString() {
         return "TailablePath [file=" + this.path + ", linkOptions=" + Arrays.toString(this.linkOptions) + "]";
      }
   }
}
