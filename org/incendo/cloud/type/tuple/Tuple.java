package org.incendo.cloud.type.tuple;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface Tuple {
   int size();

   @NonNull Object @NonNull [] toArray();
}
