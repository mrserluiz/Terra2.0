package ca.solostudios.strata;

import ca.solostudios.strata.parser.VersionParser;
import ca.solostudios.strata.parser.VersionRangeParser;
import ca.solostudios.strata.parser.tokenizer.ParseException;
import ca.solostudios.strata.version.BuildMetadata;
import ca.solostudios.strata.version.CoreVersion;
import ca.solostudios.strata.version.PreRelease;
import ca.solostudios.strata.version.Version;
import ca.solostudios.strata.version.VersionRange;
import java.math.BigInteger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Versions {
   private Versions() {
   }

   @NotNull
   @Contract(value = "_, _, _ -> new", pure = true)
   public static Version getVersion(int major, int minor, int patch) {
      return new Version(new CoreVersion(BigInteger.valueOf(major), BigInteger.valueOf(minor), BigInteger.valueOf(patch)), PreRelease.NULL, BuildMetadata.NULL);
   }

   @NotNull
   @Contract(value = "_, _, _ -> new", pure = true)
   public static Version getVersion(@NotNull BigInteger major, @NotNull BigInteger minor, @NotNull BigInteger patch) {
      return new Version(new CoreVersion(major, minor, patch), PreRelease.NULL, BuildMetadata.NULL);
   }

   @NotNull
   @Contract(value = "_, _, _, _ -> new", pure = true)
   public static Version getVersion(@NotNull BigInteger major, @NotNull BigInteger minor, @NotNull BigInteger patch, @NotNull PreRelease preRelease) {
      return new Version(new CoreVersion(major, minor, patch), preRelease, BuildMetadata.NULL);
   }

   @NotNull
   @Contract(value = "_, _, _, _ -> new", pure = true)
   public static Version getVersion(@NotNull BigInteger major, @NotNull BigInteger minor, @NotNull BigInteger patch, @NotNull BuildMetadata buildMetadata) {
      return new Version(new CoreVersion(major, minor, patch), PreRelease.NULL, buildMetadata);
   }

   @NotNull
   @Contract(value = "_, _, _, _, _ -> new", pure = true)
   public static Version getVersion(
      @NotNull BigInteger major, @NotNull BigInteger minor, @NotNull BigInteger patch, @NotNull PreRelease preRelease, @NotNull BuildMetadata buildMetadata
   ) {
      return new Version(new CoreVersion(major, minor, patch), preRelease, buildMetadata);
   }

   @NotNull
   @Contract(value = "_ -> new", pure = true)
   public static Version parseVersion(@NotNull String versionString) throws ParseException {
      return new VersionParser(versionString).parse();
   }

   @NotNull
   @Contract(value = "_, _, _ -> new", pure = true)
   public static Version parseVersion(@NotNull String coreVersion, @Nullable String preReleaseVersion, @Nullable String buildMetadataVersion) throws ParseException {
      StringBuilder builder = new StringBuilder(
         coreVersion.length()
            + (preReleaseVersion == null ? 0 : preReleaseVersion.length())
            + (buildMetadataVersion == null ? 0 : buildMetadataVersion.length())
      );
      builder.append(coreVersion);
      if (preReleaseVersion != null) {
         builder.append('-').append(preReleaseVersion);
      }

      if (buildMetadataVersion != null) {
         builder.append('+').append(buildMetadataVersion);
      }

      return new VersionParser(builder.toString()).parse();
   }

   @NotNull
   @Contract(value = "_ -> new", pure = true)
   public static VersionRange parseVersionRange(@NotNull String versionString) throws ParseException {
      return new VersionRangeParser(versionString).parse();
   }

   @NotNull
   @Contract(value = "_, _, _, _ -> new", pure = true)
   public static VersionRange getVersionRange(@Nullable Version startVersion, boolean startInclusive, @Nullable Version endVersion, boolean endInclusive) {
      return new VersionRange(startVersion, startInclusive, endVersion, endInclusive);
   }
}
