package xyz.jpenilla.reflectionremapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.mappingio.tree.MappingTree;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import xyz.jpenilla.reflectionremapper.internal.util.StringPool;
import xyz.jpenilla.reflectionremapper.internal.util.Util;

@DefaultQualifier(NonNull.class)
final class ReflectionRemapperImpl implements ReflectionRemapper {
   private final Map<String, ReflectionRemapperImpl.ClassMapping> mappingsByObf;
   private final Map<String, ReflectionRemapperImpl.ClassMapping> mappingsByDeobf;

   private ReflectionRemapperImpl(final Set<ReflectionRemapperImpl.ClassMapping> mappings) {
      this.mappingsByObf = Collections.unmodifiableMap(
         mappings.stream().collect(Collectors.toMap(ReflectionRemapperImpl.ClassMapping::obfName, Function.identity()))
      );
      this.mappingsByDeobf = Collections.unmodifiableMap(
         mappings.stream().collect(Collectors.toMap(ReflectionRemapperImpl.ClassMapping::deobfName, Function.identity()))
      );
   }

   @Override
   public String remapClassName(final String className) {
      ReflectionRemapperImpl.ClassMapping map = this.mappingsByDeobf.get(className);
      return map == null ? className : map.obfName();
   }

   @Override
   public String remapFieldName(final Class<?> holdingClass, final String fieldName) {
      ReflectionRemapperImpl.ClassMapping clsMap = this.mappingsByObf.get(holdingClass.getName());
      return clsMap == null ? fieldName : clsMap.fieldsDeobfToObf().getOrDefault(fieldName, fieldName);
   }

   @Override
   public String remapMethodName(final Class<?> holdingClass, final String methodName, final Class<?>... paramTypes) {
      ReflectionRemapperImpl.ClassMapping clsMap = this.mappingsByObf.get(holdingClass.getName());
      return clsMap == null ? methodName : clsMap.methods().getOrDefault(methodKey(methodName, paramTypes), methodName);
   }

   private static String methodKey(final String deobfName, final Class<?>... paramTypes) {
      return deobfName + paramsDescriptor(paramTypes);
   }

   private static String methodKey(final String deobfName, final String obfMethodDesc) {
      return deobfName + paramsDescFromMethodDesc(obfMethodDesc);
   }

   private static String paramsDescriptor(final Class<?>... params) {
      StringBuilder builder = new StringBuilder();

      for (Class<?> param : params) {
         builder.append(Util.descriptorString(param));
      }

      return builder.toString();
   }

   private static String paramsDescFromMethodDesc(final String methodDescriptor) {
      String ret = methodDescriptor.substring(1);
      return ret.substring(0, ret.indexOf(")"));
   }

   static ReflectionRemapperImpl fromMappingTree(final MappingTree tree, final String fromNamespace, final String toNamespace) {
      StringPool pool = new StringPool();
      Set<ReflectionRemapperImpl.ClassMapping> mappings = new HashSet<>();

      for (MappingTree.ClassMapping cls : tree.getClasses()) {
         Map<String, String> fields = new HashMap<>();

         for (MappingTree.FieldMapping field : cls.getFields()) {
            fields.put(pool.string(Objects.requireNonNull(field.getName(fromNamespace))), pool.string(Objects.requireNonNull(field.getName(toNamespace))));
         }

         Map<String, String> methods = new HashMap<>();

         for (MappingTree.MethodMapping method : cls.getMethods()) {
            methods.put(
               pool.string(methodKey(Objects.requireNonNull(method.getName(fromNamespace)), Objects.requireNonNull(method.getDesc(toNamespace)))),
               pool.string(Objects.requireNonNull(method.getName(toNamespace)))
            );
         }

         ReflectionRemapperImpl.ClassMapping map = new ReflectionRemapperImpl.ClassMapping(
            Objects.requireNonNull(cls.getName(toNamespace)).replace('/', '.'),
            Objects.requireNonNull(cls.getName(fromNamespace)).replace('/', '.'),
            Collections.unmodifiableMap(fields),
            Collections.unmodifiableMap(methods)
         );
         mappings.add(map);
      }

      return new ReflectionRemapperImpl(mappings);
   }

   private static final class ClassMapping {
      private final String obfName;
      private final String deobfName;
      private final Map<String, String> fieldsDeobfToObf;
      private final Map<String, String> methods;

      private ClassMapping(final String obfName, final String deobfName, final Map<String, String> fieldsDeobfToObf, final Map<String, String> methods) {
         this.obfName = obfName;
         this.deobfName = deobfName;
         this.fieldsDeobfToObf = fieldsDeobfToObf;
         this.methods = methods;
      }

      public String obfName() {
         return this.obfName;
      }

      public String deobfName() {
         return this.deobfName;
      }

      public Map<String, String> fieldsDeobfToObf() {
         return this.fieldsDeobfToObf;
      }

      public Map<String, String> methods() {
         return this.methods;
      }

      @Override
      public boolean equals(final Object obj) {
         if (obj == this) {
            return true;
         } else if (obj != null && obj.getClass() == this.getClass()) {
            ReflectionRemapperImpl.ClassMapping that = (ReflectionRemapperImpl.ClassMapping)obj;
            return Objects.equals(this.obfName, that.obfName)
               && Objects.equals(this.deobfName, that.deobfName)
               && Objects.equals(this.fieldsDeobfToObf, that.fieldsDeobfToObf)
               && Objects.equals(this.methods, that.methods);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.obfName, this.deobfName, this.fieldsDeobfToObf, this.methods);
      }

      @Override
      public String toString() {
         return "ClassMapping[obfName="
            + this.obfName
            + ", deobfName="
            + this.deobfName
            + ", fieldsDeobfToObf="
            + this.fieldsDeobfToObf
            + ", methods="
            + this.methods
            + ']';
      }
   }
}
