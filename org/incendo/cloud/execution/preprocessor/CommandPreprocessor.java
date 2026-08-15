package org.incendo.cloud.execution.preprocessor;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.incendo.cloud.services.type.ConsumerService;

@API(status = Status.STABLE)
public interface CommandPreprocessor<C> extends ConsumerService<CommandPreprocessingContext<C>> {
}
