package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.IOUtils;
import java.io.IOException;
import java.io.OutputStream;

public class DemuxOutputStream extends OutputStream {
   private final InheritableThreadLocal<OutputStream> outputStreamThreadLocal = new InheritableThreadLocal<>();

   public OutputStream bindStream(OutputStream output) {
      OutputStream stream = this.outputStreamThreadLocal.get();
      this.outputStreamThreadLocal.set(output);
      return stream;
   }

   @Override
   public void close() throws IOException {
      IOUtils.close(this.outputStreamThreadLocal.get());
   }

   @Override
   public void flush() throws IOException {
      OutputStream output = this.outputStreamThreadLocal.get();
      if (null != output) {
         output.flush();
      }
   }

   @Override
   public void write(int ch) throws IOException {
      OutputStream output = this.outputStreamThreadLocal.get();
      if (null != output) {
         output.write(ch);
      }
   }
}
