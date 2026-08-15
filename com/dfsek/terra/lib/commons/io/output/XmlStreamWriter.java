package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.build.AbstractStreamBuilder;
import com.dfsek.terra.lib.commons.io.input.XmlStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;

public class XmlStreamWriter extends Writer {
   private static final int BUFFER_SIZE = 8192;
   private final OutputStream out;
   private final Charset defaultCharset;
   private StringWriter prologWriter = new StringWriter(8192);
   private Writer writer;
   private Charset charset;

   public static XmlStreamWriter.Builder builder() {
      return new XmlStreamWriter.Builder();
   }

   @Deprecated
   public XmlStreamWriter(File file) throws FileNotFoundException {
      this(file, null);
   }

   @Deprecated
   public XmlStreamWriter(File file, String defaultEncoding) throws FileNotFoundException {
      this(new FileOutputStream(file), defaultEncoding);
   }

   @Deprecated
   public XmlStreamWriter(OutputStream out) {
      this(out, StandardCharsets.UTF_8);
   }

   private XmlStreamWriter(OutputStream out, Charset defaultEncoding) {
      this.out = out;
      this.defaultCharset = Objects.requireNonNull(defaultEncoding);
   }

   @Deprecated
   public XmlStreamWriter(OutputStream out, String defaultEncoding) {
      this(out, Charsets.toCharset(defaultEncoding, StandardCharsets.UTF_8));
   }

   @Override
   public void close() throws IOException {
      if (this.writer == null) {
         this.charset = this.defaultCharset;
         this.writer = new OutputStreamWriter(this.out, this.charset);
         this.writer.write(this.prologWriter.toString());
      }

      this.writer.close();
   }

   private void detectEncoding(char[] cbuf, int off, int len) throws IOException {
      int size = len;
      StringBuffer xmlProlog = this.prologWriter.getBuffer();
      if (xmlProlog.length() + len > 8192) {
         size = 8192 - xmlProlog.length();
      }

      this.prologWriter.write(cbuf, off, size);
      if (xmlProlog.length() >= 5) {
         if (xmlProlog.substring(0, 5).equals("<?xml")) {
            int xmlPrologEnd = xmlProlog.indexOf("?>");
            if (xmlPrologEnd > 0) {
               Matcher m = XmlStreamReader.ENCODING_PATTERN.matcher(xmlProlog.substring(0, xmlPrologEnd));
               if (m.find()) {
                  String encName = m.group(1).toUpperCase(Locale.ROOT);
                  this.charset = Charset.forName(encName.substring(1, encName.length() - 1));
               } else {
                  this.charset = this.defaultCharset;
               }
            } else if (xmlProlog.length() >= 8192) {
               this.charset = this.defaultCharset;
            }
         } else {
            this.charset = this.defaultCharset;
         }

         if (this.charset != null) {
            this.prologWriter = null;
            this.writer = new OutputStreamWriter(this.out, this.charset);
            this.writer.write(xmlProlog.toString());
            if (len > size) {
               this.writer.write(cbuf, off + size, len - size);
            }
         }
      }
   }

   @Override
   public void flush() throws IOException {
      if (this.writer != null) {
         this.writer.flush();
      }
   }

   public String getDefaultEncoding() {
      return this.defaultCharset.name();
   }

   public String getEncoding() {
      return this.charset.name();
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException {
      if (this.prologWriter != null) {
         this.detectEncoding(cbuf, off, len);
      } else {
         this.writer.write(cbuf, off, len);
      }
   }

   public static class Builder extends AbstractStreamBuilder<XmlStreamWriter, XmlStreamWriter.Builder> {
      public Builder() {
         this.setCharsetDefault(StandardCharsets.UTF_8);
         this.setCharset(StandardCharsets.UTF_8);
      }

      public XmlStreamWriter get() throws IOException {
         return new XmlStreamWriter(this.getOutputStream(), this.getCharset());
      }
   }
}
