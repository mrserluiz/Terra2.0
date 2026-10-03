/*
 * This file is part of Terra.
 *
 * Terra is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Terra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Terra.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.dfsek.terra.bukkit.world.block.data;

import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.block.state.properties.Property;
import com.dfsek.terra.bukkit.world.BukkitAdapter;


public class BukkitBlockState implements BlockState {
    private final org.bukkit.block.data.BlockData delegate;
    private org.bukkit.loot.LootTable lootTable;

    protected BukkitBlockState(org.bukkit.block.data.BlockData delegate) {
        this.delegate = delegate;
    }

    public static BlockState newInstance(org.bukkit.block.data.BlockData bukkitData) {
        return new BukkitBlockState(bukkitData);
    }

    public static BlockState newInstance(org.bukkit.block.data.BlockData data, String table) {
        BukkitBlockState state = new BukkitBlockState(data);
        if(table != null) {
            org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(table);
            if(key == null) throw new IllegalArgumentException("Invalid loot table " + table);
            state.lootTable = org.bukkit.Bukkit.getLootTable(key);
            if(state.lootTable == null) throw new IllegalArgumentException("Unknown loot table " + table);
        }
        return state;
    }

    public void applyBlockEntity(org.bukkit.block.BlockState placed) {
        if(lootTable == null) return;
        if(!(placed instanceof org.bukkit.loot.Lootable lootable)) {
            throw new IllegalArgumentException("Loot table requires a lootable block: " + delegate.getMaterial());
        }
        lootable.setLootTable(lootTable);
        if(!placed.update(true, false)) throw new IllegalStateException("Failed to update generated loot block");
    }

    public boolean hasBlockEntityData() { return lootTable != null; }


    @Override
    public org.bukkit.block.data.BlockData getHandle() {
        return delegate;
    }

    @Override
    public boolean matches(BlockState data) {
        return delegate.getMaterial() == ((BukkitBlockState) data).getHandle().getMaterial();
    }

    @Override
    public <T extends Comparable<T>> boolean has(Property<T> property) {
        return false;
    }

    @Override
    public <T extends Comparable<T>> T get(Property<T> property) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> BlockState set(Property<T> property, T value) {
        return null;
    }

    @Override
    public BlockType getBlockType() {
        return BukkitAdapter.adapt(delegate.getMaterial());
    }

    @Override
    public String getAsString(boolean properties) {
        return delegate.getAsString(!properties) + (lootTable == null ? "" : "{LootTable:'" + lootTable.getKey() + "'}");
    }

    @Override
    public boolean isAir() {
        return delegate.getMaterial().isAir();
    }
}
