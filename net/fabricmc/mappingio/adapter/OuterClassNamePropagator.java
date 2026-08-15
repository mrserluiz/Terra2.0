package net.fabricmc.mappingio.adapter;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingUtil;
import net.fabricmc.mappingio.MappingVisitor;
import org.jetbrains.annotations.Nullable;

public class OuterClassNamePropagator extends ForwardingMappingVisitor {
   private static final int collectClassesPass = 1;
   private static final int fixOuterClassesPass = 2;
   private static final int firstEmitPass = 3;
   private final Map<String, String[]> dstNamesBySrcName = new HashMap<>();
   private final Set<String> modifiedClasses = new HashSet<>();
   private int pass = 1;
   private int dstNsCount = -1;
   private String srcName;
   private boolean[] visitedDstName;
   private Map<String, String>[] dstNameBySrcNameByNamespace;

   public OuterClassNamePropagator(MappingVisitor next) {
      super(next);
   }

   @Override
   public Set<MappingFlag> getFlags() {
      Set<MappingFlag> ret = EnumSet.noneOf(MappingFlag.class);
      ret.addAll(this.next.getFlags());
      ret.add(MappingFlag.NEEDS_MULTIPLE_PASSES);
      return ret;
   }

   @Override
   public boolean visitHeader() throws IOException {
      return this.pass < 3 ? true : super.visitHeader();
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      this.dstNsCount = dstNamespaces.size();
      if (this.pass == 1) {
         this.visitedDstName = new boolean[this.dstNsCount];
         this.dstNameBySrcNameByNamespace = new HashMap[this.dstNsCount];
      } else if (this.pass >= 3) {
         super.visitNamespaces(srcNamespace, dstNamespaces);
      }
   }

   @Override
   public void visitMetadata(String key, @Nullable String value) throws IOException {
      if (this.pass >= 3) {
         super.visitMetadata(key, value);
      }
   }

   @Override
   public boolean visitContent() throws IOException {
      return this.pass < 3 ? true : super.visitContent();
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.srcName = srcName;
      if (this.pass == 1) {
         this.dstNamesBySrcName.putIfAbsent(srcName, new String[this.dstNsCount]);
      } else if (this.pass >= 3) {
         super.visitClass(srcName);
      }

      return true;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) throws IOException {
      if (this.pass == 1) {
         if (targetKind != MappedElementKind.CLASS) {
            return;
         }

         this.dstNamesBySrcName.get(this.srcName)[namespace] = name;
      } else if (this.pass >= 3) {
         if (targetKind == MappedElementKind.CLASS) {
            this.visitedDstName[namespace] = true;
            name = this.dstNamesBySrcName.get(this.srcName)[namespace];
         }

         super.visitDstName(targetKind, namespace, name);
      }
   }

   @Override
   public void visitDstDesc(MappedElementKind targetKind, int namespace, String desc) throws IOException {
      if (this.pass >= 3) {
         if (this.modifiedClasses.contains(this.srcName)) {
            Map<String, String> nsDstNameBySrcName = this.dstNameBySrcNameByNamespace[namespace];
            if (nsDstNameBySrcName == null) {
               this.dstNameBySrcNameByNamespace[namespace] = nsDstNameBySrcName = this.dstNamesBySrcName
                  .entrySet()
                  .stream()
                  .filter(entry -> entry.getValue()[namespace] != null)
                  .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()[namespace]), HashMap::putAll);
            }

            desc = MappingUtil.mapDesc(desc, nsDstNameBySrcName);
         }

         super.visitDstDesc(targetKind, namespace, desc);
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (targetKind == MappedElementKind.CLASS && this.pass > 1) {
         String[] dstNames = this.dstNamesBySrcName.get(this.srcName);

         for (int ns = 0; ns < dstNames.length; ns++) {
            String dstName = dstNames[ns];
            if (this.pass == 2) {
               if (dstName == null) {
                  String[] parts = this.srcName.split(Pattern.quote("$"));

                  for (int pos = parts.length - 2; pos >= 0; pos--) {
                     String outerSrcName = String.join("$", Arrays.copyOfRange(parts, 0, pos + 1));
                     String outerDstName = this.dstNamesBySrcName.get(outerSrcName)[ns];
                     if (outerDstName != null) {
                        dstName = outerDstName + "$" + String.join("$", Arrays.copyOfRange(parts, pos + 1, parts.length));
                        dstNames[ns] = dstName;
                        this.modifiedClasses.add(this.srcName);
                        break;
                     }
                  }
               }
            } else if (!this.visitedDstName[ns] && dstName != null) {
               super.visitDstName(targetKind, ns, dstName);
            }
         }
      }

      if (this.pass < 3) {
         return false;
      }

      Arrays.fill(this.visitedDstName, false);
      return super.visitElementContent(targetKind);
   }

   @Override
   public boolean visitEnd() throws IOException {
      return this.pass++ < 3 ? false : super.visitEnd();
   }
}
