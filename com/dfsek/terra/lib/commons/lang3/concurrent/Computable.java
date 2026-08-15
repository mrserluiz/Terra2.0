package com.dfsek.terra.lib.commons.lang3.concurrent;

public interface Computable<I, O> {
   O compute(I var1) throws InterruptedException;
}
