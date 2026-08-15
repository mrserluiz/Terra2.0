package com.dfsek.paralithic.eval.tokenizer;

public class Char implements Position {
   private final char value;
   private final int line;
   private final int pos;

   public Char(char value, int line, int pos) {
      this.value = value;
      this.line = line;
      this.pos = pos;
   }

   public char getValue() {
      return this.value;
   }

   @Override
   public int getLine() {
      return this.line;
   }

   @Override
   public int getPos() {
      return this.pos;
   }

   public boolean isDigit() {
      return Character.isDigit(this.value);
   }

   public boolean isLetter() {
      return Character.isLetter(this.value);
   }

   public boolean isWhitespace() {
      return Character.isWhitespace(this.value) && !this.isEndOfInput();
   }

   public boolean isEndOfInput() {
      return this.value == 0;
   }

   public boolean isNewLine() {
      return this.value == '\n';
   }

   @Override
   public String toString() {
      return this.isEndOfInput() ? "<End Of Input>" : String.valueOf(this.value);
   }

   public boolean is(char... tests) {
      for (char test : tests) {
         if (test == this.value && test != 0) {
            return true;
         }
      }

      return false;
   }

   public String getStringValue() {
      return this.isEndOfInput() ? "" : String.valueOf(this.value);
   }
}
