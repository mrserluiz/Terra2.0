package com.dfsek.terra.lib.commons.io.serialization;

import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ObjectStreamClassPredicate implements Predicate<ObjectStreamClass> {
   private final List<ClassNameMatcher> acceptMatchers = new ArrayList<>();
   private final List<ClassNameMatcher> rejectMatchers = new ArrayList<>();

   public ObjectStreamClassPredicate accept(Class<?>... classes) {
      Stream.of(classes).map(c -> new FullClassNameMatcher(c.getName())).forEach(this.acceptMatchers::add);
      return this;
   }

   public ObjectStreamClassPredicate accept(ClassNameMatcher matcher) {
      this.acceptMatchers.add(matcher);
      return this;
   }

   public ObjectStreamClassPredicate accept(Pattern pattern) {
      this.acceptMatchers.add(new RegexpClassNameMatcher(pattern));
      return this;
   }

   public ObjectStreamClassPredicate accept(String... patterns) {
      Stream.of(patterns).map(WildcardClassNameMatcher::new).forEach(this.acceptMatchers::add);
      return this;
   }

   public ObjectStreamClassPredicate reject(Class<?>... classes) {
      Stream.of(classes).map(c -> new FullClassNameMatcher(c.getName())).forEach(this.rejectMatchers::add);
      return this;
   }

   public ObjectStreamClassPredicate reject(ClassNameMatcher m) {
      this.rejectMatchers.add(m);
      return this;
   }

   public ObjectStreamClassPredicate reject(Pattern pattern) {
      this.rejectMatchers.add(new RegexpClassNameMatcher(pattern));
      return this;
   }

   public ObjectStreamClassPredicate reject(String... patterns) {
      Stream.of(patterns).map(WildcardClassNameMatcher::new).forEach(this.rejectMatchers::add);
      return this;
   }

   public boolean test(ObjectStreamClass objectStreamClass) {
      return this.test(objectStreamClass.getName());
   }

   public boolean test(String name) {
      for (ClassNameMatcher m : this.rejectMatchers) {
         if (m.matches(name)) {
            return false;
         }
      }

      for (ClassNameMatcher m : this.acceptMatchers) {
         if (m.matches(name)) {
            return true;
         }
      }

      return false;
   }
}
