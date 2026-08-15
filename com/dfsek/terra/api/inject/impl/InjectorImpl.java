package com.dfsek.terra.api.inject.impl;

import com.dfsek.terra.api.inject.Injector;
import com.dfsek.terra.api.inject.annotations.Inject;
import com.dfsek.terra.api.inject.exception.InjectionException;
import com.dfsek.terra.api.util.reflection.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class InjectorImpl<T> implements Injector<T> {
   private final T value;
   private final Set<Class<? extends T>> targets = new HashSet<>();

   public InjectorImpl(T value) {
      this.value = value;
   }

   @Override
   public void addExplicitTarget(Class<? extends T> target) {
      this.targets.add(target);
   }

   @Override
   public void inject(Object object) throws InjectionException {
      for (Field field : ReflectionUtil.getFields(object.getClass())) {
         Inject inject = field.getAnnotation(Inject.class);
         if (inject != null && (this.value.getClass().equals(field.getType()) || this.targets.contains(field.getType()))) {
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod)) {
               throw new InjectionException("Attempted to inject final field: " + field);
            }

            if (Modifier.isStatic(mod)) {
               throw new InjectionException("Attempted to inject static field: " + field);
            }

            field.setAccessible(true);

            try {
               field.set(object, this.value);
            } catch (IllegalAccessException e) {
               throw new InjectionException("Failed to inject field: " + field, e);
            }
         }
      }
   }
}
