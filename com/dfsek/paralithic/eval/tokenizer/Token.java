package com.dfsek.paralithic.eval.tokenizer;

import java.util.Objects;
import java.util.stream.Stream;

public class Token implements Position {
   protected int pos;
   private Token.TokenType type;
   private String trigger = "";
   private String internTrigger;
   private String contents = "";
   private String source = "";
   private int line;

   private Token() {
   }

   public static Token create(Token.TokenType type, Position pos) {
      Token result = new Token();
      result.type = type;
      result.line = pos.getLine();
      result.pos = pos.getPos();
      return result;
   }

   public static Token createAndFill(Token.TokenType type, Char ch) {
      Token result = new Token();
      result.type = type;
      result.line = ch.getLine();
      result.pos = ch.getPos();
      result.contents = ch.getStringValue();
      result.trigger = ch.getStringValue();
      result.source = ch.toString();
      return result;
   }

   public Token addToTrigger(Char ch) {
      this.trigger = this.trigger + ch.getValue();
      this.internTrigger = null;
      this.source = this.source + ch.getValue();
      return this;
   }

   public Token addToSource(Char ch) {
      this.source = this.source + ch.getValue();
      return this;
   }

   public Token addToContent(Char ch) {
      return this.addToContent(ch.getValue());
   }

   public Token addToContent(char ch) {
      this.contents = this.contents + ch;
      this.source = this.source + ch;
      return this;
   }

   public Token silentAddToContent(char ch) {
      this.contents = this.contents + ch;
      return this;
   }

   @Override
   public int getLine() {
      return this.line;
   }

   @Override
   public int getPos() {
      return this.pos;
   }

   public void setContent(String content) {
      this.contents = content;
   }

   public boolean isEnd() {
      return this.type == Token.TokenType.EOI;
   }

   public boolean isNotEnd() {
      return this.type != Token.TokenType.EOI;
   }

   public boolean wasTriggeredBy(String... triggers) {
      return Stream.of(triggers).filter(Objects::nonNull).anyMatch(trigger -> Objects.equals(trigger, this.getTrigger()));
   }

   public String getTrigger() {
      if (this.internTrigger == null) {
         this.internTrigger = this.trigger.intern();
      }

      return this.internTrigger;
   }

   public void setTrigger(String trigger) {
      this.trigger = trigger;
      this.internTrigger = null;
   }

   public boolean hasContent(String content) {
      if (content == null) {
         throw new IllegalArgumentException("content must not be null");
      } else {
         return content.equalsIgnoreCase(this.contents);
      }
   }

   public String getContents() {
      return this.contents;
   }

   public boolean isSymbol(String... symbols) {
      if (symbols.length == 0) {
         return this.is(Token.TokenType.SYMBOL);
      }

      for (String symbol : symbols) {
         if (this.matches(Token.TokenType.SYMBOL, symbol)) {
            return true;
         }
      }

      return false;
   }

   public boolean is(Token.TokenType type) {
      return this.type == type;
   }

   public boolean matches(Token.TokenType type, String trigger) {
      if (!this.is(type)) {
         return false;
      } else if (trigger == null) {
         throw new IllegalArgumentException("trigger must not be null");
      } else {
         return Objects.equals(this.getTrigger(), trigger.intern());
      }
   }

   public boolean isKeyword(String... keywords) {
      if (keywords.length == 0) {
         return this.is(Token.TokenType.KEYWORD);
      }

      for (String keyword : keywords) {
         if (this.matches(Token.TokenType.KEYWORD, keyword)) {
            return true;
         }
      }

      return false;
   }

   public boolean isIdentifier(String... values) {
      if (values.length == 0) {
         return this.is(Token.TokenType.ID);
      }

      for (String value : values) {
         if (this.matches(Token.TokenType.ID, value)) {
            return true;
         }
      }

      return false;
   }

   public boolean isSpecialIdentifier(String... triggers) {
      if (triggers.length == 0) {
         return this.is(Token.TokenType.SPECIAL_ID);
      }

      for (String possibleTrigger : triggers) {
         if (this.matches(Token.TokenType.SPECIAL_ID, possibleTrigger)) {
            return true;
         }
      }

      return false;
   }

   public boolean isSpecialIdentifierWithContent(String trigger, String... contents) {
      if (!this.matches(Token.TokenType.SPECIAL_ID, trigger)) {
         return false;
      }

      if (contents.length == 0) {
         return true;
      }

      for (String content : contents) {
         if (content != null && content.equals(this.contents)) {
            return true;
         }
      }

      return false;
   }

   public boolean isNumber() {
      return this.isInteger() || this.isDecimal() || this.isScientificDecimal();
   }

   public boolean isInteger() {
      return this.is(Token.TokenType.INTEGER);
   }

   public boolean isDecimal() {
      return this.is(Token.TokenType.DECIMAL);
   }

   public boolean isScientificDecimal() {
      return this.is(Token.TokenType.SCIENTIFIC_DECIMAL);
   }

   public boolean isString() {
      return this.is(Token.TokenType.STRING);
   }

   @Override
   public String toString() {
      return this.type.toString() + ":" + this.source + " (" + this.line + ":" + this.pos + ")";
   }

   public Token.TokenType getType() {
      return this.type;
   }

   public String getSource() {
      return this.source;
   }

   public void setSource(String source) {
      this.source = source;
   }

   public enum TokenType {
      ID,
      SPECIAL_ID,
      STRING,
      DECIMAL,
      SCIENTIFIC_DECIMAL,
      INTEGER,
      SYMBOL,
      KEYWORD,
      EOI;
   }
}
