package com.dfsek.terra.api.util.collection;

import com.dfsek.terra.api.noise.NoiseSampler;
import com.dfsek.terra.api.util.MathUtil;
import com.dfsek.terra.api.util.mutable.MutableInteger;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.util.vector.Vector3Int;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

public class ProbabilityCollection<E> implements Collection<E> {
   protected final Map<E, MutableInteger> cont = new HashMap<>();
   private Object[] array = new Object[0];
   private int size;

   public ProbabilityCollection<E> add(E item, int probability) {
      if (!this.cont.containsKey(item)) {
         this.size++;
      }

      this.cont.computeIfAbsent(item, ix -> new MutableInteger(0)).increment();
      int oldLength = this.array.length;
      Object[] newArray = new Object[this.array.length + probability];
      System.arraycopy(this.array, 0, newArray, 0, this.array.length);
      this.array = newArray;

      for (int i = oldLength; i < this.array.length; i++) {
         this.array[i] = item;
      }

      return this;
   }

   public E get(Random r) {
      return (E)(this.array.length == 0 ? null : this.array[r.nextInt(this.array.length)]);
   }

   public E get(NoiseSampler n, double x, double y, double z, long seed) {
      return (E)(this.array.length == 0 ? null : this.array[MathUtil.normalizeIndex(n.noise(seed, x, y, z), this.array.length)]);
   }

   public E get(NoiseSampler n, Vector3Int vector3Int, long seed) {
      return (E)(this.array.length == 0
         ? null
         : this.array[MathUtil.normalizeIndex(n.noise(seed, vector3Int.getX(), vector3Int.getY(), vector3Int.getZ()), this.array.length)]);
   }

   public E get(NoiseSampler n, Vector3 vector3Int, long seed) {
      return (E)(this.array.length == 0
         ? null
         : this.array[MathUtil.normalizeIndex(n.noise(seed, vector3Int.getX(), vector3Int.getY(), vector3Int.getZ()), this.array.length)]);
   }

   public E get(NoiseSampler n, double x, double z, long seed) {
      return (E)(this.array.length == 0 ? null : this.array[MathUtil.normalizeIndex(n.noise(seed, x, z), this.array.length)]);
   }

   public <T> ProbabilityCollection<T> map(Function<E, T> mapper, boolean carryNull) {
      ProbabilityCollection<T> newCollection = new ProbabilityCollection();
      newCollection.array = new Object[this.array.length];

      for (int i = 0; i < this.array.length; i++) {
         if (!carryNull || this.array[i] != null) {
            newCollection.array[i] = mapper.apply((E)this.array[i]);
         }
      }

      return newCollection;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder("[");
      this.cont.forEach((item, prob) -> builder.append(item).append(": ").append(prob).append(", "));
      return builder.append("]").toString();
   }

   @Override
   public int size() {
      return this.size;
   }

   @Override
   public boolean isEmpty() {
      return this.array.length == 0;
   }

   @Override
   public boolean contains(Object o) {
      return this.cont.containsKey(o);
   }

   @NotNull
   @Override
   public Iterator<E> iterator() {
      return this.cont.keySet().iterator();
   }

   @NotNull
   @Override
   public Object[] toArray() {
      return this.cont.keySet().toArray();
   }

   @NotNull
   @Override
   public <T> T[] toArray(@NotNull T[] a) {
      return (T[])this.cont.keySet().toArray(a);
   }

   @Override
   public boolean add(E e) {
      this.add(e, 1);
      return true;
   }

   @Override
   public boolean remove(Object o) {
      throw new UnsupportedOperationException("Cannot remove item from ProbabilityCollection!");
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return this.cont.keySet().containsAll(c);
   }

   @Override
   public boolean addAll(@NotNull Collection<? extends E> c) {
      c.forEach(this::add);
      return true;
   }

   @Override
   public boolean removeAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException("Cannot remove item from ProbabilityCollection!");
   }

   @Override
   public boolean retainAll(@NotNull Collection<?> c) {
      throw new UnsupportedOperationException("Cannot remove item from ProbabilityCollection!");
   }

   @Override
   public void clear() {
      this.cont.clear();
      this.array = new Object[0];
   }

   public int getTotalProbability() {
      return this.array.length;
   }

   public int getProbability(E item) {
      MutableInteger integer = this.cont.get(item);
      return integer == null ? 0 : integer.get();
   }

   public Set<E> getContents() {
      return new HashSet<>(this.cont.keySet());
   }

   public static final class Singleton<T> extends ProbabilityCollection<T> {
      private final T single;

      public Singleton(T single) {
         this.single = single;
         this.cont.put(single, new MutableInteger(1));
      }

      @Override
      public ProbabilityCollection<T> add(T item, int probability) {
         throw new UnsupportedOperationException();
      }

      @Override
      public T get(Random r) {
         return this.single;
      }

      @Override
      public T get(NoiseSampler n, double x, double y, double z, long seed) {
         return this.single;
      }

      @Override
      public T get(NoiseSampler n, double x, double z, long seed) {
         return this.single;
      }

      @Override
      public <T1> ProbabilityCollection<T1> map(Function<T, T1> mapper, boolean carryNull) {
         return carryNull && this.single == null ? new ProbabilityCollection.Singleton(null) : new ProbabilityCollection.Singleton<>(mapper.apply(this.single));
      }

      @Override
      public int size() {
         return 1;
      }

      @Override
      public int getTotalProbability() {
         return 1;
      }

      @Override
      public Set<T> getContents() {
         return Collections.singleton(this.single);
      }
   }
}
