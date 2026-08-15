package ca.solostudios.strata.parser.tokenizer;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Lookahead<T> {
   protected List<T> itemBuffer = new ArrayList<>();
   protected boolean endReached = false;
   protected T endOfInputIndicator = (T)null;

   @NotNull
   public T next() throws ParseException {
      return this.next(1);
   }

   @NotNull
   public T current() throws ParseException {
      return this.next(0);
   }

   @NotNull
   public T next(int offset) throws ParseException {
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

   @NotNull
   public T consume() throws ParseException {
      T result = this.current();
      this.consume(1);
      return result;
   }

   public void consume(int numberOfItems) throws ParseException {
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

   @NotNull
   protected abstract T endOfInput();

   @Nullable
   protected abstract T fetch() throws ParseException;
}
