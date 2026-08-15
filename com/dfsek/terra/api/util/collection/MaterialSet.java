package com.dfsek.terra.api.util.collection;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.BlockState;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class MaterialSet extends HashSet<BlockType> {
   private static final long serialVersionUID = 3056512763631017301L;

   public static MaterialSet singleton(BlockType material) {
      return new MaterialSet.Singleton(material);
   }

   public static MaterialSet get(BlockType... materials) {
      MaterialSet set = new MaterialSet();
      set.addAll(Arrays.asList(materials));
      return set;
   }

   public static MaterialSet get(BlockState... materials) {
      MaterialSet set = new MaterialSet();
      Arrays.stream(materials).forEach(set::add);
      return set;
   }

   public static MaterialSet empty() {
      return new MaterialSet();
   }

   private void add(BlockState data) {
      this.add(data.getBlockType());
   }

   private static final class Singleton extends MaterialSet {
      private final BlockType element;

      Singleton(BlockType e) {
         this.element = e;
      }

      @Override
      public Iterator<BlockType> iterator() {
         return new Iterator<BlockType>() {
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
               return this.hasNext;
            }

            public BlockType next() {
               if (this.hasNext) {
                  this.hasNext = false;
                  return Singleton.this.element;
               } else {
                  throw new NoSuchElementException();
               }
            }

            @Override
            public void remove() {
               throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super BlockType> action) {
               Objects.requireNonNull(action);
               if (this.hasNext) {
                  this.hasNext = false;
                  action.accept(Singleton.this.element);
               }
            }
         };
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public boolean contains(Object o) {
         return Objects.equals(o, this.element);
      }

      @Override
      public void forEach(Consumer<? super BlockType> action) {
         action.accept(this.element);
      }

      @Override
      public Spliterator<BlockType> spliterator() {
         return new Spliterator<BlockType>() {
            long est = 1L;

            @Override
            public Spliterator<BlockType> trySplit() {
               return null;
            }

            @Override
            public boolean tryAdvance(Consumer<? super BlockType> consumer) {
               Objects.requireNonNull(consumer);
               if (this.est > 0L) {
                  this.est--;
                  consumer.accept(Singleton.this.element);
                  return true;
               } else {
                  return false;
               }
            }

            @Override
            public void forEachRemaining(Consumer<? super BlockType> consumer) {
               this.tryAdvance(consumer);
            }

            @Override
            public long estimateSize() {
               return this.est;
            }

            @Override
            public int characteristics() {
               int value = Singleton.this.element != null ? 256 : 0;
               return value | 64 | 16384 | 1024 | 1 | 16;
            }
         };
      }

      @Override
      public boolean removeIf(Predicate<? super BlockType> filter) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int hashCode() {
         return Objects.hashCode(this.element);
      }
   }
}
