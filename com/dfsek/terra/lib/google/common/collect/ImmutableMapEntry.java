package com.dfsek.terra.lib.google.common.collect;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import org.jspecify.annotations.Nullable;

@GwtIncompatible
class ImmutableMapEntry<K, V> extends ImmutableEntry<K, V> {
   static <K, V> ImmutableMapEntry<K, V>[] createEntryArray(int size) {
      return new ImmutableMapEntry[size];
   }

   ImmutableMapEntry(K key, V value) {
      super(key, value);
      CollectPreconditions.checkEntryNotNull(key, value);
   }

   ImmutableMapEntry(ImmutableMapEntry<K, V> contents) {
      super(contents.getKey(), contents.getValue());
   }

   @Nullable ImmutableMapEntry<K, V> getNextInKeyBucket() {
      return null;
   }

   @Nullable ImmutableMapEntry<K, V> getNextInValueBucket() {
      return null;
   }

   boolean isReusable() {
      return true;
   }

   static final class NonTerminalImmutableBiMapEntry<K, V> extends ImmutableMapEntry.NonTerminalImmutableMapEntry<K, V> {
      private final transient @Nullable ImmutableMapEntry<K, V> nextInValueBucket;

      NonTerminalImmutableBiMapEntry(K key, V value, @Nullable ImmutableMapEntry<K, V> nextInKeyBucket, @Nullable ImmutableMapEntry<K, V> nextInValueBucket) {
         super(key, value, nextInKeyBucket);
         this.nextInValueBucket = nextInValueBucket;
      }

      @Override
      @Nullable ImmutableMapEntry<K, V> getNextInValueBucket() {
         return this.nextInValueBucket;
      }
   }

   static class NonTerminalImmutableMapEntry<K, V> extends ImmutableMapEntry<K, V> {
      private final transient @Nullable ImmutableMapEntry<K, V> nextInKeyBucket;

      NonTerminalImmutableMapEntry(K key, V value, @Nullable ImmutableMapEntry<K, V> nextInKeyBucket) {
         super(key, value);
         this.nextInKeyBucket = nextInKeyBucket;
      }

      @Override
      final @Nullable ImmutableMapEntry<K, V> getNextInKeyBucket() {
         return this.nextInKeyBucket;
      }

      @Override
      final boolean isReusable() {
         return false;
      }
   }
}
