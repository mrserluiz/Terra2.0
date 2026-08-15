package ca.solostudios.strata.parser.tokenizer;

public interface Position {
   Position UNKNOWN = () -> 0;

   int getPos();
}
