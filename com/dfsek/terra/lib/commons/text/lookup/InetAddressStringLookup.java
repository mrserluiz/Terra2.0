package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.function.FailableSupplier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

final class InetAddressStringLookup extends AbstractStringLookup {
   static final InetAddressStringLookup LOCAL_HOST = new InetAddressStringLookup(InetAddress::getLocalHost);
   static final InetAddressStringLookup LOOPACK_ADDRESS = new InetAddressStringLookup(InetAddress::getLoopbackAddress);
   private final FailableSupplier<InetAddress, UnknownHostException> inetAddressSupplier;

   private InetAddressStringLookup(FailableSupplier<InetAddress, UnknownHostException> inetAddressSupplier) {
      this.inetAddressSupplier = Objects.requireNonNull(inetAddressSupplier, "inetAddressSupplier");
   }

   private InetAddress getInetAddress() throws UnknownHostException {
      return this.inetAddressSupplier.get();
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      try {
         switch (key) {
            case "name":
               return this.getInetAddress().getHostName();
            case "canonical-name":
               return this.getInetAddress().getCanonicalHostName();
            case "address":
               return this.getInetAddress().getHostAddress();
            default:
               throw new IllegalArgumentException(key);
         }
      } catch (UnknownHostException e) {
         return null;
      }
   }
}
