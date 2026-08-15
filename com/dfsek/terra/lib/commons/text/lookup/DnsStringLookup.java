package com.dfsek.terra.lib.commons.text.lookup;

import java.net.InetAddress;
import java.net.UnknownHostException;

final class DnsStringLookup extends AbstractStringLookup {
   static final DnsStringLookup INSTANCE = new DnsStringLookup();

   private DnsStringLookup() {
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String[] keys = key.trim().split("\\|");
      int keyLen = keys.length;
      String subKey = keys[0].trim();
      String subValue = keyLen < 2 ? key : keys[1].trim();

      try {
         InetAddress inetAddress = InetAddress.getByName(subValue);
         switch (subKey) {
            case "name":
               return inetAddress.getHostName();
            case "canonical-name":
               return inetAddress.getCanonicalHostName();
            case "address":
               return inetAddress.getHostAddress();
            default:
               return inetAddress.getHostAddress();
         }
      } catch (UnknownHostException e) {
         return null;
      }
   }
}
