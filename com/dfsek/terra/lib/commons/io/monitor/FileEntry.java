package com.dfsek.terra.lib.commons.io.monitor;

import com.dfsek.terra.lib.commons.io.FileUtils;
import com.dfsek.terra.lib.commons.io.file.attribute.FileTimes;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

public class FileEntry implements Serializable {
   private static final long serialVersionUID = -2505664948818681153L;
   static final FileEntry[] EMPTY_FILE_ENTRY_ARRAY = new FileEntry[0];
   private final FileEntry parent;
   private FileEntry[] children;
   private final File file;
   private String name;
   private boolean exists;
   private boolean directory;
   private SerializableFileTime lastModified = SerializableFileTime.EPOCH;
   private long length;

   public FileEntry(File file) {
      this(null, file);
   }

   public FileEntry(FileEntry parent, File file) {
      this.file = Objects.requireNonNull(file, "file");
      this.parent = parent;
      this.name = file.getName();
   }

   public FileEntry[] getChildren() {
      return this.children != null ? this.children : EMPTY_FILE_ENTRY_ARRAY;
   }

   public File getFile() {
      return this.file;
   }

   public long getLastModified() {
      return this.lastModified.toMillis();
   }

   public FileTime getLastModifiedFileTime() {
      return this.lastModified.unwrap();
   }

   public long getLength() {
      return this.length;
   }

   public int getLevel() {
      return this.parent == null ? 0 : this.parent.getLevel() + 1;
   }

   public String getName() {
      return this.name;
   }

   public FileEntry getParent() {
      return this.parent;
   }

   public boolean isDirectory() {
      return this.directory;
   }

   public boolean isExists() {
      return this.exists;
   }

   public FileEntry newChildInstance(File file) {
      return new FileEntry(this, file);
   }

   public boolean refresh(File file) {
      boolean origExists = this.exists;
      SerializableFileTime origLastModified = this.lastModified;
      boolean origDirectory = this.directory;
      long origLength = this.length;
      this.name = file.getName();
      this.exists = Files.exists(file.toPath());
      this.directory = this.exists && file.isDirectory();

      try {
         this.setLastModified(this.exists ? FileUtils.lastModifiedFileTime(file) : FileTimes.EPOCH);
      } catch (IOException e) {
         this.setLastModified(SerializableFileTime.EPOCH);
      }

      this.length = this.exists && !this.directory ? file.length() : 0L;
      return this.exists != origExists || !this.lastModified.equals(origLastModified) || this.directory != origDirectory || this.length != origLength;
   }

   public void setChildren(FileEntry... children) {
      this.children = children;
   }

   public void setDirectory(boolean directory) {
      this.directory = directory;
   }

   public void setExists(boolean exists) {
      this.exists = exists;
   }

   public void setLastModified(FileTime lastModified) {
      this.setLastModified(new SerializableFileTime(lastModified));
   }

   public void setLastModified(long lastModified) {
      this.setLastModified(FileTime.fromMillis(lastModified));
   }

   void setLastModified(SerializableFileTime lastModified) {
      this.lastModified = lastModified;
   }

   public void setLength(long length) {
      this.length = length;
   }

   public void setName(String name) {
      this.name = name;
   }
}
