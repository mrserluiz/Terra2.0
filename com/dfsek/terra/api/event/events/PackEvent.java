package com.dfsek.terra.api.event.events;

import com.dfsek.terra.api.config.ConfigPack;

public interface PackEvent extends Event {
   ConfigPack getPack();
}
