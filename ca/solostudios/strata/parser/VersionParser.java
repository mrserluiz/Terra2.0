package ca.solostudios.strata.parser;

import ca.solostudios.strata.parser.tokenizer.Char;
import ca.solostudios.strata.parser.tokenizer.LookaheadReader;
import ca.solostudios.strata.parser.tokenizer.ParseException;
import ca.solostudios.strata.version.BuildMetadata;
import ca.solostudios.strata.version.CoreVersion;
import ca.solostudios.strata.version.PreRelease;
import ca.solostudios.strata.version.PreReleaseIdentifier;
import ca.solostudios.strata.version.Version;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class VersionParser {
   private static final char PLUS = '+';
   private static final char DOT = '.';
   private static final char DASH = '-';
   @NotNull
   private final LookaheadReader input;
   @NotNull
   private final String versionString;

   public VersionParser(@NotNull String versionString) {
      this.input = new LookaheadReader(new StringReader(versionString));
      this.versionString = versionString;
   }

   @NotNull
   @Contract(value = "-> new", pure = true)
   public Version parse() throws ParseException {
      CoreVersion coreVersion = this.parseCoreVersion();
      PreRelease preRelease = PreRelease.NULL;
      BuildMetadata buildMetadata = BuildMetadata.NULL;
      Char next = this.input.consume();
      if (next.is('-')) {
         preRelease = this.parsePreRelease();
         next = this.input.consume();
      }

      if (next.is('+')) {
         buildMetadata = this.parseBuildMetadata();
         next = this.input.consume();
      }

      if (next.isEndOfInput()) {
         return new Version(coreVersion, preRelease, buildMetadata);
      } else {
         throw new ParseException("Expected end of version. Illegal character found.", this.versionString, next);
      }
   }

   private CoreVersion parseCoreVersion() throws ParseException {
      BigInteger major = new BigInteger(this.consumeNumber());
      this.consumeCharacter('.');
      BigInteger minor = new BigInteger(this.consumeNumber());
      this.consumeCharacter('.');
      BigInteger patch = new BigInteger(this.consumeNumber());
      return new CoreVersion(major, minor, patch);
   }

   private PreRelease parsePreRelease() throws ParseException {
      List<PreReleaseIdentifier> identifiers = new ArrayList<>();
      identifiers.add(this.parsePreReleaseIdentifier());

      while (this.input.current().is('.')) {
         this.input.consume();
         identifiers.add(this.parsePreReleaseIdentifier());
      }

      return new PreRelease(identifiers);
   }

   private PreReleaseIdentifier parsePreReleaseIdentifier() throws ParseException {
      return this.lookaheadAlphaNumeric()
         ? new PreReleaseIdentifier.AlphaNumericalPreReleaseIdentifier(this.consumeAlphaNumeric())
         : new PreReleaseIdentifier.NumericalPreReleaseIdentifier(new BigInteger(this.consumeNumber()));
   }

   private PreReleaseIdentifier.AlphaNumericalPreReleaseIdentifier parseAlphaNumericPreReleaseIdentifier() throws ParseException {
      return new PreReleaseIdentifier.AlphaNumericalPreReleaseIdentifier(this.consumeAlphaNumeric());
   }

   private BuildMetadata parseBuildMetadata() throws ParseException {
      StringBuilder sb = new StringBuilder();
      if (!this.input.current().isAlphaNumeric()) {
         throw new ParseException("Alpha-Numeric identifier expected.", this.versionString, this.input.current());
      }

      do {
         Char consumed = this.input.consume();
         if (consumed.is('.')) {
            if (this.input.current().is('.')) {
               throw new ParseException("Alpha-Numeric identifier expected, but found period.", this.versionString, this.input.current());
            }

            if (this.input.current().isEndOfInput()) {
               throw new ParseException("Alpha-Numeric identifier expected, but found end of input.", this.versionString, this.input.current());
            }
         }

         sb.append(consumed.getValue());
      } while (this.input.current().isAlphaNumeric() || this.input.current().is('.'));

      return new BuildMetadata(sb.toString());
   }

   private String consumeNumber() throws ParseException {
      StringBuilder sb = new StringBuilder();
      if (!this.input.current().isDigit()) {
         throw new ParseException("Numeric identifier expected.", this.versionString, this.input.current());
      }

      if (this.input.current().is('0') && this.input.next().isDigit()) {
         throw new ParseException("Numeric identifier must not contain leading zeros.", this.versionString, this.input.current());
      }

      do {
         sb.append(this.input.consume().getValue());
      } while (this.input.current().isDigit());

      return sb.toString();
   }

   private boolean lookaheadAlphaNumeric() throws ParseException {
      boolean foundNonDigit = false;

      for (int i = 0; !foundNonDigit; i++) {
         foundNonDigit = this.input.next(i).isLetter() || this.input.next(i).is('-');
         if (!this.input.next(i).isAlphaNumeric()) {
            return foundNonDigit;
         }
      }

      return true;
   }

   private String consumeAlphaNumeric() throws ParseException {
      StringBuilder sb = new StringBuilder();
      if (!this.input.current().isAlphaNumeric()) {
         throw new ParseException("Alpha-Numeric identifier expected.", this.versionString, this.input.current());
      }

      do {
         sb.append(this.input.consume().getValue());
      } while (this.input.current().isAlphaNumeric());

      return sb.toString();
   }

   private void consumeCharacter(char expected) throws ParseException {
      if (this.input.current().is(expected)) {
         this.input.consume();
      } else {
         throw new ParseException(String.format("Illegal character. Character '%s' expected.", expected), this.versionString, this.input.current());
      }
   }
}
