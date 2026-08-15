package ca.solostudios.strata.version;

import ca.solostudios.strata.Versions;
import ca.solostudios.strata.parser.tokenizer.ParseException;
import java.util.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VersionRange implements Formattable {
   @Nullable
   private final Version startVersion;
   private final boolean startInclusive;
   @Nullable
   private final Version endVersion;
   private final boolean endInclusive;

   public VersionRange(@Nullable Version startVersion, boolean startInclusive, @Nullable Version endVersion, boolean endInclusive) {
      this.startVersion = startVersion;
      this.startInclusive = startInclusive;
      this.endVersion = endVersion;
      this.endInclusive = endInclusive;
   }

   @Nullable
   public Version getStartVersion() {
      return this.startVersion;
   }

   public boolean isStartInclusive() {
      return this.startInclusive;
   }

   @Nullable
   public Version getEndVersion() {
      return this.endVersion;
   }

   public boolean isEndInclusive() {
      return this.endInclusive;
   }

   public boolean isSatisfiedBy(String version) throws ParseException {
      return this.isSatisfiedBy(Versions.parseVersion(version));
   }

   public boolean isSatisfiedBy(Version version) {
      if (this.startVersion != null) {
         if (this.startInclusive) {
            if (0 < this.startVersion.getCoreVersion().compareTo(version.getCoreVersion())) {
               return false;
            }
         } else if (0 <= this.startVersion.getCoreVersion().compareTo(version.getCoreVersion())) {
            return false;
         }
      }

      if (this.endVersion != null) {
         return this.endInclusive
            ? 0 <= this.endVersion.getCoreVersion().compareTo(version.getCoreVersion())
            : 0 < this.endVersion.getCoreVersion().compareTo(version.getCoreVersion());
      } else {
         return true;
      }
   }

   @Contract(pure = true)
   @Override
   public String toString() {
      return String.format(
         "VersionRange{startVersion=%s, startInclusive=%b, endVersion=%s, endInclusive=%b}",
         this.startVersion,
         this.startInclusive,
         this.endVersion,
         this.endInclusive
      );
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         VersionRange range = (VersionRange)o;
         return this.startInclusive == range.startInclusive
            && this.endInclusive == range.endInclusive
            && Objects.equals(this.startVersion, range.startVersion)
            && Objects.equals(this.endVersion, range.endVersion);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.startVersion != null ? this.startVersion.hashCode() : 0;
      result = 31 * result + (this.startInclusive ? 1 : 0);
      result = 31 * result + (this.endVersion != null ? this.endVersion.hashCode() : 0);
      return 31 * result + (this.endInclusive ? 1 : 0);
   }

   @NotNull
   @Override
   public String getFormatted() {
      StringBuilder sb = new StringBuilder();
      if (this.startInclusive) {
         sb.append('[');
      } else {
         sb.append('(');
      }

      if (this.startVersion != null) {
         sb.append(this.startVersion.getFormatted());
      }

      sb.append(",");
      if (this.endVersion != null) {
         sb.append(this.endVersion.getFormatted());
      }

      if (this.endInclusive) {
         sb.append(']');
      } else {
         sb.append(')');
      }

      return sb.toString();
   }
}
