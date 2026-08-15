package org.incendo.cloud.parser;

import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public class ParserParameter<T> {
   private final String key;
   private final TypeToken<T> expectedType;

   public ParserParameter(final @NonNull String key, final @NonNull TypeToken<T> expectedType) {
      this.key = key;
      this.expectedType = expectedType;
   }

   public @NonNull String key() {
      return this.key;
   }

   public @NonNull TypeToken<T> expectedType() {
      return this.expectedType;
   }

   @Override
   public final boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ParserParameter<?> that = (ParserParameter<?>)o;
         return Objects.equals(this.key, that.key) && Objects.equals(this.expectedType, that.expectedType);
      } else {
         return false;
      }
   }

   @Override
   public final int hashCode() {
      return Objects.hash(this.key, this.expectedType);
   }
}
