package org.incendo.cloud;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.STABLE)
public interface SenderMapperHolder<B, M> {
   @NonNull SenderMapper<B, M> senderMapper();
}
