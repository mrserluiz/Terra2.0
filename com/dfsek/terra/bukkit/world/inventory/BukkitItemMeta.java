package com.dfsek.terra.bukkit.world.inventory;

import com.dfsek.terra.api.inventory.item.Enchantment;
import com.dfsek.terra.api.inventory.item.ItemMeta;
import com.dfsek.terra.bukkit.world.BukkitAdapter;
import com.dfsek.terra.bukkit.world.inventory.meta.BukkitDamageable;
import com.dfsek.terra.bukkit.world.inventory.meta.BukkitEnchantment;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.inventory.meta.Damageable;

public class BukkitItemMeta implements ItemMeta {
   private final org.bukkit.inventory.meta.ItemMeta delegate;

   protected BukkitItemMeta(org.bukkit.inventory.meta.ItemMeta delegate) {
      this.delegate = delegate;
   }

   public static BukkitItemMeta newInstance(org.bukkit.inventory.meta.ItemMeta delegate) {
      return delegate instanceof Damageable ? new BukkitDamageable((Damageable)delegate) : new BukkitItemMeta(delegate);
   }

   public org.bukkit.inventory.meta.ItemMeta getHandle() {
      return this.delegate;
   }

   @Override
   public void addEnchantment(Enchantment enchantment, int level) {
      this.delegate.addEnchant(((BukkitEnchantment)enchantment).getHandle(), level, true);
   }

   @Override
   public Map<Enchantment, Integer> getEnchantments() {
      Map<Enchantment, Integer> map = new HashMap<>();
      this.delegate.getEnchants().forEach((enchantment, integer) -> map.put(BukkitAdapter.adapt(enchantment), integer));
      return map;
   }
}
