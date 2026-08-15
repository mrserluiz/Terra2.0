package ca.solostudios.strata.parser.tokenizer;

import org.jetbrains.annotations.NotNull;

public class Char implements Position {
   private final char value;
   private final int pos;

   public Char(char value, int pos) {
      this.value = value;
      this.pos = pos;
   }

   public boolean is(char test) {
      return test == this.value && test != 0;
   }

   public boolean is(char... tests) {
      for (char test : tests) {
         if (test == this.value && test != 0) {
            return true;
         }
      }

      return false;
   }

   public char getValue() {
      return this.value;
   }

   public boolean isAlphaNumeric() {
      return this.isLetter() || this.isDigit() || this.is('-');
   }

   public boolean isDigit() {
      return this.value >= '0' && this.value <= '9';
   }

   public boolean isLetter() {
      return this.value >= 'a' && this.value <= 'z' || this.value >= 'A' && this.value <= 'Z';
   }

   public boolean isEndOfInput() {
      return this.value == 0;
   }

   @NotNull
   public String getStringValue() {
      return this.isEndOfInput() ? "" : String.valueOf(this.value);
   }

   @Override
   public String toString() {
      return this.isEndOfInput() ? "<EOI>" : String.valueOf(this.value);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Char aChar = (Char)o;
         return this.value != aChar.value ? false : this.pos == aChar.pos;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.value;
      return 31 * result + this.pos;
   }

   @Override
   public int getPos() {
      return this.pos;
   }
}
