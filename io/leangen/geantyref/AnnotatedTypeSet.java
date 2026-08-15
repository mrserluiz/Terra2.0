package io.leangen.geantyref;

import java.lang.reflect.AnnotatedType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotatedTypeSet<E extends AnnotatedType> implements Set<E> {
   private final Set<E> inner;

   public AnnotatedTypeSet() {
      this(new HashSet<>());
   }

   public AnnotatedTypeSet(Set<E> inner) {
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
   public boolean contains(Object o) {
      return o instanceof AnnotatedType && this.inner.contains(GenericTypeReflector.toCanonical((AnnotatedType)o));
   }

   @Override
   public Iterator<E> iterator() {
      return this.inner.iterator();
   }

   @Override
   public Object[] toArray() {
      return this.inner.toArray();
   }

   @Override
   public <T> T[] toArray(T[] a) {
      return (T[])this.inner.toArray(a);
   }

   public boolean add(E e) {
      return this.inner.add(GenericTypeReflector.toCanonical(e));
   }

   @Override
   public boolean remove(Object o) {
      return o instanceof AnnotatedType && this.inner.remove(GenericTypeReflector.toCanonical((AnnotatedType)o));
   }

   @Override
   public boolean containsAll(Collection<?> c) {
      return this.inner.containsAll(this.canonical(c));
   }

   @Override
   public boolean addAll(Collection<? extends E> c) {
      return this.inner.addAll(c.stream().map(GenericTypeReflector::toCanonical).collect(Collectors.toList()));
   }

   @Override
   public boolean retainAll(Collection<?> c) {
      return this.inner.retainAll(this.canonical(c));
   }

   @Override
   public boolean removeAll(Collection<?> c) {
      return this.inner.removeAll(this.canonical(c));
   }

   @Override
   public void clear() {
      this.inner.clear();
   }

   private Collection<?> canonical(Collection<?> c) {
      return c.stream().map(e -> e instanceof AnnotatedType ? GenericTypeReflector.toCanonical((AnnotatedType)e) : e).collect(Collectors.toList());
   }
}
