package com.dfsek.terra.lib.commons.text.lookup;

import com.dfsek.terra.lib.commons.lang3.time.FastDateFormat;
import java.util.Date;

final class DateStringLookup extends AbstractStringLookup {
   static final DateStringLookup INSTANCE = new DateStringLookup();

   private DateStringLookup() {
   }

   private String formatDate(long dateMillis, String format) {
      FastDateFormat dateFormat = null;
      if (format != null) {
         try {
            dateFormat = FastDateFormat.getInstance(format);
         } catch (Exception ex) {
            throw IllegalArgumentExceptions.format(ex, "Invalid date format: [%s]", format);
         }
      }

      if (dateFormat == null) {
         dateFormat = FastDateFormat.getInstance();
      }

      return dateFormat.format(new Date(dateMillis));
   }

   @Override
   public String lookup(String key) {
      return this.formatDate(System.currentTimeMillis(), key);
   }
}
