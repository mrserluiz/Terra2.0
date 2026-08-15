package org.incendo.cloud.annotation.specifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@API(status = Status.STABLE)
public @interface Liberal {
}
