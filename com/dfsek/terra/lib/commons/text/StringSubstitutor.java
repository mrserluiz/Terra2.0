package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.lang3.Validate;
import com.dfsek.terra.lib.commons.text.lookup.StringLookup;
import com.dfsek.terra.lib.commons.text.lookup.StringLookupFactory;
import com.dfsek.terra.lib.commons.text.matcher.StringMatcher;
import com.dfsek.terra.lib.commons.text.matcher.StringMatcherFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StringSubstitutor {
   public static final char DEFAULT_ESCAPE = '$';
   public static final String DEFAULT_VAR_DEFAULT = ":-";
   public static final String DEFAULT_VAR_END = "}";
   public static final String DEFAULT_VAR_START = "${";
   public static final StringMatcher DEFAULT_PREFIX = StringMatcherFactory.INSTANCE.stringMatcher("${");
   public static final StringMatcher DEFAULT_SUFFIX = StringMatcherFactory.INSTANCE.stringMatcher("}");
   public static final StringMatcher DEFAULT_VALUE_DELIMITER = StringMatcherFactory.INSTANCE.stringMatcher(":-");
   private boolean disableSubstitutionInValues;
   private boolean enableSubstitutionInVariables;
   private boolean failOnUndefinedVariable;
   private char escapeChar;
   private StringMatcher prefixMatcher;
   private boolean preserveEscapes;
   private StringMatcher suffixMatcher;
   private StringMatcher valueDelimiterMatcher;
   private StringLookup variableResolver;

   public static StringSubstitutor createInterpolator() {
      return new StringSubstitutor(StringLookupFactory.INSTANCE.interpolatorStringLookup());
   }

   public static <V> String replace(Object source, Map<String, V> valueMap) {
      return new StringSubstitutor(valueMap).replace(source);
   }

   public static <V> String replace(Object source, Map<String, V> valueMap, String prefix, String suffix) {
      return new StringSubstitutor(valueMap, prefix, suffix).replace(source);
   }

   public static String replace(Object source, Properties valueProperties) {
      return valueProperties == null
         ? source.toString()
         : replace(source, valueProperties.stringPropertyNames().stream().collect(Collectors.toMap(Function.identity(), valueProperties::getProperty)));
   }

   public static String replaceSystemProperties(Object source) {
      return new StringSubstitutor(StringLookupFactory.INSTANCE.systemPropertyStringLookup()).replace(source);
   }

   public StringSubstitutor() {
      this(null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public <V> StringSubstitutor(Map<String, V> valueMap) {
      this(StringLookupFactory.INSTANCE.mapStringLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public <V> StringSubstitutor(Map<String, V> valueMap, String prefix, String suffix) {
      this(StringLookupFactory.INSTANCE.mapStringLookup(valueMap), prefix, suffix, '$');
   }

   public <V> StringSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape) {
      this(StringLookupFactory.INSTANCE.mapStringLookup(valueMap), prefix, suffix, escape);
   }

   public <V> StringSubstitutor(Map<String, V> valueMap, String prefix, String suffix, char escape, String valueDelimiter) {
      this(StringLookupFactory.INSTANCE.mapStringLookup(valueMap), prefix, suffix, escape, valueDelimiter);
   }

   public StringSubstitutor(StringLookup variableResolver) {
      this(variableResolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public StringSubstitutor(StringLookup variableResolver, String prefix, String suffix, char escape) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefix(prefix);
      this.setVariableSuffix(suffix);
      this.setEscapeChar(escape);
      this.setValueDelimiterMatcher(DEFAULT_VALUE_DELIMITER);
   }

   public StringSubstitutor(StringLookup variableResolver, String prefix, String suffix, char escape, String valueDelimiter) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefix(prefix);
      this.setVariableSuffix(suffix);
      this.setEscapeChar(escape);
      this.setValueDelimiter(valueDelimiter);
   }

   public StringSubstitutor(StringLookup variableResolver, StringMatcher prefixMatcher, StringMatcher suffixMatcher, char escape) {
      this(variableResolver, prefixMatcher, suffixMatcher, escape, DEFAULT_VALUE_DELIMITER);
   }

   public StringSubstitutor(
      StringLookup variableResolver, StringMatcher prefixMatcher, StringMatcher suffixMatcher, char escape, StringMatcher valueDelimiterMatcher
   ) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefixMatcher(prefixMatcher);
      this.setVariableSuffixMatcher(suffixMatcher);
      this.setEscapeChar(escape);
      this.setValueDelimiterMatcher(valueDelimiterMatcher);
   }

   public StringSubstitutor(StringSubstitutor other) {
      this.disableSubstitutionInValues = other.isDisableSubstitutionInValues();
      this.enableSubstitutionInVariables = other.isEnableSubstitutionInVariables();
      this.failOnUndefinedVariable = other.isEnableUndefinedVariableException();
      this.escapeChar = other.getEscapeChar();
      this.prefixMatcher = other.getVariablePrefixMatcher();
      this.preserveEscapes = other.isPreserveEscapes();
      this.suffixMatcher = other.getVariableSuffixMatcher();
      this.valueDelimiterMatcher = other.getValueDelimiterMatcher();
      this.variableResolver = other.getStringLookup();
   }

   private void checkCyclicSubstitution(String varName, List<String> priorVariables) {
      if (priorVariables.contains(varName)) {
         TextStringBuilder buf = new TextStringBuilder(256);
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

   public StringLookup getStringLookup() {
      return this.variableResolver;
   }

   public StringMatcher getValueDelimiterMatcher() {
      return this.valueDelimiterMatcher;
   }

   public StringMatcher getVariablePrefixMatcher() {
      return this.prefixMatcher;
   }

   public StringMatcher getVariableSuffixMatcher() {
      return this.suffixMatcher;
   }

   public boolean isDisableSubstitutionInValues() {
      return this.disableSubstitutionInValues;
   }

   public boolean isEnableSubstitutionInVariables() {
      return this.enableSubstitutionInVariables;
   }

   public boolean isEnableUndefinedVariableException() {
      return this.failOnUndefinedVariable;
   }

   public boolean isPreserveEscapes() {
      return this.preserveEscapes;
   }

   public String replace(char[] source) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(source.length).append(source);
      this.substitute(buf, 0, source.length);
      return buf.toString();
   }

   public String replace(char[] source, int offset, int length) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(length).append(source, offset, length);
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

      TextStringBuilder buf = new TextStringBuilder(length).append(source.toString(), offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public String replace(Object source) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder().append(source);
      this.substitute(buf, 0, buf.length());
      return buf.toString();
   }

   public String replace(String source) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(source);
      return !this.substitute(buf, 0, source.length()) ? source : buf.toString();
   }

   public String replace(String source, int offset, int length) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(length).append(source, offset, length);
      return !this.substitute(buf, 0, length) ? source.substring(offset, offset + length) : buf.toString();
   }

   public String replace(StringBuffer source) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(source.length()).append(source);
      this.substitute(buf, 0, buf.length());
      return buf.toString();
   }

   public String replace(StringBuffer source, int offset, int length) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(length).append(source, offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public String replace(TextStringBuilder source) {
      if (source == null) {
         return null;
      }

      TextStringBuilder builder = new TextStringBuilder(source.length()).append(source);
      this.substitute(builder, 0, builder.length());
      return builder.toString();
   }

   public String replace(TextStringBuilder source, int offset, int length) {
      if (source == null) {
         return null;
      }

      TextStringBuilder buf = new TextStringBuilder(length).append(source, offset, length);
      this.substitute(buf, 0, length);
      return buf.toString();
   }

   public boolean replaceIn(StringBuffer source) {
      return source == null ? false : this.replaceIn(source, 0, source.length());
   }

   public boolean replaceIn(StringBuffer source, int offset, int length) {
      if (source == null) {
         return false;
      }

      TextStringBuilder buf = new TextStringBuilder(length).append(source, offset, length);
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

      TextStringBuilder buf = new TextStringBuilder(length).append(source, offset, length);
      if (!this.substitute(buf, 0, length)) {
         return false;
      }

      source.replace(offset, offset + length, buf.toString());
      return true;
   }

   public boolean replaceIn(TextStringBuilder source) {
      return source == null ? false : this.substitute(source, 0, source.length());
   }

   public boolean replaceIn(TextStringBuilder source, int offset, int length) {
      return source == null ? false : this.substitute(source, offset, length);
   }

   protected String resolveVariable(String variableName, TextStringBuilder buf, int startPos, int endPos) {
      StringLookup resolver = this.getStringLookup();
      return resolver == null ? null : resolver.lookup(variableName);
   }

   public StringSubstitutor setDisableSubstitutionInValues(boolean disableSubstitutionInValues) {
      this.disableSubstitutionInValues = disableSubstitutionInValues;
      return this;
   }

   public StringSubstitutor setEnableSubstitutionInVariables(boolean enableSubstitutionInVariables) {
      this.enableSubstitutionInVariables = enableSubstitutionInVariables;
      return this;
   }

   public StringSubstitutor setEnableUndefinedVariableException(boolean failOnUndefinedVariable) {
      this.failOnUndefinedVariable = failOnUndefinedVariable;
      return this;
   }

   public StringSubstitutor setEscapeChar(char escapeChar) {
      this.escapeChar = escapeChar;
      return this;
   }

   public StringSubstitutor setPreserveEscapes(boolean preserveEscapes) {
      this.preserveEscapes = preserveEscapes;
      return this;
   }

   public StringSubstitutor setValueDelimiter(char valueDelimiter) {
      return this.setValueDelimiterMatcher(StringMatcherFactory.INSTANCE.charMatcher(valueDelimiter));
   }

   public StringSubstitutor setValueDelimiter(String valueDelimiter) {
      if (valueDelimiter != null && !valueDelimiter.isEmpty()) {
         return this.setValueDelimiterMatcher(StringMatcherFactory.INSTANCE.stringMatcher(valueDelimiter));
      }

      this.setValueDelimiterMatcher(null);
      return this;
   }

   public StringSubstitutor setValueDelimiterMatcher(StringMatcher valueDelimiterMatcher) {
      this.valueDelimiterMatcher = valueDelimiterMatcher;
      return this;
   }

   public StringSubstitutor setVariablePrefix(char prefix) {
      return this.setVariablePrefixMatcher(StringMatcherFactory.INSTANCE.charMatcher(prefix));
   }

   public StringSubstitutor setVariablePrefix(String prefix) {
      Validate.isTrue(prefix != null, "Variable prefix must not be null!");
      return this.setVariablePrefixMatcher(StringMatcherFactory.INSTANCE.stringMatcher(prefix));
   }

   public StringSubstitutor setVariablePrefixMatcher(StringMatcher prefixMatcher) {
      Validate.isTrue(prefixMatcher != null, "Variable prefix matcher must not be null!");
      this.prefixMatcher = prefixMatcher;
      return this;
   }

   public StringSubstitutor setVariableResolver(StringLookup variableResolver) {
      this.variableResolver = variableResolver;
      return this;
   }

   public StringSubstitutor setVariableSuffix(char suffix) {
      return this.setVariableSuffixMatcher(StringMatcherFactory.INSTANCE.charMatcher(suffix));
   }

   public StringSubstitutor setVariableSuffix(String suffix) {
      Validate.isTrue(suffix != null, "Variable suffix must not be null!");
      return this.setVariableSuffixMatcher(StringMatcherFactory.INSTANCE.stringMatcher(suffix));
   }

   public StringSubstitutor setVariableSuffixMatcher(StringMatcher suffixMatcher) {
      Validate.isTrue(suffixMatcher != null, "Variable suffix matcher must not be null!");
      this.suffixMatcher = suffixMatcher;
      return this;
   }

   protected boolean substitute(TextStringBuilder builder, int offset, int length) {
      return this.substitute(builder, offset, length, null).altered;
   }

   private StringSubstitutor.Result substitute(TextStringBuilder builder, int offset, int length, List<String> priorVariables) {
      Objects.requireNonNull(builder, "builder");
      StringMatcher prefixMatcher = this.getVariablePrefixMatcher();
      StringMatcher suffixMatcher = this.getVariableSuffixMatcher();
      char escapeCh = this.getEscapeChar();
      StringMatcher valueDelimMatcher = this.getValueDelimiterMatcher();
      boolean substitutionInVariablesEnabled = this.isEnableSubstitutionInVariables();
      boolean substitutionInValuesDisabled = this.isDisableSubstitutionInValues();
      boolean undefinedVariableException = this.isEnableUndefinedVariableException();
      boolean preserveEscapes = this.isPreserveEscapes();
      boolean altered = false;
      int lengthChange = 0;
      int bufEnd = offset + length;
      int pos = offset;
      int escPos = -1;

      while (pos < bufEnd) {
         int startMatchLen = prefixMatcher.isMatch(builder, pos, offset, bufEnd);
         if (startMatchLen == 0) {
            pos++;
         } else {
            if (pos > offset && builder.charAt(pos - 1) == escapeCh) {
               if (preserveEscapes) {
                  pos++;
                  continue;
               }

               escPos = pos - 1;
            }

            int startPos = pos;
            pos += startMatchLen;
            int endMatchLen = 0;
            int nestedVarCount = 0;

            while (pos < bufEnd) {
               if (substitutionInVariablesEnabled && prefixMatcher.isMatch(builder, pos, offset, bufEnd) != 0) {
                  endMatchLen = prefixMatcher.isMatch(builder, pos, offset, bufEnd);
                  nestedVarCount++;
                  pos += endMatchLen;
               } else {
                  endMatchLen = suffixMatcher.isMatch(builder, pos, offset, bufEnd);
                  if (endMatchLen == 0) {
                     pos++;
                  } else {
                     if (nestedVarCount == 0) {
                        if (escPos >= 0) {
                           builder.deleteCharAt(escPos);
                           escPos = -1;
                           lengthChange--;
                           altered = true;
                           bufEnd--;
                           pos = startPos + 1;
                           startPos--;
                           break;
                        }

                        String varNameExpr = builder.midString(startPos + startMatchLen, pos - startPos - startMatchLen);
                        if (substitutionInVariablesEnabled) {
                           TextStringBuilder bufName = new TextStringBuilder(varNameExpr);
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
                                 && (substitutionInVariablesEnabled || prefixMatcher.isMatch(varNameExprChars, i, i, varNameExprChars.length) == 0);
                              i++
                           ) {
                              if (valueDelimMatcher.isMatch(varNameExprChars, i, 0, varNameExprChars.length) != 0) {
                                 valueDelimiterMatchLen = valueDelimMatcher.isMatch(varNameExprChars, i, 0, varNameExprChars.length);
                                 varName = varNameExpr.substring(0, i);
                                 varDefaultValue = varNameExpr.substring(i + valueDelimiterMatchLen);
                                 break;
                              }
                           }
                        }

                        if (priorVariables == null) {
                           priorVariables = new ArrayList<>();
                           priorVariables.add(builder.midString(offset, length));
                        }

                        this.checkCyclicSubstitution(varName, priorVariables);
                        priorVariables.add(varName);
                        String varValue = this.resolveVariable(varName, builder, startPos, endPos);
                        if (varValue == null) {
                           varValue = varDefaultValue;
                        }

                        if (varValue != null) {
                           int varLen = varValue.length();
                           builder.replace(startPos, endPos, varValue);
                           altered = true;
                           int change = 0;
                           if (!substitutionInValuesDisabled) {
                              change = this.substitute(builder, startPos, varLen, priorVariables).lengthChange;
                           }

                           change = change + varLen - (endPos - startPos);
                           pos += change;
                           bufEnd += change;
                           lengthChange += change;
                        } else if (undefinedVariableException) {
                           throw new IllegalArgumentException(
                              String.format("Cannot resolve variable '%s' (enableSubstitutionInVariables=%s).", varName, substitutionInVariablesEnabled)
                           );
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

      return new StringSubstitutor.Result(altered, lengthChange);
   }

   @Override
   public String toString() {
      return "StringSubstitutor [disableSubstitutionInValues="
         + this.disableSubstitutionInValues
         + ", enableSubstitutionInVariables="
         + this.enableSubstitutionInVariables
         + ", enableUndefinedVariableException="
         + this.failOnUndefinedVariable
         + ", escapeChar="
         + this.escapeChar
         + ", prefixMatcher="
         + this.prefixMatcher
         + ", preserveEscapes="
         + this.preserveEscapes
         + ", suffixMatcher="
         + this.suffixMatcher
         + ", valueDelimiterMatcher="
         + this.valueDelimiterMatcher
         + ", variableResolver="
         + this.variableResolver
         + "]";
   }

   private static final class Result {
      public final boolean altered;
      public final int lengthChange;

      private Result(boolean altered, int lengthChange) {
         this.altered = altered;
         this.lengthChange = lengthChange;
      }

      @Override
      public String toString() {
         return "Result [altered=" + this.altered + ", lengthChange=" + this.lengthChange + "]";
      }
   }
}
