package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class LineReader {
   private final Readable readable;
   private final @Nullable Reader reader;
   private final CharBuffer cbuf = CharStreams.createBuffer();
   private final char[] buf = this.cbuf.array();
   private final Queue<String> lines = new ArrayDeque<>();
   private final LineBuffer lineBuf = new LineBuffer() {
      @Override
      protected void handleLine(String line, String end) {
         LineReader.this.lines.add(line);
      }
   };

   public LineReader(Readable readable) {
      this.readable = Preconditions.checkNotNull(readable);
      this.reader = readable instanceof Reader ? (Reader)readable : null;
   }

   @CanIgnoreReturnValue
   public @Nullable String readLine() throws IOException {
      while (this.lines.peek() == null) {
         Java8Compatibility.clear(this.cbuf);
         int read = this.reader != null ? this.reader.read(this.buf, 0, this.buf.length) : this.readable.read(this.cbuf);
         if (read == -1) {
            this.lineBuf.finish();
            break;
         }

         this.lineBuf.add(this.buf, 0, read);
      }

      return this.lines.poll();
   }
}
