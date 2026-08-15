package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import com.dfsek.terra.lib.commons.lang3.StringUtils;
import com.dfsek.terra.lib.commons.text.matcher.StringMatcher;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TextStringBuilder implements CharSequence, Appendable, Serializable, Builder<String> {
   private static final char SPACE = ' ';
   static final int CAPACITY = 32;
   private static final int EOS = -1;
   private static final int FALSE_STRING_SIZE = Boolean.FALSE.toString().length();
   private static final long serialVersionUID = 1L;
   private static final int TRUE_STRING_SIZE = Boolean.TRUE.toString().length();
   private static final int MAX_BUFFER_SIZE = 2147483639;
   private char[] buffer;
   private String newLine;
   private String nullText;
   private int reallocations;
   private int size;

   private static int createPositiveCapacity(int minCapacity) {
      if (minCapacity < 0) {
         throw new OutOfMemoryError("Unable to allocate array size: " + Integer.toUnsignedString(minCapacity));
      } else {
         return Math.max(minCapacity, 2147483639);
      }
   }

   public static TextStringBuilder wrap(char[] initialBuffer) {
      Objects.requireNonNull(initialBuffer, "initialBuffer");
      return new TextStringBuilder(initialBuffer, initialBuffer.length);
   }

   public static TextStringBuilder wrap(char[] initialBuffer, int length) {
      return new TextStringBuilder(initialBuffer, length);
   }

   public TextStringBuilder() {
      this(32);
   }

   private TextStringBuilder(char[] initialBuffer, int length) {
      this.buffer = Objects.requireNonNull(initialBuffer, "initialBuffer");
      if (length >= 0 && length <= initialBuffer.length) {
         this.size = length;
      } else {
         throw new IllegalArgumentException("initialBuffer.length=" + initialBuffer.length + ", length=" + length);
      }
   }

   public TextStringBuilder(CharSequence seq) {
      this(StringUtils.length(seq) + 32);
      if (seq != null) {
         this.append(seq);
      }
   }

   public TextStringBuilder(int initialCapacity) {
      this.buffer = new char[initialCapacity <= 0 ? 32 : initialCapacity];
   }

   public TextStringBuilder(String str) {
      this(StringUtils.length(str) + 32);
      if (str != null) {
         this.append(str);
      }
   }

   public TextStringBuilder append(boolean value) {
      if (value) {
         this.ensureCapacityInternal(this.size + TRUE_STRING_SIZE);
         this.appendTrue(this.size);
      } else {
         this.ensureCapacityInternal(this.size + FALSE_STRING_SIZE);
         this.appendFalse(this.size);
      }

      return this;
   }

   public TextStringBuilder append(char ch) {
      int len = this.length();
      this.ensureCapacityInternal(len + 1);
      this.buffer[this.size++] = ch;
      return this;
   }

   public TextStringBuilder append(char[] chars) {
      if (chars == null) {
         return this.appendNull();
      }

      int strLen = chars.length;
      if (strLen > 0) {
         int len = this.length();
         this.ensureCapacityInternal(len + strLen);
         System.arraycopy(chars, 0, this.buffer, len, strLen);
         this.size += strLen;
      }

      return this;
   }

   public TextStringBuilder append(char[] chars, int startIndex, int length) {
      if (chars == null) {
         return this.appendNull();
      }

      if (startIndex >= 0 && startIndex <= chars.length) {
         if (length >= 0 && startIndex + length <= chars.length) {
            if (length > 0) {
               int len = this.length();
               this.ensureCapacityInternal(len + length);
               System.arraycopy(chars, startIndex, this.buffer, len, length);
               this.size += length;
            }

            return this;
         } else {
            throw new StringIndexOutOfBoundsException("Invalid length: " + length);
         }
      } else {
         throw new StringIndexOutOfBoundsException("Invalid startIndex: " + length);
      }
   }

   public TextStringBuilder append(CharBuffer str) {
      return this.append(str, 0, StringUtils.length(str));
   }

   public TextStringBuilder append(CharBuffer buf, int startIndex, int length) {
      if (buf == null) {
         return this.appendNull();
      }

      if (buf.hasArray()) {
         int totalLength = buf.remaining();
         if (startIndex < 0 || startIndex > totalLength) {
            throw new StringIndexOutOfBoundsException("startIndex must be valid");
         }

         if (length < 0 || startIndex + length > totalLength) {
            throw new StringIndexOutOfBoundsException("length must be valid");
         }

         int len = this.length();
         this.ensureCapacityInternal(len + length);
         System.arraycopy(buf.array(), buf.arrayOffset() + buf.position() + startIndex, this.buffer, len, length);
         this.size += length;
      } else {
         this.append(buf.toString(), startIndex, length);
      }

      return this;
   }

   public TextStringBuilder append(CharSequence seq) {
      if (seq == null) {
         return this.appendNull();
      } else if (seq instanceof TextStringBuilder) {
         return this.append((TextStringBuilder)seq);
      } else if (seq instanceof StringBuilder) {
         return this.append((StringBuilder)seq);
      } else if (seq instanceof StringBuffer) {
         return this.append((StringBuffer)seq);
      } else {
         return seq instanceof CharBuffer ? this.append((CharBuffer)seq) : this.append(seq.toString());
      }
   }

   public TextStringBuilder append(CharSequence seq, int startIndex, int endIndex) {
      if (seq == null) {
         return this.appendNull();
      } else if (endIndex <= 0) {
         throw new StringIndexOutOfBoundsException("endIndex must be valid");
      } else if (startIndex >= endIndex) {
         throw new StringIndexOutOfBoundsException("endIndex must be greater than startIndex");
      } else {
         return this.append(seq.toString(), startIndex, endIndex - startIndex);
      }
   }

   public TextStringBuilder append(double value) {
      return this.append(String.valueOf(value));
   }

   public TextStringBuilder append(float value) {
      return this.append(String.valueOf(value));
   }

   public TextStringBuilder append(int value) {
      return this.append(String.valueOf(value));
   }

   public TextStringBuilder append(long value) {
      return this.append(String.valueOf(value));
   }

   public TextStringBuilder append(Object obj) {
      if (obj == null) {
         return this.appendNull();
      } else {
         return obj instanceof CharSequence ? this.append((CharSequence)obj) : this.append(obj.toString());
      }
   }

   public TextStringBuilder append(String str) {
      return this.append(str, 0, StringUtils.length(str));
   }

   public TextStringBuilder append(String str, int startIndex, int length) {
      if (str == null) {
         return this.appendNull();
      }

      if (startIndex >= 0 && startIndex <= str.length()) {
         if (length >= 0 && startIndex + length <= str.length()) {
            if (length > 0) {
               int len = this.length();
               this.ensureCapacityInternal(len + length);
               str.getChars(startIndex, startIndex + length, this.buffer, len);
               this.size += length;
            }

            return this;
         } else {
            throw new StringIndexOutOfBoundsException("length must be valid");
         }
      } else {
         throw new StringIndexOutOfBoundsException("startIndex must be valid");
      }
   }

   public TextStringBuilder append(String format, Object... objs) {
      return this.append(String.format(format, objs));
   }

   public TextStringBuilder append(StringBuffer str) {
      return this.append(str, 0, StringUtils.length(str));
   }

   public TextStringBuilder append(StringBuffer str, int startIndex, int length) {
      if (str == null) {
         return this.appendNull();
      }

      if (startIndex >= 0 && startIndex <= str.length()) {
         if (length >= 0 && startIndex + length <= str.length()) {
            if (length > 0) {
               int len = this.length();
               this.ensureCapacityInternal(len + length);
               str.getChars(startIndex, startIndex + length, this.buffer, len);
               this.size += length;
            }

            return this;
         } else {
            throw new StringIndexOutOfBoundsException("length must be valid");
         }
      } else {
         throw new StringIndexOutOfBoundsException("startIndex must be valid");
      }
   }

   public TextStringBuilder append(StringBuilder str) {
      return this.append(str, 0, StringUtils.length(str));
   }

   public TextStringBuilder append(StringBuilder str, int startIndex, int length) {
      if (str == null) {
         return this.appendNull();
      }

      if (startIndex >= 0 && startIndex <= str.length()) {
         if (length >= 0 && startIndex + length <= str.length()) {
            if (length > 0) {
               int len = this.length();
               this.ensureCapacityInternal(len + length);
               str.getChars(startIndex, startIndex + length, this.buffer, len);
               this.size += length;
            }

            return this;
         } else {
            throw new StringIndexOutOfBoundsException("length must be valid");
         }
      } else {
         throw new StringIndexOutOfBoundsException("startIndex must be valid");
      }
   }

   public TextStringBuilder append(TextStringBuilder str) {
      return this.append(str, 0, StringUtils.length(str));
   }

   public TextStringBuilder append(TextStringBuilder str, int startIndex, int length) {
      if (str == null) {
         return this.appendNull();
      }

      if (startIndex >= 0 && startIndex <= str.length()) {
         if (length >= 0 && startIndex + length <= str.length()) {
            if (length > 0) {
               int len = this.length();
               this.ensureCapacityInternal(len + length);
               str.getChars(startIndex, startIndex + length, this.buffer, len);
               this.size += length;
            }

            return this;
         } else {
            throw new StringIndexOutOfBoundsException("length must be valid");
         }
      } else {
         throw new StringIndexOutOfBoundsException("startIndex must be valid");
      }
   }

   public TextStringBuilder appendAll(Iterable<?> iterable) {
      if (iterable != null) {
         iterable.forEach(this::append);
      }

      return this;
   }

   public TextStringBuilder appendAll(Iterator<?> it) {
      if (it != null) {
         it.forEachRemaining(this::append);
      }

      return this;
   }

   public <T> TextStringBuilder appendAll(T... array) {
      if (array != null && array.length > 0) {
         for (Object element : array) {
            this.append(element);
         }
      }

      return this;
   }

   private void appendFalse(int index) {
      this.buffer[index++] = 'f';
      this.buffer[index++] = 'a';
      this.buffer[index++] = 'l';
      this.buffer[index++] = 's';
      this.buffer[index] = 'e';
      this.size = this.size + FALSE_STRING_SIZE;
   }

   public TextStringBuilder appendFixedWidthPadLeft(int value, int width, char padChar) {
      return this.appendFixedWidthPadLeft(String.valueOf(value), width, padChar);
   }

   public TextStringBuilder appendFixedWidthPadLeft(Object obj, int width, char padChar) {
      if (width > 0) {
         this.ensureCapacityInternal(this.size + width);
         String str = Objects.toString(obj, this.getNullText());
         if (str == null) {
            str = "";
         }

         int strLen = str.length();
         if (strLen >= width) {
            str.getChars(strLen - width, strLen, this.buffer, this.size);
         } else {
            int padLen = width - strLen;
            int toIndex = this.size + padLen;
            Arrays.fill(this.buffer, this.size, toIndex, padChar);
            str.getChars(0, strLen, this.buffer, toIndex);
         }

         this.size += width;
      }

      return this;
   }

   public TextStringBuilder appendFixedWidthPadRight(int value, int width, char padChar) {
      return this.appendFixedWidthPadRight(String.valueOf(value), width, padChar);
   }

   public TextStringBuilder appendFixedWidthPadRight(Object obj, int width, char padChar) {
      if (width > 0) {
         this.ensureCapacityInternal(this.size + width);
         String str = Objects.toString(obj, this.getNullText());
         if (str == null) {
            str = "";
         }

         int strLen = str.length();
         if (strLen >= width) {
            str.getChars(0, width, this.buffer, this.size);
         } else {
            str.getChars(0, strLen, this.buffer, this.size);
            int fromIndex = this.size + strLen;
            Arrays.fill(this.buffer, fromIndex, fromIndex + width - strLen, padChar);
         }

         this.size += width;
      }

      return this;
   }

   public TextStringBuilder appendln(boolean value) {
      return this.append(value).appendNewLine();
   }

   public TextStringBuilder appendln(char ch) {
      return this.append(ch).appendNewLine();
   }

   public TextStringBuilder appendln(char[] chars) {
      return this.append(chars).appendNewLine();
   }

   public TextStringBuilder appendln(char[] chars, int startIndex, int length) {
      return this.append(chars, startIndex, length).appendNewLine();
   }

   public TextStringBuilder appendln(double value) {
      return this.append(value).appendNewLine();
   }

   public TextStringBuilder appendln(float value) {
      return this.append(value).appendNewLine();
   }

   public TextStringBuilder appendln(int value) {
      return this.append(value).appendNewLine();
   }

   public TextStringBuilder appendln(long value) {
      return this.append(value).appendNewLine();
   }

   public TextStringBuilder appendln(Object obj) {
      return this.append(obj).appendNewLine();
   }

   public TextStringBuilder appendln(String str) {
      return this.append(str).appendNewLine();
   }

   public TextStringBuilder appendln(String str, int startIndex, int length) {
      return this.append(str, startIndex, length).appendNewLine();
   }

   public TextStringBuilder appendln(String format, Object... objs) {
      return this.append(format, objs).appendNewLine();
   }

   public TextStringBuilder appendln(StringBuffer str) {
      return this.append(str).appendNewLine();
   }

   public TextStringBuilder appendln(StringBuffer str, int startIndex, int length) {
      return this.append(str, startIndex, length).appendNewLine();
   }

   public TextStringBuilder appendln(StringBuilder str) {
      return this.append(str).appendNewLine();
   }

   public TextStringBuilder appendln(StringBuilder str, int startIndex, int length) {
      return this.append(str, startIndex, length).appendNewLine();
   }

   public TextStringBuilder appendln(TextStringBuilder str) {
      return this.append(str).appendNewLine();
   }

   public TextStringBuilder appendln(TextStringBuilder str, int startIndex, int length) {
      return this.append(str, startIndex, length).appendNewLine();
   }

   public TextStringBuilder appendNewLine() {
      if (this.newLine == null) {
         this.append(System.lineSeparator());
         return this;
      } else {
         return this.append(this.newLine);
      }
   }

   public TextStringBuilder appendNull() {
      return this.nullText == null ? this : this.append(this.nullText);
   }

   public TextStringBuilder appendPadding(int length, char padChar) {
      if (length >= 0) {
         this.ensureCapacityInternal(this.size + length);

         for (int i = 0; i < length; i++) {
            this.buffer[this.size++] = padChar;
         }
      }

      return this;
   }

   public TextStringBuilder appendSeparator(char separator) {
      if (this.isNotEmpty()) {
         this.append(separator);
      }

      return this;
   }

   public TextStringBuilder appendSeparator(char standard, char defaultIfEmpty) {
      if (this.isEmpty()) {
         this.append(defaultIfEmpty);
      } else {
         this.append(standard);
      }

      return this;
   }

   public TextStringBuilder appendSeparator(char separator, int loopIndex) {
      if (loopIndex > 0) {
         this.append(separator);
      }

      return this;
   }

   public TextStringBuilder appendSeparator(String separator) {
      return this.appendSeparator(separator, null);
   }

   public TextStringBuilder appendSeparator(String separator, int loopIndex) {
      if (separator != null && loopIndex > 0) {
         this.append(separator);
      }

      return this;
   }

   public TextStringBuilder appendSeparator(String standard, String defaultIfEmpty) {
      String str = this.isEmpty() ? defaultIfEmpty : standard;
      if (str != null) {
         this.append(str);
      }

      return this;
   }

   public void appendTo(Appendable appendable) throws IOException {
      if (appendable instanceof Writer) {
         ((Writer)appendable).write(this.buffer, 0, this.size);
      } else if (appendable instanceof StringBuilder) {
         ((StringBuilder)appendable).append(this.buffer, 0, this.size);
      } else if (appendable instanceof StringBuffer) {
         ((StringBuffer)appendable).append(this.buffer, 0, this.size);
      } else if (appendable instanceof CharBuffer) {
         ((CharBuffer)appendable).put(this.buffer, 0, this.size);
      } else {
         appendable.append(this);
      }
   }

   private void appendTrue(int index) {
      this.buffer[index++] = 't';
      this.buffer[index++] = 'r';
      this.buffer[index++] = 'u';
      this.buffer[index] = 'e';
      this.size = this.size + TRUE_STRING_SIZE;
   }

   public TextStringBuilder appendWithSeparators(Iterable<?> iterable, String separator) {
      if (iterable != null) {
         this.appendWithSeparators(iterable.iterator(), separator);
      }

      return this;
   }

   public TextStringBuilder appendWithSeparators(Iterator<?> it, String separator) {
      if (it != null) {
         String sep = Objects.toString(separator, "");

         while (it.hasNext()) {
            this.append(it.next());
            if (it.hasNext()) {
               this.append(sep);
            }
         }
      }

      return this;
   }

   public TextStringBuilder appendWithSeparators(Object[] array, String separator) {
      if (array != null && array.length > 0) {
         String sep = Objects.toString(separator, "");
         this.append(array[0]);

         for (int i = 1; i < array.length; i++) {
            this.append(sep);
            this.append(array[i]);
         }
      }

      return this;
   }

   public Reader asReader() {
      return new TextStringBuilder.TextStringBuilderReader();
   }

   public StringTokenizer asTokenizer() {
      return new TextStringBuilder.TextStringBuilderTokenizer();
   }

   public Writer asWriter() {
      return new TextStringBuilder.TextStringBuilderWriter();
   }

   @Deprecated
   public String build() {
      return this.toString();
   }

   public int capacity() {
      return this.buffer.length;
   }

   @Override
   public char charAt(int index) {
      this.validateIndex(index);
      return this.buffer[index];
   }

   public TextStringBuilder clear() {
      this.size = 0;
      return this;
   }

   public boolean contains(char ch) {
      char[] thisBuf = this.buffer;

      for (int i = 0; i < this.size; i++) {
         if (thisBuf[i] == ch) {
            return true;
         }
      }

      return false;
   }

   public boolean contains(String str) {
      return this.indexOf(str, 0) >= 0;
   }

   public boolean contains(StringMatcher matcher) {
      return this.indexOf(matcher, 0) >= 0;
   }

   public TextStringBuilder delete(int startIndex, int endIndex) {
      int actualEndIndex = this.validateRange(startIndex, endIndex);
      int len = actualEndIndex - startIndex;
      if (len > 0) {
         this.deleteImpl(startIndex, actualEndIndex, len);
      }

      return this;
   }

   public TextStringBuilder deleteAll(char ch) {
      for (int i = 0; i < this.size; i++) {
         if (this.buffer[i] == ch) {
            int start = i;

            do {
               i++;
            } while (i < this.size && this.buffer[i] == ch);

            int len = i - start;
            this.deleteImpl(start, i, len);
            i -= len;
         }
      }

      return this;
   }

   public TextStringBuilder deleteAll(String str) {
      int len = str == null ? 0 : str.length();
      if (len > 0) {
         for (int index = this.indexOf(str, 0); index >= 0; index = this.indexOf(str, index)) {
            this.deleteImpl(index, index + len, len);
         }
      }

      return this;
   }

   public TextStringBuilder deleteAll(StringMatcher matcher) {
      return this.replace(matcher, null, 0, this.size, -1);
   }

   public TextStringBuilder deleteCharAt(int index) {
      this.validateIndex(index);
      this.deleteImpl(index, index + 1, 1);
      return this;
   }

   public TextStringBuilder deleteFirst(char ch) {
      for (int i = 0; i < this.size; i++) {
         if (this.buffer[i] == ch) {
            this.deleteImpl(i, i + 1, 1);
            break;
         }
      }

      return this;
   }

   public TextStringBuilder deleteFirst(String str) {
      int len = str == null ? 0 : str.length();
      if (len > 0) {
         int index = this.indexOf(str, 0);
         if (index >= 0) {
            this.deleteImpl(index, index + len, len);
         }
      }

      return this;
   }

   public TextStringBuilder deleteFirst(StringMatcher matcher) {
      return this.replace(matcher, null, 0, this.size, 1);
   }

   private void deleteImpl(int startIndex, int endIndex, int len) {
      System.arraycopy(this.buffer, endIndex, this.buffer, startIndex, this.size - endIndex);
      this.size -= len;
   }

   public char drainChar(int index) {
      this.validateIndex(index);
      char c = this.buffer[index];
      this.deleteCharAt(index);
      return c;
   }

   public int drainChars(int startIndex, int endIndex, char[] target, int targetIndex) {
      int length = endIndex - startIndex;
      if (!this.isEmpty() && length != 0 && target.length != 0) {
         int actualLen = Math.min(Math.min(this.size, length), target.length - targetIndex);
         this.getChars(startIndex, actualLen, target, targetIndex);
         this.delete(startIndex, actualLen);
         return actualLen;
      } else {
         return 0;
      }
   }

   public boolean endsWith(String str) {
      if (str == null) {
         return false;
      }

      int len = str.length();
      if (len == 0) {
         return true;
      }

      if (len > this.size) {
         return false;
      }

      int pos = this.size - len;

      for (int i = 0; i < len; pos++) {
         if (this.buffer[pos] != str.charAt(i)) {
            return false;
         }

         i++;
      }

      return true;
   }

   public TextStringBuilder ensureCapacity(int capacity) {
      if (capacity > 0) {
         this.ensureCapacityInternal(capacity);
      }

      return this;
   }

   private void ensureCapacityInternal(int capacity) {
      if (capacity - this.buffer.length > 0) {
         this.resizeBuffer(capacity);
      }
   }

   @Override
   public boolean equals(Object obj) {
      return obj instanceof TextStringBuilder && this.equals((TextStringBuilder)obj);
   }

   public boolean equals(TextStringBuilder other) {
      if (this == other) {
         return true;
      }

      if (other == null) {
         return false;
      }

      if (this.size != other.size) {
         return false;
      }

      char[] thisBuf = this.buffer;
      char[] otherBuf = other.buffer;

      for (int i = this.size - 1; i >= 0; i--) {
         if (thisBuf[i] != otherBuf[i]) {
            return false;
         }
      }

      return true;
   }

   public boolean equalsIgnoreCase(TextStringBuilder other) {
      if (this == other) {
         return true;
      }

      if (this.size != other.size) {
         return false;
      }

      char[] thisBuf = this.buffer;
      char[] otherBuf = other.buffer;

      for (int i = this.size - 1; i >= 0; i--) {
         char c1 = thisBuf[i];
         char c2 = otherBuf[i];
         if (c1 != c2 && Character.toUpperCase(c1) != Character.toUpperCase(c2)) {
            return false;
         }
      }

      return true;
   }

   public String get() {
      return this.toString();
   }

   char[] getBuffer() {
      return this.buffer;
   }

   public char[] getChars(char[] target) {
      int len = this.length();
      if (target == null || target.length < len) {
         target = new char[len];
      }

      System.arraycopy(this.buffer, 0, target, 0, len);
      return target;
   }

   @Override
   public void getChars(int startIndex, int endIndex, char[] target, int targetIndex) {
      if (startIndex < 0) {
         throw new StringIndexOutOfBoundsException(startIndex);
      }

      if (endIndex < 0 || endIndex > this.length()) {
         throw new StringIndexOutOfBoundsException(endIndex);
      }

      if (startIndex > endIndex) {
         throw new StringIndexOutOfBoundsException("end < start");
      }

      System.arraycopy(this.buffer, startIndex, target, targetIndex, endIndex - startIndex);
   }

   public String getNewLineText() {
      return this.newLine;
   }

   public String getNullText() {
      return this.nullText;
   }

   @Override
   public int hashCode() {
      char[] buf = this.buffer;
      int result = 0;

      for (int i = 0; i < this.size; i++) {
         result = 31 * result + buf[i];
      }

      return result;
   }

   public int indexOf(char ch) {
      return this.indexOf(ch, 0);
   }

   public int indexOf(char ch, int startIndex) {
      startIndex = Math.max(0, startIndex);
      if (startIndex >= this.size) {
         return -1;
      }

      char[] thisBuf = this.buffer;

      for (int i = startIndex; i < this.size; i++) {
         if (thisBuf[i] == ch) {
            return i;
         }
      }

      return -1;
   }

   public int indexOf(String str) {
      return this.indexOf(str, 0);
   }

   public int indexOf(String str, int startIndex) {
      startIndex = Math.max(0, startIndex);
      if (str != null && startIndex < this.size) {
         int strLen = str.length();
         if (strLen == 1) {
            return this.indexOf(str.charAt(0), startIndex);
         }

         if (strLen == 0) {
            return startIndex;
         }

         if (strLen > this.size) {
            return -1;
         }

         char[] thisBuf = this.buffer;
         int len = this.size - strLen + 1;

         label39:
         for (int i = startIndex; i < len; i++) {
            for (int j = 0; j < strLen; j++) {
               if (str.charAt(j) != thisBuf[i + j]) {
                  continue label39;
               }
            }

            return i;
         }

         return -1;
      } else {
         return -1;
      }
   }

   public int indexOf(StringMatcher matcher) {
      return this.indexOf(matcher, 0);
   }

   public int indexOf(StringMatcher matcher, int startIndex) {
      startIndex = Math.max(0, startIndex);
      if (matcher != null && startIndex < this.size) {
         int len = this.size;
         char[] buf = this.buffer;

         for (int i = startIndex; i < len; i++) {
            if (matcher.isMatch(buf, i, startIndex, len) > 0) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public TextStringBuilder insert(int index, boolean value) {
      this.validateIndex(index);
      if (value) {
         this.ensureCapacityInternal(this.size + TRUE_STRING_SIZE);
         System.arraycopy(this.buffer, index, this.buffer, index + TRUE_STRING_SIZE, this.size - index);
         this.appendTrue(index);
      } else {
         this.ensureCapacityInternal(this.size + FALSE_STRING_SIZE);
         System.arraycopy(this.buffer, index, this.buffer, index + FALSE_STRING_SIZE, this.size - index);
         this.appendFalse(index);
      }

      return this;
   }

   public TextStringBuilder insert(int index, char value) {
      this.validateIndex(index);
      this.ensureCapacityInternal(this.size + 1);
      System.arraycopy(this.buffer, index, this.buffer, index + 1, this.size - index);
      this.buffer[index] = value;
      this.size++;
      return this;
   }

   public TextStringBuilder insert(int index, char[] chars) {
      this.validateIndex(index);
      if (chars == null) {
         return this.insert(index, this.nullText);
      }

      int len = chars.length;
      if (len > 0) {
         this.ensureCapacityInternal(this.size + len);
         System.arraycopy(this.buffer, index, this.buffer, index + len, this.size - index);
         System.arraycopy(chars, 0, this.buffer, index, len);
         this.size += len;
      }

      return this;
   }

   public TextStringBuilder insert(int index, char[] chars, int offset, int length) {
      this.validateIndex(index);
      if (chars == null) {
         return this.insert(index, this.nullText);
      }

      if (offset >= 0 && offset <= chars.length) {
         if (length >= 0 && offset + length <= chars.length) {
            if (length > 0) {
               this.ensureCapacityInternal(this.size + length);
               System.arraycopy(this.buffer, index, this.buffer, index + length, this.size - index);
               System.arraycopy(chars, offset, this.buffer, index, length);
               this.size += length;
            }

            return this;
         } else {
            throw new StringIndexOutOfBoundsException("Invalid length: " + length);
         }
      } else {
         throw new StringIndexOutOfBoundsException("Invalid offset: " + offset);
      }
   }

   public TextStringBuilder insert(int index, double value) {
      return this.insert(index, String.valueOf(value));
   }

   public TextStringBuilder insert(int index, float value) {
      return this.insert(index, String.valueOf(value));
   }

   public TextStringBuilder insert(int index, int value) {
      return this.insert(index, String.valueOf(value));
   }

   public TextStringBuilder insert(int index, long value) {
      return this.insert(index, String.valueOf(value));
   }

   public TextStringBuilder insert(int index, Object obj) {
      return obj == null ? this.insert(index, this.nullText) : this.insert(index, obj.toString());
   }

   public TextStringBuilder insert(int index, String str) {
      this.validateIndex(index);
      if (str == null) {
         str = this.nullText;
      }

      if (str != null) {
         int strLen = str.length();
         if (strLen > 0) {
            int newSize = this.size + strLen;
            this.ensureCapacityInternal(newSize);
            System.arraycopy(this.buffer, index, this.buffer, index + strLen, this.size - index);
            this.size = newSize;
            str.getChars(0, strLen, this.buffer, index);
         }
      }

      return this;
   }

   @Override
   public boolean isEmpty() {
      return this.size == 0;
   }

   public boolean isNotEmpty() {
      return this.size != 0;
   }

   public boolean isReallocated() {
      return this.reallocations > 0;
   }

   public int lastIndexOf(char ch) {
      return this.lastIndexOf(ch, this.size - 1);
   }

   public int lastIndexOf(char ch, int startIndex) {
      startIndex = startIndex >= this.size ? this.size - 1 : startIndex;
      if (startIndex < 0) {
         return -1;
      }

      for (int i = startIndex; i >= 0; i--) {
         if (this.buffer[i] == ch) {
            return i;
         }
      }

      return -1;
   }

   public int lastIndexOf(String str) {
      return this.lastIndexOf(str, this.size - 1);
   }

   public int lastIndexOf(String str, int startIndex) {
      startIndex = startIndex >= this.size ? this.size - 1 : startIndex;
      if (str != null && startIndex >= 0) {
         int strLen = str.length();
         if (strLen > 0 && strLen <= this.size) {
            if (strLen == 1) {
               return this.lastIndexOf(str.charAt(0), startIndex);
            }

            label42:
            for (int i = startIndex - strLen + 1; i >= 0; i--) {
               for (int j = 0; j < strLen; j++) {
                  if (str.charAt(j) != this.buffer[i + j]) {
                     continue label42;
                  }
               }

               return i;
            }
         } else if (strLen == 0) {
            return startIndex;
         }

         return -1;
      } else {
         return -1;
      }
   }

   public int lastIndexOf(StringMatcher matcher) {
      return this.lastIndexOf(matcher, this.size);
   }

   public int lastIndexOf(StringMatcher matcher, int startIndex) {
      startIndex = startIndex >= this.size ? this.size - 1 : startIndex;
      if (matcher != null && startIndex >= 0) {
         char[] buf = this.buffer;
         int endIndex = startIndex + 1;

         for (int i = startIndex; i >= 0; i--) {
            if (matcher.isMatch(buf, i, 0, endIndex) > 0) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public String leftString(int length) {
      if (length <= 0) {
         return "";
      } else {
         return length >= this.size ? new String(this.buffer, 0, this.size) : new String(this.buffer, 0, length);
      }
   }

   @Override
   public int length() {
      return this.size;
   }

   public String midString(int index, int length) {
      if (index < 0) {
         index = 0;
      }

      if (length <= 0 || index >= this.size) {
         return "";
      } else {
         return this.size <= index + length ? new String(this.buffer, index, this.size - index) : new String(this.buffer, index, length);
      }
   }

   public TextStringBuilder minimizeCapacity() {
      if (this.buffer.length > this.size) {
         this.reallocate(this.size);
      }

      return this;
   }

   public int readFrom(CharBuffer charBuffer) {
      int oldSize = this.size;
      int remaining = charBuffer.remaining();
      this.ensureCapacityInternal(this.size + remaining);
      charBuffer.get(this.buffer, this.size, remaining);
      this.size += remaining;
      return this.size - oldSize;
   }

   public int readFrom(Readable readable) throws IOException {
      if (readable instanceof Reader) {
         return this.readFrom((Reader)readable);
      }

      if (readable instanceof CharBuffer) {
         return this.readFrom((CharBuffer)readable);
      }

      int oldSize = this.size;

      while (true) {
         this.ensureCapacityInternal(this.size + 1);
         CharBuffer buf = CharBuffer.wrap(this.buffer, this.size, this.buffer.length - this.size);
         int read = readable.read(buf);
         if (read == -1) {
            return this.size - oldSize;
         }

         this.size += read;
      }
   }

   public int readFrom(Reader reader) throws IOException {
      int oldSize = this.size;
      this.ensureCapacityInternal(this.size + 1);
      int readCount = reader.read(this.buffer, this.size, this.buffer.length - this.size);
      if (readCount == -1) {
         return -1;
      }

      do {
         this.size += readCount;
         this.ensureCapacityInternal(this.size + 1);
         readCount = reader.read(this.buffer, this.size, this.buffer.length - this.size);
      } while (readCount != -1);

      return this.size - oldSize;
   }

   public int readFrom(Reader reader, int count) throws IOException {
      if (count <= 0) {
         return 0;
      }

      int oldSize = this.size;
      this.ensureCapacityInternal(this.size + count);
      int target = count;
      int readCount = reader.read(this.buffer, this.size, target);
      if (readCount == -1) {
         return -1;
      }

      do {
         target -= readCount;
         this.size += readCount;
         readCount = reader.read(this.buffer, this.size, target);
      } while (target > 0 && readCount != -1);

      return this.size - oldSize;
   }

   private void reallocate(int newLength) {
      this.buffer = Arrays.copyOf(this.buffer, newLength);
      this.reallocations++;
   }

   public TextStringBuilder replace(int startIndex, int endIndex, String replaceStr) {
      endIndex = this.validateRange(startIndex, endIndex);
      int insertLen = replaceStr == null ? 0 : replaceStr.length();
      this.replaceImpl(startIndex, endIndex, endIndex - startIndex, replaceStr, insertLen);
      return this;
   }

   public TextStringBuilder replace(StringMatcher matcher, String replaceStr, int startIndex, int endIndex, int replaceCount) {
      endIndex = this.validateRange(startIndex, endIndex);
      return this.replaceImpl(matcher, replaceStr, startIndex, endIndex, replaceCount);
   }

   public TextStringBuilder replaceAll(char search, char replace) {
      if (search != replace) {
         for (int i = 0; i < this.size; i++) {
            if (this.buffer[i] == search) {
               this.buffer[i] = replace;
            }
         }
      }

      return this;
   }

   public TextStringBuilder replaceAll(String searchStr, String replaceStr) {
      int searchLen = searchStr == null ? 0 : searchStr.length();
      if (searchLen > 0) {
         int replaceLen = replaceStr == null ? 0 : replaceStr.length();

         for (int index = this.indexOf(searchStr, 0); index >= 0; index = this.indexOf(searchStr, index + replaceLen)) {
            this.replaceImpl(index, index + searchLen, searchLen, replaceStr, replaceLen);
         }
      }

      return this;
   }

   public TextStringBuilder replaceAll(StringMatcher matcher, String replaceStr) {
      return this.replace(matcher, replaceStr, 0, this.size, -1);
   }

   public TextStringBuilder replaceFirst(char search, char replace) {
      if (search != replace) {
         for (int i = 0; i < this.size; i++) {
            if (this.buffer[i] == search) {
               this.buffer[i] = replace;
               break;
            }
         }
      }

      return this;
   }

   public TextStringBuilder replaceFirst(String searchStr, String replaceStr) {
      int searchLen = searchStr == null ? 0 : searchStr.length();
      if (searchLen > 0) {
         int index = this.indexOf(searchStr, 0);
         if (index >= 0) {
            int replaceLen = replaceStr == null ? 0 : replaceStr.length();
            this.replaceImpl(index, index + searchLen, searchLen, replaceStr, replaceLen);
         }
      }

      return this;
   }

   public TextStringBuilder replaceFirst(StringMatcher matcher, String replaceStr) {
      return this.replace(matcher, replaceStr, 0, this.size, 1);
   }

   private void replaceImpl(int startIndex, int endIndex, int removeLen, String insertStr, int insertLen) {
      int newSize = this.size - removeLen + insertLen;
      if (insertLen != removeLen) {
         this.ensureCapacityInternal(newSize);
         System.arraycopy(this.buffer, endIndex, this.buffer, startIndex + insertLen, this.size - endIndex);
         this.size = newSize;
      }

      if (insertLen > 0) {
         insertStr.getChars(0, insertLen, this.buffer, startIndex);
      }
   }

   private TextStringBuilder replaceImpl(StringMatcher matcher, String replaceStr, int from, int to, int replaceCount) {
      if (matcher != null && this.size != 0) {
         int replaceLen = replaceStr == null ? 0 : replaceStr.length();

         for (int i = from; i < to && replaceCount != 0; i++) {
            char[] buf = this.buffer;
            int removeLen = matcher.isMatch(buf, i, from, to);
            if (removeLen > 0) {
               this.replaceImpl(i, i + removeLen, removeLen, replaceStr, replaceLen);
               to = to - removeLen + replaceLen;
               i = i + replaceLen - 1;
               if (replaceCount > 0) {
                  replaceCount--;
               }
            }
         }

         return this;
      } else {
         return this;
      }
   }

   private void resizeBuffer(int minCapacity) {
      int oldCapacity = this.buffer.length;
      int newCapacity = oldCapacity * 2;
      if (Integer.compareUnsigned(newCapacity, minCapacity) < 0) {
         newCapacity = minCapacity;
      }

      if (Integer.compareUnsigned(newCapacity, 2147483639) > 0) {
         newCapacity = createPositiveCapacity(minCapacity);
      }

      this.reallocate(newCapacity);
   }

   public TextStringBuilder reverse() {
      if (this.size == 0) {
         return this;
      }

      int half = this.size / 2;
      char[] buf = this.buffer;
      int leftIdx = 0;

      for (int rightIdx = this.size - 1; leftIdx < half; rightIdx--) {
         char swap = buf[leftIdx];
         buf[leftIdx] = buf[rightIdx];
         buf[rightIdx] = swap;
         leftIdx++;
      }

      return this;
   }

   public String rightString(int length) {
      if (length <= 0) {
         return "";
      } else {
         return length >= this.size ? new String(this.buffer, 0, this.size) : new String(this.buffer, this.size - length, length);
      }
   }

   public TextStringBuilder set(CharSequence str) {
      this.clear();
      this.append(str);
      return this;
   }

   public TextStringBuilder setCharAt(int index, char ch) {
      this.validateIndex(index);
      this.buffer[index] = ch;
      return this;
   }

   public TextStringBuilder setLength(int length) {
      if (length < 0) {
         throw new StringIndexOutOfBoundsException(length);
      }

      if (length < this.size) {
         this.size = length;
      } else if (length > this.size) {
         this.ensureCapacityInternal(length);
         int oldEnd = this.size;
         this.size = length;
         Arrays.fill(this.buffer, oldEnd, length, '\u0000');
      }

      return this;
   }

   public TextStringBuilder setNewLineText(String newLine) {
      this.newLine = newLine;
      return this;
   }

   public TextStringBuilder setNullText(String nullText) {
      if (nullText != null && nullText.isEmpty()) {
         nullText = null;
      }

      this.nullText = nullText;
      return this;
   }

   public int size() {
      return this.size;
   }

   public boolean startsWith(String str) {
      if (str == null) {
         return false;
      }

      int len = str.length();
      if (len == 0) {
         return true;
      }

      if (len > this.size) {
         return false;
      }

      for (int i = 0; i < len; i++) {
         if (this.buffer[i] != str.charAt(i)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public CharSequence subSequence(int startIndex, int endIndex) {
      if (startIndex < 0) {
         throw new StringIndexOutOfBoundsException(startIndex);
      } else if (endIndex > this.size) {
         throw new StringIndexOutOfBoundsException(endIndex);
      } else if (startIndex > endIndex) {
         throw new StringIndexOutOfBoundsException(endIndex - startIndex);
      } else {
         return this.substring(startIndex, endIndex);
      }
   }

   public String substring(int start) {
      return this.substring(start, this.size);
   }

   public String substring(int startIndex, int endIndex) {
      endIndex = this.validateRange(startIndex, endIndex);
      return new String(this.buffer, startIndex, endIndex - startIndex);
   }

   public char[] toCharArray() {
      return this.size == 0 ? ArrayUtils.EMPTY_CHAR_ARRAY : Arrays.copyOf(this.buffer, this.size);
   }

   public char[] toCharArray(int startIndex, int endIndex) {
      endIndex = this.validateRange(startIndex, endIndex);
      int len = endIndex - startIndex;
      return len == 0 ? ArrayUtils.EMPTY_CHAR_ARRAY : Arrays.copyOfRange(this.buffer, startIndex, endIndex);
   }

   @Override
   public String toString() {
      return new String(this.buffer, 0, this.size);
   }

   public StringBuffer toStringBuffer() {
      return new StringBuffer(this.size).append(this.buffer, 0, this.size);
   }

   public StringBuilder toStringBuilder() {
      return new StringBuilder(this.size).append(this.buffer, 0, this.size);
   }

   public TextStringBuilder trim() {
      if (this.size == 0) {
         return this;
      }

      int len = this.size;
      char[] buf = this.buffer;
      int pos = 0;

      while (pos < len && buf[pos] <= ' ') {
         pos++;
      }

      while (pos < len && buf[len - 1] <= ' ') {
         len--;
      }

      if (len < this.size) {
         this.delete(len, this.size);
      }

      if (pos > 0) {
         this.delete(0, pos);
      }

      return this;
   }

   protected void validateIndex(int index) {
      if (index < 0 || index >= this.size) {
         throw new StringIndexOutOfBoundsException(index);
      }
   }

   protected int validateRange(int startIndex, int endIndex) {
      if (startIndex < 0) {
         throw new StringIndexOutOfBoundsException(startIndex);
      }

      if (endIndex > this.size) {
         endIndex = this.size;
      }

      if (startIndex > endIndex) {
         throw new StringIndexOutOfBoundsException("end < start");
      } else {
         return endIndex;
      }
   }

   final class TextStringBuilderReader extends Reader {
      private int mark;
      private int pos;

      @Override
      public void close() {
      }

      @Override
      public void mark(int readAheadLimit) {
         this.mark = this.pos;
      }

      @Override
      public boolean markSupported() {
         return true;
      }

      @Override
      public int read() {
         return !this.ready() ? -1 : TextStringBuilder.this.charAt(this.pos++);
      }

      @Override
      public int read(char[] b, int off, int len) {
         if (off < 0 || len < 0 || off > b.length || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
         }

         if (len == 0) {
            return 0;
         }

         if (this.pos >= TextStringBuilder.this.size()) {
            return -1;
         }

         if (this.pos + len > TextStringBuilder.this.size()) {
            len = TextStringBuilder.this.size() - this.pos;
         }

         TextStringBuilder.this.getChars(this.pos, this.pos + len, b, off);
         this.pos += len;
         return len;
      }

      @Override
      public boolean ready() {
         return this.pos < TextStringBuilder.this.size();
      }

      @Override
      public void reset() {
         this.pos = this.mark;
      }

      @Override
      public long skip(long n) {
         if (this.pos + n > TextStringBuilder.this.size()) {
            n = TextStringBuilder.this.size() - this.pos;
         }

         if (n < 0L) {
            return 0L;
         }

         this.pos = Math.addExact(this.pos, Math.toIntExact(n));
         return n;
      }
   }

   final class TextStringBuilderTokenizer extends StringTokenizer {
      @Override
      public String getContent() {
         String str = super.getContent();
         return str == null ? TextStringBuilder.this.toString() : str;
      }

      @Override
      protected List<String> tokenize(char[] chars, int offset, int count) {
         return chars == null ? super.tokenize(TextStringBuilder.this.getBuffer(), 0, TextStringBuilder.this.size()) : super.tokenize(chars, offset, count);
      }
   }

   final class TextStringBuilderWriter extends Writer {
      @Override
      public void close() {
      }

      @Override
      public void flush() {
      }

      @Override
      public void write(char[] cbuf) {
         TextStringBuilder.this.append(cbuf);
      }

      @Override
      public void write(char[] cbuf, int off, int len) {
         TextStringBuilder.this.append(cbuf, off, len);
      }

      @Override
      public void write(int c) {
         TextStringBuilder.this.append((char)c);
      }

      @Override
      public void write(String str) {
         TextStringBuilder.this.append(str);
      }

      @Override
      public void write(String str, int off, int len) {
         TextStringBuilder.this.append(str, off, len);
      }
   }
}
