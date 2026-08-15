package org.incendo.cloud.parser;

import io.leangen.geantyref.AnnotatedTypeMap;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.incendo.cloud.annotation.specifier.FlagYielding;
import org.incendo.cloud.annotation.specifier.Greedy;
import org.incendo.cloud.annotation.specifier.Liberal;
import org.incendo.cloud.annotation.specifier.Quoted;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.ByteParser;
import org.incendo.cloud.parser.standard.CharacterParser;
import org.incendo.cloud.parser.standard.DoubleParser;
import org.incendo.cloud.parser.standard.DurationParser;
import org.incendo.cloud.parser.standard.EnumParser;
import org.incendo.cloud.parser.standard.FloatParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.LongParser;
import org.incendo.cloud.parser.standard.ShortParser;
import org.incendo.cloud.parser.standard.StringArrayParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.parser.standard.UUIDParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

@API(status = Status.STABLE)
public final class StandardParserRegistry<C> implements ParserRegistry<C> {
   private static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPINGS = new HashMap<Class<?>, Class<?>>() {
      {
         this.put(char.class, Character.class);
         this.put(int.class, Integer.class);
         this.put(short.class, Short.class);
         this.put(byte.class, Byte.class);
         this.put(float.class, Float.class);
         this.put(double.class, Double.class);
         this.put(long.class, Long.class);
         this.put(boolean.class, Boolean.class);
      }
   };
   private final Map<String, Function<ParserParameters, ArgumentParser<C, ?>>> namedParsers = new HashMap<>();
   private final Map<AnnotatedType, Function<ParserParameters, ArgumentParser<C, ?>>> parserSuppliers = new AnnotatedTypeMap<>();
   private final Map<Class<? extends Annotation>, ParserRegistry.AnnotationMapper<?>> annotationMappers = new HashMap<>();
   private final Map<String, SuggestionProvider<C>> namedSuggestionProviders = new HashMap<>();

   public StandardParserRegistry() {
      this.registerAnnotationMapper(Range.class, new StandardParserRegistry.RangeMapper());
      this.registerAnnotationMapper(Greedy.class, new StandardParserRegistry.GreedyMapper());
      this.registerAnnotationMapper(Quoted.class, (quoted, typeToken) -> ParserParameters.single(StandardParameters.QUOTED, true));
      this.registerAnnotationMapper(Liberal.class, (liberal, typeToken) -> ParserParameters.single(StandardParameters.LIBERAL, true));
      this.registerAnnotationMapper(FlagYielding.class, (flagYielding, typeToken) -> ParserParameters.single(StandardParameters.FLAG_YIELDING, true));
      this.registerParserSupplier(
         TypeToken.get(Byte.class),
         options -> new ByteParser<>((Byte)options.get(StandardParameters.RANGE_MIN, -128), (Byte)options.get(StandardParameters.RANGE_MAX, 127))
      );
      this.registerParserSupplier(
         TypeToken.get(Short.class),
         options -> new ShortParser<>((Short)options.get(StandardParameters.RANGE_MIN, -32768), (Short)options.get(StandardParameters.RANGE_MAX, 32767))
      );
      this.registerParserSupplier(
         TypeToken.get(Integer.class),
         options -> new IntegerParser<>(
            (Integer)options.get(StandardParameters.RANGE_MIN, Integer.MIN_VALUE), (Integer)options.get(StandardParameters.RANGE_MAX, Integer.MAX_VALUE)
         )
      );
      this.registerParserSupplier(
         TypeToken.get(Long.class),
         options -> new LongParser<>(
            (Long)options.get(StandardParameters.RANGE_MIN, Long.MIN_VALUE), (Long)options.get(StandardParameters.RANGE_MAX, Long.MAX_VALUE)
         )
      );
      this.registerParserSupplier(
         TypeToken.get(Float.class),
         options -> new FloatParser<>(
            (Float)options.get(StandardParameters.RANGE_MIN, Float.NEGATIVE_INFINITY),
            (Float)options.get(StandardParameters.RANGE_MAX, Float.POSITIVE_INFINITY)
         )
      );
      this.registerParserSupplier(
         TypeToken.get(Double.class),
         options -> new DoubleParser<>(
            (Double)options.get(StandardParameters.RANGE_MIN, Double.NEGATIVE_INFINITY),
            (Double)options.get(StandardParameters.RANGE_MAX, Double.POSITIVE_INFINITY)
         )
      );
      this.registerParserSupplier(TypeToken.get(Character.class), options -> new CharacterParser<>());
      this.registerParserSupplier(TypeToken.get(String[].class), options -> new StringArrayParser<>(options.get(StandardParameters.FLAG_YIELDING, false)));
      this.registerParserSupplier(TypeToken.get(String.class), options -> {
         boolean greedy = options.get(StandardParameters.GREEDY, false);
         boolean greedyFlagAware = options.get(StandardParameters.FLAG_YIELDING, false);
         boolean quoted = options.get(StandardParameters.QUOTED, false);
         if (greedyFlagAware && quoted) {
            throw new IllegalArgumentException("Don't know whether to create GREEDY_FLAG_YIELDING or QUOTED StringArgument.StringParser, both specified.");
         }

         if (greedy && quoted) {
            throw new IllegalArgumentException("Don't know whether to create GREEDY or QUOTED StringArgument.StringParser, both specified.");
         }

         StringParser.StringMode stringMode;
         if (greedyFlagAware) {
            stringMode = StringParser.StringMode.GREEDY_FLAG_YIELDING;
         } else if (greedy) {
            stringMode = StringParser.StringMode.GREEDY;
         } else if (quoted) {
            stringMode = StringParser.StringMode.QUOTED;
         } else {
            stringMode = StringParser.StringMode.SINGLE;
         }

         return new StringParser<>(stringMode);
      });
      this.registerParserSupplier(TypeToken.get(Boolean.class), options -> {
         boolean liberal = options.get(StandardParameters.LIBERAL, false);
         return new BooleanParser<>(liberal);
      });
      this.registerParser(UUIDParser.uuidParser());
      this.registerParser(DurationParser.durationParser());
      ServiceLoader<ParserContributor> loader = ServiceLoader.load(ParserContributor.class, ParserContributor.class.getClassLoader());
      loader.iterator().forEachRemaining(contributor -> contributor.contribute(this));
   }

   private static boolean isPrimitive(final @NonNull TypeToken<?> type) {
      return GenericTypeReflector.erase(type.getType()).isPrimitive();
   }

   public <T> @This StandardParserRegistry<C> registerParserSupplier(
      final @NonNull TypeToken<T> type, final @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
   ) {
      this.parserSuppliers.put(type.getAnnotatedType(), supplier);
      return this;
   }

   public @This StandardParserRegistry<C> registerNamedParserSupplier(
      final @NonNull String name, final @NonNull Function<@NonNull ParserParameters, @NonNull ArgumentParser<C, ?>> supplier
   ) {
      this.namedParsers.put(name, supplier);
      return this;
   }

   public <A extends Annotation> @This StandardParserRegistry<C> registerAnnotationMapper(
      final @NonNull Class<A> annotation, final ParserRegistry.@NonNull AnnotationMapper<A> mapper
   ) {
      this.annotationMappers.put(annotation, mapper);
      return this;
   }

   @Override
   public @NonNull ParserParameters parseAnnotations(
      final @NonNull TypeToken<?> parsingType, final @NonNull Collection<? extends @NonNull Annotation> annotations
   ) {
      ParserParameters parserParameters = new ParserParameters();
      annotations.forEach(annotation -> {
         ParserRegistry.AnnotationMapper mapper = this.annotationMappers.get(annotation.annotationType());
         if (mapper != null) {
            ParserParameters parserParametersCasted = mapper.mapAnnotation(annotation, parsingType);
            parserParameters.merge(parserParametersCasted);
         }
      });
      return parserParameters;
   }

   @Override
   public <T> @NonNull Optional<ArgumentParser<C, T>> createParser(final @NonNull TypeToken<T> type, final @NonNull ParserParameters parserParameters) {
      TypeToken<?> actualType;
      if (GenericTypeReflector.erase(type.getType()).isPrimitive()) {
         actualType = TypeToken.get(PRIMITIVE_MAPPINGS.get(GenericTypeReflector.erase(type.getType())));
      } else {
         actualType = type;
      }

      Function<ParserParameters, ArgumentParser<C, ?>> producer = this.parserSuppliers.get(actualType.getAnnotatedType());
      if (producer == null) {
         if (GenericTypeReflector.isSuperType(Enum.class, actualType.getType())) {
            EnumParser enumArgument = new EnumParser<>(GenericTypeReflector.erase(actualType.getType()));
            return Optional.of(enumArgument);
         } else {
            return Optional.empty();
         }
      } else {
         ArgumentParser<C, T> parser = (ArgumentParser<C, T>)producer.apply(parserParameters);
         return Optional.of(parser);
      }
   }

   @Override
   public <T> @NonNull Optional<ArgumentParser<C, T>> createParser(final @NonNull String name, final @NonNull ParserParameters parserParameters) {
      Function<ParserParameters, ArgumentParser<C, ?>> producer = this.namedParsers.get(name);
      if (producer == null) {
         return Optional.empty();
      }

      ArgumentParser<C, T> parser = (ArgumentParser<C, T>)producer.apply(parserParameters);
      return Optional.of(parser);
   }

   @Override
   public void registerSuggestionProvider(final @NonNull String name, final @NonNull SuggestionProvider<C> suggestionProvider) {
      this.namedSuggestionProviders.put(name.toLowerCase(Locale.ENGLISH), suggestionProvider);
   }

   @Override
   public @NonNull Optional<SuggestionProvider<C>> getSuggestionProvider(final @NonNull String name) {
      SuggestionProvider<C> suggestionProvider = this.namedSuggestionProviders.get(name.toLowerCase(Locale.ENGLISH));
      return Optional.ofNullable(suggestionProvider);
   }

   private static final class GreedyMapper implements ParserRegistry.AnnotationMapper<Greedy> {
      private GreedyMapper() {
      }

      public @NonNull ParserParameters mapAnnotation(final @NonNull Greedy greedy, final @NonNull TypeToken<?> typeToken) {
         return ParserParameters.single(StandardParameters.GREEDY, true);
      }
   }

   private static final class RangeMapper implements ParserRegistry.AnnotationMapper<Range> {
      private RangeMapper() {
      }

      public @NonNull ParserParameters mapAnnotation(final @NonNull Range range, final @NonNull TypeToken<?> type) {
         Class<?> clazz;
         if (StandardParserRegistry.isPrimitive(type)) {
            clazz = StandardParserRegistry.PRIMITIVE_MAPPINGS.get(GenericTypeReflector.erase(type.getType()));
         } else {
            clazz = GenericTypeReflector.erase(type.getType());
         }

         if (!Number.class.isAssignableFrom(clazz)) {
            return ParserParameters.empty();
         }

         Number min = null;
         Number max = null;
         if (clazz.equals(Byte.class)) {
            if (!range.min().isEmpty()) {
               min = Byte.parseByte(range.min());
            }

            if (!range.max().isEmpty()) {
               max = Byte.parseByte(range.max());
            }
         } else if (clazz.equals(Short.class)) {
            if (!range.min().isEmpty()) {
               min = Short.parseShort(range.min());
            }

            if (!range.max().isEmpty()) {
               max = Short.parseShort(range.max());
            }
         } else if (clazz.equals(Integer.class)) {
            if (!range.min().isEmpty()) {
               min = Integer.parseInt(range.min());
            }

            if (!range.max().isEmpty()) {
               max = Integer.parseInt(range.max());
            }
         } else if (clazz.equals(Long.class)) {
            if (!range.min().isEmpty()) {
               min = Long.parseLong(range.min());
            }

            if (!range.max().isEmpty()) {
               max = Long.parseLong(range.max());
            }
         } else if (clazz.equals(Float.class)) {
            if (!range.min().isEmpty()) {
               min = Float.parseFloat(range.min());
            }

            if (!range.max().isEmpty()) {
               max = Float.parseFloat(range.max());
            }
         } else if (clazz.equals(Double.class)) {
            if (!range.min().isEmpty()) {
               min = Double.parseDouble(range.min());
            }

            if (!range.max().isEmpty()) {
               max = Double.parseDouble(range.max());
            }
         }

         ParserParameters parserParameters = new ParserParameters();
         if (min != null) {
            parserParameters.store(StandardParameters.RANGE_MIN, min);
         }

         if (max != null) {
            parserParameters.store(StandardParameters.RANGE_MAX, max);
         }

         return parserParameters;
      }
   }
}
