package com.dfsek.terra.lib.commons.io.serialization;

import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.regex.Pattern;

public class ValidatingObjectInputStream extends ObjectInputStream {
   private final ObjectStreamClassPredicate predicate;

   public static ValidatingObjectInputStream.Builder builder() {
      return new ValidatingObjectInputStream.Builder();
   }

   @Deprecated
   public ValidatingObjectInputStream(InputStream input) throws IOException {
      this(input, new ObjectStreamClassPredicate());
   }

   private ValidatingObjectInputStream(InputStream input, ObjectStreamClassPredicate predicate) throws IOException {
      super(input);
      this.predicate = predicate;
   }

   public ValidatingObjectInputStream accept(Class<?>... classes) {
      this.predicate.accept(classes);
      return this;
   }

   public ValidatingObjectInputStream accept(ClassNameMatcher matcher) {
      this.predicate.accept(matcher);
      return this;
   }

   public ValidatingObjectInputStream accept(Pattern pattern) {
      this.predicate.accept(pattern);
      return this;
   }

   public ValidatingObjectInputStream accept(String... patterns) {
      this.predicate.accept(patterns);
      return this;
   }

   private void checkClassName(String name) throws InvalidClassException {
      if (!this.predicate.test(name)) {
         this.invalidClassNameFound(name);
      }
   }

   protected void invalidClassNameFound(String className) throws InvalidClassException {
      throw new InvalidClassException("Class name not accepted: " + className);
   }

   public <T> T readObjectCast() throws ClassNotFoundException, IOException {
      return (T)super.readObject();
   }

   public ValidatingObjectInputStream reject(Class<?>... classes) {
      this.predicate.reject(classes);
      return this;
   }

   public ValidatingObjectInputStream reject(ClassNameMatcher matcher) {
      this.predicate.reject(matcher);
      return this;
   }

   public ValidatingObjectInputStream reject(Pattern pattern) {
      this.predicate.reject(pattern);
      return this;
   }

   public ValidatingObjectInputStream reject(String... patterns) {
      this.predicate.reject(patterns);
      return this;
   }

   @Override
   protected Class<?> resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException {
      this.checkClassName(osc.getName());
      return super.resolveClass(osc);
   }

   public static class Builder extends AbstractStreamBuilder<ValidatingObjectInputStream, ValidatingObjectInputStream.Builder> {
      private ObjectStreamClassPredicate predicate = new ObjectStreamClassPredicate();

      public ValidatingObjectInputStream.Builder accept(Class<?>... classes) {
         this.predicate.accept(classes);
         return this;
      }

      public ValidatingObjectInputStream.Builder accept(ClassNameMatcher matcher) {
         this.predicate.accept(matcher);
         return this;
      }

      public ValidatingObjectInputStream.Builder accept(Pattern pattern) {
         this.predicate.accept(pattern);
         return this;
      }

      public ValidatingObjectInputStream.Builder accept(String... patterns) {
         this.predicate.accept(patterns);
         return this;
      }

      public ValidatingObjectInputStream get() throws IOException {
         return new ValidatingObjectInputStream(this.getInputStream(), this.predicate);
      }

      public ObjectStreamClassPredicate getPredicate() {
         return this.predicate;
      }

      public ValidatingObjectInputStream.Builder reject(Class<?>... classes) {
         this.predicate.reject(classes);
         return this;
      }

      public ValidatingObjectInputStream.Builder reject(ClassNameMatcher matcher) {
         this.predicate.reject(matcher);
         return this;
      }

      public ValidatingObjectInputStream.Builder reject(Pattern pattern) {
         this.predicate.reject(pattern);
         return this;
      }

      public ValidatingObjectInputStream.Builder reject(String... patterns) {
         this.predicate.reject(patterns);
         return this;
      }

      public ValidatingObjectInputStream.Builder setPredicate(ObjectStreamClassPredicate predicate) {
         this.predicate = predicate != null ? predicate : new ObjectStreamClassPredicate();
         return this;
      }
   }
}
