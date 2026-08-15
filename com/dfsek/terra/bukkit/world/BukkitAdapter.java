package com.dfsek.terra.bukkit.world;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.block.state.properties.enums.Axis;
import com.dfsek.terra.api.block.state.properties.enums.Half;
import com.dfsek.terra.api.block.state.properties.enums.RailShape;
import com.dfsek.terra.api.block.state.properties.enums.RedstoneConnection;
import com.dfsek.terra.api.command.CommandSender;
import com.dfsek.terra.api.entity.Entity;
import com.dfsek.terra.api.inventory.ItemStack;
import com.dfsek.terra.api.inventory.item.Enchantment;
import com.dfsek.terra.api.util.vector.Vector3;
import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.api.world.chunk.Chunk;
import com.dfsek.terra.api.world.info.WorldProperties;
import com.dfsek.terra.bukkit.BukkitCommandSender;
import com.dfsek.terra.bukkit.BukkitEntity;
import com.dfsek.terra.bukkit.BukkitPlayer;
import com.dfsek.terra.bukkit.world.block.BukkitBlockTypeAndItem;
import com.dfsek.terra.bukkit.world.block.data.BukkitBlockState;
import com.dfsek.terra.bukkit.world.inventory.BukkitItemStack;
import com.dfsek.terra.bukkit.world.inventory.meta.BukkitEnchantment;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rail.Shape;
import org.bukkit.block.data.type.RedstoneWire.Connection;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.Vector;

public final class BukkitAdapter {
   public static BlockState adapt(BlockData data) {
      return BukkitBlockState.newInstance(data);
   }

   public static BlockData adapt(BlockState data) {
      return ((BukkitBlockState)data).getHandle();
   }

   public static Axis adapt(org.bukkit.Axis axis) {
      return switch (axis) {
         case X -> Axis.X;
         case Y -> Axis.Y;
         case Z -> Axis.Z;
         default -> throw new MatchException(null, null);
      };
   }

   public static WorldProperties adapt(WorldInfo worldInfo) {
      return new BukkitWorldProperties(worldInfo);
   }

   public static WorldInfo adapt(WorldProperties properties) {
      return (WorldInfo)properties.getHandle();
   }

   public static Half adapt(org.bukkit.block.data.Bisected.Half half) {
      return switch (half) {
         case BOTTOM -> Half.BOTTOM;
         case TOP -> Half.TOP;
         default -> throw new MatchException(null, null);
      };
   }

   public static RedstoneConnection adapt(Connection connection) {
      return switch (connection) {
         case NONE -> RedstoneConnection.NONE;
         case UP -> RedstoneConnection.UP;
         case SIDE -> RedstoneConnection.SIDE;
         default -> throw new MatchException(null, null);
      };
   }

   public static Connection adapt(RedstoneConnection connection) {
      return switch (connection) {
         case SIDE -> Connection.SIDE;
         case UP -> Connection.UP;
         case NONE -> Connection.NONE;
      };
   }

   public static RailShape adapt(Shape shape) {
      return switch (shape) {
         case SOUTH_WEST -> RailShape.SOUTH_WEST;
         case SOUTH_EAST -> RailShape.SOUTH_EAST;
         case NORTH_EAST -> RailShape.NORTH_EAST;
         case NORTH_WEST -> RailShape.NORTH_WEST;
         case ASCENDING_EAST -> RailShape.ASCENDING_EAST;
         case ASCENDING_WEST -> RailShape.ASCENDING_WEST;
         case ASCENDING_SOUTH -> RailShape.ASCENDING_SOUTH;
         case ASCENDING_NORTH -> RailShape.ASCENDING_NORTH;
         case NORTH_SOUTH -> RailShape.NORTH_SOUTH;
         case EAST_WEST -> RailShape.EAST_WEST;
         default -> throw new MatchException(null, null);
      };
   }

   public static Shape adapt(RailShape shape) {
      return switch (shape) {
         case EAST_WEST -> Shape.EAST_WEST;
         case NORTH_SOUTH -> Shape.NORTH_SOUTH;
         case ASCENDING_NORTH -> Shape.ASCENDING_NORTH;
         case ASCENDING_SOUTH -> Shape.ASCENDING_SOUTH;
         case ASCENDING_WEST -> Shape.ASCENDING_WEST;
         case ASCENDING_EAST -> Shape.ASCENDING_EAST;
         case NORTH_WEST -> Shape.NORTH_WEST;
         case NORTH_EAST -> Shape.NORTH_EAST;
         case SOUTH_EAST -> Shape.SOUTH_EAST;
         case SOUTH_WEST -> Shape.SOUTH_WEST;
      };
   }

   public static org.bukkit.block.data.Bisected.Half adapt(Half half) {
      return switch (half) {
         case TOP -> org.bukkit.block.data.Bisected.Half.TOP;
         case BOTTOM -> org.bukkit.block.data.Bisected.Half.BOTTOM;
         default -> throw new IllegalStateException();
      };
   }

   public static org.bukkit.Axis adapt(Axis axis) {
      return switch (axis) {
         case Z -> org.bukkit.Axis.Z;
         case Y -> org.bukkit.Axis.Y;
         case X -> org.bukkit.Axis.X;
      };
   }

   public static Vector3 adapt(Location location) {
      return Vector3.of(location.getX(), location.getY(), location.getZ());
   }

   public static Vector adapt(Vector3 vector3) {
      return new Vector(vector3.getX(), vector3.getY(), vector3.getZ());
   }

   public static Vector3 adapt(Vector vector) {
      return Vector3.of(vector.getX(), vector.getY(), vector.getZ());
   }

   public static CommandSender adapt(org.bukkit.command.CommandSender sender) {
      return new BukkitCommandSender(sender);
   }

   public static Entity adapt(org.bukkit.entity.Entity entity) {
      return new BukkitEntity(entity);
   }

   public static org.bukkit.command.CommandSender adapt(CommandSender sender) {
      return ((BukkitCommandSender)sender).getHandle();
   }

   public static ServerWorld adapt(World world) {
      return new BukkitServerWorld(world);
   }

   public static World adapt(ServerWorld world) {
      return (World)world.getHandle();
   }

   public static Chunk adapt(org.bukkit.Chunk chunk) {
      return new BukkitChunk(chunk);
   }

   public static org.bukkit.Chunk adapt(Chunk chunk) {
      return (org.bukkit.Chunk)chunk.getHandle();
   }

   public static Enchantment adapt(org.bukkit.enchantments.Enchantment enchantment) {
      return new BukkitEnchantment(enchantment);
   }

   public static org.bukkit.enchantments.Enchantment adapt(Enchantment enchantment) {
      return ((BukkitEnchantment)enchantment).getHandle();
   }

   public static Player adapt(com.dfsek.terra.api.entity.Player player) {
      return ((BukkitPlayer)player).getHandle();
   }

   public static com.dfsek.terra.api.entity.Player adapt(Player player) {
      return new BukkitPlayer(player);
   }

   public static BukkitBlockTypeAndItem adapt(Material material) {
      return new BukkitBlockTypeAndItem(material);
   }

   public static Material adapt(BlockType type) {
      return ((BukkitBlockTypeAndItem)type).getHandle();
   }

   public static ItemStack adapt(org.bukkit.inventory.ItemStack in) {
      return new BukkitItemStack(in);
   }

   public static org.bukkit.inventory.ItemStack adapt(ItemStack in) {
      return ((BukkitItemStack)in).getHandle();
   }
}
