package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public class Joiner {
   private final String separator;

   public static Joiner on(String separator) {
      return new Joiner(separator);
   }

   public static Joiner on(char separator) {
      return new Joiner(String.valueOf(separator));
   }

   private Joiner(String separator) {
      this.separator = Preconditions.checkNotNull(separator);
   }

   private Joiner(Joiner prototype) {
      this.separator = prototype.separator;
   }

   @CanIgnoreReturnValue
   public <A extends Appendable> A appendTo(A appendable, Iterable<?> parts) throws IOException {
      return this.appendTo(appendable, parts.iterator());
   }

   @CanIgnoreReturnValue
   public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
      Preconditions.checkNotNull(appendable);
      if (parts.hasNext()) {
         appendable.append(this.toString(parts.next()));

         while (parts.hasNext()) {
            appendable.append(this.separator);
            appendable.append(this.toString(parts.next()));
         }
      }

      return appendable;
   }

   @CanIgnoreReturnValue
   public final <A extends Appendable> A appendTo(A appendable, Object[] parts) throws IOException {
      List<?> partsList = Arrays.asList(parts);
      return this.appendTo(appendable, partsList);
   }

   @CanIgnoreReturnValue
   public final <A extends Appendable> A appendTo(A appendable, @Nullable Object first, @Nullable Object second, @Nullable Object... rest) throws IOException {
      return this.appendTo(appendable, iterable(first, second, rest));
   }

   @CanIgnoreReturnValue
   public final StringBuilder appendTo(StringBuilder builder, Iterable<?> parts) {
      return this.appendTo(builder, parts.iterator());
   }

   @CanIgnoreReturnValue
   public final StringBuilder appendTo(StringBuilder builder, Iterator<?> parts) {
      try {
         this.appendTo((StringBuilder)builder, parts);
         return builder;
      } catch (IOException impossible) {
         throw new AssertionError(impossible);
      }
   }

   @CanIgnoreReturnValue
   public final StringBuilder appendTo(StringBuilder builder, Object[] parts) {
      List<?> partsList = Arrays.asList(parts);
      return this.appendTo(builder, partsList);
   }

   @CanIgnoreReturnValue
   public final StringBuilder appendTo(StringBuilder builder, @Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
      return this.appendTo(builder, iterable(first, second, rest));
   }

   public String join(Iterable<?> parts) {
      if (parts instanceof List) {
         int size = ((List)parts).size();
         if (size == 0) {
            return "";
         }

         CharSequence[] toJoin = new CharSequence[size];
         int i = 0;

         for (Object part : parts) {
            if (i == toJoin.length) {
               toJoin = Arrays.copyOf(toJoin, expandedCapacity(toJoin.length, toJoin.length + 1));
            }

            toJoin[i++] = this.toString(part);
         }

         if (i != toJoin.length) {
            toJoin = Arrays.copyOf(toJoin, i);
         }

         return String.join(this.separator, toJoin);
      } else {
         return this.join(parts.iterator());
      }
   }

   public final String join(Iterator<?> parts) {
      return this.appendTo(new StringBuilder(), parts).toString();
   }

   public final String join(Object[] parts) {
      List<?> partsList = Arrays.asList(parts);
      return this.join(partsList);
   }

   public final String join(@Nullable Object first, @Nullable Object second, @Nullable Object... rest) {
      return this.join(iterable(first, second, rest));
   }

   public Joiner useForNull(String nullText) {
      Preconditions.checkNotNull(nullText);
      return new Joiner(this) {
         @Override
         CharSequence toString(@Nullable Object part) {
            return part == null ? nullText : Joiner.this.toString(part);
         }

         @Override
         public Joiner useForNull(String nullText) {
            throw new UnsupportedOperationException("already specified useForNull");
         }

         @Override
         public Joiner skipNulls() {
            throw new UnsupportedOperationException("already specified useForNull");
         }
      };
   }

   public Joiner skipNulls() {
      return new Joiner(this) {
         @Override
         public String join(Iterable<?> parts) {
            return this.join(parts.iterator());
         }

         @Override
         public <A extends Appendable> A appendTo(A appendable, Iterator<?> parts) throws IOException {
            Preconditions.checkNotNull(appendable, "appendable");
            Preconditions.checkNotNull(parts, "parts");

            while (parts.hasNext()) {
               Object part = parts.next();
               if (part != null) {
                  appendable.append(Joiner.this.toString(part));
                  break;
               }
            }

            while (parts.hasNext()) {
               Object part = parts.next();
               if (part != null) {
                  appendable.append(Joiner.this.separator);
                  appendable.append(Joiner.this.toString(part));
               }
            }

            return appendable;
         }

         @Override
         public Joiner useForNull(String nullText) {
            throw new UnsupportedOperationException("already specified skipNulls");
         }

         @Override
         public Joiner.MapJoiner withKeyValueSeparator(String kvs) {
            throw new UnsupportedOperationException("can't use .skipNulls() with maps");
         }
      };
   }

   public Joiner.MapJoiner withKeyValueSeparator(char keyValueSeparator) {
      return this.withKeyValueSeparator(String.valueOf(keyValueSeparator));
   }

   public Joiner.MapJoiner withKeyValueSeparator(String keyValueSeparator) {
      return new Joiner.MapJoiner(this, keyValueSeparator);
   }

   CharSequence toString(@Nullable Object part) {
      java.util.Objects.requireNonNull(part);
      return part instanceof CharSequence ? (CharSequence)part : part.toString();
   }

   private static Iterable<@Nullable Object> iterable(@Nullable Object first, @Nullable Object second, @Nullable Object[] rest) {
      Preconditions.checkNotNull(rest);
      return new AbstractList<Object>() {
         @Override
         public int size() {
            return rest.length + 2;
         }

         @Override
         public @Nullable Object get(int index) {
            switch (index) {
               case 0:
                  return first;
               case 1:
                  return second;
               default:
                  return rest[index - 2];
            }
         }
      };
   }

   private static int expandedCapacity(int oldCapacity, int minCapacity) {
      if (minCapacity < 0) {
         throw new IllegalArgumentException("cannot store more than Integer.MAX_VALUE elements");
      }

      if (minCapacity <= oldCapacity) {
         return oldCapacity;
      }

      int newCapacity = oldCapacity + (oldCapacity >> 1) + 1;
      if (newCapacity < minCapacity) {
         newCapacity = Integer.highestOneBit(minCapacity - 1) << 1;
      }

      if (newCapacity < 0) {
         newCapacity = Integer.MAX_VALUE;
      }

      return newCapacity;
   }

   public static final class MapJoiner {
      private final Joiner joiner;
      private final String keyValueSeparator;

      private MapJoiner(Joiner joiner, String keyValueSeparator) {
         this.joiner = joiner;
         this.keyValueSeparator = Preconditions.checkNotNull(keyValueSeparator);
      }

      @CanIgnoreReturnValue
      public <A extends Appendable> A appendTo(A appendable, Map<?, ?> map) throws IOException {
         return this.appendTo(appendable, map.entrySet());
      }

      @CanIgnoreReturnValue
      public StringBuilder appendTo(StringBuilder builder, Map<?, ?> map) {
         return this.appendTo(builder, map.entrySet());
      }

      @CanIgnoreReturnValue
      public <A extends Appendable> A appendTo(A appendable, Iterable<? extends Entry<?, ?>> entries) throws IOException {
         return this.appendTo(appendable, entries.iterator());
      }

      @CanIgnoreReturnValue
      public <A extends Appendable> A appendTo(A appendable, Iterator<? extends Entry<?, ?>> parts) throws IOException {
         Preconditions.checkNotNull(appendable);
         if (parts.hasNext()) {
            Entry<?, ?> entry = (Entry<?, ?>)parts.next();
            appendable.append(this.joiner.toString(entry.getKey()));
            appendable.append(this.keyValueSeparator);
            appendable.append(this.joiner.toString(entry.getValue()));

            while (parts.hasNext()) {
               appendable.append(this.joiner.separator);
               Entry<?, ?> e = (Entry<?, ?>)parts.next();
               appendable.append(this.joiner.toString(e.getKey()));
               appendable.append(this.keyValueSeparator);
               appendable.append(this.joiner.toString(e.getValue()));
            }
         }

         return appendable;
      }

      @CanIgnoreReturnValue
      public StringBuilder appendTo(StringBuilder builder, Iterable<? extends Entry<?, ?>> entries) {
         return this.appendTo(builder, entries.iterator());
      }

      @CanIgnoreReturnValue
      public StringBuilder appendTo(StringBuilder builder, Iterator<? extends Entry<?, ?>> entries) {
         try {
            this.appendTo((StringBuilder)builder, entries);
            return builder;
         } catch (IOException impossible) {
            throw new AssertionError(impossible);
         }
      }

      public String join(Map<?, ?> map) {
         return this.join(map.entrySet());
      }

      public String join(Iterable<? extends Entry<?, ?>> entries) {
         return this.join(entries.iterator());
      }

      public String join(Iterator<? extends Entry<?, ?>> entries) {
         return this.appendTo(new StringBuilder(), entries).toString();
      }

      public Joiner.MapJoiner useForNull(String nullText) {
         return new Joiner.MapJoiner(this.joiner.useForNull(nullText), this.keyValueSeparator);
      }
   }
}
