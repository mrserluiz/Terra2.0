package com.dfsek.tectonic.api.loader;

import com.dfsek.tectonic.api.TypeRegistry;
import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;
import com.dfsek.tectonic.api.exception.ConfigException;
import com.dfsek.tectonic.api.loader.type.TypeLoader;
import com.dfsek.tectonic.api.preprocessor.ValuePreprocessor;
import com.dfsek.tectonic.impl.abstraction.AbstractConfiguration;
import com.dfsek.tectonic.impl.abstraction.AbstractPool;
import com.dfsek.tectonic.impl.abstraction.Prototype;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public class AbstractConfigLoader implements TypeRegistry {
   private final ConfigLoader delegate = new ConfigLoader();

   @NotNull
   public AbstractConfigLoader registerLoader(@NotNull Type t, @NotNull TypeLoader<?> loader) {
      this.delegate.registerLoader(t, loader);
      return this;
   }

   @NotNull
   public <T> AbstractConfigLoader registerLoader(@NotNull Type t, @NotNull Supplier<ObjectTemplate<T>> provider) {
      this.delegate.registerLoader(t, provider);
      return this;
   }

   public <T extends Annotation> AbstractConfigLoader registerPreprocessor(Class<? extends T> clazz, ValuePreprocessor<T> processor) {
      this.delegate.registerPreprocessor(clazz, processor);
      return this;
   }

   public Set<AbstractConfiguration> loadConfigs(List<Configuration> configurations) throws ConfigException {
      AbstractPool pool = new AbstractPool();

      for (Configuration config : configurations) {
         Prototype p = new Prototype(config);
         pool.add(p);
      }

      pool.loadAll();
      Set<AbstractConfiguration> abstractConfigs = new HashSet<>();

      for (Prototype p : pool.getPrototypes()) {
         if (!p.isAbstract()) {
            AbstractConfiguration config = new AbstractConfiguration(p);
            this.build(config, Collections.singletonList(p));
            abstractConfigs.add(config);
         }
      }

      return abstractConfigs;
   }

   public <E extends ConfigTemplate> Set<E> loadTemplates(List<Configuration> configurations, Supplier<E> provider) throws ConfigException {
      Set<E> templates = new HashSet<>();
      this.loadConfigs(configurations).forEach(config -> templates.add(this.delegate.load(provider.get(), config)));
      return templates;
   }

   private void build(AbstractConfiguration provider, List<Prototype> prototypes) {
      for (Prototype prototype : prototypes) {
         provider.add(prototype);
         if (!prototype.isRoot()) {
            int layer = provider.next();
            this.build(provider, prototype.getParents());
            provider.reset(layer);
         }
      }
   }
}
