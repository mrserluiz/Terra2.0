package com.dfsek.paralithic.eval.tokenizer;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tokenizer extends Lookahead<Token> {
   private static final char SCIENTIFIC_NOTATION_SEPARATOR = 'e';
   private static final char ALTERNATE_SCIENTIFIC_NOTATION_SEPARATOR = 'E';
   private static final char EFFECTIVE_SCIENTIFIC_NOTATION_SEPARATOR = 'e';
   private static final char[] BRACKETS = new char[]{'(', '[', '{', '}', ']', ')'};
   private static final boolean TREAT_SINGLE_PIPE_AS_BRACKET = true;
   private final Set<Character> specialIdStarters = new HashSet<>();
   private final Set<Character> specialIdTerminators = new HashSet<>();
   private final Map<String, String> keywords = new HashMap<>();
   private final Map<Character, Character> stringDelimiters = new IdentityHashMap<>();
   protected LookaheadReader input;
   private char decimalSeparator = '.';
   private char effectiveDecimalSeparator = '.';
   private char groupingSeparator = '_';
   private String lineComment = "//";
   private String blockCommentStart = "/*";
   private String blockCommentEnd = "*/";
   private boolean keywordsCaseSensitive = false;

   public Tokenizer(Reader input) {
      this.input = new LookaheadReader(input);
      this.input.setProblemCollector(this.problemCollector);
      this.addStringDelimiter('"', '\\');
      this.addStringDelimiter('\'', '\u0000');
   }

   public void addStringDelimiter(char stringDelimiter, char escapeCharacter) {
      this.stringDelimiters.put(stringDelimiter, escapeCharacter);
   }

   protected Token endOfInput() {
      return Token.createAndFill(Token.TokenType.EOI, this.input.current());
   }

   protected Token fetch() {
      while (this.input.current().isWhitespace()) {
         this.input.consume();
      }

      if (this.input.current().isEndOfInput()) {
         return null;
      }

      if (this.isAtStartOfLineComment(true)) {
         this.skipToEndOfLine();
         return this.fetch();
      }

      if (this.isAtStartOfBlockComment(true)) {
         this.skipBlockComment();
         return this.fetch();
      }

      if (this.isAtStartOfNumber()) {
         return this.fetchNumber();
      }

      if (this.isAtStartOfIdentifier()) {
         return this.fetchId();
      }

      if (this.stringDelimiters.containsKey(this.input.current().getValue())) {
         return this.fetchString();
      }

      if (this.isAtBracket(false)) {
         return Token.createAndFill(Token.TokenType.SYMBOL, this.input.consume());
      }

      if (this.isAtStartOfSpecialId()) {
         return this.fetchSpecialId();
      }

      if (this.isSymbolCharacter(this.input.current())) {
         return this.fetchSymbol();
      }

      this.problemCollector
         .add(ParseError.error(this.input.current(), String.format("Invalid character in input: '%s'", this.input.current().getStringValue())));
      this.input.consume();
      return this.fetch();
   }

   @Override
   public void setProblemCollector(List<ParseError> problemCollector) {
      super.setProblemCollector(problemCollector);
      this.input.setProblemCollector(problemCollector);
   }

   protected boolean isAtStartOfSpecialId() {
      return this.specialIdStarters.contains(this.input.current().getValue());
   }

   protected boolean isAtStartOfNumber() {
      return this.input.current().isDigit()
         || this.input.current().is('-') && this.input.next().isDigit()
         || this.input.current().is('-') && this.input.next().is('.') && this.input.next(2).isDigit()
         || this.input.current().is('.') && this.input.next().isDigit();
   }

   protected boolean isAtBracket(boolean inSymbol) {
      return this.input.current().is(BRACKETS) || !inSymbol && this.input.current().is('|') && !this.input.next().is('|');
   }

   protected boolean canConsumeThisString(String string, boolean consume) {
      if (string == null) {
         return false;
      }

      for (int i = 0; i < string.length(); i++) {
         if (!this.input.next(i).is(string.charAt(i))) {
            return false;
         }
      }

      if (consume) {
         this.input.consume(string.length());
      }

      return true;
   }

   protected boolean isAtStartOfLineComment(boolean consume) {
      return this.lineComment != null ? this.canConsumeThisString(this.lineComment, consume) : false;
   }

   protected void skipToEndOfLine() {
      while (!this.input.current().isEndOfInput() && !this.input.current().isNewLine()) {
         this.input.consume();
      }
   }

   protected boolean isAtStartOfBlockComment(boolean consume) {
      return this.canConsumeThisString(this.blockCommentStart, consume);
   }

   protected boolean isAtEndOfBlockComment() {
      return this.canConsumeThisString(this.blockCommentEnd, true);
   }

   protected void skipBlockComment() {
      while (!this.input.current().isEndOfInput()) {
         if (this.isAtEndOfBlockComment()) {
            return;
         }

         this.input.consume();
      }

      this.problemCollector.add(ParseError.error(this.input.current(), "Premature end of block comment"));
   }

   protected Token fetchString() {
      char separator = this.input.current().getValue();
      char escapeChar = this.stringDelimiters.get(this.input.current().getValue());
      Token result = Token.create(Token.TokenType.STRING, this.input.current());
      result.addToTrigger(this.input.consume());

      while (!this.input.current().isNewLine() && !this.input.current().is(separator) && !this.input.current().isEndOfInput()) {
         if (escapeChar != 0 && this.input.current().is(escapeChar)) {
            result.addToSource(this.input.consume());
            if (!this.handleStringEscape(separator, escapeChar, result)) {
               this.problemCollector
                  .add(ParseError.error(this.input.next(), String.format("Cannot use '%s' as escaped character", this.input.next().getStringValue())));
            }
         } else {
            result.addToContent(this.input.consume());
         }
      }

      if (this.input.current().is(separator)) {
         result.addToSource(this.input.consume());
      } else {
         this.problemCollector.add(ParseError.error(this.input.current(), "Premature end of string constant"));
      }

      return result;
   }

   protected boolean handleStringEscape(char separator, char escapeChar, Token stringToken) {
      if (this.input.current().is(separator)) {
         stringToken.addToContent(separator);
         stringToken.addToSource(this.input.consume());
         return true;
      } else if (this.input.current().is(escapeChar)) {
         stringToken.silentAddToContent(escapeChar);
         stringToken.addToSource(this.input.consume());
         return true;
      } else if (this.input.current().is('n')) {
         stringToken.silentAddToContent('\n');
         stringToken.addToSource(this.input.consume());
         return true;
      } else if (this.input.current().is('r')) {
         stringToken.silentAddToContent('\r');
         stringToken.addToSource(this.input.consume());
         return true;
      } else {
         return false;
      }
   }

   protected boolean isAtStartOfIdentifier() {
      return this.input.current().isLetter();
   }

   protected Token fetchId() {
      Token result = Token.create(Token.TokenType.ID, this.input.current());
      result.addToContent(this.input.consume());

      while (this.isIdentifierChar(this.input.current())) {
         result.addToContent(this.input.consume());
      }

      if (!this.input.current().isEndOfInput() && this.specialIdTerminators.contains(this.input.current().getValue())) {
         Token specialId = Token.create(Token.TokenType.SPECIAL_ID, result);
         specialId.setTrigger(this.input.current().getStringValue());
         specialId.setContent(result.getContents());
         specialId.setSource(result.getContents());
         specialId.addToSource(this.input.current());
         this.input.consume();
         return this.handleKeywords(specialId);
      } else {
         return this.handleKeywords(result);
      }
   }

   protected Token handleKeywords(Token idToken) {
      String keyword = this.keywords.get(this.keywordsCaseSensitive ? idToken.getContents().intern() : idToken.getContents().toLowerCase().intern());
      if (keyword != null) {
         Token keywordToken = Token.create(Token.TokenType.KEYWORD, idToken);
         keywordToken.setTrigger(keyword);
         keywordToken.setContent(idToken.getContents());
         keywordToken.setSource(idToken.getSource());
         return keywordToken;
      } else {
         return idToken;
      }
   }

   protected boolean isIdentifierChar(Char current) {
      return current.isDigit() || current.isLetter() || current.is('_');
   }

   protected Token fetchSpecialId() {
      Token result = Token.create(Token.TokenType.SPECIAL_ID, this.input.current());
      result.addToTrigger(this.input.consume());

      while (this.isIdentifierChar(this.input.current())) {
         result.addToContent(this.input.consume());
      }

      return this.handleKeywords(result);
   }

   protected Token fetchSymbol() {
      Token result = Token.create(Token.TokenType.SYMBOL, this.input.current());
      result.addToTrigger(this.input.consume());
      if (result.isSymbol("*") && this.input.current().is('*')
         || result.isSymbol("&") && this.input.current().is('&')
         || result.isSymbol("|") && this.input.current().is('|')
         || result.isSymbol() && this.input.current().is('=')) {
         result.addToTrigger(this.input.consume());
      }

      return result;
   }

   protected boolean isSymbolCharacter(Char ch) {
      if (!ch.isEndOfInput() && !ch.isDigit() && !ch.isLetter() && !ch.isWhitespace()) {
         char c = ch.getValue();
         return Character.isISOControl(c)
            ? false
            : !this.isAtBracket(true)
               && !this.isAtStartOfBlockComment(false)
               && !this.isAtStartOfLineComment(false)
               && !this.isAtStartOfNumber()
               && !this.isAtStartOfIdentifier()
               && !this.stringDelimiters.containsKey(ch.getValue());
      } else {
         return false;
      }
   }

   protected Token fetchNumber() {
      Token result = Token.create(Token.TokenType.INTEGER, this.input.current());
      result.addToContent(this.input.consume());

      while (
         this.input.current().isDigit()
            || this.input.current().is(this.decimalSeparator)
            || this.input.current().is(this.groupingSeparator) && this.input.next().isDigit()
            || (this.input.current().is('e') || this.input.current().is('E'))
               && (this.input.next().isDigit() || this.input.next().is('+') || this.input.next().is('-'))
      ) {
         if (this.input.current().is(this.groupingSeparator)) {
            result.addToSource(this.input.consume());
         } else if (!this.input.current().is(this.decimalSeparator)) {
            if (!this.input.current().is('e') && !this.input.current().is('E')) {
               result.addToContent(this.input.consume());
            } else if (result.is(Token.TokenType.SCIENTIFIC_DECIMAL)) {
               this.problemCollector.add(ParseError.error(this.input.current(), "Unexpected scientific notation separators"));
            } else {
               Token scientificDecimalToken = Token.create(Token.TokenType.SCIENTIFIC_DECIMAL, result);
               scientificDecimalToken.setContent(result.getContents() + "e");
               scientificDecimalToken.setSource(result.getSource() + "e");
               result = scientificDecimalToken;
               this.input.consume();
               if (this.input.current().is('+') || this.input.current().is('-')) {
                  result.addToContent(this.input.consume());
               }
            }
         } else {
            if (!result.is(Token.TokenType.DECIMAL) && !result.is(Token.TokenType.SCIENTIFIC_DECIMAL)) {
               Token decimalToken = Token.create(Token.TokenType.DECIMAL, result);
               decimalToken.setContent(result.getContents() + this.effectiveDecimalSeparator);
               decimalToken.setSource(result.getSource());
               result = decimalToken;
            } else {
               this.problemCollector.add(ParseError.error(this.input.current(), "Unexpected decimal separators"));
            }

            result.addToSource(this.input.consume());
         }
      }

      return result;
   }

   public boolean isKeywordsCaseSensitive() {
      return this.keywordsCaseSensitive;
   }

   public void setKeywordsCaseSensitive(boolean keywordsCaseSensitive) {
      this.keywordsCaseSensitive = keywordsCaseSensitive;
   }

   public void addKeyword(String keyword) {
      this.keywords.put(this.keywordsCaseSensitive ? keyword.intern() : keyword.toLowerCase().intern(), keyword);
   }

   public void addSpecialIdStarter(char character) {
      this.specialIdStarters.add(character);
   }

   public void addSpecialIdTerminator(char character) {
      this.specialIdTerminators.add(character);
   }

   public void clearStringDelimiters() {
      this.stringDelimiters.clear();
   }

   public void addUnescapedStringDelimiter(char stringDelimiter) {
      this.stringDelimiters.put(stringDelimiter, '\u0000');
   }

   public char getDecimalSeparator() {
      return this.decimalSeparator;
   }

   public void setDecimalSeparator(char decimalSeparator) {
      this.decimalSeparator = decimalSeparator;
   }

   public char getEffectiveDecimalSeparator() {
      return this.effectiveDecimalSeparator;
   }

   public void setEffectiveDecimalSeparator(char effectiveDecimalSeparator) {
      this.effectiveDecimalSeparator = effectiveDecimalSeparator;
   }

   public char getGroupingSeparator() {
      return this.groupingSeparator;
   }

   public void setGroupingSeparator(char groupingSeparator) {
      this.groupingSeparator = groupingSeparator;
   }

   public String getLineComment() {
      return this.lineComment;
   }

   public void setLineComment(String lineComment) {
      this.lineComment = lineComment;
   }

   public String getBlockCommentStart() {
      return this.blockCommentStart;
   }

   public void setBlockCommentStart(String blockCommentStart) {
      this.blockCommentStart = blockCommentStart;
   }

   public String getBlockCommentEnd() {
      return this.blockCommentEnd;
   }

   public void setBlockCommentEnd(String blockCommentEnd) {
      this.blockCommentEnd = blockCommentEnd;
   }

   @Override
   public String toString() {
      if (this.itemBuffer.isEmpty()) {
         return "No Token fetched...";
      } else {
         return this.itemBuffer.size() < 2 ? "Current: " + this.current() : "Current: " + this.current().toString() + ", Next: " + this.next().toString();
      }
   }

   public boolean more() {
      return this.current().isNotEnd();
   }

   public boolean atEnd() {
      return this.current().isEnd();
   }

   public void addWarning(Position pos, String message, Object... parameters) {
      this.getProblemCollector().add(ParseError.warning(pos, String.format(message, parameters)));
   }

   public void consumeExpectedSymbol(String symbol) {
      if (this.current().matches(Token.TokenType.SYMBOL, symbol)) {
         this.consume();
      } else {
         this.addError(this.current(), "Unexpected token: '%s'. Expected: '%s'", this.current().getSource(), symbol);
      }
   }

   public void addError(Position pos, String message, Object... parameters) {
      this.getProblemCollector().add(ParseError.error(pos, String.format(message, parameters)));
   }

   public void consumeExpectedKeyword(String keyword) {
      if (this.current().matches(Token.TokenType.KEYWORD, keyword)) {
         this.consume();
      } else {
         this.addError(this.current(), "Unexpected token: '%s'. Expected: '%s'", this.current().getSource(), keyword);
      }
   }

   public void throwOnErrorOrWarning() throws ParseException {
      if (!this.getProblemCollector().isEmpty()) {
         throw ParseException.create(this.getProblemCollector());
      }
   }

   public void throwOnError() throws ParseException {
      for (ParseError e : this.getProblemCollector()) {
         if (e.getSeverity() == ParseError.Severity.ERROR) {
            throw ParseException.create(this.getProblemCollector());
         }
      }
   }
}
