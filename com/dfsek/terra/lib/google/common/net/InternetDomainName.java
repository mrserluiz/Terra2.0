package com.dfsek.terra.lib.google.common.net;

import com.dfsek.terra.lib.google.common.annotations.GwtCompatible;
import com.dfsek.terra.lib.google.common.base.Ascii;
import com.dfsek.terra.lib.google.common.base.CharMatcher;
import com.dfsek.terra.lib.google.common.base.Joiner;
import com.dfsek.terra.lib.google.common.base.Optional;
import com.dfsek.terra.lib.google.common.base.Preconditions;
import com.dfsek.terra.lib.google.common.base.Splitter;
import com.dfsek.terra.lib.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.google.thirdparty.publicsuffix.PublicSuffixPatterns;
import com.google.thirdparty.publicsuffix.PublicSuffixType;
import java.util.List;
import org.jspecify.annotations.Nullable;

@Immutable
@GwtCompatible(emulated = true)
public final class InternetDomainName {
   private static final CharMatcher DOTS_MATCHER = CharMatcher.anyOf(".。．｡");
   private static final Splitter DOT_SPLITTER = Splitter.on('.');
   private static final Joiner DOT_JOINER = Joiner.on('.');
   private static final int NO_SUFFIX_FOUND = -1;
   private static final int SUFFIX_NOT_INITIALIZED = -2;
   private static final int MAX_PARTS = 127;
   private static final int MAX_LENGTH = 253;
   private static final int MAX_DOMAIN_PART_LENGTH = 63;
   private final String name;
   private final ImmutableList<String> parts;
   @LazyInit
   private int publicSuffixIndexCache = -2;
   @LazyInit
   private int registrySuffixIndexCache = -2;
   private static final CharMatcher DASH_MATCHER = CharMatcher.anyOf("-_");
   private static final CharMatcher DIGIT_MATCHER = CharMatcher.inRange('0', '9');
   private static final CharMatcher LETTER_MATCHER = CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z'));
   private static final CharMatcher PART_CHAR_MATCHER = DIGIT_MATCHER.or(LETTER_MATCHER).or(DASH_MATCHER);

   InternetDomainName(String name) {
      name = Ascii.toLowerCase(DOTS_MATCHER.replaceFrom(name, '.'));
      if (name.endsWith(".")) {
         name = name.substring(0, name.length() - 1);
      }

      Preconditions.checkArgument(name.length() <= 253, "Domain name too long: '%s':", name);
      this.name = name;
      this.parts = ImmutableList.copyOf(DOT_SPLITTER.split(name));
      Preconditions.checkArgument(this.parts.size() <= 127, "Domain has too many parts: '%s'", name);
      Preconditions.checkArgument(validateSyntax(this.parts), "Not a valid domain name: '%s'", name);
   }

   private InternetDomainName(String name, ImmutableList<String> parts) {
      Preconditions.checkArgument(!parts.isEmpty(), "Cannot create an InternetDomainName with zero parts.");
      this.name = name;
      this.parts = parts;
   }

   private int publicSuffixIndex() {
      int publicSuffixIndexLocal = this.publicSuffixIndexCache;
      if (publicSuffixIndexLocal == -2) {
         this.publicSuffixIndexCache = publicSuffixIndexLocal = this.findSuffixOfType(Optional.absent());
      }

      return publicSuffixIndexLocal;
   }

   private int registrySuffixIndex() {
      int registrySuffixIndexLocal = this.registrySuffixIndexCache;
      if (registrySuffixIndexLocal == -2) {
         this.registrySuffixIndexCache = registrySuffixIndexLocal = this.findSuffixOfType(Optional.of(PublicSuffixType.REGISTRY));
      }

      return registrySuffixIndexLocal;
   }

   private int findSuffixOfType(Optional<PublicSuffixType> desiredType) {
      int partsSize = this.parts.size();

      for (int i = 0; i < partsSize; i++) {
         String ancestorName = DOT_JOINER.join(this.parts.subList(i, partsSize));
         if (i > 0 && matchesType(desiredType, Optional.fromNullable(PublicSuffixPatterns.UNDER.get(ancestorName)))) {
            return i - 1;
         }

         if (matchesType(desiredType, Optional.fromNullable(PublicSuffixPatterns.EXACT.get(ancestorName)))) {
            return i;
         }

         if (PublicSuffixPatterns.EXCLUDED.containsKey(ancestorName)) {
            return i + 1;
         }
      }

      return -1;
   }

   @CanIgnoreReturnValue
   public static InternetDomainName from(String domain) {
      return new InternetDomainName(Preconditions.checkNotNull(domain));
   }

   private static boolean validateSyntax(List<String> parts) {
      int lastIndex = parts.size() - 1;
      if (!validatePart(parts.get(lastIndex), true)) {
         return false;
      }

      for (int i = 0; i < lastIndex; i++) {
         String part = parts.get(i);
         if (!validatePart(part, false)) {
            return false;
         }
      }

      return true;
   }

   private static boolean validatePart(String part, boolean isFinalPart) {
      if (part.length() >= 1 && part.length() <= 63) {
         String asciiChars = CharMatcher.ascii().retainFrom(part);
         if (!PART_CHAR_MATCHER.matchesAllOf(asciiChars)) {
            return false;
         } else {
            return DASH_MATCHER.matches(part.charAt(0)) || DASH_MATCHER.matches(part.charAt(part.length() - 1))
               ? false
               : !isFinalPart || !DIGIT_MATCHER.matches(part.charAt(0));
         }
      } else {
         return false;
      }
   }

   public ImmutableList<String> parts() {
      return this.parts;
   }

   public boolean isPublicSuffix() {
      return this.publicSuffixIndex() == 0;
   }

   public boolean hasPublicSuffix() {
      return this.publicSuffixIndex() != -1;
   }

   public @Nullable InternetDomainName publicSuffix() {
      return this.hasPublicSuffix() ? this.ancestor(this.publicSuffixIndex()) : null;
   }

   public boolean isUnderPublicSuffix() {
      return this.publicSuffixIndex() > 0;
   }

   public boolean isTopPrivateDomain() {
      return this.publicSuffixIndex() == 1;
   }

   public InternetDomainName topPrivateDomain() {
      if (this.isTopPrivateDomain()) {
         return this;
      }

      Preconditions.checkState(this.isUnderPublicSuffix(), "Not under a public suffix: %s", this.name);
      return this.ancestor(this.publicSuffixIndex() - 1);
   }

   public boolean isRegistrySuffix() {
      return this.registrySuffixIndex() == 0;
   }

   public boolean hasRegistrySuffix() {
      return this.registrySuffixIndex() != -1;
   }

   public @Nullable InternetDomainName registrySuffix() {
      return this.hasRegistrySuffix() ? this.ancestor(this.registrySuffixIndex()) : null;
   }

   public boolean isUnderRegistrySuffix() {
      return this.registrySuffixIndex() > 0;
   }

   public boolean isTopDomainUnderRegistrySuffix() {
      return this.registrySuffixIndex() == 1;
   }

   public InternetDomainName topDomainUnderRegistrySuffix() {
      if (this.isTopDomainUnderRegistrySuffix()) {
         return this;
      }

      Preconditions.checkState(this.isUnderRegistrySuffix(), "Not under a registry suffix: %s", this.name);
      return this.ancestor(this.registrySuffixIndex() - 1);
   }

   public boolean hasParent() {
      return this.parts.size() > 1;
   }

   public InternetDomainName parent() {
      Preconditions.checkState(this.hasParent(), "Domain '%s' has no parent", this.name);
      return this.ancestor(1);
   }

   private InternetDomainName ancestor(int levels) {
      ImmutableList<String> ancestorParts = this.parts.subList(levels, this.parts.size());
      int substringFrom = levels;

      for (int i = 0; i < levels; i++) {
         substringFrom += this.parts.get(i).length();
      }

      String ancestorName = this.name.substring(substringFrom);
      return new InternetDomainName(ancestorName, ancestorParts);
   }

   public InternetDomainName child(String leftParts) {
      return from(Preconditions.checkNotNull(leftParts) + "." + this.name);
   }

   public static boolean isValid(String name) {
      try {
         InternetDomainName unused = from(name);
         return true;
      } catch (IllegalArgumentException e) {
         return false;
      }
   }

   private static boolean matchesType(Optional<PublicSuffixType> desiredType, Optional<PublicSuffixType> actualType) {
      return desiredType.isPresent() ? desiredType.equals(actualType) : actualType.isPresent();
   }

   @Override
   public String toString() {
      return this.name;
   }

   @Override
   public boolean equals(@Nullable Object object) {
      if (object == this) {
         return true;
      } else if (object instanceof InternetDomainName) {
         InternetDomainName that = (InternetDomainName)object;
         return this.name.equals(that.name);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }
}
