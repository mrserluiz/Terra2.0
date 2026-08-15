package com.dfsek.terra.lib.commons.io.filefilter;

import com.dfsek.terra.lib.commons.io.IOCase;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import java.io.File;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RegexFileFilter extends AbstractFileFilter implements Serializable {
   private static final long serialVersionUID = 4269646126155225062L;
   private final Pattern pattern;
   private final transient Function<Path, String> pathToString;

   private static Pattern compile(String pattern, int flags) {
      Objects.requireNonNull(pattern, "pattern");
      return Pattern.compile(pattern, flags);
   }

   private static int toFlags(IOCase ioCase) {
      return IOCase.isCaseSensitive(ioCase) ? 0 : 2;
   }

   public RegexFileFilter(Pattern pattern) {
      this(pattern, PathUtils::getFileNameString);
   }

   public RegexFileFilter(Pattern pattern, Function<Path, String> pathToString) {
      Objects.requireNonNull(pattern, "pattern");
      this.pattern = pattern;
      this.pathToString = pathToString != null ? pathToString : Objects::toString;
   }

   public RegexFileFilter(String pattern) {
      this(pattern, 0);
   }

   public RegexFileFilter(String pattern, int flags) {
      this(compile(pattern, flags));
   }

   public RegexFileFilter(String pattern, IOCase ioCase) {
      this(compile(pattern, toFlags(ioCase)));
   }

   @Override
   public boolean accept(File dir, String name) {
      return this.pattern.matcher(name).matches();
   }

   @Override
   public FileVisitResult accept(Path path, BasicFileAttributes attributes) {
      String result = this.pathToString.apply(path);
      return this.toFileVisitResult(result != null && this.pattern.matcher(result).matches());
   }

   @Override
   public String toString() {
      return "RegexFileFilter [pattern=" + this.pattern + "]";
   }
}
