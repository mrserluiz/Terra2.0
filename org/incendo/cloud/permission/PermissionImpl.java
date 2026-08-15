package org.incendo.cloud.permission;

import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
@Generated(from = "Permission", generator = "Immutables")
@Immutable
final class PermissionImpl implements Permission {
   private final @NonNull String permissionString;

   private PermissionImpl(@NonNull String permissionString) {
      this.permissionString = Objects.requireNonNull(permissionString, "permissionString");
   }

   private PermissionImpl(PermissionImpl original, @NonNull String permissionString) {
      this.permissionString = permissionString;
   }

   @Override
   public @NonNull String permissionString() {
      return this.permissionString;
   }

   public final PermissionImpl withPermissionString(String value) {
      String newValue = Objects.requireNonNull(value, "permissionString");
      return this.permissionString.equals(newValue) ? this : new PermissionImpl(this, newValue);
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof PermissionImpl && this.equalTo(0, (PermissionImpl)another);
   }

   private boolean equalTo(int synthetic, PermissionImpl another) {
      return this.permissionString.equals(another.permissionString);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      return h + (h << 5) + this.permissionString.hashCode();
   }

   @Override
   public String toString() {
      return "Permission{permissionString=" + this.permissionString + "}";
   }

   public static PermissionImpl of(@NonNull String permissionString) {
      return new PermissionImpl(permissionString);
   }

   public static PermissionImpl copyOf(Permission instance) {
      return instance instanceof PermissionImpl ? (PermissionImpl)instance : of(instance.permissionString());
   }
}
