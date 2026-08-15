package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.StandardSystemProperty;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.stream.Stream;

@J2ktIncompatible
@GwtIncompatible
public abstract class CharSink {
   protected CharSink() {
   }

   public abstract Writer openStream() throws IOException;

   public Writer openBufferedStream() throws IOException {
      Writer writer = this.openStream();
      return writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer);
   }

   public void write(CharSequence charSequence) throws IOException {
      Preconditions.checkNotNull(charSequence);
      Writer out = this.openStream();

      try {
         out.append(charSequence);
      } catch (Throwable var6) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (out != null) {
         out.close();
      }
   }

   public void writeLines(Iterable<? extends CharSequence> lines) throws IOException {
      this.writeLines(lines, System.getProperty("line.separator"));
   }

   public void writeLines(Iterable<? extends CharSequence> lines, String lineSeparator) throws IOException {
      this.writeLines(lines.iterator(), lineSeparator);
   }

   public void writeLines(Stream<? extends CharSequence> lines) throws IOException {
      this.writeLines(lines, StandardSystemProperty.LINE_SEPARATOR.value());
   }

   public void writeLines(Stream<? extends CharSequence> lines, String lineSeparator) throws IOException {
      this.writeLines(lines.iterator(), lineSeparator);
   }

   private void writeLines(Iterator<? extends CharSequence> lines, String lineSeparator) throws IOException {
      Preconditions.checkNotNull(lineSeparator);
      Writer out = this.openBufferedStream();

      try {
         while (lines.hasNext()) {
            out.append(lines.next()).append(lineSeparator);
         }
      } catch (Throwable var7) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (out != null) {
         out.close();
      }
   }

   @CanIgnoreReturnValue
   public long writeFrom(Readable readable) throws IOException {
      Preconditions.checkNotNull(readable);
      Writer out = this.openStream();

      long var3;
      try {
         var3 = CharStreams.copy(readable, out);
      } catch (Throwable var6) {
         if (out != null) {
            try {
               out.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (out != null) {
         out.close();
      }

      return var3;
   }
}
