package xyz.jpenilla.reflectionremapper.proxy;

import java.lang.reflect.Proxy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

@DefaultQualifier(NonNull.class)
public final class ReflectionProxyFactory {
   private final ReflectionRemapper reflectionRemapper;
   private final ClassLoader classLoader;

   private ReflectionProxyFactory(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
      this.reflectionRemapper = reflectionRemapper;
      this.classLoader = classLoader;
   }

   public <I> I reflectionProxy(final Class<I> proxyInterface) {
      return (I)Proxy.newProxyInstance(
         this.classLoader, new Class[]{proxyInterface}, new ReflectionProxyInvocationHandler<>(proxyInterface, this.reflectionRemapper)
      );
   }

   public static ReflectionProxyFactory create(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
      return new ReflectionProxyFactory(reflectionRemapper, classLoader);
   }
}
