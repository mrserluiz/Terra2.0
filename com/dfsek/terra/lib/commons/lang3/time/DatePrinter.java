package com.dfsek.terra.lib.commons.lang3.time;

import java.text.FieldPosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public interface DatePrinter {
   String format(Calendar var1);

   <B extends Appendable> B format(Calendar var1, B var2);

   @Deprecated
   StringBuffer format(Calendar var1, StringBuffer var2);

   String format(Date var1);

   <B extends Appendable> B format(Date var1, B var2);

   @Deprecated
   StringBuffer format(Date var1, StringBuffer var2);

   String format(long var1);

   <B extends Appendable> B format(long var1, B var3);

   @Deprecated
   StringBuffer format(long var1, StringBuffer var3);

   StringBuffer format(Object var1, StringBuffer var2, FieldPosition var3);

   Locale getLocale();

   String getPattern();

   TimeZone getTimeZone();
}
