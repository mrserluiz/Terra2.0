package com.dfsek.terra.lib.commons.text.diff;

public class KeepCommand<T> extends EditCommand<T> {
   public KeepCommand(T object) {
      super(object);
   }

   @Override
   public void accept(CommandVisitor<T> visitor) {
      visitor.visitKeepCommand(this.getObject());
   }
}
