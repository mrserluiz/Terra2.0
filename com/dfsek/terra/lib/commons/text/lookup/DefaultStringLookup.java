package com.dfsek.terra.lib.commons.text.lookup;

public enum DefaultStringLookup {
   BASE64_DECODER("base64Decoder", StringLookupFactory.INSTANCE.base64DecoderStringLookup()),
   BASE64_ENCODER("base64Encoder", StringLookupFactory.INSTANCE.base64EncoderStringLookup()),
   CONST("const", StringLookupFactory.INSTANCE.constantStringLookup()),
   DATE("date", StringLookupFactory.INSTANCE.dateStringLookup()),
   DNS("dns", StringLookupFactory.INSTANCE.dnsStringLookup()),
   ENVIRONMENT("env", StringLookupFactory.INSTANCE.environmentVariableStringLookup()),
   FILE("file", StringLookupFactory.INSTANCE.fileStringLookup()),
   JAVA("java", StringLookupFactory.INSTANCE.javaPlatformStringLookup()),
   LOCAL_HOST("localhost", StringLookupFactory.INSTANCE.localHostStringLookup()),
   LOOPBACK_ADDRESS("loobackAddress", StringLookupFactory.INSTANCE.loopbackAddressStringLookup()),
   PROPERTIES("properties", StringLookupFactory.INSTANCE.propertiesStringLookup()),
   RESOURCE_BUNDLE("resourceBundle", StringLookupFactory.INSTANCE.resourceBundleStringLookup()),
   SCRIPT("script", StringLookupFactory.INSTANCE.scriptStringLookup()),
   SYSTEM_PROPERTIES("sys", StringLookupFactory.INSTANCE.systemPropertyStringLookup()),
   URL("url", StringLookupFactory.INSTANCE.urlStringLookup()),
   URL_DECODER("urlDecoder", StringLookupFactory.INSTANCE.urlDecoderStringLookup()),
   URL_ENCODER("urlEncoder", StringLookupFactory.INSTANCE.urlEncoderStringLookup()),
   XML("xml", StringLookupFactory.INSTANCE.xmlStringLookup()),
   XML_DECODER("xmlDecoder", StringLookupFactory.INSTANCE.xmlDecoderStringLookup()),
   XML_ENCODER("xmlEncoder", StringLookupFactory.INSTANCE.xmlEncoderStringLookup());

   private final String key;
   private final StringLookup lookup;

   DefaultStringLookup(final String prefix, final StringLookup lookup) {
      this.key = prefix;
      this.lookup = lookup;
   }

   public String getKey() {
      return this.key;
   }

   public StringLookup getStringLookup() {
      return this.lookup;
   }
}
