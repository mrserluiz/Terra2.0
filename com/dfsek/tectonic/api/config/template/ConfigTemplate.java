package com.dfsek.tectonic.api.config.template;

import com.dfsek.tectonic.api.loader.TemplateLoader;
import com.dfsek.tectonic.impl.loading.template.ReflectiveTemplateLoader;

public interface ConfigTemplate {
   default TemplateLoader loader() {
      return new ReflectiveTemplateLoader();
   }
}
