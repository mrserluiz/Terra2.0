package com.dfsek.terra.lib.commons.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;

@Deprecated
public class MessageDigestCalculatingInputStream extends ObservableInputStream {
   private static final String DEFAULT_ALGORITHM = "MD5";
   private final MessageDigest messageDigest;

   public static MessageDigestCalculatingInputStream.Builder builder() {
      return new MessageDigestCalculatingInputStream.Builder();
   }

   static MessageDigest getDefaultMessageDigest() throws NoSuchAlgorithmException {
      return MessageDigest.getInstance("MD5");
   }

   private MessageDigestCalculatingInputStream(MessageDigestCalculatingInputStream.Builder builder) throws IOException {
      super(builder);
      this.messageDigest = builder.messageDigest;
   }

   @Deprecated
   public MessageDigestCalculatingInputStream(InputStream inputStream) throws NoSuchAlgorithmException {
      this(inputStream, getDefaultMessageDigest());
   }

   @Deprecated
   public MessageDigestCalculatingInputStream(InputStream inputStream, MessageDigest messageDigest) {
      super(inputStream, new MessageDigestCalculatingInputStream.MessageDigestMaintainingObserver(messageDigest));
      this.messageDigest = messageDigest;
   }

   @Deprecated
   public MessageDigestCalculatingInputStream(InputStream inputStream, String algorithm) throws NoSuchAlgorithmException {
      this(inputStream, MessageDigest.getInstance(algorithm));
   }

   public MessageDigest getMessageDigest() {
      return this.messageDigest;
   }

   public static class Builder extends ObservableInputStream.AbstractBuilder<MessageDigestCalculatingInputStream.Builder> {
      private MessageDigest messageDigest;

      public Builder() {
         try {
            this.messageDigest = MessageDigestCalculatingInputStream.getDefaultMessageDigest();
         } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
         }
      }

      public MessageDigestCalculatingInputStream get() throws IOException {
         this.setObservers(Arrays.asList(new MessageDigestCalculatingInputStream.MessageDigestMaintainingObserver(this.messageDigest)));
         return new MessageDigestCalculatingInputStream(this);
      }

      public void setMessageDigest(MessageDigest messageDigest) {
         this.messageDigest = messageDigest;
      }

      public void setMessageDigest(String algorithm) throws NoSuchAlgorithmException {
         this.messageDigest = MessageDigest.getInstance(algorithm);
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
