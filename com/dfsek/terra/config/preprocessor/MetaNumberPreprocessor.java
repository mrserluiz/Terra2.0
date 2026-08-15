package com.dfsek.terra.config.preprocessor;

import com.dfsek.paralithic.eval.parser.Parser;
import com.dfsek.paralithic.eval.tokenizer.ParseException;
import com.dfsek.tectonic.api.config.Configuration;
import com.dfsek.tectonic.api.depth.DepthTracker;
import com.dfsek.tectonic.api.exception.LoadException;
import com.dfsek.tectonic.api.loader.ConfigLoader;
import com.dfsek.tectonic.api.preprocessor.Result;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.util.reflection.TypeKey;
import java.lang.reflect.AnnotatedType;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class MetaNumberPreprocessor extends MetaPreprocessor<Meta> {
   public static final TypeKey<String> META_STRING_KEY = new TypeKey<String>() {};
   private final Parser.ParseOptions parseOptions;

   public MetaNumberPreprocessor(Map<String, Configuration> configs, Parser.ParseOptions parseOptions) {
      super(configs);
      this.parseOptions = parseOptions;
   }

   private static boolean isNumber(Class<?> clazz) {
      return Number.class.isAssignableFrom(clazz)
         || byte.class.equals(clazz)
         || int.class.equals(clazz)
         || long.class.equals(clazz)
         || float.class.equals(clazz)
         || double.class.equals(clazz);
   }

   @NotNull
   public <T> Result<T> process(AnnotatedType t, T c, ConfigLoader loader, Meta annotation, DepthTracker depthTracker) {
      if (t.getType() instanceof Class && isNumber((Class<?>)t.getType()) && c instanceof String) {
         String expression = (String)loader.loadType(META_STRING_KEY.getAnnotatedType(), c, depthTracker);

         try {
            return Result.overwrite((T)new Parser(this.parseOptions).eval(expression), depthTracker);
         } catch (ParseException e) {
            throw new LoadException("Invalid expression: ", e, depthTracker);
         }
      } else {
         return Result.noOp();
      }
   }
}
