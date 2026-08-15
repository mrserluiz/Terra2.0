package org.incendo.cloud.execution.postprocessor;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.incendo.cloud.services.type.ConsumerService;

@API(status = Status.STABLE)
public interface CommandPostprocessor<C> extends ConsumerService<CommandPostprocessingContext<C>> {
}
