package com.dfsek.tectonic.api.preprocessor;

import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import org.jetbrains.annotations.NotNull;

public interface ValuePreprocessor<A extends Annotation> {
   @NotNull
   <T> Result<T> process(AnnotatedType var1, T var2, ConfigLoader var3, A var4, DepthTracker var5);
}
