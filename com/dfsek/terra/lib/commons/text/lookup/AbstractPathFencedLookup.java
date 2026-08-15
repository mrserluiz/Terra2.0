package com.dfsek.terra.lib.commons.text.lookup;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class AbstractPathFencedLookup extends AbstractStringLookup {
   protected final List<Path> fences;

   AbstractPathFencedLookup(Path... fences) {
      this.fences = fences != null ? Arrays.stream(fences).map(Path::toAbsolutePath).collect(Collectors.toList()) : Collections.emptyList();
   }

   protected Path getPath(String fileName) {
      Path path = Paths.get(fileName);
      if (this.fences.isEmpty()) {
         return path;
      } else {
         Path pathAbs = path.normalize().toAbsolutePath();
         Optional<Path> first = this.fences.stream().filter(pathAbs::startsWith).findFirst();
         if (first.isPresent()) {
            return path;
         } else {
            throw IllegalArgumentExceptions.format("[%s] -> [%s] not in %s", fileName, pathAbs, this.fences);
         }
      }
   }
}
