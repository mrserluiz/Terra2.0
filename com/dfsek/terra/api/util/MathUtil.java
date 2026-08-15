package com.dfsek.terra.api.util;

import java.util.List;

public final class MathUtil {
   public static final double EPSILON = 1.0E-5;
   private static final int SIN_BITS = 12;
   private static final int SIN_MASK = ~(-1 << SIN_BITS);
   private static final int SIN_COUNT = SIN_MASK + 1;
   private static final double radFull = Math.PI * 2;
   private static final double radToIndex = SIN_COUNT / radFull;
   private static final double degFull = 360.0;
   private static final double degToIndex = SIN_COUNT / degFull;
   private static final double[] sin = new double[SIN_COUNT];
   private static final double[] cos = new double[SIN_COUNT];

   public static double sin(double rad) {
      return sin[(int)(rad * radToIndex) & SIN_MASK];
   }

   public static double cos(double rad) {
      return cos[(int)(rad * radToIndex) & SIN_MASK];
   }

   public static double tan(double rad) {
      return sin(rad) / cos(rad);
   }

   public static double invSqrt(double x) {
      double halfX = 0.5 * x;
      long i = Double.doubleToLongBits(x);
      i = 6910470738111508698L - (i >> 1);
      double y = Double.longBitsToDouble(i);
      return y * (1.5 - halfX * y * y);
   }

   public static double standardDeviation(List<Number> numArray) {
      double sum = 0.0;
      double standardDeviation = 0.0;
      int length = numArray.size();

      for (Number num : numArray) {
         sum += num.doubleValue();
      }

      double mean = sum / length;

      for (Number num : numArray) {
         standardDeviation += Math.pow(num.doubleValue() - mean, 2.0);
      }

      return Math.sqrt(standardDeviation / length);
   }

   public static long hashToLong(String s) {
      if (s == null) {
         return 0L;
      }

      long hash = 0L;

      for (char c : s.toCharArray()) {
         hash = 31L * hash + c;
      }

      return hash;
   }

   public static boolean equals(double a, double b) {
      return a == b || Math.abs(a - b) < 1.0E-5;
   }

   public static int normalizeIndex(double val, int size) {
      return Math.max(Math.min((int)Math.floor((val + 1.0) / 2.0 * size), size - 1), 0);
   }

   public static long squash(int first, int last) {
      return (long)first << 32 | last & 4294967295L;
   }

   public static double clamp(double in) {
      return Math.min(Math.max(in, -1.0), 1.0);
   }

   public static int clamp(int min, int i, int max) {
      return Math.max(Math.min(i, max), min);
   }

   public static double normalInverse(double p, double mu, double sigma) {
      if (p < 0.0 || p > 1.0) {
         throw new IllegalArgumentException("Probability must be in range [0, 1]");
      }

      if (sigma < 0.0) {
         throw new IllegalArgumentException("Standard deviation must be positive.");
      }

      if (p == 0.0) {
         return Double.NEGATIVE_INFINITY;
      }

      if (p == 1.0) {
         return Double.POSITIVE_INFINITY;
      }

      if (sigma == 0.0) {
         return mu;
      }

      double q = p - 0.5;
      double val;
      if (Math.abs(q) <= 0.425) {
         double r = 0.180625 - q * q;
         val = q
            * (
               (
                        (
                                 ((((r * 2509.0809287301227 + 33430.57558358813) * r + 67265.7709270087) * r + 45921.95393154987) * r + 13731.69376550946) * r
                                    + 1971.5909503065513
                              )
                              * r
                           + 133.14166789178438
                     )
                     * r
                  + 3.3871328727963665
            )
            / (
               (
                        (
                                 ((((r * 5226.495278852854 + 28729.085735721943) * r + 39307.89580009271) * r + 21213.794301586597) * r + 5394.196021424751)
                                       * r
                                    + 687.1870074920579
                              )
                              * r
                           + 42.31333070160091
                     )
                     * r
                  + 1.0
            );
      } else {
         double r;
         if (q > 0.0) {
            r = 1.0 - p;
         } else {
            r = p;
         }

         r = Math.sqrt(-Math.log(r));
         if (r <= 5.0) {
            r -= 1.6;
            val = (
                  (
                           (
                                    (
                                             (((r * 7.745450142783414E-4 + 0.022723844989269184) * r + 0.2417807251774506) * r + 1.2704582524523684) * r
                                                + 3.6478483247632045
                                          )
                                          * r
                                       + 5.769497221460691
                                 )
                                 * r
                              + 4.630337846156546
                        )
                        * r
                     + 1.4234371107496835
               )
               / (
                  (
                           (
                                    (
                                             (((r * 1.0507500716444169E-9 + 5.475938084995345E-4) * r + 0.015198666563616457) * r + 0.14810397642748008) * r
                                                + 0.6897673349851
                                          )
                                          * r
                                       + 1.6763848301838038
                                 )
                                 * r
                              + 2.053191626637759
                        )
                        * r
                     + 1.0
               );
         } else {
            r -= 5.0;
            val = (
                  (
                           (
                                    (
                                             (((r * 2.0103343992922881E-7 + 2.7115555687434876E-5) * r + 0.0012426609473880784) * r + 0.026532189526576124) * r
                                                + 0.29656057182850487
                                          )
                                          * r
                                       + 1.7848265399172913
                                 )
                                 * r
                              + 5.463784911164114
                        )
                        * r
                     + 6.657904643501103
               )
               / (
                  (
                           (
                                    (
                                             (((r * 2.0442631033899397E-15 + 1.421511758316446E-7) * r + 1.8463183175100548E-5) * r + 7.868691311456133E-4) * r
                                                + 0.014875361290850615
                                          )
                                          * r
                                       + 0.1369298809227358
                                 )
                                 * r
                              + 0.599832206555888
                        )
                        * r
                     + 1.0
               );
         }

         if (q < 0.0) {
            val = -val;
         }
      }

      return mu + sigma * val;
   }

   public static long murmur64(long h) {
      h ^= h >>> 33;
      h *= -49064778989728563L;
      h ^= h >>> 33;
      h *= -4265267296055464877L;
      return h ^ h >>> 33;
   }

   public static double lerp(double t, double v0, double v1) {
      return v0 + t * (v1 - v0);
   }

   public static double cubicLerp(double a, double b, double c, double d, double t) {
      double p = d - c - (a - b);
      return t * t * t * p + t * t * (a - b - p) + t * (c - a) + b;
   }

   public static double interpHermite(double t) {
      return t * t * (3.0 - 2.0 * t);
   }

   public static double interpQuintic(double t) {
      return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
   }

   static {
      for (int i = 0; i < SIN_COUNT; i++) {
         sin[i] = Math.sin((i + 0.5F) / SIN_COUNT * radFull);
         cos[i] = Math.cos((i + 0.5F) / SIN_COUNT * radFull);
      }

      for (int i = 0; i < 360; i += 90) {
         sin[(int)(i * degToIndex) & SIN_MASK] = Math.sin(i * Math.PI / 180.0);
         cos[(int)(i * degToIndex) & SIN_MASK] = Math.cos(i * Math.PI / 180.0);
      }
   }
}
