package com.dfsek.terra.lib.commons.text.diff;

public class InsertCommand<T> extends EditCommand<T> {
   public InsertCommand(T object) {
      super(object);
   }

   @Override
   public void accept(CommandVisitor<T> visitor) {
      visitor.visitInsertCommand(this.getObject());
   }
}
