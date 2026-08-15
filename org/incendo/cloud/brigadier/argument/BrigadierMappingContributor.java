package org.incendo.cloud.brigadier.argument;

import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.CloudBrigadierManager;

public interface BrigadierMappingContributor {
   <C, S> void contribute(CommandManager<C> manager, CloudBrigadierManager<C, S> brigadierManager);
}
