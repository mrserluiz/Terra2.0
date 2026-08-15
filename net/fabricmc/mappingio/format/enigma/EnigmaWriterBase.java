package net.fabricmc.mappingio.format.enigma;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingWriter;
import org.jetbrains.annotations.Nullable;

abstract class EnigmaWriterBase implements MappingWriter {
   protected static final Set<MappingFlag> flags = EnumSet.of(
      MappingFlag.NEEDS_ELEMENT_UNIQUENESS, MappingFlag.NEEDS_SRC_FIELD_DESC, MappingFlag.NEEDS_SRC_METHOD_DESC
   );
   protected static final String toEscape = "\\\n\r\u0000\t";
   protected static final String escaped = "\\nr0t";
   protected Writer writer;
   protected int indent;
   protected String srcClassName;
   protected String currentClass;
   protected String lastWrittenClass = "";
   protected String dstName;
   protected String[] dstNames;
   protected String desc;

   EnigmaWriterBase(Writer writer) throws IOException {
      this.writer = writer;
   }

   @Override
   public void close() throws IOException {
      this.writer.close();
   }

   @Override
   public Set<MappingFlag> getFlags() {
      return flags;
   }

   @Override
   public boolean visitHeader() throws IOException {
      return false;
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) {
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.srcClassName = srcName;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.writeIndent(0);
      this.writer.write("FIELD ");
      this.writer.write(srcName);
      this.desc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.writeIndent(0);
      this.writer.write("METHOD ");
      this.writer.write(srcName);
      this.desc = srcDesc;
      return true;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      this.writeIndent(1);
      this.writer.write("ARG ");
      this.writer.write(Integer.toString(lvIndex));
      return true;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) {
      return false;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
      if (namespace == 0) {
         if (targetKind == MappedElementKind.CLASS) {
            this.dstName = name;
         } else {
            this.writer.write(32);
            this.writer.write(name);
         }
      }
   }

   @Override
   public abstract boolean visitElementContent(MappedElementKind var1) throws IOException;

   protected static int getNextOuterEnd(String name, int startPos) {
      int pos;
      while ((pos = name.indexOf(36, startPos + 1)) > 0) {
         if (name.charAt(pos - 1) != '/') {
            return pos;
         }

         startPos = pos + 1;
      }

      return -1;
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
      int start = 0;

      while (start < comment.length()) {
         int pos = comment.indexOf(10, start);
         int end = pos >= 0 ? pos : comment.length();
         this.writeIndent(targetKind.level);
         this.writer.write("COMMENT");
         if (end > start) {
            this.writer.write(32);

            for (int i = start; i < end; i++) {
               char c = comment.charAt(i);
               int idx = "\\\n\r\u0000\t".indexOf(c);
               if (idx >= 0) {
                  if (i > start) {
                     this.writer.write(comment, start, i - start);
                  }

                  this.writer.write(92);
                  this.writer.write("\\nr0t".charAt(idx));
                  start = i + 1;
               }
            }

            if (start < end) {
               this.writer.write(comment, start, end - start);
            }
         }

         this.writer.write(10);
         start = end + 1;
         if (pos < 0) {
            break;
         }
      }
   }

   protected void writeMismatchedOrMissingClasses() throws IOException {
      this.indent = 0;
      int srcStart = 0;

      do {
         int srcEnd = getNextOuterEnd(this.srcClassName, srcStart);
         if (srcEnd < 0) {
            srcEnd = this.srcClassName.length();
         }

         int srcLen = srcEnd - srcStart;
         if (!this.lastWrittenClass.regionMatches(srcStart, this.srcClassName, srcStart, srcLen)
            || srcEnd < this.lastWrittenClass.length() && this.lastWrittenClass.charAt(srcEnd) != '$') {
            this.writeIndent(0);
            this.writer.write("CLASS ");
            this.writer.write(this.srcClassName, srcStart, srcLen);
            if (this.dstName != null) {
               int dstStart = 0;

               for (int i = 0; i < this.indent; i++) {
                  dstStart = getNextOuterEnd(this.dstName, dstStart);
                  if (dstStart < 0) {
                     break;
                  }

                  dstStart++;
               }

               if (dstStart >= 0) {
                  int dstEnd = getNextOuterEnd(this.dstName, dstStart);
                  if (dstEnd < 0) {
                     dstEnd = this.dstName.length();
                  }

                  int dstLen = dstEnd - dstStart;
                  if (dstLen != srcLen || !this.srcClassName.regionMatches(srcStart, this.dstName, dstStart, srcLen)) {
                     this.writer.write(32);
                     this.writer.write(this.dstName, dstStart, dstLen);
                  }
               }
            }

            this.writer.write(10);
         }

         this.indent++;
         srcStart = srcEnd + 1;
      } while (srcStart < this.srcClassName.length());

      this.lastWrittenClass = this.srcClassName;
      this.dstName = null;
   }

   protected void writeIndent(int extra) throws IOException {
      for (int i = 0; i < this.indent + extra; i++) {
         this.writer.write(9);
      }
   }
}
