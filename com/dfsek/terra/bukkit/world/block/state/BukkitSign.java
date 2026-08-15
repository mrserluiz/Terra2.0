package com.dfsek.terra.bukkit.world.block.state;

import com.dfsek.terra.api.block.entity.SerialState;
import com.dfsek.terra.api.block.entity.Sign;
import org.jetbrains.annotations.NotNull;

public class BukkitSign extends BukkitBlockEntity implements Sign {
   protected BukkitSign(org.bukkit.block.Sign block) {
      super(block);
   }

   @Override
   public void setLine(int index, @NotNull String line) throws IndexOutOfBoundsException {
      ((org.bukkit.block.Sign)this.getHandle()).setLine(index, line);
   }

   @NotNull
   @Override
   public String[] getLines() {
      return ((org.bukkit.block.Sign)this.getHandle()).getLines();
   }

   @NotNull
   @Override
   public String getLine(int index) throws IndexOutOfBoundsException {
      return ((org.bukkit.block.Sign)this.getHandle()).getLine(index);
   }

   @Override
   public void applyState(String state) {
      SerialState.parse(state).forEach((k, v) -> {
         if (!k.startsWith("text")) {
            throw new IllegalArgumentException("Invalid property: " + k);
         }

         this.setLine(Integer.parseInt(k.substring(4)), v);
      });
   }
}
