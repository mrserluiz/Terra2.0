package xyz.jpenilla.reflectionremapper.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;
import xyz.jpenilla.reflectionremapper.internal.util.Util;
import xyz.jpenilla.reflectionremapper.proxy.annotation.ConstructorInvoker;
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldGetter;
import xyz.jpenilla.reflectionremapper.proxy.annotation.FieldSetter;
import xyz.jpenilla.reflectionremapper.proxy.annotation.MethodName;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Static;
import xyz.jpenilla.reflectionremapper.proxy.annotation.Type;

final class ReflectionProxyInvocationHandler<I> implements InvocationHandler {
   private static final Lookup LOOKUP = MethodHandles.lookup();
   private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
   private final Class<I> interfaceClass;
   private final Map<Method, MethodHandle> methods = new HashMap<>();
   private final Map<Method, MethodHandle> getters = new HashMap<>();
   private final Map<Method, MethodHandle> setters = new HashMap<>();
   private final Map<Method, MethodHandle> staticGetters = new HashMap<>();
   private final Map<Method, MethodHandle> staticSetters = new HashMap<>();
   private final Map<Method, MethodHandle> defaultMethods = new ConcurrentHashMap<>();

   ReflectionProxyInvocationHandler(final Class<I> interfaceClass, final ReflectionRemapper reflectionRemapper) {
      this.interfaceClass = interfaceClass;
      this.scanInterface(reflectionRemapper);
   }

   @Override
   public Object invoke(final Object proxy, final Method method, Object[] args) throws Throwable {
      if (isEqualsMethod(method)) {
         return proxy == args[0];
      }

      if (isHashCodeMethod(method)) {
         return 0;
      }

      if (isToStringMethod(method)) {
         return String.format("ReflectionProxy[interface=%s, implementation=%s]", this.interfaceClass.getTypeName(), proxy.getClass().getTypeName());
      }

      if (args == null) {
         args = EMPTY_OBJECT_ARRAY;
      }

      if (method.isDefault()) {
         return this.handleDefaultMethod(proxy, method, args);
      } else {
         MethodHandle methodHandle = this.methods.get(method);
         if (methodHandle != null) {
            return args.length == 0 ? (Object)methodHandle.invokeExact() : (Object)methodHandle.invokeExact(args);
         } else {
            MethodHandle getter = this.getters.get(method);
            if (getter != null) {
               return (Object)getter.invokeExact((Object)args[0]);
            } else {
               MethodHandle setter = this.setters.get(method);
               if (setter != null) {
                  return (Object)setter.invokeExact((Object)args[0], (Object)args[1]);
               } else {
                  MethodHandle staticGetter = this.staticGetters.get(method);
                  if (staticGetter != null) {
                     return (Object)staticGetter.invokeExact();
                  } else {
                     MethodHandle staticSetter = this.staticSetters.get(method);
                     if (staticSetter != null) {
                        return (Object)staticSetter.invokeExact((Object)args[0]);
                     } else {
                        throw new IllegalStateException();
                     }
                  }
               }
            }
         }
      }
   }

   private @Nullable Object handleDefaultMethod(final Object proxy, final Method method, final Object[] args) throws Throwable {
      MethodHandle handle = this.defaultMethods
         .computeIfAbsent(method, m -> adapt(Util.sneakyThrows(() -> Util.handleForDefaultMethod(this.interfaceClass, m)).bindTo(proxy)));
      return args.length == 0 ? (Object)handle.invokeExact() : (Object)handle.invokeExact(args);
   }

   private void scanInterface(final ReflectionRemapper reflectionRemapper) {
      Class<?> prevProxy = null;
      Class<?> prevProxied = null;

      for (Class<?> cls : Util.topDownInterfaceHierarchy(this.interfaceClass)) {
         Class<?> proxied = Util.findProxiedClass(cls, reflectionRemapper::remapClassName);
         if (prevProxied != null && !prevProxied.isAssignableFrom(proxied)) {
            throw new IllegalArgumentException(
               "Reflection proxy interface "
                  + cls.getName()
                  + " proxies "
                  + proxied.getName()
                  + ", and extends from reflection proxy interface "
                  + prevProxy.getName()
                  + " which proxies "
                  + prevProxied.getName()
                  + ", but the proxied types are not compatible."
            );
         }

         this.scanInterface(
            cls,
            proxied,
            reflectionRemapper::remapClassOrArrayName,
            fieldName -> reflectionRemapper.remapFieldName(proxied, fieldName),
            (methodName, parameters) -> reflectionRemapper.remapMethodName(proxied, methodName, parameters)
         );
         prevProxied = proxied;
         prevProxy = cls;
      }
   }

   private void scanInterface(
      final Class<?> interfaceClass,
      final Class<?> proxiedClass,
      final UnaryOperator<String> classMapper,
      final UnaryOperator<String> fieldMapper,
      final BiFunction<String, Class<?>[], String> methodMapper
   ) {
      for (Method method : interfaceClass.getDeclaredMethods()) {
         if (!isEqualsMethod(method)
            && !isHashCodeMethod(method)
            && !isToStringMethod(method)
            && !Util.isSynthetic(method.getModifiers())
            && !method.isDefault()) {
            boolean constructorInvoker = method.getDeclaredAnnotation(ConstructorInvoker.class) != null;
            if (constructorInvoker) {
               this.methods
                  .put(method, adapt(Util.sneakyThrows(() -> LOOKUP.unreflectConstructor(this.findProxiedConstructor(proxiedClass, method, classMapper)))));
            } else {
               FieldGetter getterAnnotation = method.getDeclaredAnnotation(FieldGetter.class);
               FieldSetter setterAnnotation = method.getDeclaredAnnotation(FieldSetter.class);
               if (getterAnnotation != null && setterAnnotation != null) {
                  throw new IllegalArgumentException(
                     "Method "
                        + method.getName()
                        + " in "
                        + interfaceClass.getTypeName()
                        + " is annotated with @FieldGetter and @FieldSetter, don't know which to use."
                  );
               }

               boolean hasStaticAnnotation = method.getDeclaredAnnotation(Static.class) != null;
               if (getterAnnotation != null) {
                  MethodHandle handle = Util.sneakyThrows(() -> LOOKUP.unreflectGetter(findProxiedField(proxiedClass, getterAnnotation.value(), fieldMapper)));
                  if (hasStaticAnnotation) {
                     checkParameterCount(method, interfaceClass, 0, "Static @FieldGetters should have no parameters.");
                     this.staticGetters.put(method, handle.asType(MethodType.methodType(Object.class)));
                  } else {
                     checkParameterCount(method, interfaceClass, 1, "Non-static @FieldGetters should have one parameter.");
                     this.getters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class)));
                  }
               } else if (setterAnnotation != null) {
                  MethodHandle handle = Util.sneakyThrows(() -> LOOKUP.unreflectSetter(findProxiedField(proxiedClass, setterAnnotation.value(), fieldMapper)));
                  if (hasStaticAnnotation) {
                     checkParameterCount(method, interfaceClass, 1, "Static @FieldSetters should have one parameter.");
                     this.staticSetters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class)));
                  } else {
                     checkParameterCount(method, interfaceClass, 2, "Non-static @FieldSetters should have two parameters.");
                     this.setters.put(method, handle.asType(MethodType.methodType(Object.class, Object.class, Object.class)));
                  }
               } else {
                  if (!hasStaticAnnotation && method.getParameterCount() < 1) {
                     throw new IllegalArgumentException(
                        "Non-static method invokers should have at least one parameter. Method "
                           + method.getName()
                           + " in "
                           + interfaceClass.getTypeName()
                           + " has "
                           + method.getParameterCount()
                     );
                  }

                  this.methods
                     .put(method, adapt(Util.sneakyThrows(() -> LOOKUP.unreflect(this.findProxiedMethod(proxiedClass, method, classMapper, methodMapper)))));
               }
            }
         }
      }
   }

   private static MethodHandle adapt(final MethodHandle handle) {
      return handle.type().parameterCount() == 0
         ? handle.asType(MethodType.methodType(Object.class))
         : handle.asSpreader(Object[].class, handle.type().parameterCount()).asType(MethodType.methodType(Object.class, Object[].class));
   }

   private static void checkParameterCount(final Method method, final Class<?> holder, final int expected, final String message) {
      if (method.getParameterCount() != expected) {
         throw new IllegalArgumentException(
            String.format(
               "Unexpected amount of parameters for method %s in %s, got %d while expecting %d. %s",
               method.getName(),
               holder.getTypeName(),
               method.getParameterCount(),
               expected,
               message
            )
         );
      }
   }

   private static boolean isToStringMethod(final Method method) {
      return method.getName().equals("toString") && method.getParameterCount() == 0 && method.getReturnType() == String.class;
   }

   private static boolean isHashCodeMethod(final Method method) {
      return method.getName().equals("hashCode") && method.getParameterCount() == 0 && method.getReturnType() == int.class;
   }

   private static boolean isEqualsMethod(final Method method) {
      return method.getName().equals("equals") && method.getParameterCount() == 1 && method.getReturnType() == boolean.class;
   }

   private static Field findProxiedField(final Class<?> proxiedClass, final String fieldName, final UnaryOperator<String> fieldMapper) {
      Field field;
      try {
         field = proxiedClass.getDeclaredField(fieldMapper.apply(fieldName));
      } catch (NoSuchFieldException e) {
         throw new IllegalArgumentException("Could not find field '" + fieldName + "' in " + proxiedClass.getTypeName(), e);
      }

      try {
         field.setAccessible(true);
         return field;
      } catch (Exception ex) {
         throw new IllegalStateException("Could not set access for field '" + fieldName + "' in " + proxiedClass.getTypeName(), ex);
      }
   }

   private Constructor<?> findProxiedConstructor(final Class<?> proxiedClass, final Method method, final UnaryOperator<String> classMapper) {
      Class<?>[] actualParams = Arrays.stream(method.getParameters()).map(p -> resolveParameterTypeClass(p, classMapper)).toArray(Class[]::new);

      Constructor<?> constructor;
      try {
         constructor = proxiedClass.getDeclaredConstructor(actualParams);
      } catch (NoSuchMethodException ex) {
         throw new IllegalArgumentException(
            "Could not find constructor of " + proxiedClass.getTypeName() + " with parameter types " + Arrays.toString(method.getParameterTypes()), ex
         );
      }

      try {
         constructor.setAccessible(true);
         return constructor;
      } catch (Exception ex) {
         throw new IllegalStateException(
            "Could not set access for proxy method target constructor of "
               + proxiedClass.getTypeName()
               + " with parameter types "
               + Arrays.toString(method.getParameterTypes()),
            ex
         );
      }
   }

   private Method findProxiedMethod(
      final Class<?> proxiedClass, final Method method, final UnaryOperator<String> classMapper, final BiFunction<String, Class<?>[], String> methodMapper
   ) {
      boolean hasStaticAnnotation = method.getDeclaredAnnotation(Static.class) != null;
      Class<?>[] actualParams;
      if (hasStaticAnnotation) {
         actualParams = Arrays.stream(method.getParameters()).map(p -> resolveParameterTypeClass(p, classMapper)).toArray(Class[]::new);
      } else {
         actualParams = Arrays.stream(method.getParameters()).skip(1L).map(p -> resolveParameterTypeClass(p, classMapper)).toArray(Class[]::new);
      }

      MethodName methodAnnotation = method.getDeclaredAnnotation(MethodName.class);
      String methodName = methodAnnotation == null ? method.getName() : methodAnnotation.value();

      Method proxiedMethod;
      try {
         proxiedMethod = proxiedClass.getDeclaredMethod(methodMapper.apply(methodName, actualParams), actualParams);
      } catch (NoSuchMethodException e) {
         throw new IllegalArgumentException("Could not find proxy method target method: " + proxiedClass.getTypeName() + " " + methodName);
      }

      try {
         proxiedMethod.setAccessible(true);
         return proxiedMethod;
      } catch (Exception ex) {
         throw new IllegalStateException("Could not set access for proxy method target method: " + proxiedClass.getTypeName() + " " + methodName, ex);
      }
   }

   private static Class<?> resolveParameterTypeClass(final Parameter parameter, final UnaryOperator<String> classMapper) {
      Type typeAnnotation = parameter.getDeclaredAnnotation(Type.class);
      if (typeAnnotation == null) {
         return parameter.getType();
      }

      if (typeAnnotation.value() == Object.class && typeAnnotation.className().isEmpty()) {
         throw new IllegalArgumentException("@Type annotation must either have value() or className() set.");
      }

      if (typeAnnotation.value() != Object.class) {
         return Util.findProxiedClass(typeAnnotation.value(), classMapper);
      }

      try {
         return Class.forName(classMapper.apply(typeAnnotation.className()));
      } catch (ClassNotFoundException e) {
         throw new IllegalArgumentException("Class " + typeAnnotation.className() + " specified in @Type annotation not found.", e);
      }
   }
}
