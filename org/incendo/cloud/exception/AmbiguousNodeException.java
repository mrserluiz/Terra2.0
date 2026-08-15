package org.incendo.cloud.exception;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.internal.CommandNode;

@API(status = Status.STABLE)
public final class AmbiguousNodeException extends IllegalStateException {
   private final CommandNode<?> parentNode;
   private final CommandNode<?> ambiguousNode;
   private final List<CommandNode<?>> children;

   @API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
   public AmbiguousNodeException(
      final @Nullable CommandNode<?> parentNode, final @NonNull CommandNode<?> ambiguousNode, final @NonNull List<@NonNull CommandNode<?>> children
   ) {
      this.parentNode = parentNode;
      this.ambiguousNode = ambiguousNode;
      this.children = children;
   }

   public @Nullable CommandNode<?> parentNode() {
      return this.parentNode;
   }

   public @NonNull CommandNode<?> ambiguousNode() {
      return this.ambiguousNode;
   }

   public @NonNull List<@NonNull CommandNode<?>> children() {
      return Collections.unmodifiableList(this.children);
   }

   @Override
   public String getMessage() {
      StringBuilder stringBuilder = new StringBuilder("Ambiguous Node: ")
         .append(this.ambiguousNode.component().name())
         .append(" cannot be added as a child to ")
         .append(this.parentNode == null ? "<root>" : this.parentNode.component().name())
         .append(" (All children: ");
      Iterator<CommandNode<?>> childIterator = this.children.iterator();

      while (childIterator.hasNext()) {
         stringBuilder.append(childIterator.next().component().name());
         if (childIterator.hasNext()) {
            stringBuilder.append(", ");
         }
      }

      return stringBuilder.append(")").toString();
   }
}
