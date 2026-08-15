package com.dfsek.terra.lib.commons.lang3.time;

import com.dfsek.terra.lib.commons.lang3.ObjectUtils;
import java.util.TimeZone;

public class TimeZones {
   public static final String GMT_ID = "GMT";
   public static final TimeZone GMT = TimeZone.getTimeZone("GMT");

   public static TimeZone toTimeZone(TimeZone timeZone) {
      return ObjectUtils.getIfNull(timeZone, TimeZone::getDefault);
   }

   private TimeZones() {
   }
}
