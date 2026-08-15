package xyz.jpenilla.reflectionremapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.reflectionremapper.internal.util.Util;

@DefaultQualifier(NonNull.class)
public interface ReflectionRemapper {
   String remapClassName(String className);

   String remapFieldName(Class<?> holdingClass, String fieldName);

   String remapMethodName(Class<?> holdingClass, String methodName, Class<?>... paramTypes);

   default String remapClassOrArrayName(final String name) {
      Objects.requireNonNull(name, "name");
      if (name.isEmpty()) {
         return name;
      }

      if (name.charAt(0) == '[') {
         int last = name.lastIndexOf(91);

         try {
            if (name.charAt(last + 1) == 'L') {
               String cls = name.substring(last + 2, name.length() - 1);
               return name.substring(0, last + 2) + this.remapClassName(cls) + ';';
            } else {
               return name;
            }
         } catch (IndexOutOfBoundsException ex) {
            return name;
         }
      } else {
         return this.remapClassName(name);
      }
   }

   default ReflectionRemapper withClassNamePreprocessor(final UnaryOperator<String> preprocessor) {
      return new ClassNamePreprocessingReflectionRemapper(this, preprocessor);
   }

   static ReflectionRemapper noop() {
      return NoopReflectionRemapper.INSTANCE;
   }

   static ReflectionRemapper forMappings(final InputStream mappings, final String fromNamespace, final String toNamespace) {
      try {
         MemoryMappingTree tree = new MemoryMappingTree(true);
         tree.setSrcNamespace(fromNamespace);
         tree.setDstNamespaces(new ArrayList<>(Collections.singletonList(toNamespace)));
         MappingReader.read(new InputStreamReader(mappings, StandardCharsets.UTF_8), tree);
         return ReflectionRemapperImpl.fromMappingTree(tree, fromNamespace, toNamespace);
      } catch (IOException ex) {
         throw new RuntimeException("Failed to read mappings.", ex);
      }
   }

   static ReflectionRemapper forMappings(final Path mappings, final String fromNamespace, final String toNamespace) {
      try {
         InputStream stream = Files.newInputStream(mappings);

         ReflectionRemapper var4;
         try {
            var4 = forMappings(stream, fromNamespace, toNamespace);
         } catch (Throwable var7) {
            if (stream != null) {
               try {
                  stream.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (stream != null) {
            stream.close();
         }

         return var4;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   static ReflectionRemapper forPaperReobfMappings(final Path mappings) {
      if (Util.mojangMapped()) {
         return noop();
      }

      try {
         InputStream inputStream = Files.newInputStream(mappings);

         ReflectionRemapper var2;
         try {
            var2 = forPaperReobfMappings(inputStream);
         } catch (Throwable var5) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var4) {
                  var5.addSuppressed(var4);
               }
            }

            throw var5;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var2;
      } catch (IOException e) {
         throw new RuntimeException("Failed to read mappings.", e);
      }
   }

   static ReflectionRemapper forPaperReobfMappings(final InputStream mappings) {
      if (Util.mojangMapped()) {
         return noop();
      } else {
         return Util.firstLine(mappings).contains("mojang+yarn") ? forMappings(mappings, "mojang+yarn", "spigot") : forMappings(mappings, "mojang", "spigot");
      }
   }

   static ReflectionRemapper forReobfMappingsInPaperJar() {
      if (Util.mojangMapped()) {
         return noop();
      }

      Class<?> craftServerClass;
      try {
         Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
         Method getServerMethod = bukkitClass.getDeclaredMethod("getServer");
         craftServerClass = getServerMethod.invoke(null).getClass();
      } catch (ReflectiveOperationException e) {
         throw new RuntimeException(e);
      }

      try {
         InputStream reobfIn = craftServerClass.getClassLoader().getResourceAsStream("META-INF/mappings/reobf.tiny");

         ReflectionRemapper var4;
         try {
            if (reobfIn == null) {
               throw new IllegalStateException("Could not find mappings in expected location.");
            }

            var4 = forPaperReobfMappings(reobfIn);
         } catch (Throwable var8) {
            if (reobfIn != null) {
               try {
                  reobfIn.close();
               } catch (Throwable var6) {
                  var8.addSuppressed(var6);
               }
            }

            throw var8;
         }

         if (reobfIn != null) {
            reobfIn.close();
         }

         return var4;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }
}
