package org.incendo.cloud.injection;

import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.util.annotation.AnnotationAccessor;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "InjectionRequest", generator = "Immutables")
@Immutable
final class InjectionRequestImpl<C> implements InjectionRequest<C> {
   private final @NonNull CommandContext<C> commandContext;
   private final @NonNull TypeToken<?> injectedType;
   private final transient @NonNull Class<?> injectedClass;
   private final @NonNull AnnotationAccessor annotationAccessor;

   private InjectionRequestImpl(@NonNull CommandContext<C> commandContext, @NonNull TypeToken<?> injectedType, @NonNull AnnotationAccessor annotationAccessor) {
      this.commandContext = Objects.requireNonNull(commandContext, "commandContext");
      this.injectedType = Objects.requireNonNull(injectedType, "injectedType");
      this.annotationAccessor = Objects.requireNonNull(annotationAccessor, "annotationAccessor");
      this.injectedClass = Objects.requireNonNull(InjectionRequest.super.injectedClass(), "injectedClass");
   }

   private InjectionRequestImpl(
      InjectionRequestImpl<C> original,
      @NonNull CommandContext<C> commandContext,
      @NonNull TypeToken<?> injectedType,
      @NonNull AnnotationAccessor annotationAccessor
   ) {
      this.commandContext = commandContext;
      this.injectedType = injectedType;
      this.annotationAccessor = annotationAccessor;
      this.injectedClass = Objects.requireNonNull(InjectionRequest.super.injectedClass(), "injectedClass");
   }

   @Override
   public @NonNull CommandContext<C> commandContext() {
      return this.commandContext;
   }

   @Override
   public @NonNull TypeToken<?> injectedType() {
      return this.injectedType;
   }

   @Override
   public @NonNull Class<?> injectedClass() {
      return this.injectedClass;
   }

   @Override
   public @NonNull AnnotationAccessor annotationAccessor() {
      return this.annotationAccessor;
   }

   public final InjectionRequestImpl<C> withCommandContext(CommandContext<C> value) {
      if (this.commandContext == value) {
         return this;
      }

      CommandContext<C> newValue = Objects.requireNonNull(value, "commandContext");
      return new InjectionRequestImpl<>(this, newValue, this.injectedType, this.annotationAccessor);
   }

   public final InjectionRequestImpl<C> withInjectedType(TypeToken<?> value) {
      if (this.injectedType == value) {
         return this;
      }

      TypeToken<?> newValue = Objects.requireNonNull(value, "injectedType");
      return new InjectionRequestImpl<>(this, this.commandContext, newValue, this.annotationAccessor);
   }

   public final InjectionRequestImpl<C> withAnnotationAccessor(AnnotationAccessor value) {
      if (this.annotationAccessor == value) {
         return this;
      }

      AnnotationAccessor newValue = Objects.requireNonNull(value, "annotationAccessor");
      return new InjectionRequestImpl<>(this, this.commandContext, this.injectedType, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof InjectionRequestImpl && this.equalTo(0, (InjectionRequestImpl<?>)another);
   }

   private boolean equalTo(int synthetic, InjectionRequestImpl<?> another) {
      return this.commandContext.equals(another.commandContext)
         && this.injectedType.equals(another.injectedType)
         && this.injectedClass.equals(another.injectedClass)
         && this.annotationAccessor.equals(another.annotationAccessor);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.commandContext.hashCode();
      h += (h << 5) + this.injectedType.hashCode();
      h += (h << 5) + this.injectedClass.hashCode();
      return h + (h << 5) + this.annotationAccessor.hashCode();
   }

   @Override
   public String toString() {
      return "InjectionRequest{commandContext="
         + this.commandContext
         + ", injectedType="
         + this.injectedType
         + ", injectedClass="
         + this.injectedClass
         + ", annotationAccessor="
         + this.annotationAccessor
         + "}";
   }

   public static <C> InjectionRequestImpl<C> of(
      @NonNull CommandContext<C> commandContext, @NonNull TypeToken<?> injectedType, @NonNull AnnotationAccessor annotationAccessor
   ) {
      return new InjectionRequestImpl<>(commandContext, injectedType, annotationAccessor);
   }

   public static <C> InjectionRequestImpl<C> copyOf(InjectionRequest<C> instance) {
      return instance instanceof InjectionRequestImpl
         ? (InjectionRequestImpl)instance
         : of(instance.commandContext(), instance.injectedType(), instance.annotationAccessor());
   }
}
