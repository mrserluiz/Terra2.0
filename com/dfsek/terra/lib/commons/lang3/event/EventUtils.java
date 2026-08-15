package com.dfsek.terra.lib.commons.lang3.event;

import com.dfsek.terra.lib.commons.lang3.reflect.MethodUtils;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EventUtils {
   public static <L> void addEventListener(Object eventSource, Class<L> listenerType, L listener) {
      try {
         MethodUtils.invokeMethod(eventSource, "add" + listenerType.getSimpleName(), listener);
      } catch (ReflectiveOperationException e) {
         throw new IllegalArgumentException(
            "Unable to add listener for class "
               + eventSource.getClass().getName()
               + " and public add"
               + listenerType.getSimpleName()
               + " method which takes a parameter of type "
               + listenerType.getName()
               + "."
         );
      }
   }

   public static <L> void bindEventsToMethod(Object target, String methodName, Object eventSource, Class<L> listenerType, String... eventTypes) {
      L listener = listenerType.cast(
         Proxy.newProxyInstance(
            target.getClass().getClassLoader(), new Class[]{listenerType}, new EventUtils.EventBindingInvocationHandler(target, methodName, eventTypes)
         )
      );
      addEventListener(eventSource, listenerType, listener);
   }

   private static final class EventBindingInvocationHandler implements InvocationHandler {
      private final Object target;
      private final String methodName;
      private final Set<String> eventTypes;

      EventBindingInvocationHandler(Object target, String methodName, String[] eventTypes) {
         this.target = target;
         this.methodName = methodName;
         this.eventTypes = new HashSet<>(Arrays.asList(eventTypes));
      }

      private boolean hasMatchingParametersMethod(Method method) {
         return MethodUtils.getAccessibleMethod(this.target.getClass(), this.methodName, method.getParameterTypes()) != null;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {
         if (!this.eventTypes.isEmpty() && !this.eventTypes.contains(method.getName())) {
            return null;
         } else {
            return this.hasMatchingParametersMethod(method)
               ? MethodUtils.invokeMethod(this.target, this.methodName, parameters)
               : MethodUtils.invokeMethod(this.target, this.methodName);
         }
      }
   }
}
