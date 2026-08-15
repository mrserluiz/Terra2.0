package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.time.DurationUtils;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThreadUtils {
   @Deprecated
   public static final ThreadUtils.AlwaysTruePredicate ALWAYS_TRUE_PREDICATE = new ThreadUtils.AlwaysTruePredicate();
   private static final Predicate<?> ALWAYS_TRUE = t -> true;

   private static <T> Predicate<T> alwaysTruePredicate() {
      return (Predicate<T>)ALWAYS_TRUE;
   }

   public static Thread findThreadById(long threadId) {
      if (threadId <= 0L) {
         throw new IllegalArgumentException("The thread id must be greater than zero");
      }

      Collection<Thread> result = findThreads(t -> t != null && t.getId() == threadId);
      return result.isEmpty() ? null : result.iterator().next();
   }

   public static Thread findThreadById(long threadId, String threadGroupName) {
      Objects.requireNonNull(threadGroupName, "threadGroupName");
      Thread thread = findThreadById(threadId);
      return thread != null && thread.getThreadGroup() != null && thread.getThreadGroup().getName().equals(threadGroupName) ? thread : null;
   }

   public static Thread findThreadById(long threadId, ThreadGroup threadGroup) {
      Objects.requireNonNull(threadGroup, "threadGroup");
      Thread thread = findThreadById(threadId);
      return thread != null && threadGroup.equals(thread.getThreadGroup()) ? thread : null;
   }

   public static Collection<ThreadGroup> findThreadGroups(Predicate<ThreadGroup> predicate) {
      return findThreadGroups(getSystemThreadGroup(), true, predicate);
   }

   public static Collection<ThreadGroup> findThreadGroups(ThreadGroup threadGroup, boolean recurse, Predicate<ThreadGroup> predicate) {
      Objects.requireNonNull(threadGroup, "threadGroup");
      Objects.requireNonNull(predicate, "predicate");
      int count = threadGroup.activeGroupCount();

      ThreadGroup[] threadGroups;
      do {
         threadGroups = new ThreadGroup[count + count / 2 + 1];
         count = threadGroup.enumerate(threadGroups, recurse);
      } while (count >= threadGroups.length);

      return Collections.unmodifiableCollection(Stream.of(threadGroups).limit(count).filter(predicate).collect(Collectors.toList()));
   }

   @Deprecated
   public static Collection<ThreadGroup> findThreadGroups(ThreadGroup threadGroup, boolean recurse, ThreadUtils.ThreadGroupPredicate predicate) {
      return findThreadGroups(threadGroup, recurse, predicate::test);
   }

   @Deprecated
   public static Collection<ThreadGroup> findThreadGroups(ThreadUtils.ThreadGroupPredicate predicate) {
      return findThreadGroups(getSystemThreadGroup(), true, predicate);
   }

   public static Collection<ThreadGroup> findThreadGroupsByName(String threadGroupName) {
      return findThreadGroups(predicateThreadGroup(threadGroupName));
   }

   public static Collection<Thread> findThreads(Predicate<Thread> predicate) {
      return findThreads(getSystemThreadGroup(), true, predicate);
   }

   public static Collection<Thread> findThreads(ThreadGroup threadGroup, boolean recurse, Predicate<Thread> predicate) {
      Objects.requireNonNull(threadGroup, "The group must not be null");
      Objects.requireNonNull(predicate, "The predicate must not be null");
      int count = threadGroup.activeCount();

      Thread[] threads;
      do {
         threads = new Thread[count + count / 2 + 1];
         count = threadGroup.enumerate(threads, recurse);
      } while (count >= threads.length);

      return Collections.unmodifiableCollection(Stream.of(threads).limit(count).filter(predicate).collect(Collectors.toList()));
   }

   @Deprecated
   public static Collection<Thread> findThreads(ThreadGroup threadGroup, boolean recurse, ThreadUtils.ThreadPredicate predicate) {
      return findThreads(threadGroup, recurse, predicate::test);
   }

   @Deprecated
   public static Collection<Thread> findThreads(ThreadUtils.ThreadPredicate predicate) {
      return findThreads(getSystemThreadGroup(), true, predicate);
   }

   public static Collection<Thread> findThreadsByName(String threadName) {
      return findThreads(predicateThread(threadName));
   }

   public static Collection<Thread> findThreadsByName(String threadName, String threadGroupName) {
      Objects.requireNonNull(threadName, "threadName");
      Objects.requireNonNull(threadGroupName, "threadGroupName");
      return Collections.unmodifiableCollection(
         findThreadGroups(predicateThreadGroup(threadGroupName))
            .stream()
            .flatMap(group -> findThreads(group, false, predicateThread(threadName)).stream())
            .collect(Collectors.toList())
      );
   }

   public static Collection<Thread> findThreadsByName(String threadName, ThreadGroup threadGroup) {
      return findThreads(threadGroup, false, predicateThread(threadName));
   }

   public static Collection<ThreadGroup> getAllThreadGroups() {
      return findThreadGroups(alwaysTruePredicate());
   }

   public static Collection<Thread> getAllThreads() {
      return findThreads(alwaysTruePredicate());
   }

   public static ThreadGroup getSystemThreadGroup() {
      ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

      while (threadGroup != null && threadGroup.getParent() != null) {
         threadGroup = threadGroup.getParent();
      }

      return threadGroup;
   }

   public static void join(Thread thread, Duration duration) throws InterruptedException {
      DurationUtils.accept(thread::join, duration);
   }

   private static <T> Predicate<T> namePredicate(String name, Function<T, String> nameGetter) {
      return t -> t != null && Objects.equals(nameGetter.apply(t), Objects.requireNonNull(name));
   }

   private static Predicate<Thread> predicateThread(String threadName) {
      return namePredicate(threadName, Thread::getName);
   }

   private static Predicate<ThreadGroup> predicateThreadGroup(String threadGroupName) {
      return namePredicate(threadGroupName, ThreadGroup::getName);
   }

   public static void sleep(Duration duration) throws InterruptedException {
      DurationUtils.accept(Thread::sleep, duration);
   }

   public static void sleepQuietly(Duration duration) {
      try {
         sleep(duration);
      } catch (InterruptedException var2) {
      }
   }

   @Deprecated
   private static final class AlwaysTruePredicate implements ThreadUtils.ThreadPredicate, ThreadUtils.ThreadGroupPredicate {
      private AlwaysTruePredicate() {
      }

      @Override
      public boolean test(Thread thread) {
         return true;
      }

      @Override
      public boolean test(ThreadGroup threadGroup) {
         return true;
      }
   }

   @Deprecated
   public static class NamePredicate implements ThreadUtils.ThreadPredicate, ThreadUtils.ThreadGroupPredicate {
      private final String name;

      public NamePredicate(String name) {
         Objects.requireNonNull(name, "name");
         this.name = name;
      }

      @Override
      public boolean test(Thread thread) {
         return thread != null && thread.getName().equals(this.name);
      }

      @Override
      public boolean test(ThreadGroup threadGroup) {
         return threadGroup != null && threadGroup.getName().equals(this.name);
      }
   }

   @Deprecated
   @FunctionalInterface
   public interface ThreadGroupPredicate {
      boolean test(ThreadGroup var1);
   }

   @Deprecated
   public static class ThreadIdPredicate implements ThreadUtils.ThreadPredicate {
      private final long threadId;

      public ThreadIdPredicate(long threadId) {
         if (threadId <= 0L) {
            throw new IllegalArgumentException("The thread id must be greater than zero");
         }

         this.threadId = threadId;
      }

      @Override
      public boolean test(Thread thread) {
         return thread != null && thread.getId() == this.threadId;
      }
   }

   @Deprecated
   @FunctionalInterface
   public interface ThreadPredicate {
      boolean test(Thread var1);
   }
}
