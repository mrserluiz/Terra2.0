package net.fabricmc.mappingio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.enigma.EnigmaDirReader;
import net.fabricmc.mappingio.format.enigma.EnigmaFileReader;
import net.fabricmc.mappingio.format.intellij.MigrationMapFileReader;
import net.fabricmc.mappingio.format.jobf.JobfFileReader;
import net.fabricmc.mappingio.format.proguard.ProGuardFileReader;
import net.fabricmc.mappingio.format.simple.RecafSimpleFileReader;
import net.fabricmc.mappingio.format.srg.JamFileReader;
import net.fabricmc.mappingio.format.srg.SrgFileReader;
import net.fabricmc.mappingio.format.srg.TsrgFileReader;
import net.fabricmc.mappingio.format.tiny.Tiny1FileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import org.jetbrains.annotations.Nullable;

public final class MappingReader {
   private static final int DETECT_HEADER_LEN = 4096;

   private MappingReader() {
   }

   @Nullable
   public static MappingFormat detectFormat(Path file) throws IOException {
      if (Files.isDirectory(file)) {
         return MappingFormat.ENIGMA_DIR;
      }

      Reader reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

      MappingFormat var5;
      try {
         String fileName = file.getFileName().toString();
         int dotIdx = fileName.lastIndexOf(46);
         String fileExt = dotIdx >= 0 ? fileName.substring(dotIdx + 1) : null;
         var5 = detectFormat(reader, fileExt);
      } catch (Throwable var7) {
         try {
            reader.close();
         } catch (Throwable var6) {
            var7.addSuppressed(var6);
         }

         throw var7;
      }

      reader.close();
      return var5;
   }

   @Nullable
   public static MappingFormat detectFormat(Reader reader) throws IOException {
      return detectFormat(reader, null);
   }

   private static MappingFormat detectFormat(Reader reader, @Nullable String fileExt) throws IOException {
      char[] buffer = new char[4096];
      int pos = 0;
      BufferedReader br = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
      br.mark(4096);

      int len;
      while (pos < buffer.length && (len = br.read(buffer, pos, buffer.length - pos)) >= 0) {
         pos += len;
      }

      br.reset();
      if (pos < 3) {
         return null;
      }

      switch (String.valueOf(buffer, 0, 3)) {
         case "v1\t":
            return MappingFormat.TINY_FILE;
         case "tin":
            return MappingFormat.TINY_2_FILE;
         case "tsr":
            return MappingFormat.TSRG_2_FILE;
         case "CLA":
            return MappingFormat.ENIGMA_FILE;
         case "PK:":
         case "CL:":
         case "FD:":
         case "MD:":
            return detectSrgOrXsrg(br, fileExt);
         case "CL ":
         case "FD ":
         case "MD ":
         case "MP ":
            return MappingFormat.JAM_FILE;
         default:
            String headerStr = String.valueOf(buffer, 0, pos);
            if (headerStr.contains("<migrationMap>")) {
               return MappingFormat.INTELLIJ_MIGRATION_MAP_FILE;
            } else if ((headerStr.startsWith("p ") || headerStr.startsWith("c ") || headerStr.startsWith("f ") || headerStr.startsWith("m "))
               && headerStr.contains(" = ")) {
               return MappingFormat.JOBF_FILE;
            } else if (headerStr.contains(" -> ")) {
               return MappingFormat.PROGUARD_FILE;
            } else if (headerStr.contains("\n\t")) {
               return MappingFormat.TSRG_FILE;
            } else {
               return fileExt != null && fileExt.equals(MappingFormat.CSRG_FILE.fileExt) ? MappingFormat.CSRG_FILE : null;
            }
      }
   }

   private static MappingFormat detectSrgOrXsrg(BufferedReader reader, @Nullable String fileExt) throws IOException {
      String line;
      while ((line = reader.readLine()) != null) {
         if (line.startsWith("FD:")) {
            String[] parts = line.split(" ");
            if (parts.length >= 5 && !isEmptyOrStartsWithHash(parts[3]) && !isEmptyOrStartsWithHash(parts[4])) {
               return MappingFormat.XSRG_FILE;
            }

            return MappingFormat.SRG_FILE;
         }
      }

      return MappingFormat.XSRG_FILE.fileExt.equals(fileExt) ? MappingFormat.XSRG_FILE : MappingFormat.SRG_FILE;
   }

   private static boolean isEmptyOrStartsWithHash(String string) {
      return string.isEmpty() || string.startsWith("#");
   }

   public static List<String> getNamespaces(Path file) throws IOException {
      return getNamespaces(file, null);
   }

   public static List<String> getNamespaces(Path file, MappingFormat format) throws IOException {
      if (format == null) {
         format = detectFormat(file);
         if (format == null) {
            throw new IOException("invalid/unsupported mapping format");
         }
      }

      if (format.features().hasNamespaces()) {
         Reader reader = Files.newBufferedReader(file);

         List var3;
         try {
            var3 = getNamespaces(reader, format);
         } catch (Throwable var6) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (reader != null) {
            reader.close();
         }

         return var3;
      } else {
         return Arrays.asList("source", "target");
      }
   }

   public static List<String> getNamespaces(Reader reader) throws IOException {
      return getNamespaces(reader, null);
   }

   public static List<String> getNamespaces(Reader reader, MappingFormat format) throws IOException {
      if (format == null) {
         if (!reader.markSupported()) {
            reader = new BufferedReader(reader);
         }

         reader.mark(4096);
         format = detectFormat(reader);
         reader.reset();
         if (format == null) {
            throw new IOException("invalid/unsupported mapping format");
         }
      }

      if (format.features().hasNamespaces()) {
         checkReaderCompatible(format);
         switch (format) {
            case TINY_FILE:
               return Tiny1FileReader.getNamespaces(reader);
            case TINY_2_FILE:
               return Tiny2FileReader.getNamespaces(reader);
            case TSRG_2_FILE:
               return TsrgFileReader.getNamespaces(reader);
            default:
               throw new IllegalStateException();
         }
      } else {
         return Arrays.asList("source", "target");
      }
   }

   public static void read(Path path, MappingVisitor visitor) throws IOException {
      read(path, null, visitor);
   }

   public static void read(Path path, MappingFormat format, MappingVisitor visitor) throws IOException {
      if (format == null) {
         format = detectFormat(path);
         if (format == null) {
            throw new IOException("invalid/unsupported mapping format");
         }
      }

      if (format.hasSingleFile()) {
         Reader reader = Files.newBufferedReader(path);

         try {
            read(reader, format, visitor);
         } catch (Throwable var7) {
            if (reader != null) {
               try {
                  reader.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }
            }

            throw var7;
         }

         if (reader != null) {
            reader.close();
         }
      } else {
         switch (format) {
            case ENIGMA_DIR:
               EnigmaDirReader.read(path, visitor);
               break;
            default:
               throw new IllegalStateException();
         }
      }
   }

   public static void read(Reader reader, MappingVisitor visitor) throws IOException {
      read(reader, null, visitor);
   }

   public static void read(Reader reader, MappingFormat format, MappingVisitor visitor) throws IOException {
      if (format == null) {
         if (!reader.markSupported()) {
            reader = new BufferedReader(reader);
         }

         reader.mark(4096);
         format = detectFormat(reader);
         reader.reset();
         if (format == null) {
            throw new IOException("invalid/unsupported mapping format");
         }
      }

      checkReaderCompatible(format);
      switch (format) {
         case TINY_FILE:
            Tiny1FileReader.read(reader, visitor);
            break;
         case TINY_2_FILE:
            Tiny2FileReader.read(reader, visitor);
            break;
         case TSRG_2_FILE:
         case CSRG_FILE:
         case TSRG_FILE:
            TsrgFileReader.read(reader, visitor);
            break;
         case ENIGMA_DIR:
         default:
            throw new IllegalStateException();
         case ENIGMA_FILE:
            EnigmaFileReader.read(reader, visitor);
            break;
         case SRG_FILE:
         case XSRG_FILE:
            SrgFileReader.read(reader, visitor);
            break;
         case JAM_FILE:
            JamFileReader.read(reader, visitor);
            break;
         case PROGUARD_FILE:
            ProGuardFileReader.read(reader, visitor);
            break;
         case INTELLIJ_MIGRATION_MAP_FILE:
            MigrationMapFileReader.read(reader, visitor);
            break;
         case RECAF_SIMPLE_FILE:
            RecafSimpleFileReader.read(reader, visitor);
            break;
         case JOBF_FILE:
            JobfFileReader.read(reader, visitor);
      }
   }

   private static void checkReaderCompatible(MappingFormat format) throws IOException {
      if (!format.hasSingleFile()) {
         throw new IOException("can't read mapping format " + format.name + " using a Reader, use the Path based API");
      }
   }
}
