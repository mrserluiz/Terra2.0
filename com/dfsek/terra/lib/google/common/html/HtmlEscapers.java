package com.dfsek.terra.lib.google.common.html;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.escape.Escaper;
import com.dfsek.terra.lib.google.common.escape.Escapers;

@GwtCompatible
public final class HtmlEscapers {
   private static final Escaper HTML_ESCAPER = Escapers.builder()
      .addEscape('"', "&quot;")
      .addEscape('\'', "&#39;")
      .addEscape('&', "&amp;")
      .addEscape('<', "&lt;")
      .addEscape('>', "&gt;")
      .build();

   public static Escaper htmlEscaper() {
      return HTML_ESCAPER;
   }

   private HtmlEscapers() {
   }
}
