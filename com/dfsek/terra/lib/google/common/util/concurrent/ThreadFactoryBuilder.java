package com.dfsek.terra.lib.google.common.util.concurrent;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class ThreadFactoryBuilder {
   private @Nullable String nameFormat = null;
   private @Nullable Boolean daemon = null;
   private @Nullable Integer priority = null;
   private @Nullable UncaughtExceptionHandler uncaughtExceptionHandler = null;
   private @Nullable ThreadFactory backingThreadFactory = null;

   @CanIgnoreReturnValue
   public ThreadFactoryBuilder setNameFormat(String nameFormat) {
      String unused = format(nameFormat, 0);
      this.nameFormat = nameFormat;
      return this;
   }

   @CanIgnoreReturnValue
   public ThreadFactoryBuilder setDaemon(boolean daemon) {
      this.daemon = daemon;
      return this;
   }

   @CanIgnoreReturnValue
   public ThreadFactoryBuilder setPriority(int priority) {
      Preconditions.checkArgument(priority >= 1, "Thread priority (%s) must be >= %s", priority, 1);
      Preconditions.checkArgument(priority <= 10, "Thread priority (%s) must be <= %s", priority, 10);
      this.priority = priority;
      return this;
   }

   @CanIgnoreReturnValue
   public ThreadFactoryBuilder setUncaughtExceptionHandler(UncaughtExceptionHandler uncaughtExceptionHandler) {
      this.uncaughtExceptionHandler = Preconditions.checkNotNull(uncaughtExceptionHandler);
      return this;
   }

   @CanIgnoreReturnValue
   public ThreadFactoryBuilder setThreadFactory(ThreadFactory backingThreadFactory) {
      this.backingThreadFactory = Preconditions.checkNotNull(backingThreadFactory);
      return this;
   }

   public ThreadFactory build() {
      return doBuild(this);
   }

   private static ThreadFactory doBuild(ThreadFactoryBuilder builder) {
      final String nameFormat = builder.nameFormat;
      final Boolean daemon = builder.daemon;
      final Integer priority = builder.priority;
      final UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
      final ThreadFactory backingThreadFactory = builder.backingThreadFactory != null ? builder.backingThreadFactory : Executors.defaultThreadFactory();
      final AtomicLong count = nameFormat != null ? new AtomicLong(0L) : null;
      return new ThreadFactory() {
         @Override
         public Thread newThread(Runnable runnable) {
            Thread thread = backingThreadFactory.newThread(runnable);
            Objects.requireNonNull(thread);
            if (nameFormat != null) {
               thread.setName(ThreadFactoryBuilder.format(nameFormat, Objects.requireNonNull(count).getAndIncrement()));
            }

            if (daemon != null) {
               thread.setDaemon(daemon);
            }

            if (priority != null) {
               thread.setPriority(priority);
            }

            if (uncaughtExceptionHandler != null) {
               thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }

            return thread;
         }
      };
   }

   private static String format(String format, Object... args) {
      return String.format(Locale.ROOT, format, args);
   }
}
