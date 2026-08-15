package com.dfsek.terra.lib.google.common.reflect;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableTypeToInstanceMap or MutableTypeToInstanceMap")
public interface TypeToInstanceMap<B> extends Map<TypeToken<? extends B>, B> {
   <T extends B> @Nullable T getInstance(Class<T> type);

   <T extends B> @Nullable T getInstance(TypeToken<T> type);

   @CanIgnoreReturnValue
   <T extends B> @Nullable T putInstance(Class<@NonNull T> type, @ParametricNullness T value);

   @CanIgnoreReturnValue
   <T extends B> @Nullable T putInstance(TypeToken<@NonNull T> type, @ParametricNullness T value);
}
