package com.dfsek.terra.lib.commons.lang3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumUtils {
   private static final String CANNOT_STORE_S_S_VALUES_IN_S_BITS = "Cannot store %s %s values in %s bits";
   private static final String ENUM_CLASS_MUST_BE_DEFINED = "EnumClass must be defined.";
   private static final String NULL_ELEMENTS_NOT_PERMITTED = "null elements not permitted";
   private static final String S_DOES_NOT_SEEM_TO_BE_AN_ENUM_TYPE = "%s does not seem to be an Enum type";

   private static <E extends Enum<E>> Class<E> asEnum(Class<E> enumClass) {
      Objects.requireNonNull(enumClass, "EnumClass must be defined.");
      Validate.isTrue(enumClass.isEnum(), "%s does not seem to be an Enum type", enumClass);
      return enumClass;
   }

   private static <E extends Enum<E>> Class<E> checkBitVectorable(Class<E> enumClass) {
      E[] constants = (E[])asEnum(enumClass).getEnumConstants();
      Validate.isTrue(constants.length <= 64, "Cannot store %s %s values in %s bits", constants.length, enumClass.getSimpleName(), 64);
      return enumClass;
   }

   @SafeVarargs
   public static <E extends Enum<E>> long generateBitVector(Class<E> enumClass, E... values) {
      Validate.noNullElements(values);
      return generateBitVector(enumClass, Arrays.asList(values));
   }

   public static <E extends Enum<E>> long generateBitVector(Class<E> enumClass, Iterable<? extends E> values) {
      checkBitVectorable(enumClass);
      Objects.requireNonNull(values, "values");
      long total = 0L;

      for (E constant : values) {
         Objects.requireNonNull(constant, "null elements not permitted");
         total |= 1L << constant.ordinal();
      }

      return total;
   }

   @SafeVarargs
   public static <E extends Enum<E>> long[] generateBitVectors(Class<E> enumClass, E... values) {
      asEnum(enumClass);
      Validate.noNullElements(values);
      EnumSet<E> condensed = EnumSet.noneOf(enumClass);
      Collections.addAll(condensed, values);
      long[] result = new long[(((Enum[])enumClass.getEnumConstants()).length - 1) / 64 + 1];

      for (E value : condensed) {
         result[value.ordinal() / 64] |= 1L << value.ordinal() % 64;
      }

      ArrayUtils.reverse(result);
      return result;
   }

   public static <E extends Enum<E>> long[] generateBitVectors(Class<E> enumClass, Iterable<? extends E> values) {
      asEnum(enumClass);
      Objects.requireNonNull(values, "values");
      EnumSet<E> condensed = EnumSet.noneOf(enumClass);
      values.forEach(constant -> condensed.add(Objects.requireNonNull((E)constant, "null elements not permitted")));
      long[] result = new long[(((Enum[])enumClass.getEnumConstants()).length - 1) / 64 + 1];

      for (E value : condensed) {
         result[value.ordinal() / 64] |= 1L << value.ordinal() % 64;
      }

      ArrayUtils.reverse(result);
      return result;
   }

   public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName) {
      return getEnum(enumClass, enumName, null);
   }

   public static <E extends Enum<E>> E getEnum(Class<E> enumClass, String enumName, E defaultEnum) {
      if (enumName == null) {
         return defaultEnum;
      }

      try {
         return Enum.valueOf(enumClass, enumName);
      } catch (IllegalArgumentException ex) {
         return defaultEnum;
      }
   }

   public static <E extends Enum<E>> E getEnumIgnoreCase(Class<E> enumClass, String enumName) {
      return getEnumIgnoreCase(enumClass, enumName, null);
   }

   public static <E extends Enum<E>> E getEnumIgnoreCase(Class<E> enumClass, String enumName, E defaultEnum) {
      return getFirstEnumIgnoreCase(enumClass, enumName, Enum::name, defaultEnum);
   }

   public static <E extends Enum<E>> List<E> getEnumList(Class<E> enumClass) {
      return new ArrayList<>(Arrays.asList(enumClass.getEnumConstants()));
   }

   public static <E extends Enum<E>> Map<String, E> getEnumMap(Class<E> enumClass) {
      return getEnumMap(enumClass, Enum::name);
   }

   public static <E extends Enum<E>, K> Map<K, E> getEnumMap(Class<E> enumClass, Function<E, K> keyFunction) {
      return Stream.of(enumClass.getEnumConstants()).collect(Collectors.toMap(keyFunction::apply, Function.identity()));
   }

   public static <E extends Enum<E>> E getEnumSystemProperty(Class<E> enumClass, String propName, E defaultEnum) {
      return enumClass != null && propName != null ? getEnum(enumClass, System.getProperty(propName), defaultEnum) : defaultEnum;
   }

   public static <E extends Enum<E>> E getFirstEnumIgnoreCase(Class<E> enumClass, String enumName, Function<E, String> stringFunction, E defaultEnum) {
      return enumName != null && enumClass.isEnum()
         ? Stream.of(enumClass.getEnumConstants()).filter(e -> enumName.equalsIgnoreCase(stringFunction.apply((E)e))).findFirst().orElse(defaultEnum)
         : defaultEnum;
   }

   public static <E extends Enum<E>> boolean isValidEnum(Class<E> enumClass, String enumName) {
      return getEnum(enumClass, enumName) != null;
   }

   public static <E extends Enum<E>> boolean isValidEnumIgnoreCase(Class<E> enumClass, String enumName) {
      return getEnumIgnoreCase(enumClass, enumName) != null;
   }

   public static <E extends Enum<E>> EnumSet<E> processBitVector(Class<E> enumClass, long value) {
      checkBitVectorable(enumClass).getEnumConstants();
      return processBitVectors(enumClass, value);
   }

   public static <E extends Enum<E>> EnumSet<E> processBitVectors(Class<E> enumClass, long... values) {
      EnumSet<E> results = EnumSet.noneOf(asEnum(enumClass));
      long[] lvalues = ArrayUtils.clone(Objects.requireNonNull(values, "values"));
      ArrayUtils.reverse(lvalues);

      for (E constant : (Enum[])enumClass.getEnumConstants()) {
         int block = constant.ordinal() / 64;
         if (block < lvalues.length && (lvalues[block] & 1L << constant.ordinal() % 64) != 0L) {
            results.add(constant);
         }
      }

      return results;
   }
}
