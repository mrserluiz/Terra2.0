package com.dfsek.terra.lib.commons.text;

import java.text.Format;
import java.util.Locale;

@FunctionalInterface
public interface FormatFactory {
   Format getFormat(String var1, String var2, Locale var3);
}
