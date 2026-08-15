package org.incendo.cloud.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.immutables.annotate.InjectAnnotation;
import org.immutables.annotate.InjectAnnotation.Where;
import org.immutables.value.Value.Style;
import org.immutables.value.Value.Style.BuilderVisibility;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PACKAGE})
@Retention(RetentionPolicy.SOURCE)
@API(status = Status.INTERNAL)
@Style(
   typeImmutableEnclosing = "*",
   typeAbstract = "*",
   deferCollectionAllocation = true,
   optionalAcceptNullable = true,
   jdkOnly = true,
   allParameters = true,
   headerComments = true,
   jacksonIntegration = false,
   builderVisibility = BuilderVisibility.SAME,
   defaultAsDefault = true,
   depluralize = true
)
@InjectAnnotation(
   type = API.class,
   target = Where.IMMUTABLE_TYPE,
   code = "(status = org.apiguardian.api.API.Status.STABLE, consumers = \"org.incendo.cloud.*\")"
)
public @interface ImmutableBuilder {
}
