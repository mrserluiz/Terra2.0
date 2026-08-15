package org.incendo.cloud.bukkit.annotation.specifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@API(status = Status.STABLE, since = "1.8.0")
public @interface AllowEmptySelection {
   boolean value() default true;
}
