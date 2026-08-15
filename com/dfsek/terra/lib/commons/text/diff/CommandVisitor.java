package com.dfsek.terra.lib.commons.text.diff;

public interface CommandVisitor<T> {
   void visitDeleteCommand(T var1);

   void visitInsertCommand(T var1);

   void visitKeepCommand(T var1);
}
