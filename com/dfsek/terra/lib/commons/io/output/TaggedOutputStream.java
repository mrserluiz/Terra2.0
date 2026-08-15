package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.TaggedIOException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

public class TaggedOutputStream extends ProxyOutputStream {
   private final Serializable tag = UUID.randomUUID();

   public TaggedOutputStream(OutputStream proxy) {
      super(proxy);
   }

   @Override
   protected void handleIOException(IOException e) throws IOException {
      throw new TaggedIOException(e, this.tag);
   }

   public boolean isCauseOf(Exception exception) {
      return TaggedIOException.isTaggedWith(exception, this.tag);
   }

   public void throwIfCauseOf(Exception exception) throws IOException {
      TaggedIOException.throwCauseIfTaggedWith(exception, this.tag);
   }
}
