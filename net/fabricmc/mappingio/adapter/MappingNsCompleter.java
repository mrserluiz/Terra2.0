package net.fabricmc.mappingio.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingVisitor;
import org.jetbrains.annotations.Nullable;

public final class MappingNsCompleter extends ForwardingMappingVisitor {
   private final Map<String, String> alternatives;
   private final boolean addMissingNs;
   private int[] alternativesMapping;
   private String srcName;
   private String[] dstNames;
   private boolean relayHeaderOrMetadata;

   public MappingNsCompleter(MappingVisitor next, Map<String, String> alternatives) {
      this(next, alternatives, false);
   }

   public MappingNsCompleter(MappingVisitor next, Map<String, String> alternatives, boolean addMissingNs) {
      super(next);
      this.alternatives = alternatives;
      this.addMissingNs = addMissingNs;
   }

   @Override
   public boolean visitHeader() throws IOException {
      this.relayHeaderOrMetadata = this.next.visitHeader();
      return true;
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
      if (this.addMissingNs) {
         boolean copied = false;

         for (String ns : this.alternatives.keySet()) {
            if (!ns.equals(srcNamespace) && !dstNamespaces.contains(ns)) {
               if (!copied) {
                  dstNamespaces = new ArrayList<>(dstNamespaces);
                  copied = true;
               }

               dstNamespaces.add(ns);
            }
         }
      }

      int count = dstNamespaces.size();
      this.alternativesMapping = new int[count];
      this.dstNames = new String[count];

      for (int i = 0; i < count; i++) {
         String src = this.alternatives.get(dstNamespaces.get(i));
         int srcIdx;
         if (src == null) {
            srcIdx = i;
         } else if (src.equals(srcNamespace)) {
            srcIdx = -1;
         } else {
            srcIdx = dstNamespaces.indexOf(src);
            if (srcIdx < 0) {
               throw new RuntimeException("invalid alternative mapping ns " + src + ": not in " + dstNamespaces + " or " + srcNamespace);
            }
         }

         this.alternativesMapping[i] = srcIdx;
      }

      if (this.relayHeaderOrMetadata) {
         this.next.visitNamespaces(srcNamespace, dstNamespaces);
      }
   }

   @Override
   public void visitMetadata(String key, @Nullable String value) throws IOException {
      if (this.relayHeaderOrMetadata) {
         this.next.visitMetadata(key, value);
      }
   }

   @Override
   public boolean visitContent() throws IOException {
      this.relayHeaderOrMetadata = true;
      return this.next.visitContent();
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.srcName = srcName;
      return this.next.visitClass(srcName);
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      this.srcName = srcName;
      return this.next.visitField(srcName, srcDesc);
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      this.srcName = srcName;
      return this.next.visitMethod(srcName, srcDesc);
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      this.srcName = srcName;
      return this.next.visitMethodArg(argPosition, lvIndex, srcName);
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      this.srcName = srcName;
      return this.next.visitMethodVar(lvtRowIndex, lvIndex, startOpIdx, endOpIdx, srcName);
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
      this.dstNames[namespace] = name;
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      label42:
      for (int i = 0; i < this.dstNames.length; i++) {
         String name = this.dstNames[i];
         if (name == null) {
            int src = i;
            long visited = 1L << src;

            do {
               int newSrc = this.alternativesMapping[src];
               if (newSrc < 0) {
                  name = this.srcName;
                  break;
               }

               if (newSrc == src || (visited & 1L << newSrc) != 0L) {
                  continue label42;
               }

               src = newSrc;
               name = this.dstNames[src];
               visited |= 1L << src;
            } while (name == null);

            assert name != null;
         }

         this.next.visitDstName(targetKind, i, name);
      }

      Arrays.fill(this.dstNames, null);
      return this.next.visitElementContent(targetKind);
   }
}
