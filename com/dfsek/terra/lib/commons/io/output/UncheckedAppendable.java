package com.dfsek.terra.lib.commons.io.output;

public interface UncheckedAppendable extends Appendable {
   static UncheckedAppendable on(Appendable appendable) {
      return new UncheckedAppendableImpl(appendable);
   }

   UncheckedAppendable append(char var1);

   UncheckedAppendable append(CharSequence var1);

   UncheckedAppendable append(CharSequence var1, int var2, int var3);
}
