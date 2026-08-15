package com.dfsek.paralithic.eval;

import com.dfsek.paralithic.Expression;
import com.dfsek.paralithic.functions.dynamic.Context;
import com.dfsek.paralithic.functions.dynamic.DynamicFunction;
import com.dfsek.paralithic.node.Node;
import com.dfsek.paralithic.node.NodeUtils;
import com.dfsek.paralithic.util.DynamicClassLoader;
import com.dfsek.terra.lib.asm.ClassWriter;
import com.dfsek.terra.lib.asm.MethodVisitor;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class ExpressionBuilder {
   private static long builds = 0L;
   private static final boolean DUMP = "true".equals(System.getProperty("paralithic.debug.dump"));
   public static final String EXPRESSION_CLASS_NAME = dynamicName(Expression.class);
   public static final String DYNAMIC_FUNCTION_CLASS_NAME = dynamicName(DynamicFunction.class);
   public static final String CONTEXT_CLASS_NAME = dynamicName(Context.class);
   public static final String OBJECT_CLASS_NAME = dynamicName(Object.class);
   private final Map<String, DynamicFunction> functions;

   public ExpressionBuilder(Map<String, DynamicFunction> functions) {
      this.functions = functions;
   }

   public Expression get(Node op) {
      String implementationClassName = EXPRESSION_CLASS_NAME + "IMPL_" + builds;
      ClassWriter writer = new ClassWriter(3);
      this.functions.forEach((id, function) -> writer.visitField(1, id, "L" + DYNAMIC_FUNCTION_CLASS_NAME + ";", null, null));
      writer.visit(52, 1, implementationClassName, null, OBJECT_CLASS_NAME, new String[]{EXPRESSION_CLASS_NAME});
      MethodVisitor constructor = writer.visitMethod(1, "<init>", "()V", null, null);
      constructor.visitCode();
      constructor.visitVarInsn(25, 0);
      constructor.visitMethodInsn(183, OBJECT_CLASS_NAME, "<init>", "()V", false);
      constructor.visitInsn(177);
      constructor.visitMaxs(0, 0);
      MethodVisitor absMethod = writer.visitMethod(1, "evaluate", "(L" + CONTEXT_CLASS_NAME + ";[D)D", null, null);
      absMethod.visitCode();
      Node node = NodeUtils.simplify(op);
      node.apply(absMethod, implementationClassName);
      absMethod.visitInsn(175);
      absMethod.visitMaxs(0, 0);
      DynamicClassLoader loader = new DynamicClassLoader();
      byte[] bytes = writer.toByteArray();
      Class<?> clazz = loader.defineClass(implementationClassName.replace('/', '.'), writer.toByteArray());
      if (DUMP) {
         File dump = new File("./.paralithic/out/classes/ExpressionIMPL_" + builds + ".class");
         dump.getParentFile().mkdirs();
         System.out.println("Dumping class " + clazz.getCanonicalName() + "to " + dump.getAbsolutePath());

         try (FileOutputStream out = new FileOutputStream(dump)) {
            out.write(bytes);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      builds++;

      try {
         Object instance = clazz.getDeclaredConstructor().newInstance();

         for (Entry<String, DynamicFunction> entry : this.functions.entrySet()) {
            clazz.getDeclaredField(entry.getKey()).set(instance, entry.getValue());
         }

         return (Expression)instance;
      } catch (ReflectiveOperationException e) {
         throw new Error(e);
      }
   }

   public static String dynamicName(Class<?> clazz) {
      return clazz.getCanonicalName().replace('.', '/');
   }
}
