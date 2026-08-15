package com.dfsek.terra.lib.commons.text.numbers;

final class ParsedDecimal {
   private static final char MINUS_CHAR = '-';
   private static final char DECIMAL_SEP_CHAR = '.';
   private static final char EXPONENT_CHAR = 'E';
   private static final char ZERO_CHAR = '0';
   private static final int THOUSANDS_GROUP_SIZE = 3;
   private static final int DECIMAL_RADIX = 10;
   private static final int ROUND_CENTER = 5;
   private static final int ENG_EXPONENT_MOD = 3;
   final boolean negative;
   final int[] digits;
   int digitCount;
   int exponent;
   private char[] outputChars;
   private int outputIdx;

   private static int digitValue(char ch) {
      return ch - 48;
   }

   public static ParsedDecimal from(double d) {
      if (!Double.isFinite(d)) {
         throw new IllegalArgumentException("Double is not finite");
      }

      char[] strChars = Double.toString(d).toCharArray();
      boolean negative = strChars[0] == '-';
      int digitStartIdx = negative ? 1 : 0;
      int[] digits = new int[strChars.length - digitStartIdx - 1];
      boolean foundDecimalPoint = false;
      int digitCount = 0;
      int significantDigitCount = 0;
      int decimalPos = 0;

      int i;
      for (i = digitStartIdx; i < strChars.length; i++) {
         char ch = strChars[i];
         if (ch == '.') {
            foundDecimalPoint = true;
            decimalPos = digitCount;
         } else {
            if (ch == 'E') {
               break;
            }

            if (ch != '0' || digitCount > 0) {
               int val = digitValue(ch);
               digits[digitCount++] = val;
               if (val > 0) {
                  significantDigitCount = digitCount;
               }
            } else if (foundDecimalPoint) {
               decimalPos--;
            }
         }
      }

      if (digitCount > 0) {
         int explicitExponent = i < strChars.length ? parseExponent(strChars, i + 1) : 0;
         int exponent = explicitExponent + decimalPos - significantDigitCount;
         return new ParsedDecimal(negative, digits, significantDigitCount, exponent);
      } else {
         return new ParsedDecimal(negative, new int[]{0}, 1, 0);
      }
   }

   private static int parseExponent(char[] chars, int start) {
      int i = start;
      boolean neg = chars[i] == '-';
      if (neg) {
         i++;
      }

      int exp = 0;

      while (i < chars.length) {
         exp = exp * 10 + digitValue(chars[i]);
         i++;
      }

      return neg ? -exp : exp;
   }

   private ParsedDecimal(boolean negative, int[] digits, int digitCount, int exponent) {
      this.negative = negative;
      this.digits = digits;
      this.digitCount = digitCount;
      this.exponent = exponent;
   }

   private void append(char ch) {
      this.outputChars[this.outputIdx++] = ch;
   }

   private void append(char[] chars) {
      for (char c : chars) {
         this.append(c);
      }
   }

   private void appendFraction(int zeroCount, int startIdx, ParsedDecimal.FormatOptions opts) {
      char[] localizedDigits = opts.getDigits();
      char localizedZero = localizedDigits[0];
      if (startIdx < this.digitCount) {
         this.append(opts.getDecimalSeparator());

         for (int i = 0; i < zeroCount; i++) {
            this.append(localizedZero);
         }

         for (int i = startIdx; i < this.digitCount; i++) {
            this.appendLocalizedDigit(this.digits[i], localizedDigits);
         }
      } else if (opts.isIncludeFractionPlaceholder()) {
         this.append(opts.getDecimalSeparator());
         this.append(localizedZero);
      }
   }

   private void appendLocalizedDigit(int n, char[] digitChars) {
      this.append(digitChars[n]);
   }

   private int appendWhole(int wholeCount, ParsedDecimal.FormatOptions opts) {
      if (this.shouldIncludeMinus(opts)) {
         this.append(opts.getMinusSign());
      }

      char[] localizedDigits = opts.getDigits();
      char localizedZero = localizedDigits[0];
      int significantDigitCount = Math.max(0, Math.min(wholeCount, this.digitCount));
      if (significantDigitCount > 0) {
         int i;
         for (i = 0; i < significantDigitCount; i++) {
            this.appendLocalizedDigit(this.digits[i], localizedDigits);
         }

         while (i < wholeCount) {
            this.append(localizedZero);
            i++;
         }
      } else {
         this.append(localizedZero);
      }

      return significantDigitCount;
   }

   private int appendWholeGrouped(int wholeCount, ParsedDecimal.FormatOptions opts) {
      if (this.shouldIncludeMinus(opts)) {
         this.append(opts.getMinusSign());
      }

      char[] localizedDigits = opts.getDigits();
      char localizedZero = localizedDigits[0];
      char groupingChar = opts.getGroupingSeparator();
      int appendCount = Math.max(0, Math.min(wholeCount, this.digitCount));
      if (appendCount > 0) {
         int pos = wholeCount;

         int i;
         for (i = 0; i < appendCount; pos--) {
            this.appendLocalizedDigit(this.digits[i], localizedDigits);
            if (this.requiresGroupingSeparatorAfterPosition(pos)) {
               this.append(groupingChar);
            }

            i++;
         }

         while (i < wholeCount) {
            this.append(localizedZero);
            if (this.requiresGroupingSeparatorAfterPosition(pos)) {
               this.append(groupingChar);
            }

            i++;
            pos--;
         }
      } else {
         this.append(localizedZero);
      }

      return appendCount;
   }

   private int getDigitStringSize(int decimalPos, ParsedDecimal.FormatOptions opts) {
      int size = this.digitCount;
      if (this.shouldIncludeMinus(opts)) {
         size++;
      }

      if (decimalPos < 1) {
         size += 2 + Math.abs(decimalPos);
      } else if (decimalPos >= this.digitCount) {
         size += decimalPos - this.digitCount;
         if (opts.isIncludeFractionPlaceholder()) {
            size += 2;
         }
      } else {
         size++;
      }

      return size;
   }

   public int getExponent() {
      return this.exponent;
   }

   private int getPlainStringSize(int decimalPos, ParsedDecimal.FormatOptions opts) {
      int size = this.getDigitStringSize(decimalPos, opts);
      if (opts.isGroupThousands() && decimalPos > 0) {
         size += (decimalPos - 1) / 3;
      }

      return size;
   }

   public int getScientificExponent() {
      return this.digitCount + this.exponent - 1;
   }

   boolean isZero() {
      return this.digits[0] == 0;
   }

   public void maxPrecision(int precision) {
      if (precision > 0 && precision < this.digitCount) {
         if (this.shouldRoundUp(precision)) {
            this.roundUp(precision);
         } else {
            this.truncate(precision);
         }
      }
   }

   private String outputString() {
      String str = String.valueOf(this.outputChars);
      this.outputChars = null;
      return str;
   }

   private void prepareOutput(int size) {
      this.outputChars = new char[size];
      this.outputIdx = 0;
   }

   private boolean requiresGroupingSeparatorAfterPosition(int pos) {
      return pos > 1 && pos % 3 == 1;
   }

   public void round(int roundExponent) {
      if (roundExponent > this.exponent) {
         int max = this.digitCount + this.exponent;
         if (roundExponent < max) {
            this.maxPrecision(max - roundExponent);
         } else if (roundExponent == max && this.shouldRoundUp(0)) {
            this.setSingleDigitValue(1, roundExponent);
         } else {
            this.setSingleDigitValue(0, 0);
         }
      }
   }

   private void roundUp(int count) {
      int removedDigits = this.digitCount - count;

      int i;
      for (i = count - 1; i >= 0; i--) {
         int d = this.digits[i] + 1;
         if (d < 10) {
            this.digits[i] = d;
            break;
         }

         removedDigits++;
      }

      if (i < 0) {
         this.setSingleDigitValue(1, this.exponent + removedDigits);
      } else {
         this.truncate(this.digitCount - removedDigits);
      }
   }

   private void setSingleDigitValue(int digit, int newExponent) {
      this.digits[0] = digit;
      this.digitCount = 1;
      this.exponent = newExponent;
   }

   private boolean shouldIncludeExponent(int targetExponent, ParsedDecimal.FormatOptions opts) {
      return targetExponent != 0 || opts.isAlwaysIncludeExponent();
   }

   private boolean shouldIncludeMinus(ParsedDecimal.FormatOptions opts) {
      return this.negative && (opts.isSignedZero() || !this.isZero());
   }

   private boolean shouldRoundUp(int count) {
      int digitAfterLast = this.digits[count];
      return digitAfterLast > 5 || digitAfterLast == 5 && (count < this.digitCount - 1 || this.digits[count - 1] % 2 != 0);
   }

   public String toEngineeringString(ParsedDecimal.FormatOptions opts) {
      int decimalPos = 1 + Math.floorMod(this.getScientificExponent(), 3);
      return this.toScientificString(decimalPos, opts);
   }

   public String toPlainString(ParsedDecimal.FormatOptions opts) {
      int decimalPos = this.digitCount + this.exponent;
      int fractionZeroCount = decimalPos < 1 ? Math.abs(decimalPos) : 0;
      this.prepareOutput(this.getPlainStringSize(decimalPos, opts));
      int fractionStartIdx = opts.isGroupThousands() ? this.appendWholeGrouped(decimalPos, opts) : this.appendWhole(decimalPos, opts);
      this.appendFraction(fractionZeroCount, fractionStartIdx, opts);
      return this.outputString();
   }

   public String toScientificString(ParsedDecimal.FormatOptions opts) {
      return this.toScientificString(1, opts);
   }

   private String toScientificString(int decimalPos, ParsedDecimal.FormatOptions opts) {
      int targetExponent = this.digitCount + this.exponent - decimalPos;
      int absTargetExponent = Math.abs(targetExponent);
      boolean includeExponent = this.shouldIncludeExponent(targetExponent, opts);
      boolean negativeExponent = targetExponent < 0;
      int size = this.getDigitStringSize(decimalPos, opts);
      int exponentDigitCount = 0;
      if (includeExponent) {
         exponentDigitCount = absTargetExponent > 0 ? (int)Math.floor(Math.log10(absTargetExponent)) + 1 : 1;
         size += opts.getExponentSeparatorChars().length + exponentDigitCount;
         if (negativeExponent) {
            size++;
         }
      }

      this.prepareOutput(size);
      int fractionStartIdx = this.appendWhole(decimalPos, opts);
      this.appendFraction(0, fractionStartIdx, opts);
      if (includeExponent) {
         this.append(opts.getExponentSeparatorChars());
         if (negativeExponent) {
            this.append(opts.getMinusSign());
         }

         char[] localizedDigits = opts.getDigits();
         int rem = absTargetExponent;

         for (int i = size - 1; i >= this.outputIdx; i--) {
            this.outputChars[i] = localizedDigits[rem % 10];
            rem /= 10;
         }

         this.outputIdx = size;
      }

      return this.outputString();
   }

   private void truncate(int count) {
      int nonZeroCount = count;

      for (int i = count - 1; i > 0 && this.digits[i] == 0; i--) {
         nonZeroCount--;
      }

      this.exponent = this.exponent + (this.digitCount - nonZeroCount);
      this.digitCount = nonZeroCount;
   }

   interface FormatOptions {
      char getDecimalSeparator();

      char[] getDigits();

      char[] getExponentSeparatorChars();

      char getGroupingSeparator();

      char getMinusSign();

      boolean isAlwaysIncludeExponent();

      boolean isGroupThousands();

      boolean isIncludeFractionPlaceholder();

      boolean isSignedZero();
   }
}
