package com.dfsek.terra.transform;

import com.dfsek.terra.api.transform.Transform;
import com.dfsek.terra.api.transform.Transformer;
import com.dfsek.terra.api.transform.Validator;
import com.dfsek.terra.api.transform.exception.AttemptsFailedException;
import com.dfsek.terra.api.transform.exception.TransformException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class TransformerImpl<F, T> implements Transformer<F, T> {
   private final LinkedHashMap<Transform<F, T>, List<Validator<T>>> transformers;

   private TransformerImpl(LinkedHashMap<Transform<F, T>, List<Validator<T>>> transformer) {
      this.transformers = transformer;
   }

   @Override
   public T translate(F from) {
      List<Throwable> exceptions = new ArrayList<>();

      for (Entry<Transform<F, T>, List<Validator<T>>> transform : this.transformers.entrySet()) {
         try {
            T result = transform.getKey().transform(from);

            for (Validator<T> validator : transform.getValue()) {
               if (!validator.validate(result)) {
                  throw new TransformException("Failed to validate result: " + result.toString());
               }
            }

            return result;
         } catch (Exception exception) {
            exceptions.add(exception);
         }
      }

      throw new AttemptsFailedException("Could not transform input; all attempts failed: " + from.toString() + "\n", exceptions);
   }

   public static final class Builder<F, T> {
      private final LinkedHashMap<Transform<F, T>, List<Validator<T>>> transforms = new LinkedHashMap<>();

      @SafeVarargs
      public final TransformerImpl.Builder<F, T> addTransform(Transform<F, T> transform, Validator<T>... validators) {
         this.transforms.put(transform, Arrays.asList(validators));
         return this;
      }

      public TransformerImpl<F, T> build() {
         return new TransformerImpl<>(this.transforms);
      }
   }
}
