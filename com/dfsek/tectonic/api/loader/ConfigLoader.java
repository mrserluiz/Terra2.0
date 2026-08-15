package com.dfsek.tectonic.api.loader;

import com.dfsek.tectonic.api.TypeRegistry;
import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.ValidatedConfigTemplate;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.exception.ValidationException;
import com.dfsek.tectonic.api.exception.ValueMissingException;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.tectonic.api.preprocessor.Result;
import com.dfsek.tectonic.api.preprocessor.ValuePreprocessor;
import com.dfsek.tectonic.impl.abstraction.AbstractConfiguration;
import com.dfsek.tectonic.impl.loading.loaders.EnumLoader;
import com.dfsek.tectonic.impl.loading.loaders.StringLoader;
import com.dfsek.tectonic.impl.loading.loaders.generic.ArrayListLoader;
import com.dfsek.tectonic.impl.loading.loaders.generic.HashMapLoader;
import com.dfsek.tectonic.impl.loading.loaders.generic.HashSetLoader;
import com.dfsek.tectonic.impl.loading.loaders.other.DurationLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.BooleanLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.ByteLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.CharLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.DoubleLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.FloatLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.IntLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.LongLoader;
import com.dfsek.tectonic.impl.loading.loaders.primitives.ShortLoader;
import com.dfsek.tectonic.impl.loading.object.ObjectTemplateLoader;
import com.dfsek.tectonic.util.ReflectionUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class ConfigLoader implements TypeRegistry {
   private static final EnumLoader ENUM_LOADER = new EnumLoader();
   private final Map<Type, TypeLoader<?>> loaders = new HashMap<>();
   private final Map<Class<? extends Annotation>, List<ValuePreprocessor<?>>> preprocessors = new HashMap<>();

   public ConfigLoader() {
      BooleanLoader booleanLoader = new BooleanLoader();
      this.registerLoader(boolean.class, booleanLoader);
      this.registerLoader(Boolean.class, booleanLoader);
      ByteLoader byteLoader = new ByteLoader();
      this.registerLoader(byte.class, byteLoader);
      this.registerLoader(Byte.class, byteLoader);
      ShortLoader shortLoader = new ShortLoader();
      this.registerLoader(short.class, shortLoader);
      this.registerLoader(Short.class, shortLoader);
      CharLoader charLoader = new CharLoader();
      this.registerLoader(char.class, charLoader);
      this.registerLoader(Character.class, charLoader);
      IntLoader intLoader = new IntLoader();
      this.registerLoader(int.class, intLoader);
      this.registerLoader(Integer.class, intLoader);
      LongLoader longLoader = new LongLoader();
      this.registerLoader(long.class, longLoader);
      this.registerLoader(Long.class, longLoader);
      FloatLoader floatLoader = new FloatLoader();
      this.registerLoader(float.class, floatLoader);
      this.registerLoader(Float.class, floatLoader);
      DoubleLoader doubleLoader = new DoubleLoader();
      this.registerLoader(double.class, doubleLoader);
      this.registerLoader(Double.class, doubleLoader);
      this.registerLoader(String.class, new StringLoader());
      ArrayListLoader arrayListLoader = new ArrayListLoader();
      this.registerLoader(ArrayList.class, arrayListLoader);
      this.registerLoader(List.class, arrayListLoader);
      HashMapLoader hashMapLoader = new HashMapLoader();
      this.registerLoader(HashMap.class, hashMapLoader);
      this.registerLoader(Map.class, hashMapLoader);
      HashSetLoader hashSetLoader = new HashSetLoader();
      this.registerLoader(HashSet.class, hashSetLoader);
      this.registerLoader(Set.class, hashSetLoader);
      this.registerLoader(Duration.class, new DurationLoader());
      this.registerLoader(Enum.class, ENUM_LOADER);
   }

   @NotNull
   public ConfigLoader registerLoader(@NotNull Type t, @NotNull TypeLoader<?> loader) {
      this.loaders.put(t, loader);
      return this;
   }

   @NotNull
   public <T> ConfigLoader registerLoader(@NotNull Type t, @NotNull Supplier<ObjectTemplate<T>> provider) {
      this.loaders.put(t, new ObjectTemplateLoader<>(provider));
      return this;
   }

   public <T extends Annotation> ConfigLoader registerPreprocessor(Class<? extends T> clazz, ValuePreprocessor<T> processor) {
      this.preprocessors.computeIfAbsent(clazz, c -> new ArrayList<>()).add(processor);
      return this;
   }

   public boolean hasLoader(Type t) {
      return this.loaders.containsKey(t);
   }

   public <T extends ConfigTemplate> T load(T config, Configuration configuration) throws ConfigException {
      return this.load(config, configuration, new DepthTracker(Collections.emptyList(), configuration));
   }

   public <T extends ConfigTemplate> T load(T config, Configuration configuration, DepthTracker depthTracker) throws ConfigException {
      T result = config.loader().load(config, configuration, this::loadValue, depthTracker);
      if (result instanceof ValidatedConfigTemplate && !((ValidatedConfigTemplate)result).validate()) {
         throw new ValidationException("Failed to validate config. Reason unspecified:" + configuration.getName());
      } else {
         return result;
      }
   }

   private Object loadValue(String value, AnnotatedType type, Configuration configuration, DepthTracker depthTracker, boolean isFinal) {
      if (this.containsFinal(configuration, value)) {
         return this.loadType(type, this.getFinal(configuration, value), depthTracker);
      }

      if (!isFinal && configuration instanceof AbstractConfiguration) {
         Object abs = configuration.get(value);
         if (abs == null) {
            throw new ValueMissingException(
               "Value \"" + value + "\" was not found in the provided config, or its parents: " + configuration.getName(), depthTracker
            );
         } else {
            return this.loadType(type, abs, depthTracker);
         }
      } else {
         throw new ValueMissingException("Value \"" + value + "\" was not found in the provided config: " + configuration.getName(), depthTracker);
      }
   }

   private Object getFinal(Configuration configuration, String key) {
      return configuration instanceof AbstractConfiguration ? ((AbstractConfiguration)configuration).getBase(key) : configuration.get(key);
   }

   private boolean containsFinal(Configuration configuration, String key) {
      return configuration instanceof AbstractConfiguration ? ((AbstractConfiguration)configuration).containsBase(key) : configuration.contains(key);
   }

   public Object loadType(AnnotatedType t, Object o, DepthTracker depthTracker) throws LoadException {
      for (Annotation annotation : t.getAnnotations()) {
         if (this.preprocessors.containsKey(annotation.annotationType())) {
            for (ValuePreprocessor<?> preprocessor : this.preprocessors.get(annotation.annotationType())) {
               Result<Object> result = ((ValuePreprocessor<Annotation>)preprocessor).process(t, o, this, annotation, depthTracker);
               o = result.apply(o);
               depthTracker = result.getTracker(depthTracker);
            }
         }
      }

      return this.getObject(t, o, depthTracker);
   }

   private Object getObject(AnnotatedType t, Object o, DepthTracker depthTracker) throws LoadException {
      try {
         Type raw = t.getType();
         if (this.loaders.containsKey(raw)) {
            return this.loaders.get(raw).load(t, o, this, depthTracker);
         }

         if (t instanceof AnnotatedParameterizedType) {
            raw = ((ParameterizedType)t.getType()).getRawType();
            if (this.loaders.containsKey(raw)) {
               return this.loaders.get(raw).load(t, o, this, depthTracker);
            }
         }

         if (raw instanceof Class && ((Class)raw).isEnum()) {
            return ENUM_LOADER.load(t, o, this, depthTracker);
         }
      } catch (LoadException e) {
         throw e;
      } catch (Exception e) {
         throw new LoadException("Unexpected exception thrown during type loading: " + e.getMessage(), e, depthTracker);
      }

      throw new LoadException("No loaders are registered for type " + t.getType().getTypeName(), depthTracker);
   }

   public <T> T loadType(Class<T> clazz, Object o, DepthTracker depthTracker) throws LoadException {
      try {
         return this.loaders.containsKey(clazz)
            ? ReflectionUtil.cast(clazz, this.loaders.get(clazz).load(clazz, o, this, depthTracker))
            : ReflectionUtil.cast(clazz, o);
      } catch (LoadException e) {
         throw e;
      } catch (Exception e) {
         throw new LoadException("Unexpected exception thrown during type loading: " + e.getMessage(), e, depthTracker);
      }
   }
}
