package com.dfsek.tectonic.api;

import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import java.lang.reflect.Type;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface TypeRegistry {
   @NotNull
   @Contract("_, _ -> this")
   TypeRegistry registerLoader(@NotNull Type var1, @NotNull TypeLoader<?> var2);

   @NotNull
   @Contract("_, _ -> this")
   <T> TypeRegistry registerLoader(@NotNull Type var1, @NotNull Supplier<ObjectTemplate<T>> var2);
}
