package org.incendo.cloud.injection;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.incendo.cloud.services.type.Service;

@FunctionalInterface
@API(status = Status.STABLE)
public interface InjectionService<C> extends Service<InjectionRequest<C>, Object> {
}
