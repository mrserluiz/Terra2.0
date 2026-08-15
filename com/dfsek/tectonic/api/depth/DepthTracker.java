package com.dfsek.tectonic.api.depth;

import com.dfsek.tectonic.api.config.Configuration;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class DepthTracker {
   private final List<Level> levels;
   private final List<InjectingIntrinsicLevel> intrinsicLevels = new ArrayList<>();
   private final Configuration configuration;

   @Internal
   public DepthTracker(List<Level> levels, Configuration configuration) {
      this.levels = levels;
      this.configuration = configuration;
   }

   public DepthTracker with(Level level) {
      DepthTracker that = new DepthTracker(new ArrayList<>(this.levels), this.configuration);
      that.levels.add(level);
      this.intrinsicLevels.forEach(intrinsicLevel -> intrinsicLevel.inject(level).ifPresent(s -> that.levels.add(new IntrinsicLevel(s))));
      return that;
   }

   public void addIntrinsicLevel(InjectingIntrinsicLevel intrinsicLevel) {
      this.intrinsicLevels.add(intrinsicLevel);
   }

   public DepthTracker index(int index) {
      return this.with(new IndexLevel(index));
   }

   public DepthTracker entry(String entry) {
      return this.with(new EntryLevel(entry));
   }

   public DepthTracker intrinsic(String intrinsicInfo) {
      return this.with(new IntrinsicLevel(intrinsicInfo));
   }

   public String pathDescriptor() {
      StringBuilder builder = new StringBuilder();

      for (int depth = 0; depth < this.levels.size(); depth++) {
         Level level = this.levels.get(depth);
         if (depth > 0) {
            builder.append(level.joinDescriptor());
         }

         builder.append(level.descriptor());
      }

      return builder.toString();
   }

   public String verbosePathDescriptor() {
      StringBuilder builder = new StringBuilder("\n\t").append("From configuration \"").append(this.getConfigurationName()).append('"');

      for (Level level : this.levels) {
         builder.append("\n\t").append(level.verboseDescriptor());
      }

      return builder.toString();
   }

   public String getConfigurationName() {
      return this.configuration.getName() == null ? "Anonymous Configuration" : this.configuration.getName();
   }
}
