package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@DoNotMock("Use ImmutableClassToInstanceMap or MutableClassToInstanceMap")
@GwtCompatible
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B> {
   <T extends B> @Nullable T getInstance(Class<T> type);

   @CanIgnoreReturnValue
   <T extends B> @Nullable T putInstance(Class<@NonNull T> type, @ParametricNullness T value);
}
