package org.incendo.cloud.bukkit.data;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.entity.Entity;

@API(status = Status.STABLE, since = "2.0.0")
public interface SingleEntitySelector extends Selector.Single<Entity> {
}
