package org.incendo.cloud.services.type;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.services.State;

@FunctionalInterface
public interface SideEffectService<Context> extends Service<Context, State> {
   @NonNull State handle(@NonNull Context context) throws Exception;
}
