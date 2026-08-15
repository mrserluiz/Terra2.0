package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.lang3.Validate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

@Deprecated
public class StrSubstitutor {
   public static final char DEFAULT_ESCAPE = '$';
   public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
   public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
   public static final StrMatcher DEFAULT_VALUE_DELIMITER = StrMatcher.stringMatcher(":-");
   private char escapeChar;
   private StrMatcher prefixMatcher;
   private StrMatcher suffixMatcher;
   private StrMatcher valueDelimiterMatcher;
   private StrLookup<?> variableResolver;
   private boolean enableSubstitutionInVariables;
   private boolean preserveEscapes;
   private boolean disableSubstitutionInValues;

   public static <V> String replace(Object source, Map<String, V> valueMap) {
      return new StrSubstitutor(valueMap).replace(source);
   }

   public static <V> String replace(Object source, Map<String, V> valueMap, String prefix, String suffix) {
      return new StrSubstitutor(valueMap, prefix, suffix).replace(source);
   }

   public static String replace(Object source, Properties valueProperties) {
      return valueProperties == null
         ? source.toString()
         : replace(source, valueProperties.stringPropertyNames().stream().collect(Collectors.toMap(Function.identity(), valueProperties::getProperty)));
   }

   public static String replaceSystemProperties(Object source) {
      return new StrSubstitutor(StrLookup.systemPropertiesLookup()).replace(source);
   }

   public StrSubstitutor() {
      this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public <V> StrSubstitutor(Map<String, V> valueMap) {
      this(StrLookup.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public <V> StrSubstitutor(Map<String, V> valueMap, String prefix, String suffix) {
      this(StrLookup.mapLookup(valueMap), prefix, suffix, '$');
   }

   public <V> StrSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape) {
      this(StrLookup.mapLookup(valueMap), prefix, suffix, escape);
   }

   public <V> StrSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape, String valueDelimiter) {
      this(StrLookup.mapLookup(valueMap), prefix, suffix, escape, valueDelimiter);
   }

   public StrSubstitutor(StrLookup<?> variableResolver) {
      this(variableResolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public StrSubstitutor(StrLookup<?> variableResolver, String prefix, String suffix, char escape) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefix(prefix);
      this.setVariableSuffix(suffix);
      this.setEscapeChar(escape);
      this.setValueDelimiterMatcher(DEFAULT_VALUE_DELIMITER);
   }

   public StrSubstitutor(StrLookup<?> variableResolver, String prefix, String suffix, char escape, String valueDelimiter) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefix(prefix);
      this.setVariableSuffix(suffix);
      this.setEscapeChar(escape);
      this.setValueDelimiter(valueDelimiter);
   }

   public StrSubstitutor(StrLookup<?> variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape) {
      this(variableResolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER);
   }

   public StrSubstitutor(StrLookup<?> variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape, StrMatcher valueDelimiterMatcher) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefixMatcher(prefixMatcher);
      this.setVariableSuffixMatcher(suffixMatcher);
      this.setEscapeChar(escape);
      this.setValueDelimiterMatcher(valueDelimiterMatcher);
   }

   private void checkCyclicSubstitution(String varName, List<String> priorVariables) {
      if (priorVariables.contains(varName)) {
         StrBuilder buf = new StrBuilder(256);
         buf.append("Infinite loop in property interpolation of ");
         buf.append(priorVariables.remove(0));
         buf.append(": ");
         buf.appendWithSeparators(priorVariables, "->");
         throw new IllegalStateException(buf.toString());
      }
   }

   public char getEscapeChar() {
      return this.escapeChar;
   }

   public StrMatcher getValueDelimiterMatcher() {
      return this.valueDelimiterMatcher;
   }

   public StrMatcher getVariablePrefixMatcher() {
      return this.prefixMatcher;
   }

   public StrLookup<?> getVariableResolver() {
      return this.variableResolver;
   }

   public StrMatcher getVariableSuffixMatcher() {
      return this.suffixMatcher;
   }

   public boolean isDisableSubstitutionInValues() {
      return this.disableSubstitutionInValues;
   }

   public boolean isEnableSubstitutionInVariables() {
      return this.enableSubstitutionInVariables;
   }

   public boolean isPreserveEscapes() {
      return this.preserveEscapes;
   }

   public String replace(char[] source) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(source.length).append(source);
      this.substitute(buf, 0, source.length);
      return buf.toString();
   }

   public String replace(char[] source, int offset, int length) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(length).append(source, offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public String replace(CharSequence source) {
      return source == null ? null : this.replace(source, 0, source.length());
   }

   public String replace(CharSequence source, int offset, int length) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(length).append((CharSequence)source, offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public String replace(Object source) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder().append(source);
      this.substitute(buf, 0, buf.length());
      return buf.toString();
   }

   public String replace(StrBuilder source) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(source.length()).append(source);
      this.substitute(buf, 0, buf.length());
      return buf.toString();
   }

   public String replace(StrBuilder source, int offset, int length) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(length).append(source, offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public String replace(String source) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(source);
      return !this.substitute(buf, 0, source.length()) ? source : buf.toString();
   }

   public String replace(String source, int offset, int length) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(length).append(source, offset, length);
      return !this.substitute(buf, 0, length) ? source.substring(offset, offset + length) : buf.toString();
   }

   public String replace(StringBuffer source) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(source.length()).append(source);
      this.substitute(buf, 0, buf.length());
      return buf.toString();
   }

   public String replace(StringBuffer source, int offset, int length) {
      if (source == null) {
         return null;
      }

      StrBuilder buf = new StrBuilder(length).append(source, offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public boolean replaceIn(StrBuilder source) {
      return source == null ? false : this.substitute(source, 0, source.length());
   }

   public boolean replaceIn(StrBuilder source, int offset, int length) {
      return source == null ? false : this.substitute(source, offset, length);
   }

   public boolean replaceIn(StringBuffer source) {
      return source == null ? false : this.replaceIn(source, 0, source.length());
   }

   public boolean replaceIn(StringBuffer source, int offset, int length) {
      if (source == null) {
         return false;
      }

      StrBuilder buf = new StrBuilder(length).append(source, offset, length);
      if (!this.substitute(buf, 0, length)) {
         return false;
      }

      source.replace(offset, offset + length, buf.toString());
      return true;
   }

   public boolean replaceIn(StringBuilder source) {
      return source == null ? false : this.replaceIn(source, 0, source.length());
   }

   public boolean replaceIn(StringBuilder source, int offset, int length) {
      if (source == null) {
         return false;
      }

      StrBuilder buf = new StrBuilder(length).append(source, offset, length);
      if (!this.substitute(buf, 0, length)) {
         return false;
      }

      source.replace(offset, offset + length, buf.toString());
      return true;
   }

   protected String resolveVariable(String variableName, StrBuilder buf, int startPos, int endPos) {
      StrLookup<?> resolver = this.getVariableResolver();
      return resolver == null ? null : resolver.lookup(variableName);
   }

   public void setDisableSubstitutionInValues(boolean disableSubstitutionInValues) {
      this.disableSubstitutionInValues = disableSubstitutionInValues;
   }

   public void setEnableSubstitutionInVariables(boolean enableSubstitutionInVariables) {
      this.enableSubstitutionInVariables = enableSubstitutionInVariables;
   }

   public void setEscapeChar(char escapeCharacter) {
      this.escapeChar = escapeCharacter;
   }

   public void setPreserveEscapes(boolean preserveEscapes) {
      this.preserveEscapes = preserveEscapes;
   }

   public StrSubstitutor setValueDelimiter(char valueDelimiter) {
      return this.setValueDelimiterMatcher(StrMatcher.charMatcher(valueDelimiter));
   }

   public StrSubstitutor setValueDelimiter(String valueDelimiter) {
      if (valueDelimiter != null && !valueDelimiter.isEmpty()) {
         return this.setValueDelimiterMatcher(StrMatcher.stringMatcher(valueDelimiter));
      }

      this.setValueDelimiterMatcher(null);
      return this;
   }

   public StrSubstitutor setValueDelimiterMatcher(StrMatcher valueDelimiterMatcher) {
      this.valueDelimiterMatcher = valueDelimiterMatcher;
      return this;
   }

   public StrSubstitutor setVariablePrefix(char prefix) {
      return this.setVariablePrefixMatcher(StrMatcher.charMatcher(prefix));
   }

   public StrSubstitutor setVariablePrefix(String prefix) {
      Validate.isTrue(prefix != null, "Variable prefix must not be null!");
      return this.setVariablePrefixMatcher(StrMatcher.stringMatcher(prefix));
   }

   public StrSubstitutor setVariablePrefixMatcher(StrMatcher prefixMatcher) {
      Validate.isTrue(prefixMatcher != null, "Variable prefix matcher must not be null!");
      this.prefixMatcher = prefixMatcher;
      return this;
   }

   public void setVariableResolver(StrLookup<?> variableResolver) {
      this.variableResolver = variableResolver;
   }

   public StrSubstitutor setVariableSuffix(char suffix) {
      return this.setVariableSuffixMatcher(StrMatcher.charMatcher(suffix));
   }

   public StrSubstitutor setVariableSuffix(String suffix) {
      Validate.isTrue(suffix != null, "Variable suffix must not be null!");
      return this.setVariableSuffixMatcher(StrMatcher.stringMatcher(suffix));
   }

   public StrSubstitutor setVariableSuffixMatcher(StrMatcher suffixMatcher) {
      Validate.isTrue(suffixMatcher != null, "Variable suffix matcher must not be null!");
      this.suffixMatcher = suffixMatcher;
      return this;
   }

   protected boolean substitute(StrBuilder buf, int offset, int length) {
      return this.substitute(buf, offset, length, null) > 0;
   }

   private int substitute(StrBuilder buf, int offset, int length, List<String> priorVariables) {
      StrMatcher pfxMatcher = this.getVariablePrefixMatcher();
      StrMatcher suffMatcher = this.getVariableSuffixMatcher();
      char escape = this.getEscapeChar();
      StrMatcher valueDelimMatcher = this.getValueDelimiterMatcher();
      boolean substitutionInVariablesEnabled = this.isEnableSubstitutionInVariables();
      boolean substitutionInValuesDisabled = this.isDisableSubstitutionInValues();
      boolean top = priorVariables == null;
      boolean altered = false;
      int lengthChange = 0;
      char[] chars = buf.buffer;
      int bufEnd = offset + length;
      int pos = offset;

      while (pos < bufEnd) {
         int startMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd);
         if (startMatchLen == 0) {
            pos++;
         } else if (pos > offset && chars[pos - 1] == escape) {
            if (this.preserveEscapes) {
               pos++;
            } else {
               buf.deleteCharAt(pos - 1);
               chars = buf.buffer;
               lengthChange--;
               altered = true;
               bufEnd--;
            }
         } else {
            int startPos = pos;
            pos += startMatchLen;
            int endMatchLen = 0;
            int nestedVarCount = 0;

            while (pos < bufEnd) {
               if (substitutionInVariablesEnabled && pfxMatcher.isMatch(chars, pos, offset, bufEnd) != 0) {
                  endMatchLen = pfxMatcher.isMatch(chars, pos, offset, bufEnd);
                  nestedVarCount++;
                  pos += endMatchLen;
               } else {
                  endMatchLen = suffMatcher.isMatch(chars, pos, offset, bufEnd);
                  if (endMatchLen == 0) {
                     pos++;
                  } else {
                     if (nestedVarCount == 0) {
                        String varNameExpr = new String(chars, startPos + startMatchLen, pos - startPos - startMatchLen);
                        if (substitutionInVariablesEnabled) {
                           StrBuilder bufName = new StrBuilder(varNameExpr);
                           this.substitute(bufName, 0, bufName.length());
                           varNameExpr = bufName.toString();
                        }

                        pos += endMatchLen;
                        int endPos = pos;
                        String varName = varNameExpr;
                        String varDefaultValue = null;
                        if (valueDelimMatcher != null) {
                           char[] varNameExprChars = varNameExpr.toCharArray();
                           int valueDelimiterMatchLen = 0;

                           for (int i = 0;
                              i < varNameExprChars.length
                                 && (substitutionInVariablesEnabled || pfxMatcher.isMatch(varNameExprChars, i, i, varNameExprChars.length) == 0);
                              i++
                           ) {
                              if (valueDelimMatcher.isMatch(varNameExprChars, i) != 0) {
                                 valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars, i);
                                 varName = varNameExpr.substring(0, i);
                                 varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                                 break;
                              }
                           }
                        }

                        if (priorVariables == null) {
                           priorVariables = new ArrayList<>();
                           priorVariables.add(new String(chars, offset, length));
                        }

                        this.checkCyclicSubstitution(varName, priorVariables);
                        priorVariables.add(varName);
                        String varValue = this.resolveVariable(varName, buf, startPos, endPos);
                        if (varValue == null) {
                           varValue = varDefaultValue;
                        }

                        if (varValue != null) {
                           int varLen = varValue.length();
                           buf.replace(startPos, endPos, varValue);
                           altered = true;
                           int change = 0;
                           if (!substitutionInValuesDisabled) {
                              change = this.substitute(buf, startPos, varLen, priorVariables);
                           }

                           change = change + varLen - (endPos - startPos);
                           pos += change;
                           bufEnd += change;
                           lengthChange += change;
                           chars = buf.buffer;
                        }

                        priorVariables.remove(priorVariables.size() - 1);
                        break;
                     }

                     nestedVarCount--;
                     pos += endMatchLen;
                  }
               }
            }
         }
      }

      if (top) {
         return altered ? 1 : 0;
      } else {
         return lengthChange;
      }
   }
}
