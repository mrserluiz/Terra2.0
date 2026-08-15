package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.IOExceptionList;
import com.dfsek.terra.lib.commons.io.function.IOConsumer;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

public class FilterCollectionWriter extends Writer {
   protected final Collection<Writer> EMPTY_WRITERS = Collections.emptyList();
   protected final Collection<Writer> writers;

   protected FilterCollectionWriter(Collection<Writer> writers) {
      this.writers = writers == null ? this.EMPTY_WRITERS : writers;
   }

   protected FilterCollectionWriter(Writer... writers) {
      this.writers = writers == null ? this.EMPTY_WRITERS : Arrays.asList(writers);
   }

   @Override
   public Writer append(char c) throws IOException {
      return this.forAllWriters(w -> w.append(c));
   }

   @Override
   public Writer append(CharSequence csq) throws IOException {
      return this.forAllWriters(w -> w.append(csq));
   }

   @Override
   public Writer append(CharSequence csq, int start, int end) throws IOException {
      return this.forAllWriters(w -> w.append(csq, start, end));
   }

   @Override
   public void close() throws IOException {
      this.forAllWriters(Writer::close);
   }

   @Override
   public void flush() throws IOException {
      this.forAllWriters(Writer::flush);
   }

   private FilterCollectionWriter forAllWriters(IOConsumer<Writer> action) throws IOExceptionList {
      IOConsumer.forAll(action, this.writers());
      return this;
   }

   @Override
   public void write(char[] cbuf) throws IOException {
      this.forAllWriters(w -> w.write(cbuf));
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException {
      this.forAllWriters(w -> w.write(cbuf, off, len));
   }

   @Override
   public void write(int c) throws IOException {
      this.forAllWriters(w -> w.write(c));
   }

   @Override
   public void write(String str) throws IOException {
      this.forAllWriters(w -> w.write(str));
   }

   @Override
   public void write(String str, int off, int len) throws IOException {
      this.forAllWriters(w -> w.write(str, off, len));
   }

   private Stream<Writer> writers() {
      return this.writers.stream().filter(Objects::nonNull);
   }
}
