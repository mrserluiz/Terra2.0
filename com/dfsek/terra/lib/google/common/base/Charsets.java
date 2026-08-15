package com.dfsek.terra.lib.google.common.base;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@GwtCompatible(emulated = true)
public final class Charsets {
   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static final Charset US_ASCII = StandardCharsets.US_ASCII;
   @Deprecated
   public static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
   @Deprecated
   public static final Charset UTF_8 = StandardCharsets.UTF_8;
   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static final Charset UTF_16BE = StandardCharsets.UTF_16BE;
   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static final Charset UTF_16LE = StandardCharsets.UTF_16LE;
   @Deprecated
   @J2ktIncompatible
   @GwtIncompatible
   public static final Charset UTF_16 = StandardCharsets.UTF_16;

   private Charsets() {
   }
}
