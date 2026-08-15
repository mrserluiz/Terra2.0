package com.dfsek.terra.lib.commons.text.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;

public class IntersectionSimilarity<T> implements SimilarityScore<IntersectionResult> {
   private final Function<CharSequence, Collection<T>> converter;

   private static <T> int getIntersection(Set<T> setA, Set<T> setB) {
      int intersection = 0;

      for (T element : setA) {
         if (setB.contains(element)) {
            intersection++;
         }
      }

      return intersection;
   }

   public IntersectionSimilarity(Function<CharSequence, Collection<T>> converter) {
      if (converter == null) {
         throw new IllegalArgumentException("Converter must not be null");
      }

      this.converter = converter;
   }

   public IntersectionResult apply(CharSequence left, CharSequence right) {
      if (left != null && right != null) {
         Collection<T> objectsA = this.converter.apply(left);
         Collection<T> objectsB = this.converter.apply(right);
         int sizeA = objectsA.size();
         int sizeB = objectsB.size();
         if (Math.min(sizeA, sizeB) == 0) {
            return new IntersectionResult(sizeA, sizeB, 0);
         }

         int intersection;
         if (objectsA instanceof Set && objectsB instanceof Set) {
            intersection = sizeA < sizeB ? getIntersection((Set<T>)objectsA, (Set<T>)objectsB) : getIntersection((Set<T>)objectsB, (Set<T>)objectsA);
         } else {
            IntersectionSimilarity<T>.TinyBag bagA = this.toBag(objectsA);
            IntersectionSimilarity<T>.TinyBag bagB = this.toBag(objectsB);
            intersection = bagA.uniqueElementSize() < bagB.uniqueElementSize() ? this.getIntersection(bagA, bagB) : this.getIntersection(bagB, bagA);
         }

         return new IntersectionResult(sizeA, sizeB, intersection);
      } else {
         throw new IllegalArgumentException("Input cannot be null");
      }
   }

   private int getIntersection(IntersectionSimilarity<T>.TinyBag bagA, IntersectionSimilarity<T>.TinyBag bagB) {
      int intersection = 0;

      for (Entry<T, IntersectionSimilarity.BagCount> entry : bagA.entrySet()) {
         T element = entry.getKey();
         int count = entry.getValue().count;
         intersection += Math.min(count, bagB.getCount(element));
      }

      return intersection;
   }

   private IntersectionSimilarity<T>.TinyBag toBag(Collection<T> objects) {
      IntersectionSimilarity<T>.TinyBag bag = new IntersectionSimilarity.TinyBag(objects.size());
      objects.forEach(x$0 -> bag.add((T)x$0));
      return bag;
   }

   private static final class BagCount {
      private static final IntersectionSimilarity.BagCount ZERO = new IntersectionSimilarity.BagCount();
      private int count = 0;

      private BagCount() {
      }
   }

   private final class TinyBag {
      private final Map<T, IntersectionSimilarity.BagCount> map;

      private TinyBag(final int initialCapacity) {
         this.map = new HashMap<>(initialCapacity);
      }

      private void add(T object) {
         this.map.computeIfAbsent(object, k -> new IntersectionSimilarity.BagCount()).count++;
      }

      private Set<Entry<T, IntersectionSimilarity.BagCount>> entrySet() {
         return this.map.entrySet();
      }

      private int getCount(Object object) {
         return this.map.getOrDefault(object, IntersectionSimilarity.BagCount.ZERO).count;
      }

      private int uniqueElementSize() {
         return this.map.size();
      }
   }
}
