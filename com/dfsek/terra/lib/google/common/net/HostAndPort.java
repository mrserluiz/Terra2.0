package com.dfsek.terra.lib.google.common.net;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.annotations.GwtIncompatible;
import com.dfsek.terra.lib.google.common.annotations.J2ktIncompatible;
import com.dfsek.terra.lib.google.common.base.Objects;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Strings;
import com.dfsek.terra.lib.google.common.primitives.Ints;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import java.io.Serializable;
import org.jspecify.annotations.Nullable;

@Immutable
@GwtCompatible
public final class HostAndPort implements Serializable {
   private static final int NO_PORT = -1;
   private final String host;
   private final int port;
   private final boolean hasBracketlessColons;
   @GwtIncompatible
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   private HostAndPort(String host, int port, boolean hasBracketlessColons) {
      this.host = host;
      this.port = port;
      this.hasBracketlessColons = hasBracketlessColons;
   }

   public String getHost() {
      return this.host;
   }

   public boolean hasPort() {
      return this.port >= 0;
   }

   public int getPort() {
      Preconditions.checkState(this.hasPort());
      return this.port;
   }

   public int getPortOrDefault(int defaultPort) {
      return this.hasPort() ? this.port : defaultPort;
   }

   public static HostAndPort fromParts(String host, int port) {
      Preconditions.checkArgument(isValidPort(port), "Port out of range: %s", port);
      HostAndPort parsedHost = fromString(host);
      Preconditions.checkArgument(!parsedHost.hasPort(), "Host has a port: %s", host);
      return new HostAndPort(parsedHost.host, port, parsedHost.hasBracketlessColons);
   }

   public static HostAndPort fromHost(String host) {
      HostAndPort parsedHost = fromString(host);
      Preconditions.checkArgument(!parsedHost.hasPort(), "Host has a port: %s", host);
      return parsedHost;
   }

   @CanIgnoreReturnValue
   public static HostAndPort fromString(String hostPortString) {
      Preconditions.checkNotNull(hostPortString);
      String portString = null;
      boolean hasBracketlessColons = false;
      String host;
      if (hostPortString.startsWith("[")) {
         String[] hostAndPort = getHostAndPortFromBracketedHost(hostPortString);
         host = hostAndPort[0];
         portString = hostAndPort[1];
      } else {
         int colonPos = hostPortString.indexOf(58);
         if (colonPos >= 0 && hostPortString.indexOf(58, colonPos + 1) == -1) {
            host = hostPortString.substring(0, colonPos);
            portString = hostPortString.substring(colonPos + 1);
         } else {
            host = hostPortString;
            hasBracketlessColons = colonPos >= 0;
         }
      }

      Integer port;
      if (Strings.isNullOrEmpty(portString)) {
         port = -1;
      } else {
         port = Ints.tryParse(portString);
         Preconditions.checkArgument(port != null, "Unparseable port number: %s", hostPortString);
         Preconditions.checkArgument(isValidPort(port), "Port number out of range: %s", hostPortString);
      }

      return new HostAndPort(host, port, hasBracketlessColons);
   }

   private static String[] getHostAndPortFromBracketedHost(String hostPortString) {
      Preconditions.checkArgument(hostPortString.charAt(0) == '[', "Bracketed host-port string must start with a bracket: %s", hostPortString);
      int colonIndex = hostPortString.indexOf(58);
      int closeBracketIndex = hostPortString.lastIndexOf(93);
      Preconditions.checkArgument(colonIndex > -1 && closeBracketIndex > colonIndex, "Invalid bracketed host/port: %s", hostPortString);
      String host = hostPortString.substring(1, closeBracketIndex);
      if (closeBracketIndex + 1 == hostPortString.length()) {
         return new String[]{host, ""};
      }

      Preconditions.checkArgument(hostPortString.charAt(closeBracketIndex + 1) == ':', "Only a colon may follow a close bracket: %s", hostPortString);

      for (int i = closeBracketIndex + 2; i < hostPortString.length(); i++) {
         Preconditions.checkArgument(Character.isDigit(hostPortString.charAt(i)), "Port must be numeric: %s", hostPortString);
      }

      return new String[]{host, hostPortString.substring(closeBracketIndex + 2)};
   }

   public HostAndPort withDefaultPort(int defaultPort) {
      Preconditions.checkArgument(isValidPort(defaultPort));
      return this.hasPort() ? this : new HostAndPort(this.host, defaultPort, this.hasBracketlessColons);
   }

   @CanIgnoreReturnValue
   public HostAndPort requireBracketsForIPv6() {
      Preconditions.checkArgument(!this.hasBracketlessColons, "Possible bracketless IPv6 literal: %s", this.host);
      return this;
   }

   @Override
   public boolean equals(@Nullable Object other) {
      if (this == other) {
         return true;
      }

      if (!(other instanceof HostAndPort)) {
         return false;
      }

      HostAndPort that = (HostAndPort)other;
      return Objects.equal(this.host, that.host) && this.port == that.port;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.host, this.port);
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder(this.host.length() + 8);
      if (this.host.indexOf(58) >= 0) {
         builder.append('[').append(this.host).append(']');
      } else {
         builder.append(this.host);
      }

      if (this.hasPort()) {
         builder.append(':').append(this.port);
      }

      return builder.toString();
   }

   private static boolean isValidPort(int port) {
      return port >= 0 && port <= 65535;
   }
}
