package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.StringUtils;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;

final class XmlStringLookup extends AbstractPathFencedLookup {
   static final Map<String, Boolean> DEFAULT_FEATURES = new HashMap<>(1);
   static final XmlStringLookup INSTANCE = new XmlStringLookup(DEFAULT_FEATURES, (Path[])null);
   private final Map<String, Boolean> xPathFactoryFeatures;

   XmlStringLookup(Map<String, Boolean> xPathFactoryFeatures, Path... fences) {
      super(fences);
      this.xPathFactoryFeatures = Objects.requireNonNull(xPathFactoryFeatures, "xPathFfactoryFeatures");
   }

   @Override
   public String lookup(String key) {
      if (key == null) {
         return null;
      }

      String[] keys = key.split(SPLIT_STR);
      int keyLen = keys.length;
      if (keyLen != 2) {
         throw IllegalArgumentExceptions.format("Bad XML key format [%s]; expected format is DocumentPath:XPath.", key);
      }

      String documentPath = keys[0];
      String xpath = StringUtils.substringAfter(key, 58);

      try {
         InputStream inputStream = Files.newInputStream(this.getPath(documentPath));

         String var13;
         try {
            XPathFactory factory = XPathFactory.newInstance();

            for (Entry<String, Boolean> p : this.xPathFactoryFeatures.entrySet()) {
               factory.setFeature(p.getKey(), p.getValue());
            }

            var13 = factory.newXPath().evaluate(xpath, new InputSource(inputStream));
         } catch (Throwable var11) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var10) {
                  var11.addSuppressed(var10);
               }
            }

            throw var11;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var13;
      } catch (Exception e) {
         throw IllegalArgumentExceptions.format(e, "Error looking up XML document [%s] and XPath [%s].", documentPath, xpath);
      }
   }

   static {
      DEFAULT_FEATURES.put("http://javax.xml.XMLConstants/feature/secure-processing", Boolean.TRUE);
   }
}
