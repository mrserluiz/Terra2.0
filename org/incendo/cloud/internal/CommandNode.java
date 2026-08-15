package org.incendo.cloud.internal;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.key.SimpleMutableCloudKeyContainer;
import org.incendo.cloud.permission.Permission;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandNode<C> {
   public static final CloudKey<Set<Type>> META_KEY_SENDER_TYPES = CloudKey.cloudKey("senderTypes", new TypeToken<Set<Type>>() {});
   public static final CloudKey<Map<Type, Permission>> META_KEY_ACCESS = CloudKey.cloudKey("access", new TypeToken<Map<Type, Permission>>() {});
   private final SimpleMutableCloudKeyContainer nodeMeta = new SimpleMutableCloudKeyContainer(new HashMap<>());
   private final List<CommandNode<C>> children = new LinkedList<>();
   private final CommandComponent<C> component;
   private CommandNode<C> parent;
   private Command<C> command;

   public CommandNode(final @Nullable CommandComponent<C> component) {
      this.component = component;
   }

   public @NonNull List<@NonNull CommandNode<C>> children() {
      return Collections.unmodifiableList(this.children);
   }

   public @NonNull CommandNode<C> addChild(final @NonNull CommandComponent<C> component) {
      CommandNode<C> node = new CommandNode<>(component);
      this.children.add(node);
      return node;
   }

   public @Nullable CommandNode<C> getChild(final @NonNull CommandComponent<C> component) {
      for (CommandNode<C> child : this.children) {
         if (component.equals(child.component())) {
            return child;
         }
      }

      return null;
   }

   public boolean removeChild(final @NonNull CommandNode<C> child) {
      return this.children.remove(child);
   }

   public boolean isLeaf() {
      return this.children.isEmpty();
   }

   public @NonNull SimpleMutableCloudKeyContainer nodeMeta() {
      return this.nodeMeta;
   }

   public @MonotonicNonNull CommandComponent<C> component() {
      return this.component;
   }

   public @MonotonicNonNull Command<C> command() {
      return this.command;
   }

   public void command(final @NonNull Command<C> command) {
      if (this.command != null) {
         throw new IllegalStateException("Cannot replace owning command");
      }

      this.command = command;
   }

   public @Nullable CommandNode<C> parent() {
      return this.parent;
   }

   public void parent(final @Nullable CommandNode<C> parent) {
      this.parent = parent;
   }

   public void sortChildren() {
      this.children.sort(Comparator.comparing(CommandNode::component));
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         CommandNode<?> node = (CommandNode<?>)o;
         return Objects.equals(this.component(), node.component());
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.component());
   }

   @Override
   public String toString() {
      return "Node{value=" + this.component + '}';
   }
}
