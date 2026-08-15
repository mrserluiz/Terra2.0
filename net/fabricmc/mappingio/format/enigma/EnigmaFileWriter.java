package net.fabricmc.mappingio.format.enigma;

import java.io.IOException;
import java.io.Writer;
import net.fabricmc.mappingio.MappedElementKind;

public final class EnigmaFileWriter extends EnigmaWriterBase {
   public EnigmaFileWriter(Writer writer) throws IOException {
      super(writer);
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (targetKind == MappedElementKind.CLASS) {
         this.writeMismatchedOrMissingClasses();
      } else if (targetKind != MappedElementKind.FIELD && targetKind != MappedElementKind.METHOD) {
         this.writer.write(10);
      } else {
         this.writer.write(32);
         this.writer.write(this.desc);
         this.writer.write(10);
      }

      return true;
   }
}
