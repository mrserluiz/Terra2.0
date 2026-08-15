package ca.solostudios.strata.version;

import java.math.BigInteger;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

public abstract class PreReleaseIdentifier implements Comparable<PreReleaseIdentifier>, Formattable {
   public int compareTo(@NotNull PreReleaseIdentifier o) {
      if (this.isNumeric()) {
         return o.isNumeric() ? this.asInteger().compareTo(o.asInteger()) : -1;
      } else {
         return o.isNumeric() ? 1 : this.asString().compareTo(o.asString());
      }
   }

   protected BigInteger asInteger() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Numerical values are not supported by this implementation");
   }

   @NotNull
   protected abstract String asString();

   @NotNull
   @Override
   public String getFormatted() {
      return this.asString();
   }

   protected abstract boolean isNumeric();

   public static final class AlphaNumericalPreReleaseIdentifier extends PreReleaseIdentifier {
      @NotNull
      private final String value;

      public AlphaNumericalPreReleaseIdentifier(@NotNull String value) {
         this.value = value;
      }

      @Override
      public String toString() {
         return String.format("AlphaNumericalPreReleaseIdentifier{value='%s'}", this.value);
      }

      @NotNull
      @Override
      protected String asString() {
         return this.value;
      }

      @Override
      protected boolean isNumeric() {
         return false;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            PreReleaseIdentifier.AlphaNumericalPreReleaseIdentifier that = (PreReleaseIdentifier.AlphaNumericalPreReleaseIdentifier)o;
            return this.value.equals(that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }
   }

   public static final class NumericalPreReleaseIdentifier extends PreReleaseIdentifier {
      private final BigInteger value;

      public NumericalPreReleaseIdentifier(BigInteger value) {
         this.value = value;
      }

      @Override
      public String toString() {
         return String.format("NumericalPreReleaseIdentifier{value=%d}", this.value);
      }

      @NotNull
      @Override
      protected String asString() {
         return this.value.toString();
      }

      @Override
      protected BigInteger asInteger() {
         return this.value;
      }

      @Override
      protected boolean isNumeric() {
         return true;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            PreReleaseIdentifier.NumericalPreReleaseIdentifier that = (PreReleaseIdentifier.NumericalPreReleaseIdentifier)o;
            return Objects.equals(this.value, that.value);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return this.value.hashCode();
      }
   }
}
