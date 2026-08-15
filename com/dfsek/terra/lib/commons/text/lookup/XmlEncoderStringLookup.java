package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.text.StringEscapeUtils;

final class XmlEncoderStringLookup extends AbstractStringLookup {
   static final XmlEncoderStringLookup INSTANCE = new XmlEncoderStringLookup();

   @Override
   public String lookup(String key) {
      return StringEscapeUtils.escapeXml10(key);
   }
}
