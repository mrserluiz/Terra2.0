package io.leangen.geantyref;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnnotatedTypeMap<K extends AnnotatedType, V> implements Map<K, V> {
   private final Map<K, V> inner;

   public AnnotatedTypeMap() {
      this(new HashMap<>());
   }

   public AnnotatedTypeMap(Map<K, V> inner) {
      Objects.requireNonNull(inner);
      if (!inner.isEmpty()) {
         throw new IllegalArgumentException("The provided map must be empty");
      }

      this.inner = inner;
   }

   @Override
   public int size() {
      return this.inner.size();
   }

   @Override
   public boolean isEmpty() {
      return this.inner.isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return key instanceof AnnotatedType && this.inner.containsKey(GenericTypeReflector.toCanonical((AnnotatedType)key));
   }

   @Override
   public boolean containsValue(Object value) {
      return this.inner.containsValue(value);
   }

   @Override
   public V get(Object key) {
      return key instanceof AnnotatedType ? this.inner.get(GenericTypeReflector.toCanonical((AnnotatedType)key)) : null;
   }

   public V put(K key, V value) {
      return this.inner.put(GenericTypeReflector.toCanonical(key), value);
   }

   @Override
   public V remove(Object key) {
      return key instanceof AnnotatedType ? this.inner.remove(GenericTypeReflector.toCanonical((AnnotatedType)key)) : null;
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> m) {
      Map<? extends K, ? extends V> canonical = m.entrySet()
         .stream()
         .map(e -> new SimpleEntry(GenericTypeReflector.toCanonical((K)e.getKey()), (V)e.getValue()))
         .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
      this.inner.putAll(canonical);
   }

   @Override
   public void clear() {
      this.inner.clear();
   }

   @Override
   public Set<K> keySet() {
      return this.inner.keySet();
   }

   @Override
   public Collection<V> values() {
      return this.inner.values();
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return this.inner.entrySet();
   }

   @Override
   public boolean equals(Object o) {
      return this.inner.equals(o);
   }

   @Override
   public int hashCode() {
      return this.inner.hashCode();
   }

   @Override
   public V getOrDefault(Object key, V defaultValue) {
      return key instanceof AnnotatedType ? this.inner.getOrDefault(GenericTypeReflector.toCanonical((AnnotatedType)key), defaultValue) : defaultValue;
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.inner.forEach(action);
   }

   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      this.inner.replaceAll(function);
   }

   public V putIfAbsent(K key, V value) {
      return this.inner.putIfAbsent(GenericTypeReflector.toCanonical(key), value);
   }

   @Override
   public boolean remove(Object key, Object value) {
      return key instanceof AnnotatedType && this.inner.remove(GenericTypeReflector.toCanonical((AnnotatedType)key), value);
   }

   public boolean replace(K key, V oldValue, V newValue) {
      return this.inner.replace(GenericTypeReflector.toCanonical(key), oldValue, newValue);
   }

   public V replace(K key, V value) {
      return this.inner.replace(GenericTypeReflector.toCanonical(key), value);
   }

   public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.inner.computeIfAbsent(GenericTypeReflector.toCanonical(key), mappingFunction);
   }

   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return this.inner.computeIfPresent(GenericTypeReflector.toCanonical(key), remappingFunction);
   }

   public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return this.inner.compute(GenericTypeReflector.toCanonical(key), remappingFunction);
   }

   public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      return this.inner.merge(GenericTypeReflector.toCanonical(key), value, remappingFunction);
   }
}
