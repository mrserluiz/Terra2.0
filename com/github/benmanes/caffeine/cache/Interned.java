package com.github.benmanes.caffeine.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class Interned<K, V> extends Node<K, V> implements NodeFactory<K, V> {
   static final NodeFactory<Object, Object> FACTORY = new Interned<>();
   volatile Reference<?> keyReference;

   Interned() {
   }

   Interned(Reference<K> keyReference) {
      this.keyReference = keyReference;
   }

   @Override
   public @Nullable K getKey() {
      return (K)this.keyReference.get();
   }

   @Override
   public Object getKeyReference() {
      return this.keyReference;
   }

   @Override
   public V getValue() {
      return (V)Boolean.TRUE;
   }

   @Override
   public V getValueReference() {
      return (V)Boolean.TRUE;
   }

   @Override
   public void setValue(V value, @Nullable ReferenceQueue<V> referenceQueue) {
   }

   @Override
   public boolean containsValue(Object value) {
      return Objects.equals(value, this.getValue());
   }

   @Override
   public Node<K, V> newNode(K key, ReferenceQueue<K> keyReferenceQueue, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new Interned<>(new References.WeakKeyEqualsReference<>(key, keyReferenceQueue));
   }

   @Override
   public Node<K, V> newNode(Object keyReference, V value, ReferenceQueue<V> valueReferenceQueue, int weight, long now) {
      return new Interned<>((Reference<K>)keyReference);
   }

   @Override
   public Object newLookupKey(Object key) {
      return new References.LookupKeyEqualsReference<>(key);
   }

   @Override
   public Object newReferenceKey(K key, ReferenceQueue<K> referenceQueue) {
      return new References.WeakKeyEqualsReference<>(key, referenceQueue);
   }

   @Override
   public boolean isAlive() {
      Object keyRef = this.keyReference;
      return keyRef != RETIRED_WEAK_KEY && keyRef != DEAD_WEAK_KEY;
   }

   @Override
   public boolean isRetired() {
      return this.keyReference == RETIRED_WEAK_KEY;
   }

   @Override
   public void retire() {
      Reference<?> keyRef = this.keyReference;
      this.keyReference = RETIRED_WEAK_KEY;
      keyRef.clear();
   }

   @Override
   public boolean isDead() {
      return this.keyReference == DEAD_WEAK_KEY;
   }

   @Override
   public void die() {
      Reference<?> keyRef = this.keyReference;
      this.keyReference = DEAD_WEAK_KEY;
      keyRef.clear();
   }
}
