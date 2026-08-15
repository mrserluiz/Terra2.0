package com.dfsek.terra.lib.commons.io.build;

import com.dfsek.terra.lib.commons.io.function.IOSupplier;

public abstract class AbstractSupplier<T, B extends AbstractSupplier<T, B>> implements IOSupplier<T> {
   protected B asThis() {
      return (B)this;
   }
}
