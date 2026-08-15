package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.MoreObjects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.ObjIntConsumer;
import org.jspecify.annotations.Nullable;

@GwtCompatible(emulated = true)
public final class TreeMultiset<E> extends AbstractSortedMultiset<E> implements Serializable {
   private final transient TreeMultiset.Reference<TreeMultiset.AvlNode<E>> rootReference;
   private final transient GeneralRange<E> range;
   private final transient TreeMultiset.AvlNode<E> header;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 1L;

   public static <E extends Comparable> TreeMultiset<E> create() {
      return new TreeMultiset<>(Ordering.natural());
   }

   public static <E> TreeMultiset<E> create(@Nullable Comparator<? super E> comparator) {
      return comparator == null ? new TreeMultiset<>(Ordering.natural()) : new TreeMultiset<>(comparator);
   }

   public static <E extends Comparable> TreeMultiset<E> create(Iterable<? extends E> elements) {
      TreeMultiset<E> multiset = create();
      Iterables.addAll(multiset, elements);
      return multiset;
   }

   TreeMultiset(TreeMultiset.Reference<TreeMultiset.AvlNode<E>> rootReference, GeneralRange<E> range, TreeMultiset.AvlNode<E> endLink) {
      super(range.comparator());
      this.rootReference = rootReference;
      this.range = range;
      this.header = endLink;
   }

   TreeMultiset(Comparator<? super E> comparator) {
      super(comparator);
      this.range = GeneralRange.all(comparator);
      this.header = new TreeMultiset.AvlNode<>();
      successor(this.header, this.header);
      this.rootReference = new TreeMultiset.Reference<>();
   }

   private long aggregateForEntries(TreeMultiset.Aggregate aggr) {
      TreeMultiset.AvlNode<E> root = this.rootReference.get();
      long total = aggr.treeAggregate(root);
      if (this.range.hasLowerBound()) {
         total -= this.aggregateBelowRange(aggr, root);
      }

      if (this.range.hasUpperBound()) {
         total -= this.aggregateAboveRange(aggr, root);
      }

      return total;
   }

   private long aggregateBelowRange(TreeMultiset.Aggregate aggr, TreeMultiset.@Nullable AvlNode<E> node) {
      if (node == null) {
         return 0L;
      }

      int cmp = this.comparator().compare(NullnessCasts.uncheckedCastNullableTToT(this.range.getLowerEndpoint()), node.getElement());
      if (cmp < 0) {
         return this.aggregateBelowRange(aggr, node.left);
      }

      if (cmp == 0) {
         switch (this.range.getLowerBoundType()) {
            case OPEN:
               return aggr.nodeAggregate(node) + aggr.treeAggregate(node.left);
            case CLOSED:
               return aggr.treeAggregate(node.left);
            default:
               throw new AssertionError();
         }
      } else {
         return aggr.treeAggregate(node.left) + aggr.nodeAggregate(node) + this.aggregateBelowRange(aggr, node.right);
      }
   }

   private long aggregateAboveRange(TreeMultiset.Aggregate aggr, TreeMultiset.@Nullable AvlNode<E> node) {
      if (node == null) {
         return 0L;
      }

      int cmp = this.comparator().compare(NullnessCasts.uncheckedCastNullableTToT(this.range.getUpperEndpoint()), node.getElement());
      if (cmp > 0) {
         return this.aggregateAboveRange(aggr, node.right);
      }

      if (cmp == 0) {
         switch (this.range.getUpperBoundType()) {
            case OPEN:
               return aggr.nodeAggregate(node) + aggr.treeAggregate(node.right);
            case CLOSED:
               return aggr.treeAggregate(node.right);
            default:
               throw new AssertionError();
         }
      } else {
         return aggr.treeAggregate(node.right) + aggr.nodeAggregate(node) + this.aggregateAboveRange(aggr, node.left);
      }
   }

   @Override
   public int size() {
      return Ints.saturatedCast(this.aggregateForEntries(TreeMultiset.Aggregate.SIZE));
   }

   @Override
   int distinctElements() {
      return Ints.saturatedCast(this.aggregateForEntries(TreeMultiset.Aggregate.DISTINCT));
   }

   static int distinctElements(TreeMultiset.@Nullable AvlNode<?> node) {
      return node == null ? 0 : node.distinctElements;
   }

   @Override
   public int count(@Nullable Object element) {
      try {
         E e = (E)element;
         TreeMultiset.AvlNode<E> root = this.rootReference.get();
         return this.range.contains(e) && root != null ? root.count(this.comparator(), e) : 0;
      } catch (ClassCastException | NullPointerException e) {
         return 0;
      }
   }

   @CanIgnoreReturnValue
   @Override
   public int add(@ParametricNullness E element, int occurrences) {
      CollectPreconditions.checkNonnegative(occurrences, "occurrences");
      if (occurrences == 0) {
         return this.count(element);
      } else {
         Preconditions.checkArgument(this.range.contains(element));
         TreeMultiset.AvlNode<E> root = this.rootReference.get();
         if (root == null) {
            int unused = this.comparator().compare(element, element);
            TreeMultiset.AvlNode<E> newRoot = new TreeMultiset.AvlNode<>(element, occurrences);
            successor(this.header, newRoot, this.header);
            this.rootReference.checkAndSet(root, newRoot);
            return 0;
         } else {
            int[] result = new int[1];
            TreeMultiset.AvlNode<E> newRoot = root.add(this.comparator(), element, occurrences, result);
            this.rootReference.checkAndSet(root, newRoot);
            return result[0];
         }
      }
   }

   @CanIgnoreReturnValue
   @Override
   public int remove(@Nullable Object element, int occurrences) {
      CollectPreconditions.checkNonnegative(occurrences, "occurrences");
      if (occurrences == 0) {
         return this.count(element);
      }

      TreeMultiset.AvlNode<E> root = this.rootReference.get();
      int[] result = new int[1];

      TreeMultiset.AvlNode<E> newRoot;
      try {
         E e = (E)element;
         if (!this.range.contains(e) || root == null) {
            return 0;
         }

         newRoot = root.remove(this.comparator(), e, occurrences, result);
      } catch (ClassCastException | NullPointerException e) {
         return 0;
      }

      this.rootReference.checkAndSet(root, newRoot);
      return result[0];
   }

   @CanIgnoreReturnValue
   @Override
   public int setCount(@ParametricNullness E element, int count) {
      CollectPreconditions.checkNonnegative(count, "count");
      if (!this.range.contains(element)) {
         Preconditions.checkArgument(count == 0);
         return 0;
      }

      TreeMultiset.AvlNode<E> root = this.rootReference.get();
      if (root == null) {
         if (count > 0) {
            this.add(element, count);
         }

         return 0;
      } else {
         int[] result = new int[1];
         TreeMultiset.AvlNode<E> newRoot = root.setCount(this.comparator(), element, count, result);
         this.rootReference.checkAndSet(root, newRoot);
         return result[0];
      }
   }

   @CanIgnoreReturnValue
   @Override
   public boolean setCount(@ParametricNullness E element, int oldCount, int newCount) {
      CollectPreconditions.checkNonnegative(newCount, "newCount");
      CollectPreconditions.checkNonnegative(oldCount, "oldCount");
      Preconditions.checkArgument(this.range.contains(element));
      TreeMultiset.AvlNode<E> root = this.rootReference.get();
      if (root == null) {
         if (oldCount == 0) {
            if (newCount > 0) {
               this.add(element, newCount);
            }

            return true;
         } else {
            return false;
         }
      } else {
         int[] result = new int[1];
         TreeMultiset.AvlNode<E> newRoot = root.setCount(this.comparator(), element, oldCount, newCount, result);
         this.rootReference.checkAndSet(root, newRoot);
         return result[0] == oldCount;
      }
   }

   @Override
   public void clear() {
      if (!this.range.hasLowerBound() && !this.range.hasUpperBound()) {
         TreeMultiset.AvlNode<E> current = this.header.succ();

         while (current != this.header) {
            TreeMultiset.AvlNode<E> next = current.succ();
            current.elemCount = 0;
            current.left = null;
            current.right = null;
            current.pred = null;
            current.succ = null;
            current = next;
         }

         successor(this.header, this.header);
         this.rootReference.clear();
      } else {
         Iterators.clear(this.entryIterator());
      }
   }

   private Multiset.Entry<E> wrapEntry(TreeMultiset.AvlNode<E> baseEntry) {
      return new Multisets.AbstractEntry<E>() {
         @ParametricNullness
         @Override
         public E getElement() {
            return baseEntry.getElement();
         }

         @Override
         public int getCount() {
            int result = baseEntry.getCount();
            return result == 0 ? TreeMultiset.this.count(this.getElement()) : result;
         }
      };
   }

   private TreeMultiset.@Nullable AvlNode<E> firstNode() {
      TreeMultiset.AvlNode<E> root = this.rootReference.get();
      if (root == null) {
         return null;
      }

      TreeMultiset.AvlNode<E> node;
      if (this.range.hasLowerBound()) {
         E endpoint = NullnessCasts.uncheckedCastNullableTToT(this.range.getLowerEndpoint());
         node = root.ceiling(this.comparator(), endpoint);
         if (node == null) {
            return null;
         }

         if (this.range.getLowerBoundType() == BoundType.OPEN && this.comparator().compare(endpoint, node.getElement()) == 0) {
            node = node.succ();
         }
      } else {
         node = this.header.succ();
      }

      return node != this.header && this.range.contains(node.getElement()) ? node : null;
   }

   private TreeMultiset.@Nullable AvlNode<E> lastNode() {
      TreeMultiset.AvlNode<E> root = this.rootReference.get();
      if (root == null) {
         return null;
      }

      TreeMultiset.AvlNode<E> node;
      if (this.range.hasUpperBound()) {
         E endpoint = NullnessCasts.uncheckedCastNullableTToT(this.range.getUpperEndpoint());
         node = root.floor(this.comparator(), endpoint);
         if (node == null) {
            return null;
         }

         if (this.range.getUpperBoundType() == BoundType.OPEN && this.comparator().compare(endpoint, node.getElement()) == 0) {
            node = node.pred();
         }
      } else {
         node = this.header.pred();
      }

      return node != this.header && this.range.contains(node.getElement()) ? node : null;
   }

   @Override
   Iterator<E> elementIterator() {
      return Multisets.elementIterator(this.entryIterator());
   }

   @Override
   Iterator<Multiset.Entry<E>> entryIterator() {
      return new Iterator<Multiset.Entry<E>>() {
         TreeMultiset.@Nullable AvlNode<E> current = TreeMultiset.this.firstNode();
         Multiset.@Nullable Entry<E> prevEntry;

         @Override
         public boolean hasNext() {
            if (this.current == null) {
               return false;
            } else if (TreeMultiset.this.range.tooHigh(this.current.getElement())) {
               this.current = null;
               return false;
            } else {
               return true;
            }
         }

         public Multiset.Entry<E> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            Multiset.Entry<E> result = TreeMultiset.this.wrapEntry(Objects.requireNonNull(this.current));
            this.prevEntry = result;
            if (this.current.succ() == TreeMultiset.this.header) {
               this.current = null;
            } else {
               this.current = this.current.succ();
            }

            return result;
         }

         @Override
         public void remove() {
            Preconditions.checkState(this.prevEntry != null, "no calls to next() since the last call to remove()");
            TreeMultiset.this.setCount(this.prevEntry.getElement(), 0);
            this.prevEntry = null;
         }
      };
   }

   @Override
   Iterator<Multiset.Entry<E>> descendingEntryIterator() {
      return new Iterator<Multiset.Entry<E>>() {
         TreeMultiset.@Nullable AvlNode<E> current = TreeMultiset.this.lastNode();
         Multiset.@Nullable Entry<E> prevEntry = null;

         @Override
         public boolean hasNext() {
            if (this.current == null) {
               return false;
            } else if (TreeMultiset.this.range.tooLow(this.current.getElement())) {
               this.current = null;
               return false;
            } else {
               return true;
            }
         }

         public Multiset.Entry<E> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            }

            Objects.requireNonNull(this.current);
            Multiset.Entry<E> result = TreeMultiset.this.wrapEntry(this.current);
            this.prevEntry = result;
            if (this.current.pred() == TreeMultiset.this.header) {
               this.current = null;
            } else {
               this.current = this.current.pred();
            }

            return result;
         }

         @Override
         public void remove() {
            Preconditions.checkState(this.prevEntry != null, "no calls to next() since the last call to remove()");
            TreeMultiset.this.setCount(this.prevEntry.getElement(), 0);
            this.prevEntry = null;
         }
      };
   }

   @Override
   public void forEachEntry(ObjIntConsumer<? super E> action) {
      Preconditions.checkNotNull(action);

      for (TreeMultiset.AvlNode<E> node = this.firstNode(); node != this.header && node != null && !this.range.tooHigh(node.getElement()); node = node.succ()) {
         action.accept(node.getElement(), node.getCount());
      }
   }

   @Override
   public Iterator<E> iterator() {
      return Multisets.iteratorImpl(this);
   }

   @Override
   public SortedMultiset<E> headMultiset(@ParametricNullness E upperBound, BoundType boundType) {
      return new TreeMultiset<>(this.rootReference, this.range.intersect(GeneralRange.upTo(this.comparator(), upperBound, boundType)), this.header);
   }

   @Override
   public SortedMultiset<E> tailMultiset(@ParametricNullness E lowerBound, BoundType boundType) {
      return new TreeMultiset<>(this.rootReference, this.range.intersect(GeneralRange.downTo(this.comparator(), lowerBound, boundType)), this.header);
   }

   private static <T> void successor(TreeMultiset.AvlNode<T> a, TreeMultiset.AvlNode<T> b) {
      a.succ = b;
      b.pred = a;
   }

   private static <T> void successor(TreeMultiset.AvlNode<T> a, TreeMultiset.AvlNode<T> b, TreeMultiset.AvlNode<T> c) {
      successor(a, b);
      successor(b, c);
   }

   @J2ktIncompatible
   @GwtIncompatible
   private void writeObject(ObjectOutputStream stream) throws IOException {
      stream.defaultWriteObject();
      stream.writeObject(this.elementSet().comparator());
      Serialization.writeMultiset(this, stream);
   }

   @J2ktIncompatible
   @GwtIncompatible
   private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
      stream.defaultReadObject();
      Comparator<? super E> comparator = Objects.requireNonNull((Comparator<? super E>)stream.readObject());
      Serialization.getFieldSetter(AbstractSortedMultiset.class, "comparator").set(this, comparator);
      Serialization.getFieldSetter(TreeMultiset.class, "range").set(this, GeneralRange.all(comparator));
      Serialization.getFieldSetter(TreeMultiset.class, "rootReference").set(this, new TreeMultiset.Reference());
      TreeMultiset.AvlNode<E> header = new TreeMultiset.AvlNode<>();
      Serialization.getFieldSetter(TreeMultiset.class, "header").set(this, header);
      successor(header, header);
      Serialization.populateMultiset(this, stream);
   }

   private enum Aggregate {
      SIZE {
         @Override
         int nodeAggregate(TreeMultiset.AvlNode<?> node) {
            return node.elemCount;
         }

         @Override
         long treeAggregate(TreeMultiset.@Nullable AvlNode<?> root) {
            return root == null ? 0L : root.totalCount;
         }
      },
      DISTINCT {
         @Override
         int nodeAggregate(TreeMultiset.AvlNode<?> node) {
            return 1;
         }

         @Override
         long treeAggregate(TreeMultiset.@Nullable AvlNode<?> root) {
            return root == null ? 0L : root.distinctElements;
         }
      };

      Aggregate() {
      }

      abstract int nodeAggregate(TreeMultiset.AvlNode<?> node);

      abstract long treeAggregate(TreeMultiset.@Nullable AvlNode<?> root);
   }

   private static final class AvlNode<E> {
      private final @Nullable E elem;
      private int elemCount;
      private int distinctElements;
      private long totalCount;
      private int height;
      private TreeMultiset.@Nullable AvlNode<E> left;
      private TreeMultiset.@Nullable AvlNode<E> right;
      private TreeMultiset.@Nullable AvlNode<E> pred;
      private TreeMultiset.@Nullable AvlNode<E> succ;

      AvlNode(@ParametricNullness E elem, int elemCount) {
         Preconditions.checkArgument(elemCount > 0);
         this.elem = elem;
         this.elemCount = elemCount;
         this.totalCount = elemCount;
         this.distinctElements = 1;
         this.height = 1;
         this.left = null;
         this.right = null;
      }

      AvlNode() {
         this.elem = null;
         this.elemCount = 1;
      }

      private TreeMultiset.AvlNode<E> pred() {
         return Objects.requireNonNull(this.pred);
      }

      private TreeMultiset.AvlNode<E> succ() {
         return Objects.requireNonNull(this.succ);
      }

      int count(Comparator<? super E> comparator, @ParametricNullness E e) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp < 0) {
            return this.left == null ? 0 : this.left.count(comparator, e);
         } else if (cmp > 0) {
            return this.right == null ? 0 : this.right.count(comparator, e);
         } else {
            return this.elemCount;
         }
      }

      @CanIgnoreReturnValue
      private TreeMultiset.AvlNode<E> addRightChild(@ParametricNullness E e, int count) {
         this.right = new TreeMultiset.AvlNode<>(e, count);
         TreeMultiset.successor(this, this.right, this.succ());
         this.height = Math.max(2, this.height);
         this.distinctElements++;
         this.totalCount += count;
         return this;
      }

      @CanIgnoreReturnValue
      private TreeMultiset.AvlNode<E> addLeftChild(@ParametricNullness E e, int count) {
         this.left = new TreeMultiset.AvlNode<>(e, count);
         TreeMultiset.successor(this.pred(), this.left, this);
         this.height = Math.max(2, this.height);
         this.distinctElements++;
         this.totalCount += count;
         return this;
      }

      TreeMultiset.AvlNode<E> add(Comparator<? super E> comparator, @ParametricNullness E e, int count, int[] result) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp < 0) {
            TreeMultiset.AvlNode<E> initLeft = this.left;
            if (initLeft == null) {
               result[0] = 0;
               return this.addLeftChild(e, count);
            }

            int initHeight = initLeft.height;
            this.left = initLeft.add(comparator, e, count, result);
            if (result[0] == 0) {
               this.distinctElements++;
            }

            this.totalCount += count;
            return this.left.height == initHeight ? this : this.rebalance();
         } else if (cmp > 0) {
            TreeMultiset.AvlNode<E> initRight = this.right;
            if (initRight == null) {
               result[0] = 0;
               return this.addRightChild(e, count);
            }

            int initHeight = initRight.height;
            this.right = initRight.add(comparator, e, count, result);
            if (result[0] == 0) {
               this.distinctElements++;
            }

            this.totalCount += count;
            return this.right.height == initHeight ? this : this.rebalance();
         } else {
            result[0] = this.elemCount;
            long resultCount = (long)this.elemCount + count;
            Preconditions.checkArgument(resultCount <= 2147483647L);
            this.elemCount += count;
            this.totalCount += count;
            return this;
         }
      }

      TreeMultiset.@Nullable AvlNode<E> remove(Comparator<? super E> comparator, @ParametricNullness E e, int count, int[] result) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp < 0) {
            TreeMultiset.AvlNode<E> initLeft = this.left;
            if (initLeft == null) {
               result[0] = 0;
               return this;
            }

            this.left = initLeft.remove(comparator, e, count, result);
            if (result[0] > 0) {
               if (count >= result[0]) {
                  this.distinctElements--;
                  this.totalCount = this.totalCount - result[0];
               } else {
                  this.totalCount -= count;
               }
            }

            return result[0] == 0 ? this : this.rebalance();
         } else if (cmp > 0) {
            TreeMultiset.AvlNode<E> initRight = this.right;
            if (initRight == null) {
               result[0] = 0;
               return this;
            }

            this.right = initRight.remove(comparator, e, count, result);
            if (result[0] > 0) {
               if (count >= result[0]) {
                  this.distinctElements--;
                  this.totalCount = this.totalCount - result[0];
               } else {
                  this.totalCount -= count;
               }
            }

            return this.rebalance();
         } else {
            result[0] = this.elemCount;
            if (count >= this.elemCount) {
               return this.deleteMe();
            }

            this.elemCount -= count;
            this.totalCount -= count;
            return this;
         }
      }

      TreeMultiset.@Nullable AvlNode<E> setCount(Comparator<? super E> comparator, @ParametricNullness E e, int count, int[] result) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp < 0) {
            TreeMultiset.AvlNode<E> initLeft = this.left;
            if (initLeft == null) {
               result[0] = 0;
               return count > 0 ? this.addLeftChild(e, count) : this;
            }

            this.left = initLeft.setCount(comparator, e, count, result);
            if (count == 0 && result[0] != 0) {
               this.distinctElements--;
            } else if (count > 0 && result[0] == 0) {
               this.distinctElements++;
            }

            this.totalCount = this.totalCount + (count - result[0]);
            return this.rebalance();
         } else if (cmp > 0) {
            TreeMultiset.AvlNode<E> initRight = this.right;
            if (initRight == null) {
               result[0] = 0;
               return count > 0 ? this.addRightChild(e, count) : this;
            }

            this.right = initRight.setCount(comparator, e, count, result);
            if (count == 0 && result[0] != 0) {
               this.distinctElements--;
            } else if (count > 0 && result[0] == 0) {
               this.distinctElements++;
            }

            this.totalCount = this.totalCount + (count - result[0]);
            return this.rebalance();
         } else {
            result[0] = this.elemCount;
            if (count == 0) {
               return this.deleteMe();
            }

            this.totalCount = this.totalCount + (count - this.elemCount);
            this.elemCount = count;
            return this;
         }
      }

      TreeMultiset.@Nullable AvlNode<E> setCount(Comparator<? super E> comparator, @ParametricNullness E e, int expectedCount, int newCount, int[] result) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp < 0) {
            TreeMultiset.AvlNode<E> initLeft = this.left;
            if (initLeft == null) {
               result[0] = 0;
               return expectedCount == 0 && newCount > 0 ? this.addLeftChild(e, newCount) : this;
            }

            this.left = initLeft.setCount(comparator, e, expectedCount, newCount, result);
            if (result[0] == expectedCount) {
               if (newCount == 0 && result[0] != 0) {
                  this.distinctElements--;
               } else if (newCount > 0 && result[0] == 0) {
                  this.distinctElements++;
               }

               this.totalCount = this.totalCount + (newCount - result[0]);
            }

            return this.rebalance();
         } else if (cmp > 0) {
            TreeMultiset.AvlNode<E> initRight = this.right;
            if (initRight == null) {
               result[0] = 0;
               return expectedCount == 0 && newCount > 0 ? this.addRightChild(e, newCount) : this;
            }

            this.right = initRight.setCount(comparator, e, expectedCount, newCount, result);
            if (result[0] == expectedCount) {
               if (newCount == 0 && result[0] != 0) {
                  this.distinctElements--;
               } else if (newCount > 0 && result[0] == 0) {
                  this.distinctElements++;
               }

               this.totalCount = this.totalCount + (newCount - result[0]);
            }

            return this.rebalance();
         } else {
            result[0] = this.elemCount;
            if (expectedCount == this.elemCount) {
               if (newCount == 0) {
                  return this.deleteMe();
               }

               this.totalCount = this.totalCount + (newCount - this.elemCount);
               this.elemCount = newCount;
            }

            return this;
         }
      }

      private TreeMultiset.@Nullable AvlNode<E> deleteMe() {
         int oldElemCount = this.elemCount;
         this.elemCount = 0;
         TreeMultiset.successor(this.pred(), this.succ());
         if (this.left == null) {
            return this.right;
         } else if (this.right == null) {
            return this.left;
         } else if (this.left.height >= this.right.height) {
            TreeMultiset.AvlNode<E> newTop = this.pred();
            newTop.left = this.left.removeMax(newTop);
            newTop.right = this.right;
            newTop.distinctElements = this.distinctElements - 1;
            newTop.totalCount = this.totalCount - oldElemCount;
            return newTop.rebalance();
         } else {
            TreeMultiset.AvlNode<E> newTop = this.succ();
            newTop.right = this.right.removeMin(newTop);
            newTop.left = this.left;
            newTop.distinctElements = this.distinctElements - 1;
            newTop.totalCount = this.totalCount - oldElemCount;
            return newTop.rebalance();
         }
      }

      private TreeMultiset.@Nullable AvlNode<E> removeMin(TreeMultiset.AvlNode<E> node) {
         if (this.left == null) {
            return this.right;
         }

         this.left = this.left.removeMin(node);
         this.distinctElements--;
         this.totalCount = this.totalCount - node.elemCount;
         return this.rebalance();
      }

      private TreeMultiset.@Nullable AvlNode<E> removeMax(TreeMultiset.AvlNode<E> node) {
         if (this.right == null) {
            return this.left;
         }

         this.right = this.right.removeMax(node);
         this.distinctElements--;
         this.totalCount = this.totalCount - node.elemCount;
         return this.rebalance();
      }

      private void recomputeMultiset() {
         this.distinctElements = 1 + TreeMultiset.distinctElements(this.left) + TreeMultiset.distinctElements(this.right);
         this.totalCount = this.elemCount + totalCount(this.left) + totalCount(this.right);
      }

      private void recomputeHeight() {
         this.height = 1 + Math.max(height(this.left), height(this.right));
      }

      private void recompute() {
         this.recomputeMultiset();
         this.recomputeHeight();
      }

      private TreeMultiset.AvlNode<E> rebalance() {
         switch (this.balanceFactor()) {
            case -2:
               Objects.requireNonNull(this.right);
               if (this.right.balanceFactor() > 0) {
                  this.right = this.right.rotateRight();
               }

               return this.rotateLeft();
            case 2:
               Objects.requireNonNull(this.left);
               if (this.left.balanceFactor() < 0) {
                  this.left = this.left.rotateLeft();
               }

               return this.rotateRight();
            default:
               this.recomputeHeight();
               return this;
         }
      }

      private int balanceFactor() {
         return height(this.left) - height(this.right);
      }

      private TreeMultiset.AvlNode<E> rotateLeft() {
         Preconditions.checkState(this.right != null);
         TreeMultiset.AvlNode<E> newTop = this.right;
         this.right = newTop.left;
         newTop.left = this;
         newTop.totalCount = this.totalCount;
         newTop.distinctElements = this.distinctElements;
         this.recompute();
         newTop.recomputeHeight();
         return newTop;
      }

      private TreeMultiset.AvlNode<E> rotateRight() {
         Preconditions.checkState(this.left != null);
         TreeMultiset.AvlNode<E> newTop = this.left;
         this.left = newTop.right;
         newTop.right = this;
         newTop.totalCount = this.totalCount;
         newTop.distinctElements = this.distinctElements;
         this.recompute();
         newTop.recomputeHeight();
         return newTop;
      }

      private static long totalCount(TreeMultiset.@Nullable AvlNode<?> node) {
         return node == null ? 0L : node.totalCount;
      }

      private static int height(TreeMultiset.@Nullable AvlNode<?> node) {
         return node == null ? 0 : node.height;
      }

      private TreeMultiset.@Nullable AvlNode<E> ceiling(Comparator<? super E> comparator, @ParametricNullness E e) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp < 0) {
            return this.left == null ? this : MoreObjects.firstNonNull(this.left.ceiling(comparator, e), this);
         } else if (cmp == 0) {
            return this;
         } else {
            return this.right == null ? null : this.right.ceiling(comparator, e);
         }
      }

      private TreeMultiset.@Nullable AvlNode<E> floor(Comparator<? super E> comparator, @ParametricNullness E e) {
         int cmp = comparator.compare(e, this.getElement());
         if (cmp > 0) {
            return this.right == null ? this : MoreObjects.firstNonNull(this.right.floor(comparator, e), this);
         } else if (cmp == 0) {
            return this;
         } else {
            return this.left == null ? null : this.left.floor(comparator, e);
         }
      }

      @ParametricNullness
      E getElement() {
         return NullnessCasts.uncheckedCastNullableTToT(this.elem);
      }

      int getCount() {
         return this.elemCount;
      }

      @Override
      public String toString() {
         return Multisets.immutableEntry(this.getElement(), this.getCount()).toString();
      }
   }

   private static final class Reference<T> {
      private @Nullable T value;

      private Reference() {
      }

      public @Nullable T get() {
         return this.value;
      }

      public void checkAndSet(@Nullable T expected, @Nullable T newValue) {
         if (this.value != expected) {
            throw new ConcurrentModificationException();
         }

         this.value = newValue;
      }

      void clear() {
         this.value = null;
      }
   }
}
