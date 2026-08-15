package ca.solostudios.strata.version;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Formattable {
   @NotNull
   @Contract(pure = true)
   String getFormatted();
}
