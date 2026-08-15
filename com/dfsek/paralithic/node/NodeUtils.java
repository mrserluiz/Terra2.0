package com.dfsek.paralithic.node;

import com.dfsek.terra.lib.asm.MethodVisitor;

public class NodeUtils {
   public static Node simplify(Node in) {
      return in instanceof Simplifiable ? ((Simplifiable)in).simplify() : in;
   }

   public static boolean isInt(Class<?> clazz) {
      return int.class.equals(clazz);
   }

   public static boolean isDouble(Class<?> clazz) {
      return double.class.equals(clazz);
   }

   public static boolean isBoolean(Class<?> clazz) {
      return boolean.class.equals(clazz);
   }

   public static boolean isByte(Class<?> clazz) {
      return byte.class.equals(clazz);
   }

   public static boolean isShort(Class<?> clazz) {
      return short.class.equals(clazz);
   }

   public static boolean isLong(Class<?> clazz) {
      return long.class.equals(clazz);
   }

   public static boolean isChar(Class<?> clazz) {
      return char.class.equals(clazz);
   }

   public static boolean isFloat(Class<?> clazz) {
      return float.class.equals(clazz);
   }

   public static boolean isWeakInteger(Class<?> clazz) {
      return isInt(clazz) || isByte(clazz) || isShort(clazz);
   }

   public static char getDescriptorCharacter(Class<?> clazz) {
      if (isDouble(clazz)) {
         return 'D';
      } else if (isInt(clazz)) {
         return 'I';
      } else if (isShort(clazz)) {
         return 'S';
      } else if (isLong(clazz)) {
         return 'J';
      } else if (isByte(clazz)) {
         return 'B';
      } else if (isBoolean(clazz)) {
         return 'Z';
      } else if (isChar(clazz)) {
         return 'C';
      } else {
         throw new IllegalArgumentException("Not a primitive type: " + clazz);
      }
   }

   public static void siPush(MethodVisitor visitor, int i) {
      switch (i) {
         case -1:
            visitor.visitInsn(2);
            return;
         case 0:
            visitor.visitInsn(3);
            return;
         case 1:
            visitor.visitInsn(4);
            return;
         case 2:
            visitor.visitInsn(5);
            return;
         case 3:
            visitor.visitInsn(6);
            return;
         case 4:
            visitor.visitInsn(7);
            return;
         case 5:
            visitor.visitInsn(8);
            return;
         default:
            visitor.visitIntInsn(17, i);
      }
   }

   public static int getLocalVariableIndex(int index) {
      return 3 + index * 2;
   }
}
