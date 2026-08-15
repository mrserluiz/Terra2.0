package ca.solostudios.strata.parser.tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LookaheadReader extends Lookahead<Char> {
   @NotNull
   private final Reader input;
   private int pos = 0;

   public LookaheadReader(@NotNull Reader input) {
      this.input = new BufferedReader(input);
   }

   @Override
   public String toString() {
      if (this.itemBuffer.isEmpty()) {
         return String.format("%1d: Buffer empty", this.pos);
      }

      if (this.itemBuffer.size() < 2) {
         try {
            return String.format("%1d: %s", this.pos, this.current());
         } catch (ParseException e) {
            return String.format("%1d: Exception while fetching current.", this.pos);
         }
      } else {
         try {
            return String.format("%1d: %s, %s", this.pos, this.current(), this.next());
         } catch (ParseException e) {
            return String.format("%1d: Exception while fetching current or next.", this.pos);
         }
      }
   }

   @Nullable
   protected Char fetch() throws ParseException {
      try {
         int character = this.input.read();
         return character == -1 ? null : new Char((char)character, this.pos++);
      } catch (IOException e) {
         throw new ParseException(e, new Char('\u0000', this.pos));
      }
   }

   @NotNull
   protected Char endOfInput() {
      return new Char('\u0000', this.pos);
   }
}
