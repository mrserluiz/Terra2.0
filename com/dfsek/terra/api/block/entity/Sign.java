package com.dfsek.terra.api.block.entity;

import org.jetbrains.annotations.NotNull;

public interface Sign extends BlockEntity {
   void setLine(int var1, @NotNull String var2) throws IndexOutOfBoundsException;

   @NotNull
   String[] getLines();

   @NotNull
   String getLine(int var1) throws IndexOutOfBoundsException;
}
