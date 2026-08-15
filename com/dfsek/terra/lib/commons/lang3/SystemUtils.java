package com.dfsek.terra.lib.commons.lang3;

import java.io.File;

public class SystemUtils {
   private static final String OS_NAME_WINDOWS_PREFIX = "Windows";
   public static final String FILE_ENCODING = SystemProperties.getFileEncoding();
   @Deprecated
   public static final String FILE_SEPARATOR = SystemProperties.getFileSeparator();
   public static final String JAVA_AWT_FONTS = SystemProperties.getJavaAwtFonts();
   public static final String JAVA_AWT_GRAPHICSENV = SystemProperties.getJavaAwtGraphicsenv();
   public static final String JAVA_AWT_HEADLESS = SystemProperties.getJavaAwtHeadless();
   public static final String JAVA_AWT_PRINTERJOB = SystemProperties.getJavaAwtPrinterjob();
   public static final String JAVA_CLASS_PATH = SystemProperties.getJavaClassPath();
   public static final String JAVA_CLASS_VERSION = SystemProperties.getJavaClassVersion();
   public static final String JAVA_COMPILER = SystemProperties.getJavaCompiler();
   public static final String JAVA_ENDORSED_DIRS = SystemProperties.getJavaEndorsedDirs();
   public static final String JAVA_EXT_DIRS = SystemProperties.getJavaExtDirs();
   public static final String JAVA_HOME = SystemProperties.getJavaHome();
   public static final String JAVA_IO_TMPDIR = SystemProperties.getJavaIoTmpdir();
   public static final String JAVA_LIBRARY_PATH = SystemProperties.getJavaLibraryPath();
   public static final String JAVA_RUNTIME_NAME = SystemProperties.getJavaRuntimeName();
   public static final String JAVA_RUNTIME_VERSION = SystemProperties.getJavaRuntimeVersion();
   public static final String JAVA_SPECIFICATION_NAME = SystemProperties.getJavaSpecificationName();
   public static final String JAVA_SPECIFICATION_VENDOR = SystemProperties.getJavaSpecificationVendor();
   public static final String JAVA_SPECIFICATION_VERSION = SystemProperties.getJavaSpecificationVersion();
   private static final JavaVersion JAVA_SPECIFICATION_VERSION_AS_ENUM = JavaVersion.get(JAVA_SPECIFICATION_VERSION);
   public static final String JAVA_UTIL_PREFS_PREFERENCES_FACTORY = SystemProperties.getJavaUtilPrefsPreferencesFactory();
   public static final String JAVA_VENDOR = SystemProperties.getJavaVendor();
   public static final String JAVA_VENDOR_URL = SystemProperties.getJavaVendorUrl();
   public static final String JAVA_VERSION = SystemProperties.getJavaVersion();
   public static final String JAVA_VM_INFO = SystemProperties.getJavaVmInfo();
   public static final String JAVA_VM_NAME = SystemProperties.getJavaVmName();
   public static final String JAVA_VM_SPECIFICATION_NAME = SystemProperties.getJavaVmSpecificationName();
   public static final String JAVA_VM_SPECIFICATION_VENDOR = SystemProperties.getJavaVmSpecificationVendor();
   public static final String JAVA_VM_SPECIFICATION_VERSION = SystemProperties.getJavaVmSpecificationVersion();
   public static final String JAVA_VM_VENDOR = SystemProperties.getJavaVmVendor();
   public static final String JAVA_VM_VERSION = SystemProperties.getJavaVmVersion();
   @Deprecated
   public static final String LINE_SEPARATOR = SystemProperties.getLineSeparator();
   public static final String OS_ARCH = SystemProperties.getOsArch();
   public static final String OS_NAME = SystemProperties.getOsName();
   public static final String OS_VERSION = SystemProperties.getOsVersion();
   @Deprecated
   public static final String PATH_SEPARATOR = SystemProperties.getPathSeparator();
   public static final String USER_COUNTRY = SystemProperties.getProperty("user.country", () -> SystemProperties.getProperty("user.region"));
   public static final String USER_DIR = SystemProperties.getUserDir();
   public static final String USER_HOME = SystemProperties.getUserHome();
   public static final String USER_LANGUAGE = SystemProperties.getUserLanguage();
   public static final String USER_NAME = SystemProperties.getUserName();
   public static final String USER_TIMEZONE = SystemProperties.getUserTimezone();
   public static final boolean IS_JAVA_1_1 = getJavaVersionMatches("1.1");
   public static final boolean IS_JAVA_1_2 = getJavaVersionMatches("1.2");
   public static final boolean IS_JAVA_1_3 = getJavaVersionMatches("1.3");
   public static final boolean IS_JAVA_1_4 = getJavaVersionMatches("1.4");
   public static final boolean IS_JAVA_1_5 = getJavaVersionMatches("1.5");
   public static final boolean IS_JAVA_1_6 = getJavaVersionMatches("1.6");
   public static final boolean IS_JAVA_1_7 = getJavaVersionMatches("1.7");
   public static final boolean IS_JAVA_1_8 = getJavaVersionMatches("1.8");
   @Deprecated
   public static final boolean IS_JAVA_1_9 = getJavaVersionMatches("9");
   public static final boolean IS_JAVA_9 = getJavaVersionMatches("9");
   public static final boolean IS_JAVA_10 = getJavaVersionMatches("10");
   public static final boolean IS_JAVA_11 = getJavaVersionMatches("11");
   public static final boolean IS_JAVA_12 = getJavaVersionMatches("12");
   public static final boolean IS_JAVA_13 = getJavaVersionMatches("13");
   public static final boolean IS_JAVA_14 = getJavaVersionMatches("14");
   public static final boolean IS_JAVA_15 = getJavaVersionMatches("15");
   public static final boolean IS_JAVA_16 = getJavaVersionMatches("16");
   public static final boolean IS_JAVA_17 = getJavaVersionMatches("17");
   public static final boolean IS_JAVA_18 = getJavaVersionMatches("18");
   public static final boolean IS_JAVA_19 = getJavaVersionMatches("19");
   public static final boolean IS_JAVA_20 = getJavaVersionMatches("20");
   public static final boolean IS_JAVA_21 = getJavaVersionMatches("21");
   public static final boolean IS_JAVA_22 = getJavaVersionMatches("22");
   public static final boolean IS_OS_AIX = getOsMatchesName("AIX");
   public static final boolean IS_OS_ANDROID = SystemProperties.getJavaVendor().contains("Android");
   public static final boolean IS_OS_HP_UX = getOsMatchesName("HP-UX");
   public static final boolean IS_OS_400 = getOsMatchesName("OS/400");
   public static final boolean IS_OS_IRIX = getOsMatchesName("Irix");
   public static final boolean IS_OS_LINUX = getOsMatchesName("Linux") || getOsMatchesName("LINUX");
   public static final boolean IS_OS_MAC = getOsMatchesName("Mac");
   public static final boolean IS_OS_MAC_OSX = getOsMatchesName("Mac OS X");
   public static final boolean IS_OS_MAC_OSX_CHEETAH = getOsMatches("Mac OS X", "10.0");
   public static final boolean IS_OS_MAC_OSX_PUMA = getOsMatches("Mac OS X", "10.1");
   public static final boolean IS_OS_MAC_OSX_JAGUAR = getOsMatches("Mac OS X", "10.2");
   public static final boolean IS_OS_MAC_OSX_PANTHER = getOsMatches("Mac OS X", "10.3");
   public static final boolean IS_OS_MAC_OSX_TIGER = getOsMatches("Mac OS X", "10.4");
   public static final boolean IS_OS_MAC_OSX_LEOPARD = getOsMatches("Mac OS X", "10.5");
   public static final boolean IS_OS_MAC_OSX_SNOW_LEOPARD = getOsMatches("Mac OS X", "10.6");
   public static final boolean IS_OS_MAC_OSX_LION = getOsMatches("Mac OS X", "10.7");
   public static final boolean IS_OS_MAC_OSX_MOUNTAIN_LION = getOsMatches("Mac OS X", "10.8");
   public static final boolean IS_OS_MAC_OSX_MAVERICKS = getOsMatches("Mac OS X", "10.9");
   public static final boolean IS_OS_MAC_OSX_YOSEMITE = getOsMatches("Mac OS X", "10.10");
   public static final boolean IS_OS_MAC_OSX_EL_CAPITAN = getOsMatches("Mac OS X", "10.11");
   public static final boolean IS_OS_MAC_OSX_SIERRA = getOsMatches("Mac OS X", "10.12");
   public static final boolean IS_OS_MAC_OSX_HIGH_SIERRA = getOsMatches("Mac OS X", "10.13");
   public static final boolean IS_OS_MAC_OSX_MOJAVE = getOsMatches("Mac OS X", "10.14");
   public static final boolean IS_OS_MAC_OSX_CATALINA = getOsMatches("Mac OS X", "10.15");
   public static final boolean IS_OS_MAC_OSX_BIG_SUR = getOsMatches("Mac OS X", "11");
   public static final boolean IS_OS_MAC_OSX_MONTEREY = getOsMatches("Mac OS X", "12");
   public static final boolean IS_OS_MAC_OSX_VENTURA = getOsMatches("Mac OS X", "13");
   public static final boolean IS_OS_MAC_OSX_SONOMA = getOsMatches("Mac OS X", "14");
   public static final boolean IS_OS_FREE_BSD = getOsMatchesName("FreeBSD");
   public static final boolean IS_OS_OPEN_BSD = getOsMatchesName("OpenBSD");
   public static final boolean IS_OS_NET_BSD = getOsMatchesName("NetBSD");
   public static final boolean IS_OS_OS2 = getOsMatchesName("OS/2");
   public static final boolean IS_OS_SOLARIS = getOsMatchesName("Solaris");
   public static final boolean IS_OS_SUN_OS = getOsMatchesName("SunOS");
   public static final boolean IS_OS_UNIX = IS_OS_AIX
      || IS_OS_HP_UX
      || IS_OS_IRIX
      || IS_OS_LINUX
      || IS_OS_MAC_OSX
      || IS_OS_SOLARIS
      || IS_OS_SUN_OS
      || IS_OS_FREE_BSD
      || IS_OS_OPEN_BSD
      || IS_OS_NET_BSD;
   public static final boolean IS_OS_WINDOWS = getOsMatchesName("Windows");
   public static final boolean IS_OS_WINDOWS_2000 = getOsMatchesName("Windows 2000");
   public static final boolean IS_OS_WINDOWS_2003 = getOsMatchesName("Windows 2003");
   public static final boolean IS_OS_WINDOWS_2008 = getOsMatchesName("Windows Server 2008");
   public static final boolean IS_OS_WINDOWS_2012 = getOsMatchesName("Windows Server 2012");
   public static final boolean IS_OS_WINDOWS_95 = getOsMatchesName("Windows 95");
   public static final boolean IS_OS_WINDOWS_98 = getOsMatchesName("Windows 98");
   public static final boolean IS_OS_WINDOWS_ME = getOsMatchesName("Windows Me");
   public static final boolean IS_OS_WINDOWS_NT = getOsMatchesName("Windows NT");
   public static final boolean IS_OS_WINDOWS_XP = getOsMatchesName("Windows XP");
   public static final boolean IS_OS_WINDOWS_VISTA = getOsMatchesName("Windows Vista");
   public static final boolean IS_OS_WINDOWS_7 = getOsMatchesName("Windows 7");
   public static final boolean IS_OS_WINDOWS_8 = getOsMatchesName("Windows 8");
   public static final boolean IS_OS_WINDOWS_10 = getOsMatchesName("Windows 10");
   public static final boolean IS_OS_WINDOWS_11 = getOsMatchesName("Windows 11");
   public static final boolean IS_OS_ZOS = getOsMatchesName("z/OS");
   public static final String USER_HOME_KEY = "user.home";
   @Deprecated
   public static final String USER_NAME_KEY = "user.name";
   @Deprecated
   public static final String USER_DIR_KEY = "user.dir";
   @Deprecated
   public static final String JAVA_IO_TMPDIR_KEY = "java.io.tmpdir";
   @Deprecated
   public static final String JAVA_HOME_KEY = "java.home";
   public static final String AWT_TOOLKIT = SystemProperties.getAwtToolkit();

   public static String getEnvironmentVariable(String name, String defaultValue) {
      try {
         String value = System.getenv(name);
         return value == null ? defaultValue : value;
      } catch (SecurityException ex) {
         return defaultValue;
      }
   }

   public static String getHostName() {
      return IS_OS_WINDOWS ? System.getenv("COMPUTERNAME") : System.getenv("HOSTNAME");
   }

   public static File getJavaHome() {
      return new File(SystemProperties.getJavaHome());
   }

   public static File getJavaIoTmpDir() {
      return new File(SystemProperties.getJavaIoTmpdir());
   }

   private static boolean getJavaVersionMatches(String versionPrefix) {
      return isJavaVersionMatch(JAVA_SPECIFICATION_VERSION, versionPrefix);
   }

   private static boolean getOsMatches(String osNamePrefix, String osVersionPrefix) {
      return isOsMatch(OS_NAME, OS_VERSION, osNamePrefix, osVersionPrefix);
   }

   private static boolean getOsMatchesName(String osNamePrefix) {
      return isOsNameMatch(OS_NAME, osNamePrefix);
   }

   public static File getUserDir() {
      return new File(SystemProperties.getUserDir());
   }

   public static File getUserHome() {
      return new File(SystemProperties.getUserHome());
   }

   @Deprecated
   public static String getUserName() {
      return SystemProperties.getUserName();
   }

   @Deprecated
   public static String getUserName(String defaultValue) {
      return SystemProperties.getUserName(defaultValue);
   }

   public static boolean isJavaAwtHeadless() {
      return Boolean.TRUE.toString().equals(JAVA_AWT_HEADLESS);
   }

   public static boolean isJavaVersionAtLeast(JavaVersion requiredVersion) {
      return JAVA_SPECIFICATION_VERSION_AS_ENUM.atLeast(requiredVersion);
   }

   public static boolean isJavaVersionAtMost(JavaVersion requiredVersion) {
      return JAVA_SPECIFICATION_VERSION_AS_ENUM.atMost(requiredVersion);
   }

   static boolean isJavaVersionMatch(String version, String versionPrefix) {
      return version == null ? false : version.startsWith(versionPrefix);
   }

   static boolean isOsMatch(String osName, String osVersion, String osNamePrefix, String osVersionPrefix) {
      return osName != null && osVersion != null ? isOsNameMatch(osName, osNamePrefix) && isOsVersionMatch(osVersion, osVersionPrefix) : false;
   }

   static boolean isOsNameMatch(String osName, String osNamePrefix) {
      return osName == null ? false : osName.startsWith(osNamePrefix);
   }

   static boolean isOsVersionMatch(String osVersion, String osVersionPrefix) {
      if (StringUtils.isEmpty(osVersion)) {
         return false;
      }

      String[] versionPrefixParts = JavaVersion.split(osVersionPrefix);
      String[] versionParts = JavaVersion.split(osVersion);

      for (int i = 0; i < Math.min(versionPrefixParts.length, versionParts.length); i++) {
         if (!versionPrefixParts[i].equals(versionParts[i])) {
            return false;
         }
      }

      return true;
   }
}
