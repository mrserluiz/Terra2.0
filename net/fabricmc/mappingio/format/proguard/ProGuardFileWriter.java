package net.fabricmc.mappingio.format.proguard;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingWriter;
import org.jetbrains.annotations.Nullable;

public final class ProGuardFileWriter implements MappingWriter {
   private final Writer writer;
   private final String dstNamespaceString;
   private int dstNamespace = -1;
   private String clsSrcName;
   private String memberSrcName;
   private String memberSrcDesc;
   private String dstName;
   private boolean classContentVisitPending;

   public ProGuardFileWriter(Writer writer) {
      this(writer, 0);
   }

   public ProGuardFileWriter(Writer writer, int dstNamespace) {
      this.writer = Objects.requireNonNull(writer, "writer cannot be null");
      this.dstNamespace = dstNamespace;
      this.dstNamespaceString = null;
      if (dstNamespace < 0) {
         throw new IllegalArgumentException("Namespace must be non-negative, found " + dstNamespace);
      }
   }

   public ProGuardFileWriter(Writer writer, String dstNamespace) {
      this.writer = Objects.requireNonNull(writer, "writer cannot be null");
      this.dstNamespaceString = Objects.requireNonNull(dstNamespace, "namespace cannot be null");
   }

   @Override
   public void close() throws IOException {
      this.writer.close();
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      if (this.dstNamespaceString != null) {
         this.dstNamespace = dstNamespaces.indexOf(this.dstNamespaceString);
         if (this.dstNamespace == -1) {
            throw new RuntimeException("Invalid destination namespace '" + this.dstNamespaceString + "' not in [" + String.join(", ", dstNamespaces) + ']');
         }
      }

      if (this.dstNamespace >= dstNamespaces.size()) {
         throw new IndexOutOfBoundsException("Namespace " + this.dstNamespace + " doesn't exist in [" + String.join(", ", dstNamespaces) + ']');
      }
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.clsSrcName = srcName;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.memberSrcName = srcName;
      this.memberSrcDesc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
      if (this.dstNamespace == namespace) {
         this.dstName = name;
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (targetKind == MappedElementKind.CLASS) {
         if (this.dstName == null) {
            this.classContentVisitPending = true;
            return true;
         }
      } else {
         if (this.dstName == null) {
            return false;
         }

         if (this.classContentVisitPending) {
            String memberDstName = this.dstName;
            this.dstName = this.clsSrcName;
            this.visitElementContent(MappedElementKind.CLASS);
            this.classContentVisitPending = false;
            this.dstName = memberDstName;
         }
      }

      switch (targetKind) {
         case CLASS:
            this.writer.write(toJavaClassName(this.clsSrcName));
            this.dstName = toJavaClassName(this.dstName) + ":";
            break;
         case FIELD:
            this.writeIndent();
            this.writer.write(toJavaType(this.memberSrcDesc));
            this.writer.write(32);
            this.writer.write(this.memberSrcName);
            break;
         case METHOD:
            this.writeIndent();
            this.writer.write(toJavaType(this.memberSrcDesc.substring(this.memberSrcDesc.indexOf(41, 1) + 1)));
            this.writer.write(32);
            this.writer.write(this.memberSrcName);
            this.writer.write(40);
            List<String> argTypes = this.extractArgumentTypes(this.memberSrcDesc);

            for (int i = 0; i < argTypes.size(); i++) {
               if (i > 0) {
                  this.writer.write(44);
               }

               this.writer.write(argTypes.get(i));
            }

            this.writer.write(41);
            break;
         default:
            throw new IllegalStateException("unexpected invocation for " + targetKind);
      }

      this.writeArrow();
      this.writer.write(this.dstName);
      this.writer.write(10);
      this.dstName = null;
      return targetKind == MappedElementKind.CLASS;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
   }

   private void writeArrow() throws IOException {
      this.writer.write(" -> ");
   }

   private void writeIndent() throws IOException {
      this.writer.write("    ");
   }

   private static String toJavaClassName(String name) {
      return name.replace('/', '.');
   }

   private static String toJavaType(String descriptor) {
      StringBuilder result = new StringBuilder();
      int arrayLevel = 0;

      for (int i = 0; i < descriptor.length(); i++) {
         switch (descriptor.charAt(i)) {
            case 'B':
               result.append("byte");
               break;
            case 'C':
               result.append("char");
               break;
            case 'D':
               result.append("double");
               break;
            case 'E':
            case 'G':
            case 'H':
            case 'K':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'T':
            case 'U':
            case 'W':
            case 'X':
            case 'Y':
            default:
               throw new IllegalArgumentException("Unknown character in descriptor: " + descriptor.charAt(i));
            case 'F':
               result.append("float");
               break;
            case 'I':
               result.append("int");
               break;
            case 'J':
               result.append("long");
               break;
            case 'L':
               while (i + 1 < descriptor.length()) {
                  char c = descriptor.charAt(++i);
                  if (c == '/') {
                     result.append('.');
                  } else {
                     if (c == ';') {
                        break;
                     }

                     result.append(c);
                  }
               }
               break;
            case 'S':
               result.append("short");
               break;
            case 'V':
               result.append("void");
               break;
            case 'Z':
               result.append("boolean");
               break;
            case '[':
               arrayLevel++;
         }
      }

      while (arrayLevel > 0) {
         result.append("[]");
         arrayLevel--;
      }

      return result.toString();
   }

   private List<String> extractArgumentTypes(String desc) {
      List<String> argTypes = new ArrayList<>();
      int index = 1;

      while (desc.charAt(index) != ')') {
         int start = index;

         while (desc.charAt(index) == '[') {
            index++;
         }

         if (desc.charAt(index) == 'L') {
            index = desc.indexOf(59, index) + 1;
         } else {
            index++;
         }

         argTypes.add(toJavaType(desc.substring(start, index)));
      }

      return argTypes;
   }
}
