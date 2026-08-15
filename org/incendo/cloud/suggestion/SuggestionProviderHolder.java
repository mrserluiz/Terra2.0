package org.incendo.cloud.suggestion;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface SuggestionProviderHolder<C> {
   @NonNull SuggestionProvider<C> suggestionProvider();
}
