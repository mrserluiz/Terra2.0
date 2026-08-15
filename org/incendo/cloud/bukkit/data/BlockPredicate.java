package org.incendo.cloud.bukkit.data;

import java.util.function.Predicate;
import org.bukkit.block.Block;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface BlockPredicate extends Predicate<Block> {
   @NonNull BlockPredicate loadChunks();
}
