package com.dfsek.tectonic.api.config.template;

import com.dfsek.tectonic.api.exception.ValidationException;

public interface ValidatedConfigTemplate extends ConfigTemplate {
   boolean validate() throws ValidationException;
}
