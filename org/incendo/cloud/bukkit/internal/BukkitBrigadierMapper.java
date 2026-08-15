package org.incendo.cloud.bukkit.internal;

import com.dfsek.terra.lib.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.Constructor;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.parser.BlockPredicateParser;
import org.incendo.cloud.bukkit.parser.EnchantmentParser;
import org.incendo.cloud.bukkit.parser.ItemStackParser;
import org.incendo.cloud.bukkit.parser.ItemStackPredicateParser;
import org.incendo.cloud.bukkit.parser.NamespacedKeyParser;
import org.incendo.cloud.bukkit.parser.location.Location2DParser;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.bukkit.parser.selector.MultipleEntitySelectorParser;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import org.incendo.cloud.bukkit.parser.selector.SingleEntitySelectorParser;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.standard.UUIDParser;

@API(status = Status.INTERNAL)
public final class BukkitBrigadierMapper<C> {
   private final Logger logger;
   private final CloudBrigadierManager<C, ?> brigadierManager;

   public BukkitBrigadierMapper(final @NonNull Logger logger, final @NonNull CloudBrigadierManager<C, ?> brigadierManager) {
      this.logger = logger;
      this.brigadierManager = brigadierManager;
   }

   public void registerBuiltInMappings() {
      this.registerUUID();
      this.mapSimpleNMS(new TypeToken<NamespacedKeyParser<C>>() {}, "resource_location", true);
      this.registerEnchantment();
      this.mapSimpleNMS(new TypeToken<ItemStackParser<C>>() {}, "item_stack");
      this.mapSimpleNMS(new TypeToken<ItemStackPredicateParser<C>>() {}, "item_predicate");
      this.mapSimpleNMS(new TypeToken<BlockPredicateParser<C>>() {}, "block_predicate");
      this.mapSelector(new TypeToken<SingleEntitySelectorParser<C>>() {}, true, false);
      this.mapSelector(new TypeToken<SinglePlayerSelectorParser<C>>() {}, true, true);
      this.mapSelector(new TypeToken<MultipleEntitySelectorParser<C>>() {}, false, false);
      this.mapSelector(new TypeToken<MultiplePlayerSelectorParser<C>>() {}, false, true);
      this.mapNMS(new TypeToken<LocationParser<C>>() {}, "vec3", this::argumentVec3);
      this.mapNMS(new TypeToken<Location2DParser<C>>() {}, "vec2", this::argumentVec2);
   }

   private void registerEnchantment() {
      if (Bukkit.getServer() == null) {
         this.mapResourceKey(new TypeToken<EnchantmentParser<C>>() {}, "enchantment");
      } else {
         try {
            Class<? extends ArgumentType<?>> ench = MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("item_enchantment"));
            this.mapSimpleNMS(new TypeToken<EnchantmentParser<C>>() {}, "item_enchantment");
         } catch (IllegalArgumentException ignore) {
            this.mapResourceKey(new TypeToken<EnchantmentParser<C>>() {}, "enchantment");
         }
      }
   }

   private void registerUUID() {
      if (Bukkit.getServer() == null) {
         this.mapSimpleNMS(new TypeToken<UUIDParser<C>>() {}, "uuid");
      } else {
         try {
            Class<? extends ArgumentType<?>> uuid = MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("uuid"));
            this.mapSimpleNMS(new TypeToken<UUIDParser<C>>() {}, "uuid");
         } catch (IllegalArgumentException var2) {
         }
      }
   }

   private <T extends ArgumentParser<C, ?>> void mapResourceKey(final @NonNull TypeToken<T> parserType, final @NonNull String registryName) {
      this.mapNMS(
         parserType, "resource_key", type -> (ArgumentType<?>)type.getDeclaredConstructors()[0].newInstance(RegistryReflection.registryKey(registryName))
      );
   }

   private <T extends ArgumentParser<C, ?>> void mapSelector(final @NonNull TypeToken<T> parserType, final boolean single, final boolean playersOnly) {
      this.mapNMS(parserType, "entity", argumentTypeCls -> {
         Constructor<?> constructor = argumentTypeCls.getDeclaredConstructors()[0];
         constructor.setAccessible(true);
         return (ArgumentType<?>)constructor.newInstance(single, playersOnly);
      });
   }

   private @NonNull ArgumentType<?> argumentVec3(final Class<? extends ArgumentType<?>> type) throws ReflectiveOperationException {
      return (ArgumentType<?>)type.getDeclaredConstructor(boolean.class).newInstance(true);
   }

   private @NonNull ArgumentType<?> argumentVec2(final Class<? extends ArgumentType<?>> type) throws ReflectiveOperationException {
      return (ArgumentType<?>)type.getDeclaredConstructor(boolean.class).newInstance(true);
   }

   public <T extends ArgumentParser<C, ?>> void mapSimpleNMS(final @NonNull TypeToken<T> type, final @NonNull String argumentId) {
      this.mapSimpleNMS(type, argumentId, false);
   }

   public <T extends ArgumentParser<C, ?>> void mapSimpleNMS(
      final @NonNull TypeToken<T> type, final @NonNull String argumentId, final boolean useCloudSuggestions
   ) {
      this.mapNMS(type, argumentId, cls -> {
         Constructor<?> ctr = cls.getDeclaredConstructors()[0];
         Object[] args = ctr.getParameterCount() == 1 ? new Object[]{CommandBuildContextSupplier.commandBuildContext()} : new Object[0];
         return (ArgumentType<?>)ctr.newInstance(args);
      }, useCloudSuggestions);
   }

   public <T extends ArgumentParser<C, ?>> void mapNMS(
      final @NonNull TypeToken<T> type, final @NonNull String argumentId, final BukkitBrigadierMapper.@NonNull ArgumentTypeFactory factory
   ) {
      this.mapNMS(type, argumentId, factory, false);
   }

   public <T extends ArgumentParser<C, ?>> void mapNMS(
      final @NonNull TypeToken<T> type,
      final @NonNull String argumentId,
      final BukkitBrigadierMapper.@NonNull ArgumentTypeFactory factory,
      final boolean cloudSuggestions
   ) {
      Supplier<Class<? extends ArgumentType<?>>> argumentTypeClass = Suppliers.memoize(() -> {
         try {
            return MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft(argumentId));
         } catch (Exception e) {
            throw new RuntimeException("Failed to locate class for " + argumentId, e);
         }
      });
      this.brigadierManager.registerMapping(type, builder -> {
         builder.to(argument -> {
            try {
               return factory.makeInstance(argumentTypeClass.get());
            } catch (Exception e) {
               this.logger.log(Level.WARNING, "Failed to create instance of " + argumentId + ", falling back to StringArgumentType.word()", e);
               return StringArgumentType.word();
            }
         });
         if (cloudSuggestions) {
            builder.cloudSuggestions();
         }
      });
   }

   @API(status = Status.INTERNAL)
   @FunctionalInterface
   public interface ArgumentTypeFactory {
      ArgumentType<?> makeInstance(Class<? extends ArgumentType<?>> argumentTypeClass) throws ReflectiveOperationException;
   }
}
