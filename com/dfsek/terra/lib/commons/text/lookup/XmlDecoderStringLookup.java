package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.text.StringEscapeUtils;

final class XmlDecoderStringLookup extends AbstractStringLookup {
   static final XmlDecoderStringLookup INSTANCE = new XmlDecoderStringLookup();

   private XmlDecoderStringLookup() {
   }

   @Override
   public String lookup(String key) {
      return StringEscapeUtils.unescapeXml(key);
   }
}
