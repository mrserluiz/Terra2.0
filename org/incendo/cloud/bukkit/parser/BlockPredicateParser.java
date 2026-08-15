package org.incendo.cloud.bukkit.parser;

import com.dfsek.terra.lib.google.common.base.Suppliers;
import com.mojang.brigadier.arguments.ArgumentType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.parser.WrappedBrigadierParser;
import org.incendo.cloud.bukkit.data.BlockPredicate;
import org.incendo.cloud.bukkit.internal.CommandBuildContextSupplier;
import org.incendo.cloud.bukkit.internal.CraftBukkitReflection;
import org.incendo.cloud.bukkit.internal.MinecraftArgumentTypes;
import org.incendo.cloud.bukkit.internal.RegistryReflection;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.SuggestionProvider;

public final class BlockPredicateParser<C> implements ArgumentParser.FutureArgumentParser<C, BlockPredicate> {
   private static final Class<?> TAG_CONTAINER_CLASS;
   private static final Class<?> CRAFT_WORLD_CLASS;
   private static final Class<?> MINECRAFT_SERVER_CLASS;
   private static final Class<?> COMMAND_LISTENER_WRAPPER_CLASS;
   private static final Supplier<Class<?>> ARGUMENT_BLOCK_PREDICATE_CLASS;
   private static final Class<?> ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS;
   private static final Class<?> SHAPE_DETECTOR_BLOCK_CLASS;
   private static final Class<?> LEVEL_READER_CLASS;
   private static final Class<?> BLOCK_POSITION_CLASS;
   private static final Constructor<?> BLOCK_POSITION_CTR;
   private static final Constructor<?> SHAPE_DETECTOR_BLOCK_CTR;
   private static final Method GET_HANDLE_METHOD;
   private static final @Nullable Method CREATE_PREDICATE_METHOD;
   private static final Method GET_SERVER_METHOD;
   private static final @Nullable Method GET_TAG_REGISTRY_METHOD;
   private final ArgumentParser<C, BlockPredicate> parser = this.createParser();

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> @NonNull ParserDescriptor<C, BlockPredicate> blockPredicateParser() {
      return ParserDescriptor.of(new BlockPredicateParser<>(), BlockPredicate.class);
   }

   @API(status = Status.STABLE, since = "2.0.0")
   public static <C> CommandComponent.@NonNull Builder<C, BlockPredicate> blockPredicateComponent() {
      return CommandComponent.<C, BlockPredicate>builder().parser(blockPredicateParser());
   }

   private ArgumentParser<C, BlockPredicate> createParser() {
      Supplier<ArgumentType<Object>> inst = () -> {
         Constructor<?> ctr = ARGUMENT_BLOCK_PREDICATE_CLASS.get().getDeclaredConstructors()[0];

         try {
            return ctr.getParameterCount() == 0
               ? (ArgumentType)ctr.newInstance()
               : (ArgumentType)ctr.newInstance(CommandBuildContextSupplier.commandBuildContext());
         } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialize BlockPredicate parser.", e);
         }
      };
      return new WrappedBrigadierParser<C, Object>(inst).flatMapSuccess((ctx, result) -> {
         if (result instanceof Predicate) {
            return ArgumentParseResult.successFuture(new BlockPredicateParser.BlockPredicateImpl((Predicate<Object>)result));
         }

         Object commandSourceStack = ctx.get("_cloud_brigadier_native_sender");

         try {
            Object server = GET_SERVER_METHOD.invoke(commandSourceStack);
            Object obj;
            if (GET_TAG_REGISTRY_METHOD != null) {
               obj = GET_TAG_REGISTRY_METHOD.invoke(server);
            } else {
               obj = RegistryReflection.builtInRegistryByName("block");
            }

            Objects.requireNonNull(CREATE_PREDICATE_METHOD, "create on BlockPredicateArgument$Result");
            Predicate<Object> predicate = (Predicate<Object>)CREATE_PREDICATE_METHOD.invoke(result, obj);
            return ArgumentParseResult.successFuture(new BlockPredicateParser.BlockPredicateImpl(predicate));
         } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
         }
      });
   }

   @Override
   public @NonNull CompletableFuture<ArgumentParseResult<@NonNull BlockPredicate>> parseFuture(
      final @NonNull CommandContext<@NonNull C> commandContext, final @NonNull CommandInput commandInput
   ) {
      return this.parser.parseFuture(commandContext, commandInput);
   }

   @Override
   public @NonNull SuggestionProvider<C> suggestionProvider() {
      return this.parser.suggestionProvider();
   }

   private static <C> void registerParserSupplier(final @NonNull CommandManager<C> commandManager) {
      commandManager.parserRegistry().registerParser(blockPredicateParser());
   }

   static {
      Class<?> tagContainerClass;
      if (CraftBukkitReflection.MAJOR_REVISION > 12 && CraftBukkitReflection.MAJOR_REVISION < 16) {
         tagContainerClass = CraftBukkitReflection.needNMSClass("TagRegistry");
      } else {
         tagContainerClass = CraftBukkitReflection.firstNonNullOrThrow(
            () -> "tagContainerClass",
            CraftBukkitReflection.findNMSClass("ITagRegistry"),
            CraftBukkitReflection.findMCClass("tags.ITagRegistry"),
            CraftBukkitReflection.findMCClass("tags.TagContainer"),
            CraftBukkitReflection.findMCClass("core.IRegistry"),
            CraftBukkitReflection.findMCClass("core.Registry")
         );
      }

      TAG_CONTAINER_CLASS = tagContainerClass;
      CRAFT_WORLD_CLASS = CraftBukkitReflection.needOBCClass("CraftWorld");
      MINECRAFT_SERVER_CLASS = CraftBukkitReflection.needNMSClassOrElse("MinecraftServer", "net.minecraft.server.MinecraftServer");
      COMMAND_LISTENER_WRAPPER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
         () -> "Couldn't find CommandSourceStack class",
         CraftBukkitReflection.findNMSClass("CommandListenerWrapper"),
         CraftBukkitReflection.findMCClass("commands.CommandListenerWrapper"),
         CraftBukkitReflection.findMCClass("commands.CommandSourceStack")
      );
      ARGUMENT_BLOCK_PREDICATE_CLASS = Suppliers.memoize(() -> MinecraftArgumentTypes.getClassByKey(NamespacedKey.minecraft("block_predicate")));
      ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
         () -> "Couldn't find BlockPredicateArgument$Result class",
         CraftBukkitReflection.findNMSClass("ArgumentBlockPredicate$b"),
         CraftBukkitReflection.findMCClass("commands.arguments.blocks.ArgumentBlockPredicate$b"),
         CraftBukkitReflection.findMCClass("commands.arguments.blocks.BlockPredicateArgument$Result")
      );
      SHAPE_DETECTOR_BLOCK_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
         () -> "Couldn't find BlockInWorld class",
         CraftBukkitReflection.findNMSClass("ShapeDetectorBlock"),
         CraftBukkitReflection.findMCClass("world.level.block.state.pattern.ShapeDetectorBlock"),
         CraftBukkitReflection.findMCClass("world.level.block.state.pattern.BlockInWorld")
      );
      LEVEL_READER_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
         () -> "Couldn't find LevelReader class",
         CraftBukkitReflection.findNMSClass("IWorldReader"),
         CraftBukkitReflection.findMCClass("world.level.IWorldReader"),
         CraftBukkitReflection.findMCClass("world.level.LevelReader")
      );
      BLOCK_POSITION_CLASS = CraftBukkitReflection.firstNonNullOrThrow(
         () -> "Couldn't find BlockPos class",
         CraftBukkitReflection.findNMSClass("BlockPosition"),
         CraftBukkitReflection.findMCClass("core.BlockPosition"),
         CraftBukkitReflection.findMCClass("core.BlockPos")
      );
      BLOCK_POSITION_CTR = CraftBukkitReflection.needConstructor(BLOCK_POSITION_CLASS, int.class, int.class, int.class);
      SHAPE_DETECTOR_BLOCK_CTR = CraftBukkitReflection.needConstructor(SHAPE_DETECTOR_BLOCK_CLASS, LEVEL_READER_CLASS, BLOCK_POSITION_CLASS, boolean.class);
      GET_HANDLE_METHOD = CraftBukkitReflection.needMethod(CRAFT_WORLD_CLASS, "getHandle");
      CREATE_PREDICATE_METHOD = CraftBukkitReflection.firstNonNullOrNull(
         CraftBukkitReflection.findMethod(ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS, "create", TAG_CONTAINER_CLASS),
         CraftBukkitReflection.findMethod(ARGUMENT_BLOCK_PREDICATE_RESULT_CLASS, "a", TAG_CONTAINER_CLASS)
      );
      GET_SERVER_METHOD = CraftBukkitReflection.streamMethods(COMMAND_LISTENER_WRAPPER_CLASS)
         .filter(it -> it.getReturnType().equals(MINECRAFT_SERVER_CLASS) && it.getParameterCount() == 0)
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("Could not find CommandSourceStack#getServer."));
      GET_TAG_REGISTRY_METHOD = CraftBukkitReflection.firstNonNullOrNull(
         CraftBukkitReflection.findMethod(MINECRAFT_SERVER_CLASS, "getTagRegistry"),
         CraftBukkitReflection.findMethod(MINECRAFT_SERVER_CLASS, "getTags"),
         CraftBukkitReflection.streamMethods(MINECRAFT_SERVER_CLASS)
            .filter(it -> it.getReturnType().equals(TAG_CONTAINER_CLASS) && it.getParameterCount() == 0)
            .findFirst()
            .orElse(null)
      );
   }

   private static final class BlockPredicateImpl implements BlockPredicate {
      private final Predicate<Object> predicate;

      BlockPredicateImpl(final @NonNull Predicate<Object> predicate) {
         this.predicate = predicate;
      }

      private boolean testImpl(final @NonNull Block block, final boolean loadChunks) {
         try {
            Object blockInWorld = BlockPredicateParser.SHAPE_DETECTOR_BLOCK_CTR
               .newInstance(
                  BlockPredicateParser.GET_HANDLE_METHOD.invoke(block.getWorld()),
                  BlockPredicateParser.BLOCK_POSITION_CTR.newInstance(block.getX(), block.getY(), block.getZ()),
                  loadChunks
               );
            return this.predicate.test(blockInWorld);
         } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
         }
      }

      public boolean test(final @NonNull Block block) {
         return this.testImpl(block, false);
      }

      @Override
      public @NonNull BlockPredicate loadChunks() {
         return new BlockPredicate() {
            @Override
            public @NonNull BlockPredicate loadChunks() {
               return this;
            }

            public boolean test(final Block block) {
               return BlockPredicateImpl.this.testImpl(block, true);
            }
         };
      }
   }
}
