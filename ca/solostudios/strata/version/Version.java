package ca.solostudios.strata.version;

import java.math.BigInteger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Version implements Comparable<Version>, Formattable {
   @NotNull
   private final CoreVersion coreVersion;
   @NotNull
   private final PreRelease preRelease;
   @NotNull
   private final BuildMetadata buildMetadata;

   @Contract(pure = true)
   public Version(@NotNull CoreVersion coreVersion, @NotNull PreRelease preRelease, @NotNull BuildMetadata buildMetadata) {
      this.coreVersion = coreVersion;
      this.preRelease = preRelease;
      this.buildMetadata = buildMetadata;
   }

   @NotNull
   @Contract(pure = true)
   public BigInteger getMajor() {
      return this.coreVersion.getMajor();
   }

   @NotNull
   @Contract(pure = true)
   public BigInteger getMinor() {
      return this.coreVersion.getMinor();
   }

   @NotNull
   @Contract(pure = true)
   public BigInteger getPatch() {
      return this.coreVersion.getPatch();
   }

   @NotNull
   @Contract(pure = true)
   public CoreVersion getCoreVersion() {
      return this.coreVersion;
   }

   @NotNull
   @Contract(pure = true)
   public PreRelease getPreRelease() {
      return this.preRelease;
   }

   @NotNull
   @Contract(pure = true)
   public BuildMetadata getBuildMetadata() {
      return this.buildMetadata;
   }

   @Contract(pure = true)
   @Override
   public String toString() {
      return String.format("Version{normalVersion=%s, preRelease=%s, buildMetadata=%s}", this.coreVersion, this.preRelease, this.buildMetadata);
   }

   @Contract(value = "null -> false", pure = true)
   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Version version = (Version)o;
         return this.coreVersion.equals(version.coreVersion) && this.preRelease.equals(version.preRelease) && this.buildMetadata.equals(version.buildMetadata);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.coreVersion.hashCode();
      result = 31 * result + this.preRelease.hashCode();
      return 31 * result + this.buildMetadata.hashCode();
   }

   public int compareTo(@NotNull Version o) {
      int normalVersionComparison = this.coreVersion.compareTo(o.coreVersion);
      return normalVersionComparison != 0 ? normalVersionComparison : this.preRelease.compareTo(o.preRelease);
   }

   @NotNull
   @Override
   public String getFormatted() {
      return String.format("%s%s%s", this.coreVersion.getFormatted(), this.preRelease.getFormatted(), this.buildMetadata.getFormatted());
   }
}
