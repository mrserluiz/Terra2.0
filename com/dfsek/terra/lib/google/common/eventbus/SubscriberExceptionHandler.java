package com.dfsek.terra.lib.google.common.eventbus;

public interface SubscriberExceptionHandler {
   void handleException(Throwable exception, SubscriberExceptionContext context);
}
