package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import java.util.Queue;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class ConsumingQueueIterator<T> extends AbstractIterator<T> {
   private final Queue<T> queue;

   ConsumingQueueIterator(Queue<T> queue) {
      this.queue = Preconditions.checkNotNull(queue);
   }

   @Override
   protected @Nullable T computeNext() {
      return this.queue.isEmpty() ? this.endOfData() : this.queue.remove();
   }
}
