package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.text.translate.AggregateTranslator;
import com.dfsek.terra.lib.commons.lang3.text.translate.CharSequenceTranslator;
import com.dfsek.terra.lib.commons.lang3.text.translate.EntityArrays;
import com.dfsek.terra.lib.commons.lang3.text.translate.JavaUnicodeEscaper;
import com.dfsek.terra.lib.commons.lang3.text.translate.LookupTranslator;
import com.dfsek.terra.lib.commons.lang3.text.translate.NumericEntityEscaper;
import com.dfsek.terra.lib.commons.lang3.text.translate.NumericEntityUnescaper;
import com.dfsek.terra.lib.commons.lang3.text.translate.OctalUnescaper;
import com.dfsek.terra.lib.commons.lang3.text.translate.UnicodeUnescaper;
import com.dfsek.terra.lib.commons.lang3.text.translate.UnicodeUnpairedSurrogateRemover;
import java.io.IOException;
import java.io.Writer;

@Deprecated
public class StringEscapeUtils {
   public static final CharSequenceTranslator ESCAPE_JAVA = new LookupTranslator(new String[]{"\"", "\\\""}, new String[]{"\\", "\\\\"})
      .with(new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()))
      .with(JavaUnicodeEscaper.outsideOf(32, 127));
   public static final CharSequenceTranslator ESCAPE_ECMASCRIPT = new AggregateTranslator(
      new LookupTranslator(new String[]{"'", "\\'"}, new String[]{"\"", "\\\""}, new String[]{"\\", "\\\\"}, new String[]{"/", "\\/"}),
      new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()),
      JavaUnicodeEscaper.outsideOf(32, 127)
   );
   public static final CharSequenceTranslator ESCAPE_JSON = new AggregateTranslator(
      new LookupTranslator(new String[]{"\"", "\\\""}, new String[]{"\\", "\\\\"}, new String[]{"/", "\\/"}),
      new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE()),
      JavaUnicodeEscaper.outsideOf(32, 127)
   );
   @Deprecated
   public static final CharSequenceTranslator ESCAPE_XML = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE()), new LookupTranslator(EntityArrays.APOS_ESCAPE())
   );
   public static final CharSequenceTranslator ESCAPE_XML10 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE()),
      new LookupTranslator(EntityArrays.APOS_ESCAPE()),
      new LookupTranslator(
         new String[]{"\u0000", ""},
         new String[]{"\u0001", ""},
         new String[]{"\u0002", ""},
         new String[]{"\u0003", ""},
         new String[]{"\u0004", ""},
         new String[]{"\u0005", ""},
         new String[]{"\u0006", ""},
         new String[]{"\u0007", ""},
         new String[]{"\b", ""},
         new String[]{"\u000b", ""},
         new String[]{"\f", ""},
         new String[]{"\u000e", ""},
         new String[]{"\u000f", ""},
         new String[]{"\u0010", ""},
         new String[]{"\u0011", ""},
         new String[]{"\u0012", ""},
         new String[]{"\u0013", ""},
         new String[]{"\u0014", ""},
         new String[]{"\u0015", ""},
         new String[]{"\u0016", ""},
         new String[]{"\u0017", ""},
         new String[]{"\u0018", ""},
         new String[]{"\u0019", ""},
         new String[]{"\u001a", ""},
         new String[]{"\u001b", ""},
         new String[]{"\u001c", ""},
         new String[]{"\u001d", ""},
         new String[]{"\u001e", ""},
         new String[]{"\u001f", ""},
         new String[]{"\ufffe", ""},
         new String[]{"\uffff", ""}
      ),
      NumericEntityEscaper.between(127, 132),
      NumericEntityEscaper.between(134, 159),
      new UnicodeUnpairedSurrogateRemover()
   );
   public static final CharSequenceTranslator ESCAPE_XML11 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE()),
      new LookupTranslator(EntityArrays.APOS_ESCAPE()),
      new LookupTranslator(
         new String[]{"\u0000", ""}, new String[]{"\u000b", "&#11;"}, new String[]{"\f", "&#12;"}, new String[]{"\ufffe", ""}, new String[]{"\uffff", ""}
      ),
      NumericEntityEscaper.between(1, 8),
      NumericEntityEscaper.between(14, 31),
      NumericEntityEscaper.between(127, 132),
      NumericEntityEscaper.between(134, 159),
      new UnicodeUnpairedSurrogateRemover()
   );
   public static final CharSequenceTranslator ESCAPE_HTML3 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE()), new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE())
   );
   public static final CharSequenceTranslator ESCAPE_HTML4 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_ESCAPE()),
      new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE()),
      new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE())
   );
   public static final CharSequenceTranslator ESCAPE_CSV = new StringEscapeUtils.CsvEscaper();
   public static final CharSequenceTranslator UNESCAPE_JAVA = new AggregateTranslator(
      new OctalUnescaper(),
      new UnicodeUnescaper(),
      new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_UNESCAPE()),
      new LookupTranslator(new String[]{"\\\\", "\\"}, new String[]{"\\\"", "\""}, new String[]{"\\'", "'"}, new String[]{"\\", ""})
   );
   public static final CharSequenceTranslator UNESCAPE_ECMASCRIPT = UNESCAPE_JAVA;
   public static final CharSequenceTranslator UNESCAPE_JSON = UNESCAPE_JAVA;
   public static final CharSequenceTranslator UNESCAPE_HTML3 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE()), new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()), new NumericEntityUnescaper()
   );
   public static final CharSequenceTranslator UNESCAPE_HTML4 = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE()),
      new LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE()),
      new LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE()),
      new NumericEntityUnescaper()
   );
   public static final CharSequenceTranslator UNESCAPE_XML = new AggregateTranslator(
      new LookupTranslator(EntityArrays.BASIC_UNESCAPE()), new LookupTranslator(EntityArrays.APOS_UNESCAPE()), new NumericEntityUnescaper()
   );
   public static final CharSequenceTranslator UNESCAPE_CSV = new StringEscapeUtils.CsvUnescaper();

   public static final String escapeCsv(String input) {
      return ESCAPE_CSV.translate(input);
   }

   public static final String escapeEcmaScript(String input) {
      return ESCAPE_ECMASCRIPT.translate(input);
   }

   public static final String escapeHtml3(String input) {
      return ESCAPE_HTML3.translate(input);
   }

   public static final String escapeHtml4(String input) {
      return ESCAPE_HTML4.translate(input);
   }

   public static final String escapeJava(String input) {
      return ESCAPE_JAVA.translate(input);
   }

   public static final String escapeJson(String input) {
      return ESCAPE_JSON.translate(input);
   }

   @Deprecated
   public static final String escapeXml(String input) {
      return ESCAPE_XML.translate(input);
   }

   public static String escapeXml10(String input) {
      return ESCAPE_XML10.translate(input);
   }

   public static String escapeXml11(String input) {
      return ESCAPE_XML11.translate(input);
   }

   public static final String unescapeCsv(String input) {
      return UNESCAPE_CSV.translate(input);
   }

   public static final String unescapeEcmaScript(String input) {
      return UNESCAPE_ECMASCRIPT.translate(input);
   }

   public static final String unescapeHtml3(String input) {
      return UNESCAPE_HTML3.translate(input);
   }

   public static final String unescapeHtml4(String input) {
      return UNESCAPE_HTML4.translate(input);
   }

   public static final String unescapeJava(String input) {
      return UNESCAPE_JAVA.translate(input);
   }

   public static final String unescapeJson(String input) {
      return UNESCAPE_JSON.translate(input);
   }

   public static final String unescapeXml(String input) {
      return UNESCAPE_XML.translate(input);
   }

   static class CsvEscaper extends CharSequenceTranslator {
      private static final char CSV_DELIMITER = ',';
      private static final char CSV_QUOTE = '"';
      private static final String CSV_QUOTE_STR = String.valueOf('"');
      private static final char[] CSV_SEARCH_CHARS = new char[]{',', '"', '\r', '\n'};

      @Override
      public int translate(CharSequence input, int index, Writer out) throws IOException {
         if (index != 0) {
            throw new IllegalStateException("CsvEscaper should never reach the [1] index");
         }

         if (StringUtils.containsNone(input.toString(), CSV_SEARCH_CHARS)) {
            out.write(input.toString());
         } else {
            out.write(34);
            out.write(StringUtils.replace(input.toString(), CSV_QUOTE_STR, CSV_QUOTE_STR + CSV_QUOTE_STR));
            out.write(34);
         }

         return Character.codePointCount(input, 0, input.length());
      }
   }

   static class CsvUnescaper extends CharSequenceTranslator {
      private static final char CSV_DELIMITER = ',';
      private static final char CSV_QUOTE = '"';
      private static final String CSV_QUOTE_STR = String.valueOf('"');
      private static final char[] CSV_SEARCH_CHARS = new char[]{',', '"', '\r', '\n'};

      @Override
      public int translate(CharSequence input, int index, Writer out) throws IOException {
         if (index != 0) {
            throw new IllegalStateException("CsvUnescaper should never reach the [1] index");
         }

         if (input.charAt(0) == '"' && input.charAt(input.length() - 1) == '"') {
            String quoteless = input.subSequence(1, input.length() - 1).toString();
            if (StringUtils.containsAny(quoteless, CSV_SEARCH_CHARS)) {
               out.write(StringUtils.replace(quoteless, CSV_QUOTE_STR + CSV_QUOTE_STR, CSV_QUOTE_STR));
            } else {
               out.write(input.toString());
            }

            return Character.codePointCount(input, 0, input.length());
         } else {
            out.write(input.toString());
            return Character.codePointCount(input, 0, input.length());
         }
      }
   }
}
