package org.incendo.cloud.component.preprocessor;

import java.util.Collection;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface PreprocessorHolder<C> {
   @NonNull Collection<ComponentPreprocessor<C>> preprocessors();
}
