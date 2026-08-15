package com.dfsek.terra.lib.google.common.hash;

import com.dfsek.terra.lib.google.common.base.Supplier;
import com.google.errorprone.annotations.Immutable;

@Immutable
interface ImmutableSupplier<T> extends Supplier<T> {
}
