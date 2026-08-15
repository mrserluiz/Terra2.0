package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

@J2ktIncompatible
@GwtIncompatible
public abstract class ByteSink {
   protected ByteSink() {
   }

   public CharSink asCharSink(Charset charset) {
      return new ByteSink.AsCharSink(charset);
   }

   public abstract OutputStream openStream() throws IOException;

   public OutputStream openBufferedStream() throws IOException {
      OutputStream out = this.openStream();
      return out instanceof BufferedOutputStream ? (BufferedOutputStream)out : new BufferedOutputStream(out);
   }

   public void write(byte[] bytes) throws IOException {
      Preconditions.checkNotNull(bytes);
      OutputStream out = this.openStream();

      try {
         out.write(bytes);
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

   @CanIgnoreReturnValue
   public long writeFrom(InputStream input) throws IOException {
      Preconditions.checkNotNull(input);
      OutputStream out = this.openStream();

      long var3;
      try {
         var3 = ByteStreams.copy(input, out);
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

   private final class AsCharSink extends CharSink {
      private final Charset charset;

      private AsCharSink(Charset charset) {
         this.charset = Preconditions.checkNotNull(charset);
      }

      @Override
      public Writer openStream() throws IOException {
         return new OutputStreamWriter(ByteSink.this.openStream(), this.charset);
      }

      @Override
      public String toString() {
         return ByteSink.this.toString() + ".asCharSink(" + this.charset + ")";
      }
   }
}
