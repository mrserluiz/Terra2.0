package ca.solostudios.strata.version;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class BuildMetadata implements Formattable {
   public static final BuildMetadata NULL = new BuildMetadata("");
   @NotNull
   private final String buildMetadata;

   @Contract(pure = true)
   public BuildMetadata(@NotNull String buildMetadata) {
      this.buildMetadata = buildMetadata;
   }

   @NotNull
   @Contract(pure = true)
   public String getBuildMetadata() {
      return this.buildMetadata;
   }

   @Contract(pure = true)
   @Override
   public String toString() {
      return String.format("BuildMetadata{buildMetadata='%s'}", this.buildMetadata);
   }

   @Contract(pure = true)
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BuildMetadata that = (BuildMetadata)o;
         return this.buildMetadata.equals(that.buildMetadata);
      } else {
         return false;
      }
   }

   @Contract(pure = true)
   @Override
   public int hashCode() {
      return this.buildMetadata.hashCode();
   }

   @NotNull
   @Override
   public String getFormatted() {
      return !this.buildMetadata.isEmpty() ? String.format("+%s", this.buildMetadata) : "";
   }
}
