package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.collect.ImmutableMultimap;

@J2ktIncompatible
@GwtIncompatible
interface ServiceManagerBridge {
   ImmutableMultimap<Service.State, Service> servicesByState();
}
