package com.dfsek.terra.lib.commons.text.numbers;

import java.text.DecimalFormatSymbols;
import java.util.Objects;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public enum DoubleFormat {
   PLAIN(DoubleFormat.PlainDoubleFormat::new),
   SCIENTIFIC(DoubleFormat.ScientificDoubleFormat::new),
   ENGINEERING(DoubleFormat.EngineeringDoubleFormat::new),
   MIXED(DoubleFormat.MixedDoubleFormat::new);

   private final Function<DoubleFormat.Builder, DoubleFunction<String>> factory;

   DoubleFormat(final Function<DoubleFormat.Builder, DoubleFunction<String>> factory) {
      this.factory = factory;
   }

   public DoubleFormat.Builder builder() {
      return new DoubleFormat.Builder(this.factory);
   }

   private abstract static class AbstractDoubleFormat implements DoubleFunction<String>, ParsedDecimal.FormatOptions {
      private final int maxPrecision;
      private final int minDecimalExponent;
      private final String positiveInfinity;
      private final String negativeInfinity;
      private final String nan;
      private final boolean fractionPlaceholder;
      private final boolean signedZero;
      private final char[] digits;
      private final char decimalSeparator;
      private final char groupingSeparator;
      private final boolean groupThousands;
      private final char minusSign;
      private final char[] exponentSeparatorChars;
      private final boolean alwaysIncludeExponent;

      AbstractDoubleFormat(DoubleFormat.Builder builder) {
         this.maxPrecision = builder.maxPrecision;
         this.minDecimalExponent = builder.minDecimalExponent;
         this.positiveInfinity = builder.infinity;
         this.negativeInfinity = builder.minusSign + builder.infinity;
         this.nan = builder.nan;
         this.fractionPlaceholder = builder.fractionPlaceholder;
         this.signedZero = builder.signedZero;
         this.digits = builder.digits.toCharArray();
         this.decimalSeparator = builder.decimalSeparator;
         this.groupingSeparator = builder.groupingSeparator;
         this.groupThousands = builder.groupThousands;
         this.minusSign = builder.minusSign;
         this.exponentSeparatorChars = builder.exponentSeparator.toCharArray();
         this.alwaysIncludeExponent = builder.alwaysIncludeExponent;
      }

      public String apply(double d) {
         if (Double.isFinite(d)) {
            return this.applyFinite(d);
         } else if (Double.isInfinite(d)) {
            return d > 0.0 ? this.positiveInfinity : this.negativeInfinity;
         } else {
            return this.nan;
         }
      }

      private String applyFinite(double d) {
         ParsedDecimal n = ParsedDecimal.from(d);
         int roundExponent = Math.max(n.getExponent(), this.minDecimalExponent);
         if (this.maxPrecision > 0) {
            roundExponent = Math.max(n.getScientificExponent() - this.maxPrecision + 1, roundExponent);
         }

         n.round(roundExponent);
         return this.applyFiniteInternal(n);
      }

      protected abstract String applyFiniteInternal(ParsedDecimal var1);

      @Override
      public char getDecimalSeparator() {
         return this.decimalSeparator;
      }

      @Override
      public char[] getDigits() {
         return this.digits;
      }

      @Override
      public char[] getExponentSeparatorChars() {
         return this.exponentSeparatorChars;
      }

      @Override
      public char getGroupingSeparator() {
         return this.groupingSeparator;
      }

      @Override
      public char getMinusSign() {
         return this.minusSign;
      }

      @Override
      public boolean isAlwaysIncludeExponent() {
         return this.alwaysIncludeExponent;
      }

      @Override
      public boolean isGroupThousands() {
         return this.groupThousands;
      }

      @Override
      public boolean isIncludeFractionPlaceholder() {
         return this.fractionPlaceholder;
      }

      @Override
      public boolean isSignedZero() {
         return this.signedZero;
      }
   }

   public static final class Builder implements Supplier<DoubleFunction<String>> {
      private static final int DEFAULT_PLAIN_FORMAT_MAX_DECIMAL_EXPONENT = 6;
      private static final int DEFAULT_PLAIN_FORMAT_MIN_DECIMAL_EXPONENT = -3;
      private static final String DEFAULT_DECIMAL_DIGITS = "0123456789";
      private final Function<DoubleFormat.Builder, DoubleFunction<String>> factory;
      private int maxPrecision;
      private int minDecimalExponent = Integer.MIN_VALUE;
      private int plainFormatMaxDecimalExponent = 6;
      private int plainFormatMinDecimalExponent = -3;
      private String infinity = "Infinity";
      private String nan = "NaN";
      private boolean fractionPlaceholder = true;
      private boolean signedZero = true;
      private String digits = "0123456789";
      private char decimalSeparator = '.';
      private char groupingSeparator = ',';
      private boolean groupThousands;
      private char minusSign = '-';
      private String exponentSeparator = "E";
      private boolean alwaysIncludeExponent;

      private static String getDigitString(DecimalFormatSymbols symbols) {
         int zeroDelta = symbols.getZeroDigit() - "0123456789".charAt(0);
         char[] digitChars = new char["0123456789".length()];

         for (int i = 0; i < "0123456789".length(); i++) {
            digitChars[i] = (char)("0123456789".charAt(i) + zeroDelta);
         }

         return String.valueOf(digitChars);
      }

      private Builder(Function<DoubleFormat.Builder, DoubleFunction<String>> factory) {
         this.factory = factory;
      }

      public DoubleFormat.Builder allowSignedZero(boolean signedZero) {
         this.signedZero = signedZero;
         return this;
      }

      public DoubleFormat.Builder alwaysIncludeExponent(boolean alwaysIncludeExponent) {
         this.alwaysIncludeExponent = alwaysIncludeExponent;
         return this;
      }

      @Deprecated
      public DoubleFunction<String> build() {
         return this.get();
      }

      public DoubleFormat.Builder decimalSeparator(char decimalSeparator) {
         this.decimalSeparator = decimalSeparator;
         return this;
      }

      public DoubleFormat.Builder digits(String digits) {
         Objects.requireNonNull(digits, "digits");
         if (digits.length() != "0123456789".length()) {
            throw new IllegalArgumentException("Digits string must contain exactly " + "0123456789".length() + " characters.");
         }

         this.digits = digits;
         return this;
      }

      public DoubleFormat.Builder exponentSeparator(String exponentSeparator) {
         this.exponentSeparator = Objects.requireNonNull(exponentSeparator, "exponentSeparator");
         return this;
      }

      public DoubleFormat.Builder formatSymbols(DecimalFormatSymbols symbols) {
         Objects.requireNonNull(symbols, "symbols");
         return this.digits(getDigitString(symbols))
            .decimalSeparator(symbols.getDecimalSeparator())
            .groupingSeparator(symbols.getGroupingSeparator())
            .minusSign(symbols.getMinusSign())
            .exponentSeparator(symbols.getExponentSeparator())
            .infinity(symbols.getInfinity())
            .nan(symbols.getNaN());
      }

      public DoubleFunction<String> get() {
         return this.factory.apply(this);
      }

      public DoubleFormat.Builder groupingSeparator(char groupingSeparator) {
         this.groupingSeparator = groupingSeparator;
         return this;
      }

      public DoubleFormat.Builder groupThousands(boolean groupThousands) {
         this.groupThousands = groupThousands;
         return this;
      }

      public DoubleFormat.Builder includeFractionPlaceholder(boolean fractionPlaceholder) {
         this.fractionPlaceholder = fractionPlaceholder;
         return this;
      }

      public DoubleFormat.Builder infinity(String infinity) {
         this.infinity = Objects.requireNonNull(infinity, "infinity");
         return this;
      }

      public DoubleFormat.Builder maxPrecision(int maxPrecision) {
         this.maxPrecision = maxPrecision;
         return this;
      }

      public DoubleFormat.Builder minDecimalExponent(int minDecimalExponent) {
         this.minDecimalExponent = minDecimalExponent;
         return this;
      }

      public DoubleFormat.Builder minusSign(char minusSign) {
         this.minusSign = minusSign;
         return this;
      }

      public DoubleFormat.Builder nan(String nan) {
         this.nan = Objects.requireNonNull(nan, "nan");
         return this;
      }

      public DoubleFormat.Builder plainFormatMaxDecimalExponent(int plainFormatMaxDecimalExponent) {
         this.plainFormatMaxDecimalExponent = plainFormatMaxDecimalExponent;
         return this;
      }

      public DoubleFormat.Builder plainFormatMinDecimalExponent(int plainFormatMinDecimalExponent) {
         this.plainFormatMinDecimalExponent = plainFormatMinDecimalExponent;
         return this;
      }
   }

   private static final class EngineeringDoubleFormat extends DoubleFormat.AbstractDoubleFormat {
      EngineeringDoubleFormat(DoubleFormat.Builder builder) {
         super(builder);
      }

      @Override
      public String applyFiniteInternal(ParsedDecimal val) {
         return val.toEngineeringString(this);
      }
   }

   private static final class MixedDoubleFormat extends DoubleFormat.AbstractDoubleFormat {
      private final int plainMaxExponent;
      private final int plainMinExponent;

      MixedDoubleFormat(DoubleFormat.Builder builder) {
         super(builder);
         this.plainMaxExponent = builder.plainFormatMaxDecimalExponent;
         this.plainMinExponent = builder.plainFormatMinDecimalExponent;
      }

      @Override
      protected String applyFiniteInternal(ParsedDecimal val) {
         int sciExp = val.getScientificExponent();
         return sciExp <= this.plainMaxExponent && sciExp >= this.plainMinExponent ? val.toPlainString(this) : val.toScientificString(this);
      }
   }

   private static final class PlainDoubleFormat extends DoubleFormat.AbstractDoubleFormat {
      PlainDoubleFormat(DoubleFormat.Builder builder) {
         super(builder);
      }

      @Override
      protected String applyFiniteInternal(ParsedDecimal val) {
         return val.toPlainString(this);
      }
   }

   private static final class ScientificDoubleFormat extends DoubleFormat.AbstractDoubleFormat {
      ScientificDoubleFormat(DoubleFormat.Builder builder) {
         super(builder);
      }

      @Override
      public String applyFiniteInternal(ParsedDecimal val) {
         return val.toScientificString(this);
      }
   }
}
