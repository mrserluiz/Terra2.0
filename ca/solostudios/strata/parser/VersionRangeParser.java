package ca.solostudios.strata.parser;

import ca.solostudios.strata.Versions;
import ca.solostudios.strata.parser.tokenizer.Char;
import ca.solostudios.strata.parser.tokenizer.LookaheadReader;
import ca.solostudios.strata.parser.tokenizer.ParseException;
import ca.solostudios.strata.version.Version;
import ca.solostudios.strata.version.VersionRange;
import java.io.StringReader;
import java.math.BigInteger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class VersionRangeParser {
   private static final char OPEN_PAREN = '(';
   private static final char CLOSE_PAREN = ')';
   private static final char OPEN_BRACKET = '[';
   private static final char CLOSE_BRACKET = ']';
   private static final char COMMA = ',';
   private static final char PLUS = '+';
   private static final char DOT = '.';
   private final LookaheadReader input;
   private final String versionRangeString;

   public VersionRangeParser(String versionRangeString) {
      this.input = new LookaheadReader(new StringReader(versionRangeString));
      this.versionRangeString = versionRangeString;
   }

   @NotNull
   @Contract(value = "-> new", pure = true)
   public VersionRange parse() throws ParseException {
      return this.input.current().is('[', '(') ? this.parseVersionRange() : this.parseVersionGlob();
   }

   private VersionRange parseVersionRange() throws ParseException {
      boolean startInclusive = this.input.consume().is('[');
      Version startVersion = null;
      Version endVersion = null;
      if (this.input.current().is(',')) {
         this.input.consume();
      } else {
         StringBuilder sb = new StringBuilder();

         do {
            Char consumed = this.input.consume();
            if (this.input.current().isEndOfInput()) {
               throw new ParseException("Found end of input while looking for comma in version range string.", this.versionRangeString, this.input.current());
            }

            sb.append(consumed.getValue());
         } while (!this.input.current().is(','));

         try {
            startVersion = new VersionParser(sb.toString()).parse();
         } catch (ParseException e) {
            throw new ParseException(e, this.versionRangeString, e.getPosition());
         }

         Char next = this.input.consume();
         if (!next.is(',')) {
            throw new ParseException("Was expecting comma after version", this.versionRangeString, next);
         }
      }

      boolean endInclusive;
      if (this.input.current().is(']', ')')) {
         endInclusive = this.input.consume().is(']');
      } else {
         StringBuilder sb = new StringBuilder();

         do {
            Char consumed = this.input.consume();
            if (this.input.current().isEndOfInput()) {
               throw new ParseException("Found end of input while looking for comma in version range string.", this.versionRangeString, this.input.current());
            }

            sb.append(consumed.getValue());
         } while (!this.input.current().is(']', ')'));

         try {
            endVersion = new VersionParser(sb.toString()).parse();
         } catch (ParseException e) {
            throw new ParseException(e, this.versionRangeString, e.getPosition());
         }

         Char next = this.input.consume();
         switch (next.getValue()) {
            case ')':
               endInclusive = false;
               break;
            case ']':
               endInclusive = true;
               break;
            default:
               throw new ParseException(String.format("Was looking for '%s' or '%s' but couldn't find one", ']', ')'), this.versionRangeString, next);
         }
      }

      return new VersionRange(startVersion, startInclusive, endVersion, endInclusive);
   }

   private VersionRange parseVersionGlob() throws ParseException {
      Version lowestVersion;
      Version highestVersion;
      if (this.input.current().is('+')) {
         highestVersion = null;
         lowestVersion = Versions.getVersion(0, 0, 0);
      } else {
         BigInteger major = new BigInteger(this.consumeNumber());
         this.consumeCharacter('.');
         if (this.input.current().is('+')) {
            lowestVersion = Versions.getVersion(major, BigInteger.ZERO, BigInteger.ZERO);
            highestVersion = Versions.getVersion(major.add(BigInteger.ONE), BigInteger.ZERO, BigInteger.ZERO);
         } else {
            BigInteger minor = new BigInteger(this.consumeNumber());
            this.consumeCharacter('.');
            if (this.input.current().is('+')) {
               lowestVersion = Versions.getVersion(major, minor, BigInteger.ZERO);
               highestVersion = Versions.getVersion(major, minor.add(BigInteger.ONE), BigInteger.ZERO);
            } else {
               BigInteger patch = new BigInteger(this.consumeNumber());
               lowestVersion = Versions.getVersion(major, minor, patch);
               highestVersion = Versions.getVersion(major, minor, patch.add(BigInteger.ONE));
            }
         }
      }

      return new VersionRange(lowestVersion, true, highestVersion, false);
   }

   private String consumeNumber() throws ParseException {
      StringBuilder sb = new StringBuilder();
      if (!this.input.current().isDigit()) {
         throw new ParseException("Numeric identifier expected.", this.versionRangeString, this.input.current());
      }

      if (this.input.current().is('0') && this.input.next().isDigit()) {
         throw new ParseException("Numeric identifier must not contain leading zeros.", this.versionRangeString, this.input.current());
      }

      do {
         sb.append(this.input.consume().getValue());
      } while (this.input.current().isDigit());

      return sb.toString();
   }

   private void consumeCharacter(char expected) throws ParseException {
      if (this.input.current().is(expected)) {
         this.input.consume();
      } else {
         throw new ParseException(String.format("Illegal character. Character '%s' expected.", expected), this.versionRangeString, this.input.current());
      }
   }
}
