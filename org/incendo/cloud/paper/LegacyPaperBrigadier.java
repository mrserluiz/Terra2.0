package org.incendo.cloud.paper;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import java.util.regex.Pattern;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.CommandTree;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.brigadier.BrigadierManagerHolder;
import org.incendo.cloud.brigadier.CloudBrigadierCommand;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.brigadier.node.LiteralBrigadierNodeFactory;
import org.incendo.cloud.brigadier.permission.BrigadierPermissionChecker;
import org.incendo.cloud.bukkit.internal.BukkitBackwardsBrigadierSenderMapper;
import org.incendo.cloud.bukkit.internal.BukkitBrigadierMapper;
import org.incendo.cloud.bukkit.internal.BukkitHelper;
import org.incendo.cloud.internal.CommandNode;

class LegacyPaperBrigadier<C> implements Listener, BrigadierManagerHolder<C, BukkitBrigadierCommandSource> {
   private final CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager;
   private final LegacyPaperCommandManager<C> paperCommandManager;

   LegacyPaperBrigadier(final @NonNull LegacyPaperCommandManager<C> paperCommandManager) {
      this.paperCommandManager = paperCommandManager;
      this.brigadierManager = new CloudBrigadierManager<>(
         this.paperCommandManager,
         SenderMapper.create(
            sender -> this.paperCommandManager.senderMapper().map(sender.getBukkitSender()),
            new BukkitBackwardsBrigadierSenderMapper<>(this.paperCommandManager.senderMapper())
         )
      );
      BukkitBrigadierMapper<C> mapper = new BukkitBrigadierMapper<>(this.paperCommandManager.owningPlugin().getLogger(), this.brigadierManager);
      mapper.registerBuiltInMappings();
      PaperBrigadierMappings.register(mapper);
   }

   @Override
   public final boolean hasBrigadierManager() {
      return true;
   }

   @Override
   public final @NonNull CloudBrigadierManager<C, BukkitBrigadierCommandSource> brigadierManager() {
      return this.brigadierManager;
   }

   @EventHandler
   public void onCommandRegister(final @NonNull CommandRegisteredEvent<BukkitBrigadierCommandSource> event) {
      if (event.getCommand() instanceof PluginIdentifiableCommand) {
         if (((PluginIdentifiableCommand)event.getCommand()).getPlugin().equals(this.paperCommandManager.owningPlugin())) {
            CommandTree<C> commandTree = this.paperCommandManager.commandTree();
            String label;
            if (event.getCommandLabel().contains(":")) {
               label = event.getCommandLabel().split(Pattern.quote(":"))[1];
            } else {
               label = event.getCommandLabel();
            }

            CommandNode<C> node = commandTree.getNamedNode(label);
            if (node != null) {
               BrigadierPermissionChecker<C> permissionChecker = (sender, permission) -> commandTree.getNamedNode(label) == null
                  ? false
                  : this.paperCommandManager.testPermission(sender, permission).allowed();
               LiteralBrigadierNodeFactory<C, BukkitBrigadierCommandSource> literalFactory = this.brigadierManager.literalBrigadierNodeFactory();
               event.setLiteral(
                  literalFactory.createNode(
                     event.getLiteral().getLiteral(),
                     node,
                     new CloudBrigadierCommand<>(
                        this.paperCommandManager, this.brigadierManager, command -> BukkitHelper.stripNamespace(this.paperCommandManager, command)
                     ),
                     permissionChecker
                  )
               );
            }
         }
      }
   }
}
