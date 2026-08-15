package com.dfsek.terra.bukkit;

import com.dfsek.terra.api.command.CommandSender;
import com.dfsek.terra.api.config.ConfigPack;
import com.dfsek.terra.api.event.events.platform.CommandRegistrationEvent;
import com.dfsek.terra.api.event.events.platform.PlatformInitializationEvent;
import com.dfsek.terra.api.world.chunk.generation.ChunkGenerator;
import com.dfsek.terra.bukkit.generator.BukkitChunkGeneratorWrapper;
import com.dfsek.terra.bukkit.listeners.CommonListener;
import com.dfsek.terra.bukkit.nms.Initializer;
import com.dfsek.terra.bukkit.util.PaperUtil;
import com.dfsek.terra.bukkit.util.VersionUtil;
import com.dfsek.terra.bukkit.world.BukkitAdapter;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TerraBukkitPlugin extends JavaPlugin {
   private static final Logger logger = LoggerFactory.getLogger(TerraBukkitPlugin.class);
   private PlatformImpl platform;
   private final Map<String, ChunkGenerator> generatorMap = new HashMap<>();
   private AsyncScheduler asyncScheduler = this.getServer().getAsyncScheduler();
   private GlobalRegionScheduler globalRegionScheduler = this.getServer().getGlobalRegionScheduler();

   public void onEnable() {
      if (this.doVersionCheck()) {
         this.platform = Initializer.init(this);
         if (this.platform == null) {
            Bukkit.getPluginManager().disablePlugin(this);
         } else {
            this.platform.getEventManager().callEvent(new PlatformInitializationEvent());

            try {
               LegacyPaperCommandManager<CommandSender> commandManager = this.getCommandSenderPaperCommandManager();
               this.platform.getEventManager().callEvent(new CommandRegistrationEvent(commandManager));
            } catch (Exception e) {
               logger.error("TERRA HAS BEEN DISABLED\n\nErrors occurred while registering commands.\nPlease report this to Terra.\n".strip(), e);
               Bukkit.getPluginManager().disablePlugin(this);
               return;
            }

            Bukkit.getPluginManager().registerEvents(new CommonListener(this.platform), this);
            PaperUtil.checkPaper(this);
         }
      }
   }

   @NotNull
   private LegacyPaperCommandManager<CommandSender> getCommandSenderPaperCommandManager() throws Exception {
      LegacyPaperCommandManager<CommandSender> commandManager = new LegacyPaperCommandManager<>(
         this, ExecutionCoordinator.simpleCoordinator(), SenderMapper.create(BukkitAdapter::adapt, BukkitAdapter::adapt)
      );
      if (commandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
         commandManager.registerBrigadier();
         CloudBrigadierManager<?, ?> brigManager = commandManager.brigadierManager();
         if (brigManager != null) {
            brigManager.setNativeNumberSuggestions(false);
         }
      } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
         commandManager.registerAsynchronousCompletions();
      }

      return commandManager;
   }

   public PlatformImpl getPlatform() {
      return this.platform;
   }

   private boolean doVersionCheck() {
      logger.info("Running on Minecraft version {} with server implementation {}.", VersionUtil.getMinecraftVersionInfo(), Bukkit.getServer().getName());
      if (!VersionUtil.getSpigotVersionInfo().isSpigot()) {
         logger.error("YOU ARE RUNNING A CRAFTBUKKIT OR BUKKIT SERVER. PLEASE UPGRADE TO PAPER.");
      }

      if (!VersionUtil.getSpigotVersionInfo().isPaper()) {
         logger.error("YOU ARE RUNNING A SPIGOT SERVER. PLEASE UPGRADE TO PAPER.");
      }

      if (VersionUtil.getSpigotVersionInfo().isMohist()) {
         if (System.getProperty("IKnowMohistCausesLotsOfIssuesButIWillUseItAnyways") == null) {
            Runnable runnable = () -> logger.error(
               ".----------------------------------------------------------------------------------.\n|                                                                                  |\n|                                ⚠ !! Warning !! ⚠                                 |\n|                                                                                  |\n|                         You are currently using Mohist.                          |\n|                                                                                  |\n|                                Do not use Mohist.                                |\n|                                                                                  |\n|   The concept of combining the rigid Bukkit platform, which assumes a 100%       |\n|   Vanilla server, with the flexible Forge platform, which allows changing        |\n|   core components of the game, simply does not work. These platforms are         |\n|   incompatible at a conceptual level, the only way to combine them would         |\n|   be to make incompatible changes to both. As a result, Mohist's Bukkit          |\n|   API implementation is not compliant. This will cause many plugins to           |\n|   break. Rather than fix their platform, Mohist has chosen to distribute         |\n|   unofficial builds of plugins they deem to be \"fixed\". These builds are not     |\n|   \"fixed\", they are simply hacked together to work with Mohist's half-baked      |\n|   Bukkit implementation. To distribute these as \"fixed\" versions implies that:   |\n|       - These builds are endorsed by the original developers. (They are not)     |\n|       - The issue is on the plugin's end, not Mohist's. (It is not. The issue    |\n|       is that Mohist chooses to not create a compliant Bukkit implementation)    |\n|   Please, do not use Mohist. It causes issues with most plugins, and rather      |\n|   than fixing their platform, Mohist has chosen to distribute unofficial         |\n|   hacked-together builds of plugins, calling them \"fixed\". If you want           |\n|   to use a server API with Forge mods, look at the Sponge project, an            |\n|   API that is designed to be implementation-agnostic, with first-party           |\n|   support for the Forge mod loader. You are bound to encounter issues if         |\n|   you use Terra with Mohist. We will provide NO SUPPORT for servers running      |\n|   Mohist. If you wish to proceed anyways, you can add the JVM System Property    |\n|   \"IKnowMohistCausesLotsOfIssuesButIWillUseItAnyways\" to enable the plugin. No   |\n|   support will be provided for servers running Mohist.                           |\n|                                                                                  |\n|                   Because of this **TERRA HAS BEEN DISABLED**.                   |\n|                    Do not come ask us why it is not working.                     |\n|                                                                                  |\n|----------------------------------------------------------------------------------|\n"
                  .strip()
            );
            runnable.run();
            this.asyncScheduler.runDelayed(this, task -> runnable.run(), 200L, TimeUnit.SECONDS);
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
         }

         logger.warn(
            "You are using Mohist, so we will not give you any support for issues that may arise.\nSince you enabled the \"IKnowMohistCausesLotsOfIssuesButIWillUseItAnyways\" flag, we won't disable Terra. But be warned.\n\n> I felt a great disturbance in the JVM, as if millions of plugins suddenly cried out in stack traces and were suddenly silenced.\n> I fear something terrible has happened.\n> - Astrash\n"
               .strip()
         );
      }

      return true;
   }

   @Nullable
   public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {
      return id != null && !id.trim().isEmpty() ? new BukkitChunkGeneratorWrapper(this.generatorMap.computeIfAbsent(worldName, name -> {
         ConfigPack pack = this.platform.getConfigRegistry().getByID(id).orElseThrow(() -> new IllegalArgumentException("No such config pack \"" + id + "\""));
         return pack.getGeneratorProvider().newInstance(pack);
      }), this.platform.getRawConfigRegistry().getByID(id).orElseThrow(), this.platform.getWorldHandle().air()) : null;
   }

   public AsyncScheduler getAsyncScheduler() {
      return this.asyncScheduler;
   }

   public GlobalRegionScheduler getGlobalRegionScheduler() {
      return this.globalRegionScheduler;
   }
}
