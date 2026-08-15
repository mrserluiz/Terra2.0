package org.incendo.cloud.key;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(status = Status.STABLE)
public interface CloudKeyHolder<T> {
   @NonNull CloudKey<T> key();
}
