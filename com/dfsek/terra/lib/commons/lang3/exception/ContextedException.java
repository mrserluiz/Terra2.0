package com.dfsek.terra.lib.commons.lang3.exception;

import com.dfsek.terra.lib.commons.lang3.tuple.Pair;
import java.util.List;
import java.util.Set;

public class ContextedException extends Exception implements ExceptionContext {
   private static final long serialVersionUID = 20110706L;
   private final ExceptionContext exceptionContext;

   public ContextedException() {
      this.exceptionContext = new DefaultExceptionContext();
   }

   public ContextedException(String message) {
      super(message);
      this.exceptionContext = new DefaultExceptionContext();
   }

   public ContextedException(String message, Throwable cause) {
      super(message, cause);
      this.exceptionContext = new DefaultExceptionContext();
   }

   public ContextedException(String message, Throwable cause, ExceptionContext context) {
      super(message, cause);
      if (context == null) {
         context = new DefaultExceptionContext();
      }

      this.exceptionContext = context;
   }

   public ContextedException(Throwable cause) {
      super(cause);
      this.exceptionContext = new DefaultExceptionContext();
   }

   public ContextedException addContextValue(String label, Object value) {
      this.exceptionContext.addContextValue(label, value);
      return this;
   }

   @Override
   public List<Pair<String, Object>> getContextEntries() {
      return this.exceptionContext.getContextEntries();
   }

   @Override
   public Set<String> getContextLabels() {
      return this.exceptionContext.getContextLabels();
   }

   @Override
   public List<Object> getContextValues(String label) {
      return this.exceptionContext.getContextValues(label);
   }

   @Override
   public Object getFirstContextValue(String label) {
      return this.exceptionContext.getFirstContextValue(label);
   }

   @Override
   public String getFormattedExceptionMessage(String baseMessage) {
      return this.exceptionContext.getFormattedExceptionMessage(baseMessage);
   }

   @Override
   public String getMessage() {
      return this.getFormattedExceptionMessage(super.getMessage());
   }

   public String getRawMessage() {
      return super.getMessage();
   }

   public ContextedException setContextValue(String label, Object value) {
      this.exceptionContext.setContextValue(label, value);
      return this;
   }
}
