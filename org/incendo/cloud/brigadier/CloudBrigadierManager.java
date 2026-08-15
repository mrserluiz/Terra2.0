package org.incendo.cloud.brigadier;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.SenderMapperHolder;
import org.incendo.cloud.brigadier.argument.ArgumentTypeFactory;
import org.incendo.cloud.brigadier.argument.BrigadierMapping;
import org.incendo.cloud.brigadier.argument.BrigadierMappingBuilder;
import org.incendo.cloud.brigadier.argument.BrigadierMappingContributor;
import org.incendo.cloud.brigadier.argument.BrigadierMappings;
import org.incendo.cloud.brigadier.node.LiteralBrigadierNodeFactory;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.brigadier.suggestion.TooltipSuggestion;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.flag.CommandFlagParser;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.ByteParser;
import org.incendo.cloud.parser.standard.DoubleParser;
import org.incendo.cloud.parser.standard.FloatParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.LongParser;
import org.incendo.cloud.parser.standard.ShortParser;
import org.incendo.cloud.parser.standard.StringArrayParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.setting.Configurable;

public final class CloudBrigadierManager<C, S> implements SenderMapperHolder<S, C> {
   private final BrigadierMappings<C, S> brigadierMappings = BrigadierMappings.create();
   private final LiteralBrigadierNodeFactory<C, S> literalBrigadierNodeFactory;
   private final Map<@NonNull Class<?>, @NonNull ArgumentTypeFactory<?>> defaultArgumentTypeSuppliers;
   private final Configurable<BrigadierSetting> settings = Configurable.enumConfigurable(BrigadierSetting.class);
   private final SenderMapper<S, C> brigadierSourceMapper;

   public CloudBrigadierManager(final @NonNull CommandManager<C> commandManager, final @NonNull SenderMapper<S, C> brigadierSourceMapper) {
      this.brigadierSourceMapper = Objects.requireNonNull(brigadierSourceMapper, "brigadierSourceMapper");
      this.defaultArgumentTypeSuppliers = new HashMap<>();
      this.literalBrigadierNodeFactory = new LiteralBrigadierNodeFactory<>(
         this, commandManager, commandManager.suggestionFactory().mapped(TooltipSuggestion::tooltipSuggestion)
      );
      this.registerInternalMappings();
      ServiceLoader<BrigadierMappingContributor> loader = ServiceLoader.load(
         BrigadierMappingContributor.class, BrigadierMappingContributor.class.getClassLoader()
      );
      loader.iterator().forEachRemaining(contributor -> contributor.contribute(commandManager, this));
      commandManager.registerCommandPreProcessor(ctx -> {
         if (!ctx.commandContext().contains("_cloud_brigadier_native_sender")) {
            ctx.commandContext().store("_cloud_brigadier_native_sender", this.brigadierSourceMapper.reverse((C)ctx.commandContext().sender()));
         }
      });
   }

   private void registerInternalMappings() {
      this.registerMapping(
         new TypeToken<ByteParser<C>>() {},
         builder -> builder.to(argument -> IntegerArgumentType.integer(argument.range().minByte(), argument.range().maxByte())).cloudSuggestions()
      );
      this.registerMapping(
         new TypeToken<ShortParser<C>>() {},
         builder -> builder.to(argument -> IntegerArgumentType.integer(argument.range().minShort(), argument.range().maxShort())).cloudSuggestions()
      );
      this.registerMapping(
         new TypeToken<IntegerParser<C>>() {},
         builder -> builder.to(
               argument -> {
                  if (!argument.hasMin() && !argument.hasMax()) {
                     return IntegerArgumentType.integer();
                  } else if (argument.hasMin() && !argument.hasMax()) {
                     return IntegerArgumentType.integer(argument.range().minInt());
                  } else {
                     return !argument.hasMin()
                        ? IntegerArgumentType.integer(Integer.MIN_VALUE, argument.range().maxInt())
                        : IntegerArgumentType.integer(argument.range().minInt(), argument.range().maxInt());
                  }
               }
            )
            .cloudSuggestions()
      );
      this.registerMapping(
         new TypeToken<FloatParser<C>>() {},
         builder -> builder.to(
               argument -> {
                  if (!argument.hasMin() && !argument.hasMax()) {
                     return FloatArgumentType.floatArg();
                  } else if (argument.hasMin() && !argument.hasMax()) {
                     return FloatArgumentType.floatArg(argument.range().minFloat());
                  } else {
                     return !argument.hasMin()
                        ? FloatArgumentType.floatArg(-Float.MAX_VALUE, argument.range().maxFloat())
                        : FloatArgumentType.floatArg(argument.range().minFloat(), argument.range().maxFloat());
                  }
               }
            )
            .cloudSuggestions()
      );
      this.registerMapping(
         new TypeToken<DoubleParser<C>>() {},
         builder -> builder.to(
               argument -> {
                  if (!argument.hasMin() && !argument.hasMax()) {
                     return DoubleArgumentType.doubleArg();
                  } else if (argument.hasMin() && !argument.hasMax()) {
                     return DoubleArgumentType.doubleArg(argument.range().minDouble());
                  } else {
                     return !argument.hasMin()
                        ? DoubleArgumentType.doubleArg(-Double.MAX_VALUE, argument.range().maxDouble())
                        : DoubleArgumentType.doubleArg(argument.range().minDouble(), argument.range().maxDouble());
                  }
               }
            )
            .cloudSuggestions()
      );
      this.registerMapping(
         new TypeToken<LongParser<C>>() {},
         builder -> builder.to(
               longParser -> {
                  if (!longParser.hasMin() && !longParser.hasMax()) {
                     return LongArgumentType.longArg();
                  } else if (longParser.hasMin() && !longParser.hasMax()) {
                     return LongArgumentType.longArg(longParser.range().minLong());
                  } else {
                     return !longParser.hasMin()
                        ? LongArgumentType.longArg(Long.MIN_VALUE, longParser.range().maxLong())
                        : LongArgumentType.longArg(longParser.range().minLong(), longParser.range().maxLong());
                  }
               }
            )
            .cloudSuggestions()
      );
      this.registerMapping(new TypeToken<BooleanParser<C>>() {}, builder -> builder.toConstant(BoolArgumentType.bool()));
      this.registerMapping(new TypeToken<StringParser<C>>() {}, builder -> builder.cloudSuggestions().to(argument -> {
         switch (argument.stringMode()) {
            case QUOTED:
               return StringArgumentType.string();
            case GREEDY:
            case GREEDY_FLAG_YIELDING:
               return StringArgumentType.greedyString();
            default:
               return StringArgumentType.word();
         }
      }));
      this.registerMapping(new TypeToken<CommandFlagParser<C>>() {}, builder -> builder.cloudSuggestions().toConstant(StringArgumentType.greedyString()));
      this.registerMapping(new TypeToken<StringArrayParser<C>>() {}, builder -> builder.cloudSuggestions().toConstant(StringArgumentType.greedyString()));
      this.registerMapping(new TypeToken<WrappedBrigadierParser<C, ?>>() {}, builder -> builder.to(WrappedBrigadierParser::nativeArgumentType));
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public @NonNull Configurable<BrigadierSetting> settings() {
      return this.settings;
   }

   @Override
   public @NonNull SenderMapper<S, C> senderMapper() {
      return this.brigadierSourceMapper;
   }

   @API(status = Status.STABLE, since = "1.2.0")
   public void setNativeNumberSuggestions(final boolean nativeNumberSuggestions) {
      this.setNativeSuggestions(new TypeToken<ByteParser<C>>() {}, nativeNumberSuggestions);
      this.setNativeSuggestions(new TypeToken<ShortParser<C>>() {}, nativeNumberSuggestions);
      this.setNativeSuggestions(new TypeToken<IntegerParser<C>>() {}, nativeNumberSuggestions);
      this.setNativeSuggestions(new TypeToken<FloatParser<C>>() {}, nativeNumberSuggestions);
      this.setNativeSuggestions(new TypeToken<DoubleParser<C>>() {}, nativeNumberSuggestions);
      this.setNativeSuggestions(new TypeToken<LongParser<C>>() {}, nativeNumberSuggestions);
   }

   @API(status = Status.STABLE, since = "1.2.0")
   public <T, K extends ArgumentParser<C, T>> void setNativeSuggestions(final @NonNull TypeToken<K> argumentType, final boolean nativeSuggestions) throws IllegalArgumentException {
      Class<K> parserClass = (Class<K>)GenericTypeReflector.erase(argumentType.getType());
      BrigadierMapping<C, K, S> mapping = this.brigadierMappings.mapping(parserClass);
      if (mapping == null) {
         throw new IllegalArgumentException("No mapper registered for type: " + GenericTypeReflector.erase(argumentType.getType()).toGenericString());
      }

      this.brigadierMappings.registerMapping(parserClass, mapping.withNativeSuggestions(nativeSuggestions));
   }

   @API(status = Status.STABLE, since = "1.5.0")
   public <K extends ArgumentParser<C, ?>> void registerMapping(
      final @NonNull TypeToken<K> parserType, final Consumer<BrigadierMappingBuilder<K, S>> configurer
   ) {
      BrigadierMappingBuilder<K, S> builder = BrigadierMapping.builder();
      configurer.accept(builder);
      this.mappings().registerMappingUnsafe(GenericTypeReflector.erase(parserType.getType()), builder.build());
   }

   @API(status = Status.INTERNAL, since = "2.0.0")
   public @NonNull BrigadierMappings<C, S> mappings() {
      return this.brigadierMappings;
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public @NonNull LiteralBrigadierNodeFactory<C, S> literalBrigadierNodeFactory() {
      return this.literalBrigadierNodeFactory;
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public <T> void registerDefaultArgumentTypeSupplier(final @NonNull Class<T> clazz, final @NonNull ArgumentTypeFactory<T> factory) {
      this.defaultArgumentTypeSuppliers.put(clazz, factory);
   }

   @API(status = Status.INTERNAL, since = "2.0.0")
   public @NonNull Map<@NonNull Class<?>, @NonNull ArgumentTypeFactory<?>> defaultArgumentTypeFactories() {
      return Collections.unmodifiableMap(this.defaultArgumentTypeSuppliers);
   }
}
