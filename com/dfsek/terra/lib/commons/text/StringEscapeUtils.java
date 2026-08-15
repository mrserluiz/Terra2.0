package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.text.translate.AggregateTranslator;
import com.dfsek.terra.lib.commons.text.translate.CharSequenceTranslator;
import com.dfsek.terra.lib.commons.text.translate.CsvTranslators;
import com.dfsek.terra.lib.commons.text.translate.EntityArrays;
import com.dfsek.terra.lib.commons.text.translate.JavaUnicodeEscaper;
import com.dfsek.terra.lib.commons.text.translate.LookupTranslator;
import com.dfsek.terra.lib.commons.text.translate.NumericEntityEscaper;
import com.dfsek.terra.lib.commons.text.translate.NumericEntityUnescaper;
import com.dfsek.terra.lib.commons.text.translate.OctalUnescaper;
import com.dfsek.terra.lib.commons.text.translate.UnicodeUnescaper;
import com.dfsek.terra.lib.commons.text.translate.UnicodeUnpairedSurrogateRemover;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StringEscapeUtils {
   public static final CharSequenceTranslator ESCAPE_JAVA;
   public static final CharSequenceTranslator ESCAPE_ECMASCRIPT;
   public static final CharSequenceTranslator ESCAPE_JSON;
   public static final CharSequenceTranslator ESCAPE_XML10;
   public static final CharSequenceTranslator ESCAPE_XML11;
   public static final CharSequenceTranslator ESCAPE_HTML3 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE), new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE)
   );
   public static final CharSequenceTranslator ESCAPE_HTML4 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE),
      new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE),
      new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE)
   );
   public static final CharSequenceTranslator ESCAPE_CSV = new CsvTranslators.CsvEscaper();
   public static final CharSequenceTranslator ESCAPE_XSI;
   public static final CharSequenceTranslator UNESCAPE_JAVA;
   public static final CharSequenceTranslator UNESCAPE_ECMASCRIPT;
   public static final CharSequenceTranslator UNESCAPE_JSON;
   public static final CharSequenceTranslator UNESCAPE_HTML3 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE), new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE), new NumericEntityUnescaper()
   );
   public static final CharSequenceTranslator UNESCAPE_HTML4 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE),
      new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE),
      new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE),
      new NumericEntityUnescaper()
   );
   public static final CharSequenceTranslator UNESCAPE_XML = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE), new LookupTranslator(EntityArrays.APOS_UNESCAPE), new NumericEntityUnescaper()
   );
   public static final CharSequenceTranslator UNESCAPE_CSV = new CsvTranslators.CsvUnescaper();
   public static final CharSequenceTranslator UNESCAPE_XSI = new StringEscapeUtils.XsiUnescaper();

   public static StringEscapeUtils.Builder builder(CharSequenceTranslator translator) {
      return new StringEscapeUtils.Builder(translator);
   }

   public static String escapeCsv(String input) {
      return ESCAPE_CSV.translate(input);
   }

   public static String escapeEcmaScript(String input) {
      return ESCAPE_ECMASCRIPT.translate(input);
   }

   public static String escapeHtml3(String input) {
      return ESCAPE_HTML3.translate(input);
   }

   public static String escapeHtml4(String input) {
      return ESCAPE_HTML4.translate(input);
   }

   public static String escapeJava(String input) {
      return ESCAPE_JAVA.translate(input);
   }

   public static String escapeJson(String input) {
      return ESCAPE_JSON.translate(input);
   }

   public static String escapeXml10(String input) {
      return ESCAPE_XML10.translate(input);
   }

   public static String escapeXml11(String input) {
      return ESCAPE_XML11.translate(input);
   }

   public static String escapeXSI(String input) {
      return ESCAPE_XSI.translate(input);
   }

   public static String unescapeCsv(String input) {
      return UNESCAPE_CSV.translate(input);
   }

   public static String unescapeEcmaScript(String input) {
      return UNESCAPE_ECMASCRIPT.translate(input);
   }

   public static String unescapeHtml3(String input) {
      return UNESCAPE_HTML3.translate(input);
   }

   public static String unescapeHtml4(String input) {
      return UNESCAPE_HTML4.translate(input);
   }

   public static String unescapeJava(String input) {
      return UNESCAPE_JAVA.translate(input);
   }

   public static String unescapeJson(String input) {
      return UNESCAPE_JSON.translate(input);
   }

   public static String unescapeXml(String input) {
      return UNESCAPE_XML.translate(input);
   }

   public static String unescapeXSI(String input) {
      return UNESCAPE_XSI.translate(input);
   }

   static {
      Map<CharSequence, CharSequence> escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("\"", "\\\"");
      escapeJavaMap.put("\\", "\\\\");
      ESCAPE_JAVA = new AggregateTranslator(
         new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
         new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
         JavaUnicodeEscaper.outsideOf(32, 127)
      );
      escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("'", "\\'");
      escapeJavaMap.put("\"", "\\\"");
      escapeJavaMap.put("\\", "\\\\");
      escapeJavaMap.put("/", "\\/");
      ESCAPE_ECMASCRIPT = new AggregateTranslator(
         new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
         new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
         JavaUnicodeEscaper.outsideOf(32, 127)
      );
      escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("\"", "\\\"");
      escapeJavaMap.put("\\", "\\\\");
      escapeJavaMap.put("/", "\\/");
      ESCAPE_JSON = new AggregateTranslator(
         new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
         new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE),
         JavaUnicodeEscaper.outsideOf(32, 126)
      );
      escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("\u0000", "");
      escapeJavaMap.put("\u0001", "");
      escapeJavaMap.put("\u0002", "");
      escapeJavaMap.put("\u0003", "");
      escapeJavaMap.put("\u0004", "");
      escapeJavaMap.put("\u0005", "");
      escapeJavaMap.put("\u0006", "");
      escapeJavaMap.put("\u0007", "");
      escapeJavaMap.put("\b", "");
      escapeJavaMap.put("\u000b", "");
      escapeJavaMap.put("\f", "");
      escapeJavaMap.put("\u000e", "");
      escapeJavaMap.put("\u000f", "");
      escapeJavaMap.put("\u0010", "");
      escapeJavaMap.put("\u0011", "");
      escapeJavaMap.put("\u0012", "");
      escapeJavaMap.put("\u0013", "");
      escapeJavaMap.put("\u0014", "");
      escapeJavaMap.put("\u0015", "");
      escapeJavaMap.put("\u0016", "");
      escapeJavaMap.put("\u0017", "");
      escapeJavaMap.put("\u0018", "");
      escapeJavaMap.put("\u0019", "");
      escapeJavaMap.put("\u001a", "");
      escapeJavaMap.put("\u001b", "");
      escapeJavaMap.put("\u001c", "");
      escapeJavaMap.put("\u001d", "");
      escapeJavaMap.put("\u001e", "");
      escapeJavaMap.put("\u001f", "");
      escapeJavaMap.put("\ufffe", "");
      escapeJavaMap.put("\uffff", "");
      ESCAPE_XML10 = new AggregateTranslator(
         new LookupTranslator(EntityArrays.BASIC_ESCAPE),
         new LookupTranslator(EntityArrays.APOS_ESCAPE),
         new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
         NumericEntityEscaper.between(127, 132),
         NumericEntityEscaper.between(134, 159),
         new UnicodeUnpairedSurrogateRemover()
      );
      escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("\u0000", "");
      escapeJavaMap.put("\u000b", "&#11;");
      escapeJavaMap.put("\f", "&#12;");
      escapeJavaMap.put("\ufffe", "");
      escapeJavaMap.put("\uffff", "");
      ESCAPE_XML11 = new AggregateTranslator(
         new LookupTranslator(EntityArrays.BASIC_ESCAPE),
         new LookupTranslator(EntityArrays.APOS_ESCAPE),
         new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap)),
         NumericEntityEscaper.between(1, 8),
         NumericEntityEscaper.between(14, 31),
         NumericEntityEscaper.between(127, 132),
         NumericEntityEscaper.between(134, 159),
         new UnicodeUnpairedSurrogateRemover()
      );
      escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("|", "\\|");
      escapeJavaMap.put("&", "\\&");
      escapeJavaMap.put(";", "\\;");
      escapeJavaMap.put("<", "\\<");
      escapeJavaMap.put(">", "\\>");
      escapeJavaMap.put("(", "\\(");
      escapeJavaMap.put(")", "\\)");
      escapeJavaMap.put("$", "\\$");
      escapeJavaMap.put("`", "\\`");
      escapeJavaMap.put("\\", "\\\\");
      escapeJavaMap.put("\"", "\\\"");
      escapeJavaMap.put("'", "\\'");
      escapeJavaMap.put(" ", "\\ ");
      escapeJavaMap.put("\t", "\\\t");
      escapeJavaMap.put("\r\n", "");
      escapeJavaMap.put("\n", "");
      escapeJavaMap.put("*", "\\*");
      escapeJavaMap.put("?", "\\?");
      escapeJavaMap.put("[", "\\[");
      escapeJavaMap.put("#", "\\#");
      escapeJavaMap.put("~", "\\~");
      escapeJavaMap.put("=", "\\=");
      escapeJavaMap.put("%", "\\%");
      ESCAPE_XSI = new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap));
      escapeJavaMap = new HashMap<>();
      escapeJavaMap.put("\\\\", "\\");
      escapeJavaMap.put("\\\"", "\"");
      escapeJavaMap.put("\\'", "'");
      escapeJavaMap.put("\\", "");
      UNESCAPE_JAVA = new AggregateTranslator(
         new OctalUnescaper(),
         new UnicodeUnescaper(),
         new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE),
         new LookupTranslator(Collections.unmodifiableMap(escapeJavaMap))
      );
      UNESCAPE_ECMASCRIPT = UNESCAPE_JAVA;
      UNESCAPE_JSON = UNESCAPE_JAVA;
   }

   public static final class Builder {
      private final StringBuilder sb = new StringBuilder();
      private final CharSequenceTranslator translator;

      private Builder(CharSequenceTranslator translator) {
         this.translator = translator;
      }

      public StringEscapeUtils.Builder append(String input) {
         this.sb.append(input);
         return this;
      }

      public StringEscapeUtils.Builder escape(String input) {
         this.sb.append(this.translator.translate(input));
         return this;
      }

      @Override
      public String toString() {
         return this.sb.toString();
      }
   }

   static class XsiUnescaper extends CharSequenceTranslator {
      private static final char BACKSLASH = '\\';

      @Override
      public int translate(CharSequence input, int index, Writer writer) throws IOException {
         if (index != 0) {
            throw new IllegalStateException("XsiUnescaper should never reach the [1] index");
         }

         String s = input.toString();
         int segmentStart = 0;
         int searchOffset = 0;

         while (true) {
            int pos = s.indexOf(92, searchOffset);
            if (pos == -1) {
               if (segmentStart < s.length()) {
                  writer.write(s.substring(segmentStart));
               }

               return Character.codePointCount(input, 0, input.length());
            }

            if (pos > segmentStart) {
               writer.write(s.substring(segmentStart, pos));
            }

            segmentStart = pos + 1;
            searchOffset = pos + 2;
         }
      }
   }
}
