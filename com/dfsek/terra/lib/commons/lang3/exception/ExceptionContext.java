package com.dfsek.terra.lib.commons.lang3.exception;

import com.dfsek.terra.lib.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.Set;

public interface ExceptionContext {
   ExceptionContext addContextValue(String var1, Object var2);

   List<Pair<String, Object>> getContextEntries();

   Set<String> getContextLabels();

   List<Object> getContextValues(String var1);

   Object getFirstContextValue(String var1);

   String getFormattedExceptionMessage(String var1);

   ExceptionContext setContextValue(String var1, Object var2);
}
