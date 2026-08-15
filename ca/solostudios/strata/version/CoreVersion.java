package ca.solostudios.strata.version;

import java.math.BigInteger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class CoreVersion implements Comparable<CoreVersion>, Formattable {
   @NotNull
   private final BigInteger major;
   @NotNull
   private final BigInteger minor;
   @NotNull
   private final BigInteger patch;

   public CoreVersion(@NotNull BigInteger major, @NotNull BigInteger minor, @NotNull BigInteger patch) {
      this.major = major;
      this.minor = minor;
      this.patch = patch;
   }

   @NotNull
   @Contract(pure = true)
   public BigInteger getMajor() {
      return this.major;
   }

   @NotNull
   @Contract(pure = true)
   public BigInteger getMinor() {
      return this.minor;
   }

   @NotNull
   @Contract(pure = true)
   public BigInteger getPatch() {
      return this.patch;
   }

   @Override
   public String toString() {
      return String.format("NormalVersion{major=%d, minor=%d, patch=%d}", this.major, this.minor, this.patch);
   }

   @Contract(value = "null -> false", pure = true)
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }

      if (o != null && this.getClass() == o.getClass()) {
         CoreVersion coreVersion = (CoreVersion)o;
         if (this.major.compareTo(coreVersion.major) != 0) {
            return false;
         } else {
            return this.minor.compareTo(coreVersion.minor) != 0 ? false : this.patch.compareTo(coreVersion.patch) == 0;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.major.hashCode();
      result = 31 * result + this.minor.hashCode();
      return 31 * result + this.patch.hashCode();
   }

   public int compareTo(@NotNull CoreVersion o) {
      int majorComparison = this.major.compareTo(o.major);
      int minorComparison = this.minor.compareTo(o.minor);
      return majorComparison != 0 ? majorComparison : (minorComparison != 0 ? minorComparison : this.patch.compareTo(o.patch));
   }

   @NotNull
   @Override
   public String getFormatted() {
      return String.format("%s.%s.%s", this.major, this.minor, this.patch);
   }
}
