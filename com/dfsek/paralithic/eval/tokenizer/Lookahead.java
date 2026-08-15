package com.dfsek.paralithic.eval.tokenizer;

import java.util.ArrayList;
import java.util.List;

public abstract class Lookahead<T> {
   protected List<T> itemBuffer = new ArrayList<>();
   protected boolean endReached = false;
   protected List<ParseError> problemCollector = new ArrayList<>();
   protected T endOfInputIndicator;

   public T next() {
      return this.next(1);
   }

   public T current() {
      return this.next(0);
   }

   public T next(int offset) {
      if (offset < 0) {
         throw new IllegalArgumentException("offset < 0");
      }

      while (this.itemBuffer.size() <= offset && !this.endReached) {
         T item = this.fetch();
         if (item != null) {
            this.itemBuffer.add(item);
         } else {
            this.endReached = true;
         }
      }

      if (offset >= this.itemBuffer.size()) {
         if (this.endOfInputIndicator == null) {
            this.endOfInputIndicator = this.endOfInput();
         }

         return this.endOfInputIndicator;
      } else {
         return this.itemBuffer.get(offset);
      }
   }

   protected abstract T endOfInput();

   public T consume() {
      T result = this.current();
      this.consume(1);
      return result;
   }

   protected abstract T fetch();

   public void consume(int numberOfItems) {
      if (numberOfItems < 0) {
         throw new IllegalArgumentException("numberOfItems < 0");
      }

      while (numberOfItems-- > 0) {
         if (!this.itemBuffer.isEmpty()) {
            this.itemBuffer.remove(0);
         } else {
            if (this.endReached) {
               return;
            }

            T item = this.fetch();
            if (item == null) {
               this.endReached = true;
            }
         }
      }
   }

   public List<ParseError> getProblemCollector() {
      return this.problemCollector;
   }

   public void setProblemCollector(List<ParseError> problemCollector) {
      this.problemCollector = problemCollector;
   }
}
