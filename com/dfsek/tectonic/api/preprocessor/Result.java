package com.dfsek.tectonic.api.preprocessor;

import com.dfsek.tectonic.api.depth.DepthTracker;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

public class Result<T> implements UnaryOperator<T> {
   private final Result.TransformType type;
   private final T overwritten;
   private final Consumer<T> transformer;
   private final DepthTracker tracker;

   private Result(T overwritten, Consumer<T> transformer, Result.TransformType type, DepthTracker tracker) {
      this.type = type;
      this.overwritten = overwritten;
      this.transformer = transformer;
      this.tracker = tracker;
   }

   public static <T> Result<T> overwrite(T result, DepthTracker tracker) {
      return new Result<>(result, null, Result.TransformType.OVERWRITE, tracker);
   }

   public static <T> Result<T> noOp() {
      return new Result<>(null, null, Result.TransformType.NOP, null);
   }

   public static <T> Result<T> transform(@NotNull Consumer<T> transformer, DepthTracker tracker) {
      Objects.requireNonNull(transformer);
      return new Result<>(null, transformer, Result.TransformType.TRANSFORM, tracker);
   }

   public static <T> Result<T> transform(@NotNull Consumer<T> transformer) {
      Objects.requireNonNull(transformer);
      return new Result<>(null, transformer, Result.TransformType.TRANSFORM, null);
   }

   @Override
   public T apply(T t) {
      switch (this.type) {
         case NOP:
            return t;
         case OVERWRITE:
            return this.overwritten;
         case TRANSFORM:
            this.transformer.accept(t);
            return t;
         default:
            return t;
      }
   }

   public DepthTracker getTracker(DepthTracker original) {
      return this.tracker == null ? original : this.tracker;
   }

   private enum TransformType {
      OVERWRITE,
      TRANSFORM,
      NOP;
   }
}
