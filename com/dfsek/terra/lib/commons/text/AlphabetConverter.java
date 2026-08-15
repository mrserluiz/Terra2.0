package com.dfsek.terra.lib.commons.text;

import com.dfsek.terra.lib.commons.lang3.ArrayUtils;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

public final class AlphabetConverter {
   private static final String ARROW = " -> ";
   private final Map<Integer, String> originalToEncoded;
   private final Map<String, String> encodedToOriginal;
   private final int encodedLetterLength;

   private static String codePointToString(int i) {
      return Character.charCount(i) == 1 ? String.valueOf((char)i) : new String(Character.toChars(i));
   }

   private static Integer[] convertCharsToIntegers(Character[] chars) {
      if (ArrayUtils.isEmpty(chars)) {
         return ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
      }

      Integer[] integers = new Integer[chars.length];
      Arrays.setAll(integers, i -> Integer.valueOf(chars[i]));
      return integers;
   }

   public static AlphabetConverter createConverter(Integer[] original, Integer[] encoding, Integer[] doNotEncode) {
      Set<Integer> originalCopy = new LinkedHashSet<>(Arrays.asList(original));
      Set<Integer> encodingCopy = new LinkedHashSet<>(Arrays.asList(encoding));
      Set<Integer> doNotEncodeCopy = new LinkedHashSet<>(Arrays.asList(doNotEncode));
      Map<Integer, String> originalToEncoded = new LinkedHashMap<>();
      Map<String, String> encodedToOriginal = new LinkedHashMap<>();
      Map<Integer, String> doNotEncodeMap = new HashMap<>();

      for (int i : doNotEncodeCopy) {
         if (!originalCopy.contains(i)) {
            throw new IllegalArgumentException("Can not use 'do not encode' list because original alphabet does not contain '" + codePointToString(i) + "'");
         }

         if (!encodingCopy.contains(i)) {
            throw new IllegalArgumentException("Can not use 'do not encode' list because encoding alphabet does not contain '" + codePointToString(i) + "'");
         }

         doNotEncodeMap.put(i, codePointToString(i));
      }

      if (encodingCopy.size() >= originalCopy.size()) {
         int encodedLetterLength = 1;
         Iterator<Integer> it = encodingCopy.iterator();

         for (int originalLetter : originalCopy) {
            String originalLetterAsString = codePointToString(originalLetter);
            if (doNotEncodeMap.containsKey(originalLetter)) {
               originalToEncoded.put(originalLetter, originalLetterAsString);
               encodedToOriginal.put(originalLetterAsString, originalLetterAsString);
            } else {
               Integer next = it.next();

               while (doNotEncodeCopy.contains(next)) {
                  next = it.next();
               }

               String encodedLetter = codePointToString(next);
               originalToEncoded.put(originalLetter, encodedLetter);
               encodedToOriginal.put(encodedLetter, originalLetterAsString);
            }
         }

         return new AlphabetConverter(originalToEncoded, encodedToOriginal, encodedLetterLength);
      } else {
         if (encodingCopy.size() - doNotEncodeCopy.size() < 2) {
            throw new IllegalArgumentException(
               "Must have at least two encoding characters (excluding those in the 'do not encode' list), but has "
                  + (encodingCopy.size() - doNotEncodeCopy.size())
            );
         }

         int lettersSoFar = 1;

         for (int lettersLeft = (originalCopy.size() - doNotEncodeCopy.size()) / (encodingCopy.size() - doNotEncodeCopy.size());
            lettersLeft / encodingCopy.size() >= 1;
            lettersSoFar++
         ) {
            lettersLeft /= encodingCopy.size();
         }

         int encodedLetterLength = lettersSoFar + 1;
         AlphabetConverter ac = new AlphabetConverter(originalToEncoded, encodedToOriginal, encodedLetterLength);
         ac.addSingleEncoding(encodedLetterLength, "", encodingCopy, originalCopy.iterator(), doNotEncodeMap);
         return ac;
      }
   }

   public static AlphabetConverter createConverterFromChars(Character[] original, Character[] encoding, Character[] doNotEncode) {
      return createConverter(convertCharsToIntegers(original), convertCharsToIntegers(encoding), convertCharsToIntegers(doNotEncode));
   }

   public static AlphabetConverter createConverterFromMap(Map<Integer, String> originalToEncoded) {
      Map<Integer, String> unmodifiableOriginalToEncoded = Collections.unmodifiableMap(originalToEncoded);
      Map<String, String> encodedToOriginal = new LinkedHashMap<>();
      int encodedLetterLength = 1;

      for (Entry<Integer, String> e : unmodifiableOriginalToEncoded.entrySet()) {
         encodedToOriginal.put(e.getValue(), codePointToString(e.getKey()));
         if (e.getValue().length() > encodedLetterLength) {
            encodedLetterLength = e.getValue().length();
         }
      }

      return new AlphabetConverter(unmodifiableOriginalToEncoded, encodedToOriginal, encodedLetterLength);
   }

   private AlphabetConverter(Map<Integer, String> originalToEncoded, Map<String, String> encodedToOriginal, int encodedLetterLength) {
      this.originalToEncoded = originalToEncoded;
      this.encodedToOriginal = encodedToOriginal;
      this.encodedLetterLength = encodedLetterLength;
   }

   private void addSingleEncoding(
      int level, String currentEncoding, Collection<Integer> encoding, Iterator<Integer> originals, Map<Integer, String> doNotEncodeMap
   ) {
      if (level > 0) {
         for (int encodingLetter : encoding) {
            if (!originals.hasNext()) {
               return;
            }

            if (level != this.encodedLetterLength || !doNotEncodeMap.containsKey(encodingLetter)) {
               this.addSingleEncoding(level - 1, currentEncoding + codePointToString(encodingLetter), encoding, originals, doNotEncodeMap);
            }
         }
      } else {
         Integer next;
         for (next = originals.next(); doNotEncodeMap.containsKey(next); next = originals.next()) {
            String originalLetterAsString = codePointToString(next);
            this.originalToEncoded.put(next, originalLetterAsString);
            this.encodedToOriginal.put(originalLetterAsString, originalLetterAsString);
            if (!originals.hasNext()) {
               return;
            }
         }

         String originalLetterAsString = codePointToString(next);
         this.originalToEncoded.put(next, currentEncoding);
         this.encodedToOriginal.put(currentEncoding, originalLetterAsString);
      }
   }

   public String decode(String encoded) throws UnsupportedEncodingException {
      if (encoded == null) {
         return null;
      }

      StringBuilder result = new StringBuilder();
      int j = 0;

      while (j < encoded.length()) {
         int i = encoded.codePointAt(j);
         String s = codePointToString(i);
         if (s.equals(this.originalToEncoded.get(i))) {
            result.append(s);
            j++;
         } else {
            if (j + this.encodedLetterLength > encoded.length()) {
               throw new UnsupportedEncodingException("Unexpected end of string while decoding " + encoded);
            }

            String nextGroup = encoded.substring(j, j + this.encodedLetterLength);
            String next = this.encodedToOriginal.get(nextGroup);
            if (next == null) {
               throw new UnsupportedEncodingException("Unexpected string without decoding (" + nextGroup + ") in " + encoded);
            }

            result.append(next);
            j += this.encodedLetterLength;
         }
      }

      return result.toString();
   }

   public String encode(String original) throws UnsupportedEncodingException {
      if (original == null) {
         return null;
      }

      StringBuilder sb = new StringBuilder();
      int i = 0;

      while (i < original.length()) {
         int codePoint = original.codePointAt(i);
         String nextLetter = this.originalToEncoded.get(codePoint);
         if (nextLetter == null) {
            throw new UnsupportedEncodingException("Couldn't find encoding for '" + codePointToString(codePoint) + "' in " + original);
         }

         sb.append(nextLetter);
         i += Character.charCount(codePoint);
      }

      return sb.toString();
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }

      if (obj == this) {
         return true;
      }

      if (!(obj instanceof AlphabetConverter)) {
         return false;
      }

      AlphabetConverter other = (AlphabetConverter)obj;
      return this.originalToEncoded.equals(other.originalToEncoded)
         && this.encodedToOriginal.equals(other.encodedToOriginal)
         && this.encodedLetterLength == other.encodedLetterLength;
   }

   public int getEncodedCharLength() {
      return this.encodedLetterLength;
   }

   public Map<Integer, String> getOriginalToEncoded() {
      return Collections.unmodifiableMap(this.originalToEncoded);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.originalToEncoded, this.encodedToOriginal, this.encodedLetterLength);
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      this.originalToEncoded.forEach((k, v) -> sb.append(codePointToString(k)).append(" -> ").append(k).append(System.lineSeparator()));
      return sb.toString();
   }
}
