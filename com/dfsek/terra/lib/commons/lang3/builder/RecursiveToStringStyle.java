package com.dfsek.terra.lib.commons.lang3.builder;

import com.dfsek.terra.lib.commons.lang3.ClassUtils;
import java.util.Collection;

public class RecursiveToStringStyle extends ToStringStyle {
   private static final long serialVersionUID = 1L;

   protected boolean accept(Class<?> clazz) {
      return true;
   }

   @Override
   protected void appendDetail(StringBuffer buffer, String fieldName, Collection<?> coll) {
      this.appendClassName(buffer, coll);
      this.appendIdentityHashCode(buffer, coll);
      this.appendDetail(buffer, fieldName, (Object[])coll.toArray());
   }

   @Override
   public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
      if (!ClassUtils.isPrimitiveWrapper(value.getClass()) && !String.class.equals(value.getClass()) && this.accept(value.getClass())) {
         buffer.append(ReflectionToStringBuilder.toString(value, this));
      } else {
         super.appendDetail(buffer, fieldName, value);
      }
   }
}
