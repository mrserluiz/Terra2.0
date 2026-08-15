package org.incendo.cloud.paper.suggestion.tooltips;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent.Completion;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;

@API(status = Status.INTERNAL, since = "2.0.0")
public interface CompletionMapper {
   @NonNull Completion map(@NonNull TooltipSuggestion suggestion);
}
