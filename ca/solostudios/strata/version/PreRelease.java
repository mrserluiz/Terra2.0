package ca.solostudios.strata.version;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class PreRelease implements Comparable<PreRelease>, Formattable {
   public static final PreRelease NULL = new PreRelease(Collections.emptyList());
   @NotNull
   private final List<PreReleaseIdentifier> identifiers;

   public PreRelease(@NotNull List<PreReleaseIdentifier> identifiers) {
      this.identifiers = identifiers;
   }

   public int compareTo(@NotNull PreRelease o) {
      if (this.identifiers.isEmpty()) {
         return o.identifiers.isEmpty() ? 0 : 1;
      }

      if (o.identifiers.isEmpty()) {
         return -1;
      }

      int i = 0;

      while (this.identifiers.size() > i) {
         if (o.identifiers.size() <= i) {
            return 1;
         }

         int comparison = this.identifiers.get(i).compareTo(o.identifiers.get(i));
         i++;
         if (comparison != 0) {
            return comparison;
         }
      }

      return o.identifiers.size() <= i ? 0 : -1;
   }

   @Override
   public String toString() {
      return String.format("PreRelease{identifiers=%s}", this.identifiers);
   }

   public List<PreReleaseIdentifier> getIdentifiers() {
      return Collections.unmodifiableList(this.identifiers);
   }

   @NotNull
   @Override
   public String getFormatted() {
      if (this.identifiers.isEmpty()) {
         return "";
      }

      Iterator<PreReleaseIdentifier> iterator = this.identifiers.listIterator();
      StringBuilder builder = new StringBuilder();
      if (iterator.hasNext()) {
         builder.append('-').append(iterator.next().getFormatted());
      }

      while (iterator.hasNext()) {
         builder.append('.').append(iterator.next().getFormatted());
      }

      return builder.toString();
   }

   @Contract(value = "null -> false", pure = true)
   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         PreRelease that = (PreRelease)o;
         return this.identifiers.equals(that.identifiers);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.identifiers.hashCode();
   }
}
