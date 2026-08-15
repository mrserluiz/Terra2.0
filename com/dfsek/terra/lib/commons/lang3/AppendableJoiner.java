package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.exception.UncheckedException;
import com.dfsek.terra.lib.commons.lang3.function.FailableBiConsumer;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Supplier;

public final class AppendableJoiner<T> {
   private final CharSequence prefix;
   private final CharSequence suffix;
   private final CharSequence delimiter;
   private final FailableBiConsumer<Appendable, T, IOException> appender;

   public static <T> AppendableJoiner.Builder<T> builder() {
      return new AppendableJoiner.Builder<>();
   }

   @SafeVarargs
   static <A extends Appendable, T> A joinA(
      A appendable, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, T... elements
   ) throws IOException {
      return joinArray(appendable, prefix, suffix, delimiter, appender, elements);
   }

   private static <A extends Appendable, T> A joinArray(
      A appendable, CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender, T[] elements
   ) throws IOException {
      appendable.append(prefix);
      if (elements != null) {
         if (elements.length > 0) {
            appender.accept(appendable, elements[0]);
         }

         for (int i = 1; i < elements.length; i++) {
            appendable.append(delimiter);
            appender.accept(appendable, elements[i]);
         }
      }

      appendable.append(suffix);
      return appendable;
   }

   static <T> StringBuilder joinI(
      StringBuilder stringBuilder,
      CharSequence prefix,
      CharSequence suffix,
      CharSequence delimiter,
      FailableBiConsumer<Appendable, T, IOException> appender,
      Iterable<T> elements
   ) {
      try {
         return joinIterable(stringBuilder, prefix, suffix, delimiter, appender, elements);
      } catch (IOException e) {
         throw new UncheckedException(e);
      }
   }

   private static <A extends Appendable, T> A joinIterable(
      A appendable,
      CharSequence prefix,
      CharSequence suffix,
      CharSequence delimiter,
      FailableBiConsumer<Appendable, T, IOException> appender,
      Iterable<T> elements
   ) throws IOException {
      appendable.append(prefix);
      if (elements != null) {
         Iterator<T> iterator = elements.iterator();
         if (iterator.hasNext()) {
            appender.accept(appendable, iterator.next());
         }

         while (iterator.hasNext()) {
            appendable.append(delimiter);
            appender.accept(appendable, iterator.next());
         }
      }

      appendable.append(suffix);
      return appendable;
   }

   @SafeVarargs
   static <T> StringBuilder joinSB(
      StringBuilder stringBuilder,
      CharSequence prefix,
      CharSequence suffix,
      CharSequence delimiter,
      FailableBiConsumer<Appendable, T, IOException> appender,
      T... elements
   ) {
      try {
         return joinArray(stringBuilder, prefix, suffix, delimiter, appender, elements);
      } catch (IOException e) {
         throw new UncheckedException(e);
      }
   }

   private static CharSequence nonNull(CharSequence value) {
      return value != null ? value : "";
   }

   private AppendableJoiner(CharSequence prefix, CharSequence suffix, CharSequence delimiter, FailableBiConsumer<Appendable, T, IOException> appender) {
      this.prefix = nonNull(prefix);
      this.suffix = nonNull(suffix);
      this.delimiter = nonNull(delimiter);
      this.appender = appender != null ? appender : (a, e) -> a.append(String.valueOf(e));
   }

   public StringBuilder join(StringBuilder stringBuilder, Iterable<T> elements) {
      return joinI(stringBuilder, this.prefix, this.suffix, this.delimiter, this.appender, elements);
   }

   public StringBuilder join(StringBuilder stringBuilder, T... elements) {
      return joinSB(stringBuilder, this.prefix, this.suffix, this.delimiter, this.appender, elements);
   }

   public <A extends Appendable> A joinA(A appendable, Iterable<T> elements) throws IOException {
      return joinIterable(appendable, this.prefix, this.suffix, this.delimiter, this.appender, elements);
   }

   public <A extends Appendable> A joinA(A appendable, T... elements) throws IOException {
      return joinA(appendable, this.prefix, this.suffix, this.delimiter, this.appender, elements);
   }

   public static final class Builder<T> implements Supplier<AppendableJoiner<T>> {
      private CharSequence prefix;
      private CharSequence suffix;
      private CharSequence delimiter;
      private FailableBiConsumer<Appendable, T, IOException> appender;

      Builder() {
      }

      public AppendableJoiner<T> get() {
         return new AppendableJoiner<>(this.prefix, this.suffix, this.delimiter, this.appender);
      }

      public AppendableJoiner.Builder<T> setDelimiter(CharSequence delimiter) {
         this.delimiter = delimiter;
         return this;
      }

      public AppendableJoiner.Builder<T> setElementAppender(FailableBiConsumer<Appendable, T, IOException> appender) {
         this.appender = appender;
         return this;
      }

      public AppendableJoiner.Builder<T> setPrefix(CharSequence prefix) {
         this.prefix = prefix;
         return this;
      }

      public AppendableJoiner.Builder<T> setSuffix(CharSequence suffix) {
         this.suffix = suffix;
         return this;
      }
   }
}
