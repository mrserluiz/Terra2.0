package com.github.benmanes.caffeine.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

final class References {
   private References() {
   }

   interface InternalReference<E> {
      @Nullable E get();

      Object getKeyReference();

      default boolean referenceEquals(@Nullable Object object) {
         if (object == this) {
            return true;
         } else if (object instanceof References.InternalReference) {
            References.InternalReference<?> referent = (References.InternalReference<?>)object;
            return this.get() == referent.get();
         } else {
            return false;
         }
      }

      default boolean objectEquals(@Nullable Object object) {
         if (object == this) {
            return true;
         } else if (object instanceof References.InternalReference) {
            References.InternalReference<?> referent = (References.InternalReference<?>)object;
            return Objects.equals(this.get(), referent.get());
         } else {
            return false;
         }
      }
   }

   static final class LookupKeyEqualsReference<K> implements References.InternalReference<K> {
      private final int hashCode;
      private final K key;

      public LookupKeyEqualsReference(K key) {
         this.hashCode = key.hashCode();
         this.key = Objects.requireNonNull(key);
      }

      @Override
      public K get() {
         return this.key;
      }

      @Override
      public Object getKeyReference() {
         return this;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         return this.objectEquals(object);
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }

      @Override
      public String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static final class LookupKeyReference<K> implements References.InternalReference<K> {
      private final int hashCode;
      private final K key;

      public LookupKeyReference(K key) {
         this.hashCode = System.identityHashCode(key);
         this.key = Objects.requireNonNull(key);
      }

      @Override
      public K get() {
         return this.key;
      }

      @Override
      public Object getKeyReference() {
         return this;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }

      @Override
      public String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static final class SoftValueReference<V> extends SoftReference<V> implements References.InternalReference<V> {
      private Object keyReference;

      public SoftValueReference(Object keyReference, @Nullable V value, @Nullable ReferenceQueue<V> queue) {
         super(value, queue);
         this.keyReference = keyReference;
      }

      @Override
      public Object getKeyReference() {
         return this.keyReference;
      }

      public void setKeyReference(Object keyReference) {
         this.keyReference = keyReference;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      @Override
      public int hashCode() {
         V value = this.get();
         return value == null ? 0 : value.hashCode();
      }

      @Override
      public String toString() {
         return String.format(Locale.US, "%s{value=%s}", this.getClass().getSimpleName(), this.get());
      }
   }

   static final class WeakKeyEqualsReference<K> extends WeakReference<K> implements References.InternalReference<K> {
      private final int hashCode;

      public WeakKeyEqualsReference(K key, @Nullable ReferenceQueue<K> queue) {
         super(key, queue);
         this.hashCode = key.hashCode();
      }

      @Override
      public Object getKeyReference() {
         return this;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         return this.objectEquals(object);
      }

      @Override
      public int hashCode() {
         return this.hashCode;
      }

      @Override
      public String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static class WeakKeyReference<K> extends WeakReference<K> implements References.InternalReference<K> {
      private final int hashCode;

      public WeakKeyReference(@Nullable K key, @Nullable ReferenceQueue<K> queue) {
         super(key, queue);
         this.hashCode = System.identityHashCode(key);
      }

      @Override
      public final Object getKeyReference() {
         return this;
      }

      @Override
      public final boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      @Override
      public final int hashCode() {
         return this.hashCode;
      }

      @Override
      public final String toString() {
         return String.format(Locale.US, "%s{key=%s, hashCode=%d}", this.getClass().getSimpleName(), this.get(), this.hashCode);
      }
   }

   static final class WeakValueReference<V> extends WeakReference<V> implements References.InternalReference<V> {
      private Object keyReference;

      public WeakValueReference(Object keyReference, @Nullable V value, @Nullable ReferenceQueue<V> queue) {
         super(value, queue);
         this.keyReference = keyReference;
      }

      @Override
      public Object getKeyReference() {
         return this.keyReference;
      }

      public void setKeyReference(Object keyReference) {
         this.keyReference = keyReference;
      }

      @Override
      public boolean equals(@Nullable Object object) {
         return this.referenceEquals(object);
      }

      @Override
      public int hashCode() {
         V value = this.get();
         return value == null ? 0 : value.hashCode();
      }

      @Override
      public String toString() {
         return String.format(Locale.US, "%s{value=%s}", this.getClass().getSimpleName(), this.get());
      }
   }
}
