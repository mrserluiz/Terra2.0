package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.Beta;
import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Function;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

@Deprecated
@Beta
@GwtCompatible
public abstract class TreeTraverser<T> {
   @Deprecated
   public static <T> TreeTraverser<T> using(Function<T, ? extends Iterable<T>> nodeToChildrenFunction) {
      Preconditions.checkNotNull(nodeToChildrenFunction);
      return new TreeTraverser<T>() {
         @Override
         public Iterable<T> children(T root) {
            return (Iterable<T>)nodeToChildrenFunction.apply(root);
         }
      };
   }

   public abstract Iterable<T> children(T root);

   @Deprecated
   public final FluentIterable<T> preOrderTraversal(T root) {
      Preconditions.checkNotNull(root);
      return new FluentIterable<T>() {
         public UnmodifiableIterator<T> iterator() {
            return TreeTraverser.this.preOrderIterator(root);
         }

         @Override
         public void forEach(Consumer<? super T> action) {
            Preconditions.checkNotNull(action);
            (new Consumer<T>() {
               @Override
               public void accept(T t) {
                  action.accept(t);
                  TreeTraverser.this.children(t).forEach(this);
               }
            }).accept(root);
         }
      };
   }

   UnmodifiableIterator<T> preOrderIterator(T root) {
      return new TreeTraverser.PreOrderIterator(root);
   }

   @Deprecated
   public final FluentIterable<T> postOrderTraversal(T root) {
      Preconditions.checkNotNull(root);
      return new FluentIterable<T>() {
         public UnmodifiableIterator<T> iterator() {
            return TreeTraverser.this.postOrderIterator(root);
         }

         @Override
         public void forEach(Consumer<? super T> action) {
            Preconditions.checkNotNull(action);
            (new Consumer<T>() {
               @Override
               public void accept(T t) {
                  TreeTraverser.this.children(t).forEach(this);
                  action.accept(t);
               }
            }).accept(root);
         }
      };
   }

   UnmodifiableIterator<T> postOrderIterator(T root) {
      return new TreeTraverser.PostOrderIterator(root);
   }

   @Deprecated
   public final FluentIterable<T> breadthFirstTraversal(T root) {
      Preconditions.checkNotNull(root);
      return new FluentIterable<T>() {
         public UnmodifiableIterator<T> iterator() {
            return TreeTraverser.this.new BreadthFirstIterator(root);
         }
      };
   }

   private final class BreadthFirstIterator extends UnmodifiableIterator<T> implements PeekingIterator<T> {
      private final Queue<T> queue = new ArrayDeque<>();

      BreadthFirstIterator(T root) {
         this.queue.add(root);
      }

      @Override
      public boolean hasNext() {
         return !this.queue.isEmpty();
      }

      @Override
      public T peek() {
         return this.queue.element();
      }

      @Override
      public T next() {
         T result = this.queue.remove();
         Iterables.addAll(this.queue, TreeTraverser.this.children(result));
         return result;
      }
   }

   private final class PostOrderIterator extends AbstractIterator<T> {
      private final ArrayDeque<TreeTraverser.PostOrderNode<T>> stack = new ArrayDeque<>();

      PostOrderIterator(T root) {
         this.stack.addLast(this.expand(root));
      }

      @Override
      protected @Nullable T computeNext() {
         while (!this.stack.isEmpty()) {
            TreeTraverser.PostOrderNode<T> top = this.stack.getLast();
            if (!top.childIterator.hasNext()) {
               this.stack.removeLast();
               return top.root;
            }

            T child = top.childIterator.next();
            this.stack.addLast(this.expand(child));
         }

         return (T)this.endOfData();
      }

      private TreeTraverser.PostOrderNode<T> expand(T t) {
         return new TreeTraverser.PostOrderNode<>(t, TreeTraverser.this.children(t).iterator());
      }
   }

   private static final class PostOrderNode<T> {
      final T root;
      final Iterator<T> childIterator;

      PostOrderNode(T root, Iterator<T> childIterator) {
         this.root = Preconditions.checkNotNull(root);
         this.childIterator = Preconditions.checkNotNull(childIterator);
      }
   }

   private final class PreOrderIterator extends UnmodifiableIterator<T> {
      private final Deque<Iterator<T>> stack = new ArrayDeque<>();

      PreOrderIterator(T root) {
         this.stack.addLast(Iterators.singletonIterator(Preconditions.checkNotNull(root)));
      }

      @Override
      public boolean hasNext() {
         return !this.stack.isEmpty();
      }

      @Override
      public T next() {
         Iterator<T> itr = this.stack.getLast();
         T result = Preconditions.checkNotNull(itr.next());
         if (!itr.hasNext()) {
            this.stack.removeLast();
         }

         Iterator<T> childItr = TreeTraverser.this.children(result).iterator();
         if (childItr.hasNext()) {
            this.stack.addLast(childItr);
         }

         return result;
      }
   }
}
