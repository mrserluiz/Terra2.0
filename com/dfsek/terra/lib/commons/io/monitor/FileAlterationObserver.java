package com.dfsek.terra.lib.commons.io.monitor;

import com.dfsek.terra.lib.commons.io.FileUtils;
import com.dfsek.terra.lib.commons.io.IOCase;
import com.dfsek.terra.lib.commons.io.build.AbstractOriginSupplier;
import com.dfsek.terra.lib.commons.io.comparator.NameFileComparator;
import com.dfsek.terra.lib.commons.io.filefilter.TrueFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class FileAlterationObserver implements Serializable {
   private static final long serialVersionUID = 1185122225658782848L;
   private final transient List<FileAlterationListener> listeners = new CopyOnWriteArrayList<>();
   private final FileEntry rootEntry;
   private final transient FileFilter fileFilter;
   private final Comparator<File> comparator;

   public static FileAlterationObserver.Builder builder() {
      return new FileAlterationObserver.Builder();
   }

   private static Comparator<File> toComparator(IOCase ioCase) {
      switch (IOCase.value(ioCase, IOCase.SYSTEM)) {
         case SYSTEM:
            return NameFileComparator.NAME_SYSTEM_COMPARATOR;
         case INSENSITIVE:
            return NameFileComparator.NAME_INSENSITIVE_COMPARATOR;
         default:
            return NameFileComparator.NAME_COMPARATOR;
      }
   }

   @Deprecated
   public FileAlterationObserver(File directory) {
      this(directory, null);
   }

   @Deprecated
   public FileAlterationObserver(File directory, FileFilter fileFilter) {
      this(directory, fileFilter, null);
   }

   @Deprecated
   public FileAlterationObserver(File directory, FileFilter fileFilter, IOCase ioCase) {
      this(new FileEntry(directory), fileFilter, ioCase);
   }

   private FileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter, Comparator<File> comparator) {
      Objects.requireNonNull(rootEntry, "rootEntry");
      Objects.requireNonNull(rootEntry.getFile(), "rootEntry.getFile()");
      this.rootEntry = rootEntry;
      this.fileFilter = fileFilter != null ? fileFilter : TrueFileFilter.INSTANCE;
      this.comparator = Objects.requireNonNull(comparator, "comparator");
   }

   protected FileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter, IOCase ioCase) {
      this(rootEntry, fileFilter, toComparator(ioCase));
   }

   @Deprecated
   public FileAlterationObserver(String directoryName) {
      this(new File(directoryName));
   }

   @Deprecated
   public FileAlterationObserver(String directoryName, FileFilter fileFilter) {
      this(new File(directoryName), fileFilter);
   }

   @Deprecated
   public FileAlterationObserver(String directoryName, FileFilter fileFilter, IOCase ioCase) {
      this(new File(directoryName), fileFilter, ioCase);
   }

   public void addListener(FileAlterationListener listener) {
      if (listener != null) {
         this.listeners.add(listener);
      }
   }

   private void checkAndFire(FileEntry parentEntry, FileEntry[] previousEntries, File[] currentEntries) {
      int c = 0;
      FileEntry[] actualEntries = currentEntries.length > 0 ? new FileEntry[currentEntries.length] : FileEntry.EMPTY_FILE_ENTRY_ARRAY;
      FileEntry[] var6 = previousEntries;
      int var7 = var6.length;

      for (int var8 = 0; var8 < var7; var8++) {
         FileEntry previousEntry;
         for (previousEntry = var6[var8]; c < currentEntries.length && this.comparator.compare(previousEntry.getFile(), currentEntries[c]) > 0; c++) {
            actualEntries[c] = this.createFileEntry(parentEntry, currentEntries[c]);
            this.fireOnCreate(actualEntries[c]);
         }

         if (c < currentEntries.length && this.comparator.compare(previousEntry.getFile(), currentEntries[c]) == 0) {
            this.fireOnChange(previousEntry, currentEntries[c]);
            this.checkAndFire(previousEntry, previousEntry.getChildren(), this.listFiles(currentEntries[c]));
            actualEntries[c] = previousEntry;
            c++;
         } else {
            this.checkAndFire(previousEntry, previousEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
            this.fireOnDelete(previousEntry);
         }
      }

      while (c < currentEntries.length) {
         actualEntries[c] = this.createFileEntry(parentEntry, currentEntries[c]);
         this.fireOnCreate(actualEntries[c]);
         c++;
      }

      parentEntry.setChildren(actualEntries);
   }

   public void checkAndNotify() {
      this.listeners.forEach(listener -> listener.onStart(this));
      File rootFile = this.rootEntry.getFile();
      if (rootFile.exists()) {
         this.checkAndFire(this.rootEntry, this.rootEntry.getChildren(), this.listFiles(rootFile));
      } else if (this.rootEntry.isExists()) {
         this.checkAndFire(this.rootEntry, this.rootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
      }

      this.listeners.forEach(listener -> listener.onStop(this));
   }

   private FileEntry createFileEntry(FileEntry parent, File file) {
      FileEntry entry = parent.newChildInstance(file);
      entry.refresh(file);
      entry.setChildren(this.listFileEntries(file, entry));
      return entry;
   }

   public void destroy() throws Exception {
   }

   private void fireOnChange(FileEntry entry, File file) {
      if (entry.refresh(file)) {
         this.listeners.forEach(listener -> {
            if (entry.isDirectory()) {
               listener.onDirectoryChange(file);
            } else {
               listener.onFileChange(file);
            }
         });
      }
   }

   private void fireOnCreate(FileEntry entry) {
      this.listeners.forEach(listener -> {
         if (entry.isDirectory()) {
            listener.onDirectoryCreate(entry.getFile());
         } else {
            listener.onFileCreate(entry.getFile());
         }
      });
      Stream.of(entry.getChildren()).forEach(this::fireOnCreate);
   }

   private void fireOnDelete(FileEntry entry) {
      this.listeners.forEach(listener -> {
         if (entry.isDirectory()) {
            listener.onDirectoryDelete(entry.getFile());
         } else {
            listener.onFileDelete(entry.getFile());
         }
      });
   }

   Comparator<File> getComparator() {
      return this.comparator;
   }

   public File getDirectory() {
      return this.rootEntry.getFile();
   }

   public FileFilter getFileFilter() {
      return this.fileFilter;
   }

   public Iterable<FileAlterationListener> getListeners() {
      return new ArrayList<>(this.listeners);
   }

   public void initialize() throws Exception {
      this.rootEntry.refresh(this.rootEntry.getFile());
      this.rootEntry.setChildren(this.listFileEntries(this.rootEntry.getFile(), this.rootEntry));
   }

   private FileEntry[] listFileEntries(File file, FileEntry entry) {
      return Stream.of(this.listFiles(file)).map(f -> this.createFileEntry(entry, f)).toArray(FileEntry[]::new);
   }

   private File[] listFiles(File directory) {
      return directory.isDirectory() ? this.sort(directory.listFiles(this.fileFilter)) : FileUtils.EMPTY_FILE_ARRAY;
   }

   public void removeListener(FileAlterationListener listener) {
      if (listener != null) {
         this.listeners.removeIf(listener::equals);
      }
   }

   private File[] sort(File[] files) {
      if (files == null) {
         return FileUtils.EMPTY_FILE_ARRAY;
      }

      if (files.length > 1) {
         Arrays.sort(files, this.comparator);
      }

      return files;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getClass().getSimpleName());
      builder.append("[file='");
      builder.append(this.getDirectory().getPath());
      builder.append('\'');
      builder.append(", ");
      builder.append(this.fileFilter.toString());
      builder.append(", listeners=");
      builder.append(this.listeners.size());
      builder.append("]");
      return builder.toString();
   }

   public static final class Builder extends AbstractOriginSupplier<FileAlterationObserver, FileAlterationObserver.Builder> {
      private FileEntry rootEntry;
      private FileFilter fileFilter;
      private IOCase ioCase;

      private Builder() {
      }

      public FileAlterationObserver get() throws IOException {
         return new FileAlterationObserver(
            this.rootEntry != null ? this.rootEntry : new FileEntry(this.checkOrigin().getFile()),
            this.fileFilter,
            FileAlterationObserver.toComparator(this.ioCase)
         );
      }

      public FileAlterationObserver.Builder setFileFilter(FileFilter fileFilter) {
         this.fileFilter = fileFilter;
         return this.asThis();
      }

      public FileAlterationObserver.Builder setIOCase(IOCase ioCase) {
         this.ioCase = ioCase;
         return this.asThis();
      }

      public FileAlterationObserver.Builder setRootEntry(FileEntry rootEntry) {
         this.rootEntry = rootEntry;
         return this.asThis();
      }
   }
}
