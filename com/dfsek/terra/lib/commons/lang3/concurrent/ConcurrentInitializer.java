package com.dfsek.terra.lib.commons.lang3.concurrent;

import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;

public interface ConcurrentInitializer<T> extends FailableSupplier<T, ConcurrentException> {
}
