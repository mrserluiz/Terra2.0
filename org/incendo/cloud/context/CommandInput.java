package org.incendo.cloud.context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.incendo.cloud.type.range.ByteRange;
import org.incendo.cloud.type.range.DoubleRange;
import org.incendo.cloud.type.range.FloatRange;
import org.incendo.cloud.type.range.IntRange;
import org.incendo.cloud.type.range.LongRange;
import org.incendo.cloud.type.range.ShortRange;

@API(status = Status.EXPERIMENTAL)
public interface CommandInput {
   List<String> BOOLEAN_STRICT = Collections.unmodifiableList(Arrays.asList("TRUE", "FALSE"));
   List<String> BOOLEAN_LIBERAL = Collections.unmodifiableList(Arrays.asList("TRUE", "YES", "ON", "FALSE", "NO", "OFF"));
   List<String> BOOLEAN_LIBERAL_TRUE = Collections.unmodifiableList(Arrays.asList("TRUE", "YES", "ON"));

   static @NonNull CommandInput of(final @NonNull String input) {
      return new CommandInputImpl(input);
   }

   static @NonNull CommandInput of(final @NonNull Iterable<String> input) {
      return new CommandInputImpl(String.join(" ", input));
   }

   static @NonNull CommandInput empty() {
      return new CommandInputImpl("");
   }

   @Pure
   @NonNull String input();

   @SideEffectFree
   @NonNegative int cursor();

   @Pure
   default @NonNegative int length() {
      return this.input().length();
   }

   @SideEffectFree
   default @NonNegative int remainingLength() {
      return this.length() - this.cursor();
   }

   @SideEffectFree
   default @NonNegative int remainingTokens() {
      int count = new StringTokenizer(this.remainingInput(), " ").countTokens();
      return this.remainingInput().endsWith(" ") ? count + 1 : count;
   }

   @SideEffectFree
   default @NonNull String remainingInput() {
      return this.input().substring(this.cursor());
   }

   @SideEffectFree
   default @NonNull String readInput() {
      return this.input().substring(0, this.cursor());
   }

   @NonNull CommandInput appendString(@NonNull String string);

   @SideEffectFree
   default boolean hasRemainingInput() {
      return this.cursor() < this.length();
   }

   @SideEffectFree
   default boolean isEmpty() {
      return this.isEmpty(false);
   }

   @SideEffectFree
   default boolean isEmpty(final boolean ignoreWhitespace) {
      return !this.hasRemainingInput(ignoreWhitespace);
   }

   @SideEffectFree
   default boolean hasRemainingInput(final boolean ignoreWhitespace) {
      if (!this.hasRemainingInput()) {
         return false;
      } else {
         return ignoreWhitespace ? this.hasNonWhitespace() : true;
      }
   }

   void moveCursor(int chars);

   @This @NonNull CommandInput cursor(@NonNegative int position);

   @SideEffectFree
   default @NonNull String peekString(final @NonNegative int chars) {
      String remainingInput = this.remainingInput();
      if (chars > remainingInput.length()) {
         throw new CommandInput.CursorOutOfBoundsException(this.cursor() + chars, this.length());
      } else {
         return remainingInput.substring(0, chars);
      }
   }

   default @NonNull String read(final @NonNegative int chars) {
      String readString = this.peekString(chars);
      this.moveCursor(chars);
      return readString;
   }

   @SideEffectFree
   default char peek() {
      if (this.cursor() >= this.input().length()) {
         throw new CommandInput.CursorOutOfBoundsException(this.cursor(), this.length());
      } else {
         return this.input().charAt(this.cursor());
      }
   }

   default char read() {
      char readChar = this.peek();
      this.moveCursor(1);
      return readChar;
   }

   default @NonNull String peekString() {
      if (!this.hasRemainingInput()) {
         return "";
      }

      String remainingInput = this.remainingInput();
      int indexOfWhitespace = remainingInput.indexOf(32);
      if (indexOfWhitespace == -1) {
         return remainingInput;
      }

      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < remainingInput.length(); i++) {
         char currentChar = remainingInput.charAt(i);
         if (Character.isWhitespace(currentChar)) {
            if (builder.length() != 0) {
               break;
            }
         } else {
            builder.append(currentChar);
         }
      }

      return builder.toString();
   }

   default @NonNull String readStringSkipWhitespace(final boolean preserveSingleSpace) {
      String readString = this.readString();
      this.skipWhitespace(preserveSingleSpace);
      return readString;
   }

   default @NonNull String readStringSkipWhitespace() {
      return this.readStringSkipWhitespace(true);
   }

   default @NonNull String readString() {
      return this.skipWhitespace().readUntil(' ');
   }

   default @NonNull String readUntil(final char separator) {
      if (!this.hasRemainingInput()) {
         return "";
      } else {
         String remainingInput = this.remainingInput();
         int indexOfWhitespace = remainingInput.indexOf(separator);
         if (indexOfWhitespace == -1) {
            this.moveCursor(this.remainingLength());
            return remainingInput;
         } else {
            return this.read(indexOfWhitespace);
         }
      }
   }

   default @NonNull String readUntilAndSkip(final char separator) {
      String readString = this.readUntil(separator);
      if (!readString.isEmpty() && this.hasRemainingInput()) {
         char readChar = this.read();
         if (readChar != separator) {
            this.moveCursor(-1);
         }

         return readString;
      } else {
         return readString;
      }
   }

   default @This @NonNull CommandInput skipWhitespace(final int maxSpaces, final boolean preserveSingleSpace) {
      if (preserveSingleSpace && this.remainingLength() == 1 && this.peek() == ' ') {
         return this;
      }

      for (int i = 0; i < maxSpaces && this.hasRemainingInput() && Character.isWhitespace(this.peek()); i++) {
         this.read();
      }

      return this;
   }

   default @This @NonNull CommandInput skipWhitespace(final int maxSpaces) {
      return this.skipWhitespace(maxSpaces, false);
   }

   default @This @NonNull CommandInput skipWhitespace(final boolean preserveSingleSpace) {
      return this.skipWhitespace(Integer.MAX_VALUE, preserveSingleSpace);
   }

   default @This @NonNull CommandInput skipWhitespace() {
      return this.skipWhitespace(false);
   }

   default boolean hasNonWhitespace() {
      return this.remainingInput().chars().anyMatch(c -> !Character.isWhitespace(c));
   }

   @SideEffectFree
   default boolean isValidByte(final byte min, final byte max) {
      try {
         byte parsedByte = Byte.parseByte(this.peekString());
         return parsedByte >= min && parsedByte <= max;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   @SideEffectFree
   default boolean isValidByte(final @NonNull ByteRange range) {
      return this.isValidByte(range.minByte(), range.maxByte());
   }

   default byte readByte() {
      return Byte.parseByte(this.readString());
   }

   @SideEffectFree
   default boolean isValidShort(final short min, final short max) {
      try {
         short parsedShort = Short.parseShort(this.peekString());
         return parsedShort >= min && parsedShort <= max;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   @SideEffectFree
   default boolean isValidShort(final @NonNull ShortRange range) {
      return this.isValidShort(range.minShort(), range.maxShort());
   }

   default short readShort() {
      return Short.parseShort(this.readString());
   }

   @SideEffectFree
   default boolean isValidInteger(final int min, final int max) {
      try {
         int parsedInteger = Integer.parseInt(this.peekString());
         return parsedInteger >= min && parsedInteger <= max;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   @SideEffectFree
   default boolean isValidInteger(final @NonNull IntRange range) {
      return this.isValidInteger(range.minInt(), range.maxInt());
   }

   default int readInteger() {
      return Integer.parseInt(this.readString());
   }

   default int readInteger(final int radix) {
      return Integer.parseInt(this.readString(), radix);
   }

   @SideEffectFree
   default boolean isValidLong(final long min, final long max) {
      try {
         long parsedLong = Long.parseLong(this.peekString());
         return parsedLong >= min && parsedLong <= max;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   @SideEffectFree
   default boolean isValidLong(final @NonNull LongRange range) {
      return this.isValidLong(range.minLong(), range.maxLong());
   }

   default long readLong() {
      return Long.parseLong(this.readString());
   }

   @SideEffectFree
   default boolean isValidDouble(final double min, final double max) {
      try {
         double parsedDouble = Double.parseDouble(this.peekString());
         return parsedDouble >= min && parsedDouble <= max;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   @SideEffectFree
   default boolean isValidDouble(final @NonNull DoubleRange range) {
      return this.isValidDouble(range.minDouble(), range.maxDouble());
   }

   default double readDouble() {
      return Double.parseDouble(this.readString());
   }

   @SideEffectFree
   default boolean isValidFloat(final float min, final float max) {
      try {
         float parsedFloat = Float.parseFloat(this.peekString());
         return parsedFloat >= min && parsedFloat <= max;
      } catch (NumberFormatException ignored) {
         return false;
      }
   }

   @SideEffectFree
   default boolean isValidFloat(final @NonNull FloatRange range) {
      return this.isValidFloat(range.minFloat(), range.maxFloat());
   }

   default float readFloat() {
      return Float.parseFloat(this.readString());
   }

   @SideEffectFree
   default boolean isValidBoolean(final boolean liberal) {
      return liberal
         ? BOOLEAN_LIBERAL.contains(this.peekString().toUpperCase(Locale.ROOT))
         : BOOLEAN_STRICT.contains(this.peekString().toUpperCase(Locale.ROOT));
   }

   default boolean readBoolean() {
      return BOOLEAN_LIBERAL_TRUE.contains(this.readString().toUpperCase(Locale.ROOT));
   }

   default @NonNull String lastRemainingToken() {
      String remainingInput = this.remainingInput();
      if (!remainingInput.isEmpty() && !remainingInput.endsWith(" ")) {
         int lastSpace = remainingInput.lastIndexOf(32);
         return lastSpace == -1 ? remainingInput : remainingInput.substring(lastSpace + 1);
      } else {
         return "";
      }
   }

   default char lastRemainingCharacter() {
      String lastToken = this.lastRemainingToken();
      if (lastToken.isEmpty()) {
         throw new CommandInput.CursorOutOfBoundsException(this.cursor(), this.length());
      } else {
         return lastToken.charAt(lastToken.length() - 1);
      }
   }

   @NonNull CommandInput copy();

   default @NonNull String difference(final @NonNull CommandInput that, final boolean includeTrailingWhitespace) {
      if (!this.input().equals(that.input())) {
         return this.input();
      }

      String difference = this.input().substring(this.cursor(), that.cursor());
      return !includeTrailingWhitespace && difference.endsWith(" ") ? difference.substring(0, difference.length() - 1) : difference;
   }

   default @NonNull String difference(final @NonNull CommandInput that) {
      return this.difference(that, false);
   }

   @API(status = Status.STABLE)
   class CursorOutOfBoundsException extends NoSuchElementException {
      CursorOutOfBoundsException(final @NonNegative int cursor, final @NonNegative int length) {
         super(String.format("Cursor exceeds input length (%d > %d)", cursor, length - 1));
      }
   }
}
