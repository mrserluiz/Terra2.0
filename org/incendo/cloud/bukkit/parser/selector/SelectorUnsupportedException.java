package org.incendo.cloud.bukkit.parser.selector;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCaptionKeys;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.exception.parsing.ParserException;

@API(status = Status.STABLE, since = "2.0.0")
public final class SelectorUnsupportedException extends ParserException {
   public SelectorUnsupportedException(final @NonNull CommandContext<?> context, final @NonNull Class<?> parser) {
      super(parser, context, BukkitCaptionKeys.ARGUMENT_PARSE_FAILURE_SELECTOR_UNSUPPORTED);
   }
}
