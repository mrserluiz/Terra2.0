package com.dfsek.terra.api.transform;

import com.dfsek.terra.api.transform.exception.TransformException;

@FunctionalInterface
public interface Transform<F, T> {
   T transform(F var1) throws TransformException;
}
