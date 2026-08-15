package com.dfsek.paralithic.functions.natives;

public class NativeMath {
   private static final Class<?> MATH = Math.class;
   public static NativeFunction POW = () -> MATH.getMethod("pow", double.class, double.class);
   public static NativeFunction MAX = () -> MATH.getMethod("max", double.class, double.class);
   public static NativeFunction MIN = () -> MATH.getMethod("min", double.class, double.class);
   public static NativeFunction SIN = () -> MATH.getMethod("sin", double.class);
   public static NativeFunction COS = () -> MATH.getMethod("cos", double.class);
   public static NativeFunction TAN = () -> MATH.getMethod("tan", double.class);
   public static NativeFunction ROUND = () -> MATH.getMethod("round", double.class);
   public static NativeFunction FLOOR = () -> MATH.getMethod("floor", double.class);
   public static NativeFunction CEIL = () -> MATH.getMethod("ceil", double.class);
   public static NativeFunction SQRT = () -> MATH.getMethod("sqrt", double.class);
   public static NativeFunction SINH = () -> MATH.getMethod("sinh", double.class);
   public static NativeFunction COSH = () -> MATH.getMethod("cosh", double.class);
   public static NativeFunction TANH = () -> MATH.getMethod("tanh", double.class);
   public static NativeFunction ASIN = () -> MATH.getMethod("asin", double.class);
   public static NativeFunction ACOS = () -> MATH.getMethod("acos", double.class);
   public static NativeFunction ATAN = () -> MATH.getMethod("atan", double.class);
   public static NativeFunction ATAN2 = () -> MATH.getMethod("atan2", double.class, double.class);
   public static NativeFunction DEG = () -> MATH.getMethod("toDegrees", double.class);
   public static NativeFunction RAD = () -> MATH.getMethod("toRadians", double.class);
   public static NativeFunction ABS = () -> MATH.getMethod("abs", double.class);
   public static NativeFunction LOG = () -> MATH.getMethod("log10", double.class);
   public static NativeFunction LN = () -> MATH.getMethod("log", double.class);
   public static NativeFunction EXP = () -> MATH.getMethod("exp", double.class);
   public static NativeFunction SIGN = () -> MATH.getMethod("signum", double.class);
   public static NativeFunction SIGMOID = () -> NativeMath.class.getMethod("sigmoid", double.class, double.class);
   public static NativeFunction POW2 = () -> NativeMath.class.getMethod("pow2", double.class);
   public static NativeFunction INT_POW = () -> NativeMath.class.getMethod("intPow", double.class, double.class);

   public static double pow2(double a) {
      return a * a;
   }

   public static double sigmoid(double a, double b) {
      return 1.0 / Math.exp(-1.0 * a * b);
   }

   public static double intPow(double x, double yd) {
      long y = (long)yd;
      double result = 1.0;

      while (y > 0L) {
         if ((y & 1L) == 0L) {
            x *= x;
            y >>>= 1;
         } else {
            result *= x;
            y--;
         }
      }

      return result;
   }
}
