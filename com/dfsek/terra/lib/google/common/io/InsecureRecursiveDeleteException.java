package com.dfsek.terra.lib.google.common.io;

import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.nio.file.FileSystemException;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class InsecureRecursiveDeleteException extends FileSystemException {
   public InsecureRecursiveDeleteException(@Nullable String file) {
      super(file, null, "unable to guarantee security of recursive delete");
   }
}
