package net.fabricmc.mappingio;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.format.enigma.EnigmaDirWriter;
import net.fabricmc.mappingio.format.enigma.EnigmaFileWriter;
import net.fabricmc.mappingio.format.intellij.MigrationMapFileWriter;
import net.fabricmc.mappingio.format.jobf.JobfFileWriter;
import net.fabricmc.mappingio.format.proguard.ProGuardFileWriter;
import net.fabricmc.mappingio.format.simple.RecafSimpleFileWriter;
import net.fabricmc.mappingio.format.srg.CsrgFileWriter;
import net.fabricmc.mappingio.format.srg.JamFileWriter;
import net.fabricmc.mappingio.format.srg.SrgFileWriter;
import net.fabricmc.mappingio.format.srg.TsrgFileWriter;
import net.fabricmc.mappingio.format.tiny.Tiny1FileWriter;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;
import org.jetbrains.annotations.Nullable;

public interface MappingWriter extends Closeable, MappingVisitor {
   @Nullable
   static MappingWriter create(Path file, MappingFormat format) throws IOException {
      if (format.hasSingleFile()) {
         return create(Files.newBufferedWriter(file), format);
      }

      switch (format) {
         case ENIGMA_DIR:
            return new EnigmaDirWriter(file, true);
         default:
            return null;
      }
   }

   @Nullable
   static MappingWriter create(Writer writer, MappingFormat format) throws IOException {
      if (!format.hasSingleFile()) {
         throw new IllegalArgumentException("format " + format + " is not applicable to a single writer");
      }

      switch (format) {
         case TINY_FILE:
            return new Tiny1FileWriter(writer);
         case TINY_2_FILE:
            return new Tiny2FileWriter(writer, false);
         case ENIGMA_FILE:
            return new EnigmaFileWriter(writer);
         case SRG_FILE:
            return new SrgFileWriter(writer, false);
         case XSRG_FILE:
            return new SrgFileWriter(writer, true);
         case JAM_FILE:
            return new JamFileWriter(writer);
         case CSRG_FILE:
            return new CsrgFileWriter(writer);
         case TSRG_FILE:
            return new TsrgFileWriter(writer, false);
         case TSRG_2_FILE:
            return new TsrgFileWriter(writer, true);
         case PROGUARD_FILE:
            return new ProGuardFileWriter(writer);
         case INTELLIJ_MIGRATION_MAP_FILE:
            return new MigrationMapFileWriter(writer);
         case RECAF_SIMPLE_FILE:
            return new RecafSimpleFileWriter(writer);
         case JOBF_FILE:
            return new JobfFileWriter(writer);
         default:
            return null;
      }
   }

   @Override
   default boolean visitEnd() throws IOException {
      this.close();
      return true;
   }
}
