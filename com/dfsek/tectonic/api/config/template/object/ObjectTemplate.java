package com.dfsek.tectonic.api.config.template.object;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;

public interface ObjectTemplate<T> extends ConfigTemplate {
   T get();
}
