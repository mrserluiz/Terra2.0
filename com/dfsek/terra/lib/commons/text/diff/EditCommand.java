package com.dfsek.terra.lib.commons.text.diff;

public abstract class EditCommand<T> {
   private final T object;

   protected EditCommand(T object) {
      this.object = object;
   }

   public abstract void accept(CommandVisitor<T> var1);

   protected T getObject() {
      return this.object;
   }
}
