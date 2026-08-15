package org.incendo.cloud.help;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.help.result.HelpQueryResult;
import org.incendo.cloud.help.result.IndexCommandResult;

@API(status = Status.STABLE)
public interface HelpHandler<C> {
   @NonNull HelpQueryResult<C> query(@NonNull HelpQuery<C> query);

   default @NonNull IndexCommandResult<C> queryRootIndex(final @NonNull C sender) {
      return (IndexCommandResult<C>)this.query(HelpQuery.of(sender, ""));
   }
}
