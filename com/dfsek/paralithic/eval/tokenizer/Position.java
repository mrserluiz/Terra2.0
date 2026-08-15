package com.dfsek.paralithic.eval.tokenizer;

public interface Position {
   Position UNKNOWN = new Position() {
      @Override
      public int getLine() {
         return 0;
      }

      @Override
      public int getPos() {
         return 0;
      }
   };

   int getLine();

   int getPos();
}
