package org.incendo.cloud.setting;

import java.util.EnumSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.returnsreceiver.qual.This;

final class EnumConfigurable<S extends Enum<S> & Setting> implements Configurable<S> {
   private final EnumSet<S> settings;

   EnumConfigurable(final @NonNull Class<S> settingClass) {
      this.settings = EnumSet.noneOf(settingClass);
   }

   EnumConfigurable(final @NonNull S defaultSetting) {
      this.settings = EnumSet.of(defaultSetting);
   }

   public @This @NonNull EnumConfigurable<S> set(final @NonNull S setting, final boolean value) {
      if (value) {
         this.settings.add(setting);
      } else {
         this.settings.remove(setting);
      }

      return this;
   }

   public boolean get(final @NonNull S setting) {
      return this.settings.contains(setting);
   }
}
