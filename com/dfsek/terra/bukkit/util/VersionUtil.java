package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.lib.paperlib.PaperLib;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VersionUtil {
   public static final VersionUtil.SpigotVersionInfo SPIGOT_VERSION_INFO = new VersionUtil.SpigotVersionInfo();
   public static final VersionUtil.MinecraftVersionInfo MINECRAFT_VERSION_INFO;
   private static final Logger logger = LoggerFactory.getLogger(VersionUtil.class);

   public static VersionUtil.MinecraftVersionInfo getMinecraftVersionInfo() {
      return MINECRAFT_VERSION_INFO;
   }

   public static VersionUtil.SpigotVersionInfo getSpigotVersionInfo() {
      return SPIGOT_VERSION_INFO;
   }

   static {
      VersionUtil.MinecraftVersionInfo mcVersionInfo;
      try {
         mcVersionInfo = new VersionUtil.MinecraftVersionInfo();
      } catch (Throwable t) {
         logger.error("Error while parsing minecraft version info. Continuing launch, but setting all versions to -1.");
         mcVersionInfo = new VersionUtil.MinecraftVersionInfo(-1, -1, -1);
      }

      MINECRAFT_VERSION_INFO = mcVersionInfo;
   }

   public static final class MinecraftVersionInfo {
      private static final Logger logger = LoggerFactory.getLogger(VersionUtil.MinecraftVersionInfo.class);
      private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
      private final int major;
      private final int minor;
      private final int patch;

      private MinecraftVersionInfo() {
         this(Bukkit.getServer().getBukkitVersion().split("-")[0]);
      }

      private MinecraftVersionInfo(int major, int minor, int patch) {
         this.major = major;
         this.minor = minor;
         this.patch = patch;
      }

      private MinecraftVersionInfo(String versionString) {
         Matcher versionMatcher = VERSION_PATTERN.matcher(versionString);
         if (versionMatcher.find()) {
            this.major = Integer.parseInt(versionMatcher.group(1));
            this.minor = Integer.parseInt(versionMatcher.group(2));
            this.patch = versionMatcher.group(3) != null ? Integer.parseInt(versionMatcher.group(3)) : -1;
         } else {
            logger.warn("Error while parsing minecraft version info. Continuing launch, but setting all versions to -1.");
            this.major = -1;
            this.minor = -1;
            this.patch = -1;
         }
      }

      @Override
      public String toString() {
         if (this.major == -1 && this.minor == -1 && this.patch == -1) {
            return "Unknown";
         } else {
            return this.patch >= 0 ? String.format("v%d.%d.%d", this.major, this.minor, this.patch) : String.format("v%d.%d", this.major, this.minor);
         }
      }

      public int getMajor() {
         return this.major;
      }

      public int getMinor() {
         return this.minor;
      }

      public int getPatch() {
         return this.patch;
      }
   }

   public static final class SpigotVersionInfo {
      private final boolean spigot;
      private final boolean paper;
      private final boolean mohist;

      public SpigotVersionInfo() {
         VersionUtil.logger.debug("Parsing spigot version info...");
         this.paper = PaperLib.isPaper();
         this.spigot = PaperLib.isSpigot();
         boolean isMohist = false;

         try {
            Class.forName("com.mohistmc.MohistMC");
            isMohist = true;
         } catch (ClassNotFoundException var3) {
         }

         this.mohist = isMohist;
         VersionUtil.logger.debug("Spigot version info parsed successfully.");
      }

      public boolean isPaper() {
         return this.paper;
      }

      public boolean isMohist() {
         return this.mohist;
      }

      public boolean isSpigot() {
         return this.spigot;
      }
   }
}
