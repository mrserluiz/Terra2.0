package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.math.NumberUtils;

public enum JavaVersion {
   JAVA_0_9(1.5F, "0.9"),
   JAVA_1_1(1.1F, "1.1"),
   JAVA_1_2(1.2F, "1.2"),
   JAVA_1_3(1.3F, "1.3"),
   JAVA_1_4(1.4F, "1.4"),
   JAVA_1_5(1.5F, "1.5"),
   JAVA_1_6(1.6F, "1.6"),
   JAVA_1_7(1.7F, "1.7"),
   JAVA_1_8(1.8F, "1.8"),
   @Deprecated
   JAVA_1_9(9.0F, "9"),
   JAVA_9(9.0F, "9"),
   JAVA_10(10.0F, "10"),
   JAVA_11(11.0F, "11"),
   JAVA_12(12.0F, "12"),
   JAVA_13(13.0F, "13"),
   JAVA_14(14.0F, "14"),
   JAVA_15(15.0F, "15"),
   JAVA_16(16.0F, "16"),
   JAVA_17(17.0F, "17"),
   JAVA_18(18.0F, "18"),
   JAVA_19(19.0F, "19"),
   JAVA_20(20.0F, "20"),
   JAVA_21(21.0F, "21"),
   JAVA_22(22.0F, "22"),
   JAVA_RECENT(maxVersion(), Float.toString(maxVersion()));

   private static final String VERSION_SPLIT_REGEX = "\\.";
   private final float value;
   private final String name;

   static JavaVersion get(String versionStr) {
      if (versionStr == null) {
         return null;
      }

      switch (versionStr) {
         case "0.9":
            return JAVA_0_9;
         case "1.1":
            return JAVA_1_1;
         case "1.2":
            return JAVA_1_2;
         case "1.3":
            return JAVA_1_3;
         case "1.4":
            return JAVA_1_4;
         case "1.5":
            return JAVA_1_5;
         case "1.6":
            return JAVA_1_6;
         case "1.7":
            return JAVA_1_7;
         case "1.8":
            return JAVA_1_8;
         case "9":
            return JAVA_9;
         case "10":
            return JAVA_10;
         case "11":
            return JAVA_11;
         case "12":
            return JAVA_12;
         case "13":
            return JAVA_13;
         case "14":
            return JAVA_14;
         case "15":
            return JAVA_15;
         case "16":
            return JAVA_16;
         case "17":
            return JAVA_17;
         case "18":
            return JAVA_18;
         case "19":
            return JAVA_19;
         case "20":
            return JAVA_20;
         case "21":
            return JAVA_21;
         case "22":
            return JAVA_22;
         default:
            float v = toFloatVersion(versionStr);
            if (v - 1.0 < 1.0) {
               int firstComma = Math.max(versionStr.indexOf(46), versionStr.indexOf(44));
               int end = Math.max(versionStr.length(), versionStr.indexOf(44, firstComma));
               if (Float.parseFloat(versionStr.substring(firstComma + 1, end)) > 0.9F) {
                  return JAVA_RECENT;
               }
            } else if (v > 10.0F) {
               return JAVA_RECENT;
            }

            return null;
      }
   }

   static JavaVersion getJavaVersion(String versionStr) {
      return get(versionStr);
   }

   private static float maxVersion() {
      float v = toFloatVersion(SystemProperties.getJavaSpecificationVersion("99.0"));
      return v > 0.0F ? v : 99.0F;
   }

   static String[] split(String value) {
      return value.split("\\.");
   }

   private static float toFloatVersion(String value) {
      int defaultReturnValue = -1;
      if (!value.contains(".")) {
         return NumberUtils.toFloat(value, -1.0F);
      }

      String[] toParse = split(value);
      return toParse.length >= 2 ? NumberUtils.toFloat(toParse[0] + '.' + toParse[1], -1.0F) : -1.0F;
   }

   JavaVersion(float value, String name) {
      this.value = value;
      this.name = name;
   }

   public boolean atLeast(JavaVersion requiredVersion) {
      return this.value >= requiredVersion.value;
   }

   public boolean atMost(JavaVersion requiredVersion) {
      return this.value <= requiredVersion.value;
   }

   @Override
   public String toString() {
      return this.name;
   }
}
