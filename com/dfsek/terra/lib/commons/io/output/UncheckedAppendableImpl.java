package com.dfsek.terra.lib.commons.io.output;

import com.dfsek.terra.lib.commons.io.function.Uncheck;
import java.util.Objects;

final class UncheckedAppendableImpl implements UncheckedAppendable {
   private final Appendable appendable;

   UncheckedAppendableImpl(Appendable appendable) {
      this.appendable = Objects.requireNonNull(appendable, "appendable");
   }

   @Override
   public UncheckedAppendable append(char c) {
      Uncheck.apply(this.appendable::append, c);
      return this;
   }

   @Override
   public UncheckedAppendable append(CharSequence csq) {
      Uncheck.apply(this.appendable::append, csq);
      return this;
   }

   @Override
   public UncheckedAppendable append(CharSequence csq, int start, int end) {
      Uncheck.apply(this.appendable::append, csq, start, end);
      return this;
   }

   @Override
   public String toString() {
      return this.appendable.toString();
   }
}
