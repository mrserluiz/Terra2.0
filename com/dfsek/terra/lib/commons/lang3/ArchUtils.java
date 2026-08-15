package com.dfsek.terra.lib.commons.lang3;

import com.dfsek.terra.lib.commons.lang3.arch.Processor;
import java.util.HashMap;
import java.util.Map;

public class ArchUtils {
   private static final Map<String, Processor> ARCH_TO_PROCESSOR = new HashMap<>();

   private static void addProcessor(String key, Processor processor) {
      if (ARCH_TO_PROCESSOR.containsKey(key)) {
         throw new IllegalStateException("Key " + key + " already exists in processor map");
      }

      ARCH_TO_PROCESSOR.put(key, processor);
   }

   private static void addProcessors(Processor processor, String... keys) {
      com.dfsek.terra.lib.commons.lang3.stream.Streams.of(keys).forEach(e -> addProcessor(e, processor));
   }

   public static Processor getProcessor() {
      return getProcessor(SystemProperties.getOsArch());
   }

   public static Processor getProcessor(String value) {
      return ARCH_TO_PROCESSOR.get(value);
   }

   private static void init() {
      init_X86_32Bit();
      init_X86_64Bit();
      init_IA64_32Bit();
      init_IA64_64Bit();
      init_PPC_32Bit();
      init_PPC_64Bit();
      init_Aarch_64Bit();
      init_RISCV_32Bit();
      init_RISCV_64Bit();
   }

   private static void init_Aarch_64Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_64, Processor.Type.AARCH_64), "aarch64");
   }

   private static void init_IA64_32Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_32, Processor.Type.IA_64), "ia64_32", "ia64n");
   }

   private static void init_IA64_64Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_64, Processor.Type.IA_64), "ia64", "ia64w");
   }

   private static void init_PPC_32Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_32, Processor.Type.PPC), "ppc", "power", "powerpc", "power_pc", "power_rs");
   }

   private static void init_PPC_64Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_64, Processor.Type.PPC), "ppc64", "power64", "powerpc64", "power_pc64", "power_rs64");
   }

   private static void init_RISCV_32Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_32, Processor.Type.RISC_V), "riscv32");
   }

   private static void init_RISCV_64Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_64, Processor.Type.RISC_V), "riscv64");
   }

   private static void init_X86_32Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_32, Processor.Type.X86), "x86", "i386", "i486", "i586", "i686", "pentium");
   }

   private static void init_X86_64Bit() {
      addProcessors(new Processor(Processor.Arch.BIT_64, Processor.Type.X86), "x86_64", "amd64", "em64t", "universal");
   }

   static {
      init();
   }
}
