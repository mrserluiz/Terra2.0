package org.incendo.cloud.annotation.specifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@API(status = Status.STABLE)
public @interface Range {
   @NonNull String min() default "";

   @NonNull String max() default "";
}
