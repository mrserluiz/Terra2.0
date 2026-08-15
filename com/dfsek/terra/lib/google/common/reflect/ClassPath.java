package com.dfsek.terra.lib.google.common.reflect;

import com.dfsek.terra.lib.google.common.annotations.VisibleForTesting;
import com.dfsek.terra.lib.google.common.base.CharMatcher;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Splitter;
import com.dfsek.terra.lib.google.common.base.StandardSystemProperty;
import com.dfsek.terra.lib.google.common.collect.FluentIterable;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.dfsek.terra.lib.google.common.collect.ImmutableMap;
import com.dfsek.terra.lib.google.common.collect.ImmutableSet;
import com.dfsek.terra.lib.google.common.collect.Maps;
import com.dfsek.terra.lib.google.common.io.ByteSource;
import com.dfsek.terra.lib.google.common.io.CharSource;
import com.dfsek.terra.lib.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.Nullable;

public final class ClassPath {
   private static final Logger logger = Logger.getLogger(ClassPath.class.getName());
   private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR = Splitter.on(" ").omitEmptyStrings();
   private static final String CLASS_FILE_NAME_EXTENSION = ".class";
   private final ImmutableSet<ClassPath.ResourceInfo> resources;

   private ClassPath(ImmutableSet<ClassPath.ResourceInfo> resources) {
      this.resources = resources;
   }

   public static ClassPath from(ClassLoader classloader) throws IOException {
      ImmutableSet<ClassPath.LocationInfo> locations = locationsFrom(classloader);
      Set<File> scanned = new HashSet<>();

      for (ClassPath.LocationInfo location : locations) {
         scanned.add(location.file());
      }

      ImmutableSet.Builder<ClassPath.ResourceInfo> builder = ImmutableSet.builder();

      for (ClassPath.LocationInfo location : locations) {
         builder.addAll(location.scanResources(scanned));
      }

      return new ClassPath(builder.build());
   }

   public ImmutableSet<ClassPath.ResourceInfo> getResources() {
      return this.resources;
   }

   public ImmutableSet<ClassPath.ClassInfo> getAllClasses() {
      return FluentIterable.from(this.resources).filter(ClassPath.ClassInfo.class).toSet();
   }

   public ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses() {
      return FluentIterable.from(this.resources).filter(ClassPath.ClassInfo.class).filter(ClassPath.ClassInfo::isTopLevel).toSet();
   }

   public ImmutableSet<ClassPath.ClassInfo> getTopLevelClasses(String packageName) {
      Preconditions.checkNotNull(packageName);
      ImmutableSet.Builder<ClassPath.ClassInfo> builder = ImmutableSet.builder();

      for (ClassPath.ClassInfo classInfo : this.getTopLevelClasses()) {
         if (classInfo.getPackageName().equals(packageName)) {
            builder.add(classInfo);
         }
      }

      return builder.build();
   }

   public ImmutableSet<ClassPath.ClassInfo> getTopLevelClassesRecursive(String packageName) {
      Preconditions.checkNotNull(packageName);
      String packagePrefix = packageName + '.';
      ImmutableSet.Builder<ClassPath.ClassInfo> builder = ImmutableSet.builder();

      for (ClassPath.ClassInfo classInfo : this.getTopLevelClasses()) {
         if (classInfo.getName().startsWith(packagePrefix)) {
            builder.add(classInfo);
         }
      }

      return builder.build();
   }

   static ImmutableSet<ClassPath.LocationInfo> locationsFrom(ClassLoader classloader) {
      ImmutableSet.Builder<ClassPath.LocationInfo> builder = ImmutableSet.builder();

      for (Entry<File, ClassLoader> entry : getClassPathEntries(classloader).entrySet()) {
         builder.add(new ClassPath.LocationInfo(entry.getKey(), entry.getValue()));
      }

      return builder.build();
   }

   @VisibleForTesting
   static ImmutableSet<File> getClassPathFromManifest(File jarFile, @Nullable Manifest manifest) {
      if (manifest == null) {
         return ImmutableSet.of();
      }

      ImmutableSet.Builder<File> builder = ImmutableSet.builder();
      String classpathAttribute = manifest.getMainAttributes().getValue(Name.CLASS_PATH.toString());
      if (classpathAttribute != null) {
         for (String path : CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute)) {
            URL url;
            try {
               url = getClassPathEntry(jarFile, path);
            } catch (MalformedURLException e) {
               logger.warning("Invalid Class-Path entry: " + path);
               continue;
            }

            if (url.getProtocol().equals("file")) {
               builder.add(toFile(url));
            }
         }
      }

      return builder.build();
   }

   @VisibleForTesting
   static ImmutableMap<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
      LinkedHashMap<File, ClassLoader> entries = Maps.newLinkedHashMap();
      ClassLoader parent = classloader.getParent();
      if (parent != null) {
         entries.putAll(getClassPathEntries(parent));
      }

      for (URL url : getClassLoaderUrls(classloader)) {
         if (url.getProtocol().equals("file")) {
            File file = toFile(url);
            if (!entries.containsKey(file)) {
               entries.put(file, classloader);
            }
         }
      }

      return ImmutableMap.copyOf(entries);
   }

   private static ImmutableList<URL> getClassLoaderUrls(ClassLoader classloader) {
      if (classloader instanceof URLClassLoader) {
         return ImmutableList.copyOf(((URLClassLoader)classloader).getURLs());
      } else {
         return classloader.equals(ClassLoader.getSystemClassLoader()) ? parseJavaClassPath() : ImmutableList.of();
      }
   }

   @VisibleForTesting
   static ImmutableList<URL> parseJavaClassPath() {
      ImmutableList.Builder<URL> urls = ImmutableList.builder();

      for (String entry : Splitter.on(StandardSystemProperty.PATH_SEPARATOR.value()).split(StandardSystemProperty.JAVA_CLASS_PATH.value())) {
         try {
            try {
               urls.add(new File(entry).toURI().toURL());
            } catch (SecurityException e) {
               urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
            }
         } catch (MalformedURLException e) {
            logger.log(Level.WARNING, "malformed classpath entry: " + entry, e);
         }
      }

      return urls.build();
   }

   @VisibleForTesting
   static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
      return new URL(jarFile.toURI().toURL(), path);
   }

   @VisibleForTesting
   static String getClassName(String filename) {
      int classNameEnd = filename.length() - ".class".length();
      return filename.substring(0, classNameEnd).replace('/', '.');
   }

   @VisibleForTesting
   static File toFile(URL url) {
      Preconditions.checkArgument(url.getProtocol().equals("file"));

      try {
         return new File(url.toURI());
      } catch (URISyntaxException e) {
         return new File(url.getPath());
      }
   }

   public static final class ClassInfo extends ClassPath.ResourceInfo {
      private final String className;

      ClassInfo(File file, String resourceName, ClassLoader loader) {
         super(file, resourceName, loader);
         this.className = ClassPath.getClassName(resourceName);
      }

      public String getPackageName() {
         return Reflection.getPackageName(this.className);
      }

      public String getSimpleName() {
         int lastDollarSign = this.className.lastIndexOf(36);
         if (lastDollarSign != -1) {
            String innerClassName = this.className.substring(lastDollarSign + 1);
            return CharMatcher.inRange('0', '9').trimLeadingFrom(innerClassName);
         } else {
            String packageName = this.getPackageName();
            return packageName.isEmpty() ? this.className : this.className.substring(packageName.length() + 1);
         }
      }

      public String getName() {
         return this.className;
      }

      public boolean isTopLevel() {
         return this.className.indexOf(36) == -1;
      }

      public Class<?> load() {
         try {
            return this.loader.loadClass(this.className);
         } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
         }
      }

      @Override
      public String toString() {
         return this.className;
      }
   }

   static final class LocationInfo {
      final File home;
      private final ClassLoader classloader;

      LocationInfo(File home, ClassLoader classloader) {
         this.home = Preconditions.checkNotNull(home);
         this.classloader = Preconditions.checkNotNull(classloader);
      }

      public final File file() {
         return this.home;
      }

      public ImmutableSet<ClassPath.ResourceInfo> scanResources() throws IOException {
         return this.scanResources(new HashSet<>());
      }

      public ImmutableSet<ClassPath.ResourceInfo> scanResources(Set<File> scannedFiles) throws IOException {
         ImmutableSet.Builder<ClassPath.ResourceInfo> builder = ImmutableSet.builder();
         scannedFiles.add(this.home);
         this.scan(this.home, scannedFiles, builder);
         return builder.build();
      }

      private void scan(File file, Set<File> scannedUris, ImmutableSet.Builder<ClassPath.ResourceInfo> builder) throws IOException {
         try {
            if (!file.exists()) {
               return;
            }
         } catch (SecurityException e) {
            ClassPath.logger.warning("Cannot access " + file + ": " + e);
            return;
         }

         if (file.isDirectory()) {
            this.scanDirectory(file, builder);
         } else {
            this.scanJar(file, scannedUris, builder);
         }
      }

      private void scanJar(File file, Set<File> scannedUris, ImmutableSet.Builder<ClassPath.ResourceInfo> builder) throws IOException {
         JarFile jarFile;
         try {
            jarFile = new JarFile(file);
         } catch (IOException e) {
            return;
         }

         try {
            for (File path : ClassPath.getClassPathFromManifest(file, jarFile.getManifest())) {
               if (scannedUris.add(path.getCanonicalFile())) {
                  this.scan(path, scannedUris, builder);
               }
            }

            this.scanJarFile(jarFile, builder);
         } finally {
            try {
               jarFile.close();
            } catch (IOException var13) {
            }
         }
      }

      private void scanJarFile(JarFile file, ImmutableSet.Builder<ClassPath.ResourceInfo> builder) {
         Enumeration<JarEntry> entries = file.entries();

         while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (!entry.isDirectory() && !entry.getName().equals("META-INF/MANIFEST.MF")) {
               builder.add(ClassPath.ResourceInfo.of(new File(file.getName()), entry.getName(), this.classloader));
            }
         }
      }

      private void scanDirectory(File directory, ImmutableSet.Builder<ClassPath.ResourceInfo> builder) throws IOException {
         Set<File> currentPath = new HashSet<>();
         currentPath.add(directory.getCanonicalFile());
         this.scanDirectory(directory, "", currentPath, builder);
      }

      private void scanDirectory(File directory, String packagePrefix, Set<File> currentPath, ImmutableSet.Builder<ClassPath.ResourceInfo> builder) throws IOException {
         File[] files = directory.listFiles();
         if (files == null) {
            ClassPath.logger.warning("Cannot read directory " + directory);
         } else {
            for (File f : files) {
               String name = f.getName();
               if (f.isDirectory()) {
                  File deref = f.getCanonicalFile();
                  if (currentPath.add(deref)) {
                     this.scanDirectory(deref, packagePrefix + name + "/", currentPath, builder);
                     currentPath.remove(deref);
                  }
               } else {
                  String resourceName = packagePrefix + name;
                  if (!resourceName.equals("META-INF/MANIFEST.MF")) {
                     builder.add(ClassPath.ResourceInfo.of(f, resourceName, this.classloader));
                  }
               }
            }
         }
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (!(obj instanceof ClassPath.LocationInfo)) {
            return false;
         }

         ClassPath.LocationInfo that = (ClassPath.LocationInfo)obj;
         return this.home.equals(that.home) && this.classloader.equals(that.classloader);
      }

      @Override
      public int hashCode() {
         return this.home.hashCode();
      }

      @Override
      public String toString() {
         return this.home.toString();
      }
   }

   public static class ResourceInfo {
      private final File file;
      private final String resourceName;
      final ClassLoader loader;

      static ClassPath.ResourceInfo of(File file, String resourceName, ClassLoader loader) {
         return resourceName.endsWith(".class") ? new ClassPath.ClassInfo(file, resourceName, loader) : new ClassPath.ResourceInfo(file, resourceName, loader);
      }

      ResourceInfo(File file, String resourceName, ClassLoader loader) {
         this.file = Preconditions.checkNotNull(file);
         this.resourceName = Preconditions.checkNotNull(resourceName);
         this.loader = Preconditions.checkNotNull(loader);
      }

      public final URL url() {
         URL url = this.loader.getResource(this.resourceName);
         if (url == null) {
            throw new NoSuchElementException(this.resourceName);
         } else {
            return url;
         }
      }

      public final ByteSource asByteSource() {
         return Resources.asByteSource(this.url());
      }

      public final CharSource asCharSource(Charset charset) {
         return Resources.asCharSource(this.url(), charset);
      }

      public final String getResourceName() {
         return this.resourceName;
      }

      final File getFile() {
         return this.file;
      }

      @Override
      public int hashCode() {
         return this.resourceName.hashCode();
      }

      @Override
      public boolean equals(@Nullable Object obj) {
         if (!(obj instanceof ClassPath.ResourceInfo)) {
            return false;
         }

         ClassPath.ResourceInfo that = (ClassPath.ResourceInfo)obj;
         return this.resourceName.equals(that.resourceName) && this.loader == that.loader;
      }

      @Override
      public String toString() {
         return this.resourceName;
      }
   }
}
