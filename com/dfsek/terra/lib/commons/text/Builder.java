package com.dfsek.terra.lib.commons.text;

import java.util.function.Supplier;

@Deprecated
public interface Builder<T> extends Supplier<T> {
   T build();
}
