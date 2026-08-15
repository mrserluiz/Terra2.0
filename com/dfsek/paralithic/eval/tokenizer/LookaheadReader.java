package com.dfsek.paralithic.eval.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LookaheadReader extends Lookahead<Char> {
   private final Reader input;
   private int line = 1;
   private int pos = 0;

   public LookaheadReader(Reader input) {
      if (input == null) {
         throw new IllegalArgumentException("input must not be null");
      }

      this.input = new BufferedReader(input);
   }

   protected Char fetch() {
      try {
         int character = this.input.read();
         if (character == -1) {
            return null;
         }

         Char result = new Char((char)character, this.line, this.pos++);
         if (character == 10) {
            this.line++;
            this.pos = 0;
         }

         return result;
      } catch (IOException e) {
         this.problemCollector.add(ParseError.error(new Char('\u0000', this.line, this.pos), e.getMessage()));
         return null;
      }
   }

   protected Char endOfInput() {
      return new Char('\u0000', this.line, this.pos);
   }

   @Override
   public String toString() {
      if (this.itemBuffer.isEmpty()) {
         return this.line + ":" + this.pos + ": Buffer empty";
      } else {
         return this.itemBuffer.size() < 2
            ? this.line + ":" + this.pos + ": " + this.current()
            : this.line + ":" + this.pos + ": " + this.current() + ", " + this.next();
      }
   }
}
