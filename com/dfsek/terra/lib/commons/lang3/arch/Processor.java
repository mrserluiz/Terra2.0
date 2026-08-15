package com.dfsek.terra.lib.commons.lang3.arch;

public class Processor {
   private final Processor.Arch arch;
   private final Processor.Type type;

   public Processor(Processor.Arch arch, Processor.Type type) {
      this.arch = arch;
      this.type = type;
   }

   public Processor.Arch getArch() {
      return this.arch;
   }

   public Processor.Type getType() {
      return this.type;
   }

   public boolean is32Bit() {
      return Processor.Arch.BIT_32 == this.arch;
   }

   public boolean is64Bit() {
      return Processor.Arch.BIT_64 == this.arch;
   }

   public boolean isAarch64() {
      return Processor.Type.AARCH_64 == this.type;
   }

   public boolean isIA64() {
      return Processor.Type.IA_64 == this.type;
   }

   public boolean isPPC() {
      return Processor.Type.PPC == this.type;
   }

   public boolean isRISCV() {
      return Processor.Type.RISC_V == this.type;
   }

   public boolean isX86() {
      return Processor.Type.X86 == this.type;
   }

   @Override
   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.type.getLabel()).append(' ').append(this.arch.getLabel());
      return builder.toString();
   }

   public enum Arch {
      BIT_32("32-bit"),
      BIT_64("64-bit"),
      UNKNOWN("Unknown");

      private final String label;

      Arch(String label) {
         this.label = label;
      }

      public String getLabel() {
         return this.label;
      }
   }

   public enum Type {
      AARCH_64("AArch64"),
      X86("x86"),
      IA_64("IA-64"),
      PPC("PPC"),
      RISC_V("RISC-V"),
      UNKNOWN("Unknown");

      private final String label;

      Type(String label) {
         this.label = label;
      }

      public String getLabel() {
         return this.label;
      }
   }
}
