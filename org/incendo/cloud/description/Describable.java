package org.incendo.cloud.description;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface Describable {
   @NonNull Description description();
}
