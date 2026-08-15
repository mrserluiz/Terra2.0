package org.incendo.cloud.brigadier.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.Nullable;

@API(status = Status.STABLE, since = "2.0.0")
public interface ArgumentTypeFactory<T> {
   @Nullable ArgumentType<T> create();
}
