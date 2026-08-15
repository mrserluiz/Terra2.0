package com.dfsek.terra.api.config;

import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.terra.api.Platform;

public interface ConfigFactory<C extends ConfigTemplate, O> {
   O build(C var1, Platform var2) throws LoadException;
}
