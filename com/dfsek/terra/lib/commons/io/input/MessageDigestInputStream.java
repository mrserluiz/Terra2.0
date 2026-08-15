package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

public final class MessageDigestInputStream extends ObservableInputStream {
   private final MessageDigest messageDigest;

   public static MessageDigestInputStream.Builder builder() {
      return new MessageDigestInputStream.Builder();
   }

   private MessageDigestInputStream(MessageDigestInputStream.Builder builder) throws IOException {
      super(builder);
      this.messageDigest = Objects.requireNonNull(builder.messageDigest, "builder.messageDigest");
   }

   public MessageDigest getMessageDigest() {
      return this.messageDigest;
   }

   public static class Builder extends ObservableInputStream.AbstractBuilder<MessageDigestInputStream.Builder> {
      private MessageDigest messageDigest;

      public MessageDigestInputStream get() throws IOException {
         this.setObservers(Arrays.asList(new MessageDigestInputStream.MessageDigestMaintainingObserver(this.messageDigest)));
         return new MessageDigestInputStream(this);
      }

      public MessageDigestInputStream.Builder setMessageDigest(MessageDigest messageDigest) {
         this.messageDigest = messageDigest;
         return this;
      }

      public MessageDigestInputStream.Builder setMessageDigest(String algorithm) throws NoSuchAlgorithmException {
         this.messageDigest = MessageDigest.getInstance(algorithm);
         return this;
      }
   }

   public static class MessageDigestMaintainingObserver extends ObservableInputStream.Observer {
      private final MessageDigest messageDigest;

      public MessageDigestMaintainingObserver(MessageDigest messageDigest) {
         this.messageDigest = Objects.requireNonNull(messageDigest, "messageDigest");
      }

      @Override
      public void data(byte[] input, int offset, int length) throws IOException {
         this.messageDigest.update(input, offset, length);
      }

      @Override
      public void data(int input) throws IOException {
         this.messageDigest.update((byte)input);
      }
   }
}
