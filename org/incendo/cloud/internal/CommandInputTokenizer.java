package org.incendo.cloud.internal;

import java.util.LinkedList;
import java.util.StringTokenizer;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(status = Status.INTERNAL, consumers = "org.incendo.cloud.*")
public final class CommandInputTokenizer {
   private static final String DELIMITER = " ";
   private static final String EMPTY = "";
   private final CommandInputTokenizer.StringTokenizerFactory stringTokenizerFactory = new CommandInputTokenizer.StringTokenizerFactory();
   private final String input;

   public CommandInputTokenizer(final @NonNull String input) {
      this.input = input;
   }

   public @NonNull LinkedList<@NonNull String> tokenize() {
      StringTokenizer stringTokenizer = this.stringTokenizerFactory.createStringTokenizer();
      LinkedList<String> tokens = new LinkedList<>();

      while (stringTokenizer.hasMoreElements()) {
         tokens.add(stringTokenizer.nextToken());
      }

      if (this.input.endsWith(" ")) {
         tokens.add("");
      }

      return tokens;
   }

   private final class StringTokenizerFactory {
      private StringTokenizerFactory() {
      }

      private @NonNull StringTokenizer createStringTokenizer() {
         return new StringTokenizer(CommandInputTokenizer.this.input, " ");
      }
   }
}
