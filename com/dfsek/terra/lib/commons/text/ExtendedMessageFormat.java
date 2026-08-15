package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.text.matcher.StringMatcherFactory;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Locale.Category;

public class ExtendedMessageFormat extends MessageFormat {
   private static final long serialVersionUID = -2362048321261811743L;
   private static final int HASH_SEED = 31;
   private static final String DUMMY_PATTERN = "";
   private static final char START_FMT = ',';
   private static final char END_FE = '}';
   private static final char START_FE = '{';
   private static final char QUOTE = '\'';
   private String toPattern;
   private final Map<String, ? extends FormatFactory> registry;

   public ExtendedMessageFormat(String pattern) {
      this(pattern, Locale.getDefault(Category.FORMAT));
   }

   public ExtendedMessageFormat(String pattern, Locale locale) {
      this(pattern, locale, null);
   }

   public ExtendedMessageFormat(String pattern, Locale locale, Map<String, ? extends FormatFactory> registry) {
      super("");
      this.setLocale(locale);
      this.registry = registry != null ? Collections.unmodifiableMap(new HashMap<>(registry)) : null;
      this.applyPattern(pattern);
   }

   public ExtendedMessageFormat(String pattern, Map<String, ? extends FormatFactory> registry) {
      this(pattern, Locale.getDefault(Category.FORMAT), registry);
   }

   private void appendQuotedString(String pattern, ParsePosition pos, StringBuilder appendTo) {
      assert pattern.toCharArray()[pos.getIndex()] == '\'' : "Quoted string must start with quote character";
      if (appendTo != null) {
         appendTo.append('\'');
      }

      this.next(pos);
      int start = pos.getIndex();
      char[] c = pattern.toCharArray();
      int i = pos.getIndex();

      while (i < pattern.length()) {
         switch (c[pos.getIndex()]) {
            case '\'':
               this.next(pos);
               if (appendTo != null) {
                  appendTo.append(c, start, pos.getIndex() - start);
               }

               return;
            default:
               this.next(pos);
               i++;
         }
      }

      throw new IllegalArgumentException("Unterminated quoted string at position " + start);
   }

   @Override
   public final void applyPattern(String pattern) {
      if (this.registry == null) {
         super.applyPattern(pattern);
         this.toPattern = super.toPattern();
      } else {
         ArrayList<Format> foundFormats = new ArrayList<>();
         ArrayList<String> foundDescriptions = new ArrayList<>();
         StringBuilder stripCustom = new StringBuilder(pattern.length());
         ParsePosition pos = new ParsePosition(0);
         char[] c = pattern.toCharArray();
         int fmtCount = 0;

         while (pos.getIndex() < pattern.length()) {
            switch (c[pos.getIndex()]) {
               case '\'':
                  this.appendQuotedString(pattern, pos, stripCustom);
                  break;
               case '{':
                  fmtCount++;
                  this.seekNonWs(pattern, pos);
                  int start = pos.getIndex();
                  int index = this.readArgumentIndex(pattern, this.next(pos));
                  stripCustom.append('{').append(index);
                  this.seekNonWs(pattern, pos);
                  Format format = null;
                  String formatDescription = null;
                  if (c[pos.getIndex()] == ',') {
                     formatDescription = this.parseFormatDescription(pattern, this.next(pos));
                     format = this.getFormat(formatDescription);
                     if (format == null) {
                        stripCustom.append(',').append(formatDescription);
                     }
                  }

                  foundFormats.add(format);
                  foundDescriptions.add(format == null ? null : formatDescription);
                  if (foundFormats.size() != fmtCount) {
                     throw new IllegalArgumentException("The validated expression is false");
                  }

                  if (foundDescriptions.size() != fmtCount) {
                     throw new IllegalArgumentException("The validated expression is false");
                  }

                  if (c[pos.getIndex()] != '}') {
                     throw new IllegalArgumentException("Unreadable format element at position " + start);
                  }
               default:
                  stripCustom.append(c[pos.getIndex()]);
                  this.next(pos);
            }
         }

         super.applyPattern(stripCustom.toString());
         this.toPattern = this.insertFormats(super.toPattern(), foundDescriptions);
         if (this.containsElements(foundFormats)) {
            Format[] origFormats = this.getFormats();
            int i = 0;

            for (Format f : foundFormats) {
               if (f != null) {
                  origFormats[i] = f;
               }

               i++;
            }

            super.setFormats(origFormats);
         }
      }
   }

   private boolean containsElements(Collection<?> coll) {
      return coll != null && !coll.isEmpty() ? coll.stream().anyMatch(Objects::nonNull) : false;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!Objects.equals(this.getClass(), obj.getClass())) {
         return false;
      } else {
         ExtendedMessageFormat rhs = (ExtendedMessageFormat)obj;
         if (!Objects.equals(this.toPattern, rhs.toPattern)) {
            return false;
         } else {
            return !super.equals(obj) ? false : Objects.equals(this.registry, rhs.registry);
         }
      }
   }

   private Format getFormat(String desc) {
      if (this.registry != null) {
         String name = desc;
         String args = null;
         int i = desc.indexOf(44);
         if (i > 0) {
            name = desc.substring(0, i).trim();
            args = desc.substring(i + 1).trim();
         }

         FormatFactory factory = this.registry.get(name);
         if (factory != null) {
            return factory.getFormat(name, args, this.getLocale());
         }
      }

      return null;
   }

   private void getQuotedString(String pattern, ParsePosition pos) {
      this.appendQuotedString(pattern, pos, null);
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      result = 31 * result + Objects.hashCode(this.registry);
      return 31 * result + Objects.hashCode(this.toPattern);
   }

   private String insertFormats(String pattern, ArrayList<String> customPatterns) {
      if (!this.containsElements(customPatterns)) {
         return pattern;
      }

      StringBuilder sb = new StringBuilder(pattern.length() * 2);
      ParsePosition pos = new ParsePosition(0);
      int fe = -1;
      int depth = 0;

      while (pos.getIndex() < pattern.length()) {
         char c = pattern.charAt(pos.getIndex());
         switch (c) {
            case '\'':
               this.appendQuotedString(pattern, pos, sb);
               break;
            case '{':
               depth++;
               sb.append('{').append(this.readArgumentIndex(pattern, this.next(pos)));
               if (depth == 1) {
                  String customPattern = customPatterns.get(++fe);
                  if (customPattern != null) {
                     sb.append(',').append(customPattern);
                  }
               }
               break;
            case '}':
               depth--;
            default:
               sb.append(c);
               this.next(pos);
         }
      }

      return sb.toString();
   }

   private ParsePosition next(ParsePosition pos) {
      pos.setIndex(pos.getIndex() + 1);
      return pos;
   }

   private String parseFormatDescription(String pattern, ParsePosition pos) {
      int start = pos.getIndex();
      this.seekNonWs(pattern, pos);
      int text = pos.getIndex();
      int depth = 1;

      while (pos.getIndex() < pattern.length()) {
         switch (pattern.charAt(pos.getIndex())) {
            case '\'':
               this.getQuotedString(pattern, pos);
               break;
            case '{':
               depth++;
               this.next(pos);
               break;
            case '}':
               if (--depth == 0) {
                  return pattern.substring(text, pos.getIndex());
               }

               this.next(pos);
               break;
            default:
               this.next(pos);
         }
      }

      throw new IllegalArgumentException("Unterminated format element at position " + start);
   }

   private int readArgumentIndex(String pattern, ParsePosition pos) {
      int start = pos.getIndex();
      this.seekNonWs(pattern, pos);
      StringBuilder result = new StringBuilder();

      boolean error;
      for (error = false; !error && pos.getIndex() < pattern.length(); this.next(pos)) {
         char c = pattern.charAt(pos.getIndex());
         if (Character.isWhitespace(c)) {
            this.seekNonWs(pattern, pos);
            c = pattern.charAt(pos.getIndex());
            if (c != ',' && c != '}') {
               error = true;
               continue;
            }
         }

         if ((c == ',' || c == '}') && result.length() > 0) {
            try {
               return Integer.parseInt(result.toString());
            } catch (NumberFormatException var8) {
            }
         }

         error = !Character.isDigit(c);
         result.append(c);
      }

      if (error) {
         throw new IllegalArgumentException("Invalid format argument index at position " + start + ": " + pattern.substring(start, pos.getIndex()));
      } else {
         throw new IllegalArgumentException("Unterminated format element at position " + start);
      }
   }

   private void seekNonWs(String pattern, ParsePosition pos) {
      int len = 0;
      char[] buffer = pattern.toCharArray();

      do {
         len = StringMatcherFactory.INSTANCE.splitMatcher().isMatch(buffer, pos.getIndex(), 0, buffer.length);
         pos.setIndex(pos.getIndex() + len);
      } while (len > 0 && pos.getIndex() < pattern.length());
   }

   @Override
   public void setFormat(int formatElementIndex, Format newFormat) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setFormatByArgumentIndex(int argumentIndex, Format newFormat) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setFormats(Format[] newFormats) {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setFormatsByArgumentIndex(Format[] newFormats) {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toPattern() {
      return this.toPattern;
   }
}
