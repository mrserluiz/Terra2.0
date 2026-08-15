package com.dfsek.terra.lib.commons.io;

import com.dfsek.terra.lib.commons.io.filefilter.FileFilterUtils;
import com.dfsek.terra.lib.commons.io.filefilter.IOFileFilter;
import com.dfsek.terra.lib.commons.io.filefilter.TrueFileFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

@Deprecated
public abstract class DirectoryWalker<T> {
   private final FileFilter filter;
   private final int depthLimit;

   protected DirectoryWalker() {
      this(null, -1);
   }

   protected DirectoryWalker(FileFilter filter, int depthLimit) {
      this.filter = filter;
      this.depthLimit = depthLimit;
   }

   protected DirectoryWalker(IOFileFilter directoryFilter, IOFileFilter fileFilter, int depthLimit) {
      if (directoryFilter == null && fileFilter == null) {
         this.filter = null;
      } else {
         directoryFilter = directoryFilter != null ? directoryFilter : TrueFileFilter.TRUE;
         fileFilter = fileFilter != null ? fileFilter : TrueFileFilter.TRUE;
         directoryFilter = FileFilterUtils.makeDirectoryOnly(directoryFilter);
         fileFilter = FileFilterUtils.makeFileOnly(fileFilter);
         this.filter = directoryFilter.or(fileFilter);
      }

      this.depthLimit = depthLimit;
   }

   protected final void checkIfCancelled(File file, int depth, Collection<T> results) throws IOException {
      if (this.handleIsCancelled(file, depth, results)) {
         throw new DirectoryWalker.CancelException(file, depth);
      }
   }

   protected File[] filterDirectoryContents(File directory, int depth, File... files) throws IOException {
      return files;
   }

   protected void handleCancelled(File startDirectory, Collection<T> results, DirectoryWalker.CancelException cancel) throws IOException {
      throw cancel;
   }

   protected boolean handleDirectory(File directory, int depth, Collection<T> results) throws IOException {
      return true;
   }

   protected void handleDirectoryEnd(File directory, int depth, Collection<T> results) throws IOException {
   }

   protected void handleDirectoryStart(File directory, int depth, Collection<T> results) throws IOException {
   }

   protected void handleEnd(Collection<T> results) throws IOException {
   }

   protected void handleFile(File file, int depth, Collection<T> results) throws IOException {
   }

   protected boolean handleIsCancelled(File file, int depth, Collection<T> results) throws IOException {
      return false;
   }

   protected void handleRestricted(File directory, int depth, Collection<T> results) throws IOException {
   }

   protected void handleStart(File startDirectory, Collection<T> results) throws IOException {
   }

   protected final void walk(File startDirectory, Collection<T> results) throws IOException {
      Objects.requireNonNull(startDirectory, "startDirectory");

      try {
         this.handleStart(startDirectory, results);
         this.walk(startDirectory, 0, results);
         this.handleEnd(results);
      } catch (DirectoryWalker.CancelException cancel) {
         this.handleCancelled(startDirectory, results, cancel);
      }
   }

   private void walk(File directory, int depth, Collection<T> results) throws IOException {
      this.checkIfCancelled(directory, depth, results);
      if (this.handleDirectory(directory, depth, results)) {
         this.handleDirectoryStart(directory, depth, results);
         int childDepth = depth + 1;
         if (this.depthLimit < 0 || childDepth <= this.depthLimit) {
            this.checkIfCancelled(directory, depth, results);
            File[] childFiles = directory.listFiles(this.filter);
            childFiles = this.filterDirectoryContents(directory, depth, childFiles);
            if (childFiles == null) {
               this.handleRestricted(directory, childDepth, results);
            } else {
               for (File childFile : childFiles) {
                  if (childFile.isDirectory()) {
                     this.walk(childFile, childDepth, results);
                  } else {
                     this.checkIfCancelled(childFile, childDepth, results);
                     this.handleFile(childFile, childDepth, results);
                     this.checkIfCancelled(childFile, childDepth, results);
                  }
               }
            }
         }

         this.handleDirectoryEnd(directory, depth, results);
      }

      this.checkIfCancelled(directory, depth, results);
   }

   public static class CancelException extends IOException {
      private static final long serialVersionUID = 1347339620135041008L;
      private final File file;
      private final int depth;

      public CancelException(File file, int depth) {
         this("Operation Cancelled", file, depth);
      }

      public CancelException(String message, File file, int depth) {
         super(message);
         this.file = file;
         this.depth = depth;
      }

      public int getDepth() {
         return this.depth;
      }

      public File getFile() {
         return this.file;
      }
   }
}
