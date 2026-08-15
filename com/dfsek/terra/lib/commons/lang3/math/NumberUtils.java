package com.dfsek.terra.lib.commons.lang3.math;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import com.dfsek.terra.lib.commons.lang3.Validate;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

public class NumberUtils {
   public static final Long LONG_ZERO = 0L;
   public static final Long LONG_ONE = 1L;
   public static final Long LONG_MINUS_ONE = -1L;
   public static final Integer INTEGER_ZERO = 0;
   public static final Integer INTEGER_ONE = 1;
   public static final Integer INTEGER_TWO = 2;
   public static final Integer INTEGER_MINUS_ONE = -1;
   public static final Short SHORT_ZERO = (short)0;
   public static final Short SHORT_ONE = (short)1;
   public static final Short SHORT_MINUS_ONE = (short)-1;
   public static final Byte BYTE_ZERO = (byte)0;
   public static final Byte BYTE_ONE = (byte)1;
   public static final Byte BYTE_MINUS_ONE = (byte)-1;
   public static final Double DOUBLE_ZERO = 0.0;
   public static final Double DOUBLE_ONE = 1.0;
   public static final Double DOUBLE_MINUS_ONE = -1.0;
   public static final Float FLOAT_ZERO = 0.0F;
   public static final Float FLOAT_ONE = 1.0F;
   public static final Float FLOAT_MINUS_ONE = -1.0F;
   public static final Long LONG_INT_MAX_VALUE = 2147483647L;
   public static final Long LONG_INT_MIN_VALUE = -2147483648L;

   public static int compare(byte x, byte y) {
      return x - y;
   }

   public static int compare(int x, int y) {
      if (x == y) {
         return 0;
      } else {
         return x < y ? -1 : 1;
      }
   }

   public static int compare(long x, long y) {
      if (x == y) {
         return 0;
      } else {
         return x < y ? -1 : 1;
      }
   }

   public static int compare(short x, short y) {
      if (x == y) {
         return 0;
      } else {
         return x < y ? -1 : 1;
      }
   }

   public static BigDecimal createBigDecimal(String str) {
      if (str == null) {
         return null;
      } else if (StringUtils.isBlank(str)) {
         throw new NumberFormatException("A blank string is not a valid number");
      } else {
         return new BigDecimal(str);
      }
   }

   public static BigInteger createBigInteger(String str) {
      if (str == null) {
         return null;
      }

      if (str.isEmpty()) {
         throw new NumberFormatException("An empty string is not a valid number");
      }

      int pos = 0;
      int radix = 10;
      boolean negate = false;
      char char0 = str.charAt(0);
      if (char0 == '-') {
         negate = true;
         pos = 1;
      } else if (char0 == '+') {
         pos = 1;
      }

      if (str.startsWith("0x", pos) || str.startsWith("0X", pos)) {
         radix = 16;
         pos += 2;
      } else if (str.startsWith("#", pos)) {
         radix = 16;
         pos++;
      } else if (str.startsWith("0", pos) && str.length() > pos + 1) {
         radix = 8;
         pos++;
      }

      BigInteger value = new BigInteger(str.substring(pos), radix);
      return negate ? value.negate() : value;
   }

   public static Double createDouble(String str) {
      return str == null ? null : Double.valueOf(str);
   }

   public static Float createFloat(String str) {
      return str == null ? null : Float.valueOf(str);
   }

   public static Integer createInteger(String str) {
      return str == null ? null : Integer.decode(str);
   }

   public static Long createLong(String str) {
      return str == null ? null : Long.decode(str);
   }

   public static Number createNumber(String str) {
      if (str == null) {
         return null;
      }

      if (StringUtils.isBlank(str)) {
         throw new NumberFormatException("A blank string is not a valid number");
      }

      String[] hexPrefixes = new String[]{"0x", "0X", "#"};
      int length = str.length();
      int offset = str.charAt(0) != '+' && str.charAt(0) != '-' ? 0 : 1;
      int pfxLen = 0;

      for (String pfx : hexPrefixes) {
         if (str.startsWith(pfx, offset)) {
            pfxLen += pfx.length() + offset;
            break;
         }
      }

      if (pfxLen > 0) {
         char firstSigDigit = 0;

         for (int i = pfxLen; i < length; i++) {
            firstSigDigit = str.charAt(i);
            if (firstSigDigit != '0') {
               break;
            }

            pfxLen++;
         }

         int hexDigits = length - pfxLen;
         if (hexDigits <= 16 && (hexDigits != 16 || firstSigDigit <= '7')) {
            return hexDigits <= 8 && (hexDigits != 8 || firstSigDigit <= '7') ? createInteger(str) : createLong(str);
         } else {
            return createBigInteger(str);
         }
      } else {
         char lastChar = str.charAt(length - 1);
         int decPos = str.indexOf(46);
         int expPos = str.indexOf(101) + str.indexOf(69) + 1;
         boolean requestType = !Character.isDigit(lastChar) && lastChar != '.';
         String mant;
         String dec;
         if (decPos > -1) {
            if (expPos > -1) {
               if (expPos <= decPos || expPos > length) {
                  throw new NumberFormatException(str + " is not a valid number.");
               }

               dec = str.substring(decPos + 1, expPos);
            } else {
               dec = str.substring(decPos + 1, requestType ? length - 1 : length);
            }

            mant = getMantissa(str, decPos);
         } else {
            if (expPos > -1) {
               if (expPos > length) {
                  throw new NumberFormatException(str + " is not a valid number.");
               }

               mant = getMantissa(str, expPos);
            } else {
               mant = getMantissa(str, requestType ? length - 1 : length);
            }

            dec = null;
         }

         if (requestType) {
            String exp;
            if (expPos > -1 && expPos < length - 1) {
               exp = str.substring(expPos + 1, length - 1);
            } else {
               exp = null;
            }

            String numeric = str.substring(0, length - 1);
            switch (lastChar) {
               case 'D':
               case 'd':
                  break;
               case 'F':
               case 'f':
                  try {
                     Float f = createFloat(str);
                     if (f.isInfinite() || f == 0.0F && !isZero(mant, dec)) {
                        break;
                     }

                     return f;
                  } catch (NumberFormatException var20) {
                     break;
                  }
               case 'L':
               case 'l':
                  if (dec == null && exp == null && (!numeric.isEmpty() && numeric.charAt(0) == '-' && isDigits(numeric.substring(1)) || isDigits(numeric))) {
                     try {
                        return createLong(numeric);
                     } catch (NumberFormatException var16) {
                        return createBigInteger(numeric);
                     }
                  }

                  throw new NumberFormatException(str + " is not a valid number.");
               default:
                  throw new NumberFormatException(str + " is not a valid number.");
            }

            try {
               Double d = createDouble(str);
               if (!d.isInfinite() && (d != 0.0 || isZero(mant, dec))) {
                  return d;
               }
            } catch (NumberFormatException var19) {
            }

            try {
               return createBigDecimal(numeric);
            } catch (NumberFormatException var18) {
               throw new NumberFormatException(str + " is not a valid number.");
            }
         } else {
            String exp;
            if (expPos > -1 && expPos < length - 1) {
               exp = str.substring(expPos + 1);
            } else {
               exp = null;
            }

            if (dec == null && exp == null) {
               try {
                  return createInteger(str);
               } catch (NumberFormatException var17) {
                  try {
                     return createLong(str);
                  } catch (NumberFormatException var15) {
                     return createBigInteger(str);
                  }
               }
            } else {
               try {
                  Float f = createFloat(str);
                  Double d = createDouble(str);
                  if (!f.isInfinite() && (f != 0.0F || isZero(mant, dec)) && f.toString().equals(d.toString())) {
                     return f;
                  }

                  if (!d.isInfinite() && (d != 0.0 || isZero(mant, dec))) {
                     BigDecimal b = createBigDecimal(str);
                     if (b.compareTo(BigDecimal.valueOf(d)) == 0) {
                        return d;
                     }

                     return b;
                  }
               } catch (NumberFormatException var21) {
               }

               return createBigDecimal(str);
            }
         }
      }
   }

   private static String getMantissa(String str, int stopPos) {
      char firstChar = str.charAt(0);
      boolean hasSign = firstChar == '-' || firstChar == '+';
      int length = str.length();
      if (length > (hasSign ? 1 : 0) && length >= stopPos) {
         return hasSign ? str.substring(1, stopPos) : str.substring(0, stopPos);
      } else {
         throw new NumberFormatException(str + " is not a valid number.");
      }
   }

   private static boolean isAllZeros(String str) {
      if (str == null) {
         return true;
      }

      for (int i = str.length() - 1; i >= 0; i--) {
         if (str.charAt(i) != '0') {
            return false;
         }
      }

      return true;
   }

   public static boolean isCreatable(String str) {
      if (StringUtils.isEmpty(str)) {
         return false;
      }

      char[] chars = str.toCharArray();
      int sz = chars.length;
      boolean hasExp = false;
      boolean hasDecPoint = false;
      boolean allowSigns = false;
      boolean foundDigit = false;
      int start = chars[0] != '-' && chars[0] != '+' ? 0 : 1;
      if (sz > start + 1 && chars[start] == '0' && !StringUtils.contains(str, 46)) {
         if (chars[start + 1] == 'x' || chars[start + 1] == 'X') {
            int i = start + 2;
            if (i == sz) {
               return false;
            }

            while (i < chars.length) {
               if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                  return false;
               }

               i++;
            }

            return true;
         }

         if (Character.isDigit(chars[start + 1])) {
            for (int i = start + 1; i < chars.length; i++) {
               if (chars[i] < '0' || chars[i] > '7') {
                  return false;
               }
            }

            return true;
         }
      }

      sz--;

      int i;
      for (i = start; i < sz || i < sz + 1 && allowSigns && !foundDigit; i++) {
         if (chars[i] >= '0' && chars[i] <= '9') {
            foundDigit = true;
            allowSigns = false;
         } else if (chars[i] == '.') {
            if (hasDecPoint || hasExp) {
               return false;
            }

            hasDecPoint = true;
         } else if (chars[i] != 'e' && chars[i] != 'E') {
            if (chars[i] != '+' && chars[i] != '-') {
               return false;
            }

            if (!allowSigns) {
               return false;
            }

            allowSigns = false;
            foundDigit = false;
         } else {
            if (hasExp) {
               return false;
            }

            if (!foundDigit) {
               return false;
            }

            hasExp = true;
            allowSigns = true;
         }
      }

      if (i >= chars.length) {
         return !allowSigns && foundDigit;
      } else if (chars[i] >= '0' && chars[i] <= '9') {
         return true;
      } else if (chars[i] == 'e' || chars[i] == 'E') {
         return false;
      } else if (chars[i] == '.') {
         return !hasDecPoint && !hasExp ? foundDigit : false;
      } else if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
         return foundDigit;
      } else {
         return chars[i] != 'l' && chars[i] != 'L' ? false : foundDigit && !hasExp && !hasDecPoint;
      }
   }

   public static boolean isDigits(String str) {
      return StringUtils.isNumeric(str);
   }

   @Deprecated
   public static boolean isNumber(String str) {
      return isCreatable(str);
   }

   public static boolean isParsable(String str) {
      if (StringUtils.isEmpty(str)) {
         return false;
      } else if (str.charAt(str.length() - 1) == '.') {
         return false;
      } else if (str.charAt(0) == '-') {
         return str.length() == 1 ? false : withDecimalsParsing(str, 1);
      } else {
         return withDecimalsParsing(str, 0);
      }
   }

   private static boolean isZero(String mant, String dec) {
      return isAllZeros(mant) && isAllZeros(dec);
   }

   public static byte max(byte... array) {
      validateArray(array);
      byte max = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] > max) {
            max = array[i];
         }
      }

      return max;
   }

   public static byte max(byte a, byte b, byte c) {
      if (b > a) {
         a = b;
      }

      if (c > a) {
         a = c;
      }

      return a;
   }

   public static double max(double... array) {
      validateArray(array);
      double max = array[0];

      for (int j = 1; j < array.length; j++) {
         if (Double.isNaN(array[j])) {
            return Double.NaN;
         }

         if (array[j] > max) {
            max = array[j];
         }
      }

      return max;
   }

   public static double max(double a, double b, double c) {
      return Math.max(Math.max(a, b), c);
   }

   public static float max(float... array) {
      validateArray(array);
      float max = array[0];

      for (int j = 1; j < array.length; j++) {
         if (Float.isNaN(array[j])) {
            return Float.NaN;
         }

         if (array[j] > max) {
            max = array[j];
         }
      }

      return max;
   }

   public static float max(float a, float b, float c) {
      return Math.max(Math.max(a, b), c);
   }

   public static int max(int... array) {
      validateArray(array);
      int max = array[0];

      for (int j = 1; j < array.length; j++) {
         if (array[j] > max) {
            max = array[j];
         }
      }

      return max;
   }

   public static int max(int a, int b, int c) {
      if (b > a) {
         a = b;
      }

      if (c > a) {
         a = c;
      }

      return a;
   }

   public static long max(long... array) {
      validateArray(array);
      long max = array[0];

      for (int j = 1; j < array.length; j++) {
         if (array[j] > max) {
            max = array[j];
         }
      }

      return max;
   }

   public static long max(long a, long b, long c) {
      if (b > a) {
         a = b;
      }

      if (c > a) {
         a = c;
      }

      return a;
   }

   public static short max(short... array) {
      validateArray(array);
      short max = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] > max) {
            max = array[i];
         }
      }

      return max;
   }

   public static short max(short a, short b, short c) {
      if (b > a) {
         a = b;
      }

      if (c > a) {
         a = c;
      }

      return a;
   }

   public static byte min(byte... array) {
      validateArray(array);
      byte min = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] < min) {
            min = array[i];
         }
      }

      return min;
   }

   public static byte min(byte a, byte b, byte c) {
      if (b < a) {
         a = b;
      }

      if (c < a) {
         a = c;
      }

      return a;
   }

   public static double min(double... array) {
      validateArray(array);
      double min = array[0];

      for (int i = 1; i < array.length; i++) {
         if (Double.isNaN(array[i])) {
            return Double.NaN;
         }

         if (array[i] < min) {
            min = array[i];
         }
      }

      return min;
   }

   public static double min(double a, double b, double c) {
      return Math.min(Math.min(a, b), c);
   }

   public static float min(float... array) {
      validateArray(array);
      float min = array[0];

      for (int i = 1; i < array.length; i++) {
         if (Float.isNaN(array[i])) {
            return Float.NaN;
         }

         if (array[i] < min) {
            min = array[i];
         }
      }

      return min;
   }

   public static float min(float a, float b, float c) {
      return Math.min(Math.min(a, b), c);
   }

   public static int min(int... array) {
      validateArray(array);
      int min = array[0];

      for (int j = 1; j < array.length; j++) {
         if (array[j] < min) {
            min = array[j];
         }
      }

      return min;
   }

   public static int min(int a, int b, int c) {
      if (b < a) {
         a = b;
      }

      if (c < a) {
         a = c;
      }

      return a;
   }

   public static long min(long... array) {
      validateArray(array);
      long min = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] < min) {
            min = array[i];
         }
      }

      return min;
   }

   public static long min(long a, long b, long c) {
      if (b < a) {
         a = b;
      }

      if (c < a) {
         a = c;
      }

      return a;
   }

   public static short min(short... array) {
      validateArray(array);
      short min = array[0];

      for (int i = 1; i < array.length; i++) {
         if (array[i] < min) {
            min = array[i];
         }
      }

      return min;
   }

   public static short min(short a, short b, short c) {
      if (b < a) {
         a = b;
      }

      if (c < a) {
         a = c;
      }

      return a;
   }

   public static byte toByte(String str) {
      return toByte(str, (byte)0);
   }

   public static byte toByte(String str, byte defaultValue) {
      try {
         return Byte.parseByte(str);
      } catch (RuntimeException e) {
         return defaultValue;
      }
   }

   public static double toDouble(BigDecimal value) {
      return toDouble(value, 0.0);
   }

   public static double toDouble(BigDecimal value, double defaultValue) {
      return value == null ? defaultValue : value.doubleValue();
   }

   public static double toDouble(String str) {
      return toDouble(str, 0.0);
   }

   public static double toDouble(String str, double defaultValue) {
      try {
         return Double.parseDouble(str);
      } catch (RuntimeException e) {
         return defaultValue;
      }
   }

   public static float toFloat(String str) {
      return toFloat(str, 0.0F);
   }

   public static float toFloat(String str, float defaultValue) {
      try {
         return Float.parseFloat(str);
      } catch (RuntimeException e) {
         return defaultValue;
      }
   }

   public static int toInt(String str) {
      return toInt(str, 0);
   }

   public static int toInt(String str, int defaultValue) {
      try {
         return Integer.parseInt(str);
      } catch (RuntimeException e) {
         return defaultValue;
      }
   }

   public static long toLong(String str) {
      return toLong(str, 0L);
   }

   public static long toLong(String str, long defaultValue) {
      try {
         return Long.parseLong(str);
      } catch (RuntimeException e) {
         return defaultValue;
      }
   }

   public static BigDecimal toScaledBigDecimal(BigDecimal value) {
      return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
   }

   public static BigDecimal toScaledBigDecimal(BigDecimal value, int scale, RoundingMode roundingMode) {
      return value == null ? BigDecimal.ZERO : value.setScale(scale, roundingMode == null ? RoundingMode.HALF_EVEN : roundingMode);
   }

   public static BigDecimal toScaledBigDecimal(Double value) {
      return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
   }

   public static BigDecimal toScaledBigDecimal(Double value, int scale, RoundingMode roundingMode) {
      return value == null ? BigDecimal.ZERO : toScaledBigDecimal(BigDecimal.valueOf(value), scale, roundingMode);
   }

   public static BigDecimal toScaledBigDecimal(Float value) {
      return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
   }

   public static BigDecimal toScaledBigDecimal(Float value, int scale, RoundingMode roundingMode) {
      return value == null ? BigDecimal.ZERO : toScaledBigDecimal(BigDecimal.valueOf(value.floatValue()), scale, roundingMode);
   }

   public static BigDecimal toScaledBigDecimal(String value) {
      return toScaledBigDecimal(value, INTEGER_TWO, RoundingMode.HALF_EVEN);
   }

   public static BigDecimal toScaledBigDecimal(String value, int scale, RoundingMode roundingMode) {
      return value == null ? BigDecimal.ZERO : toScaledBigDecimal(createBigDecimal(value), scale, roundingMode);
   }

   public static short toShort(String str) {
      return toShort(str, (short)0);
   }

   public static short toShort(String str, short defaultValue) {
      try {
         return Short.parseShort(str);
      } catch (RuntimeException e) {
         return defaultValue;
      }
   }

   private static void validateArray(Object array) {
      Objects.requireNonNull(array, "array");
      Validate.isTrue(Array.getLength(array) != 0, "Array cannot be empty.");
   }

   private static boolean withDecimalsParsing(String str, int beginIdx) {
      int decimalPoints = 0;

      for (int i = beginIdx; i < str.length(); i++) {
         char ch = str.charAt(i);
         boolean isDecimalPoint = ch == '.';
         if (isDecimalPoint) {
            decimalPoints++;
         }

         if (decimalPoints > 1) {
            return false;
         }

         if (!isDecimalPoint && !Character.isDigit(ch)) {
            return false;
         }
      }

      return true;
   }
}
