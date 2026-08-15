package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.util.concurrent.locks.LockSupport;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
final class OverflowAvoidingLockSupport {
   static final long MAX_NANOSECONDS_THRESHOLD = 2147483647999999999L;

   private OverflowAvoidingLockSupport() {
   }

   static void parkNanos(@Nullable Object blocker, long nanos) {
      LockSupport.parkNanos(blocker, Math.min(nanos, 2147483647999999999L));
   }
}
