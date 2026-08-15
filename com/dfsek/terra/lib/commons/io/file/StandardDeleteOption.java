package com.dfsek.terra.lib.commons.io.file;

import com.dfsek.terra.lib.commons.io.IOUtils;
import java.util.stream.Stream;

public enum StandardDeleteOption implements DeleteOption {
   OVERRIDE_READ_ONLY;

   public static boolean overrideReadOnly(DeleteOption[] options) {
      return IOUtils.length(options) == 0 ? false : Stream.of(options).anyMatch(e -> OVERRIDE_READ_ONLY == e);
   }
}
