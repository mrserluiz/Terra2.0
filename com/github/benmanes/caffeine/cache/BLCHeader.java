package com.github.benmanes.caffeine.cache;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

final class BLCHeader {
   private BLCHeader() {
   }

   abstract static class DrainStatusRef extends BLCHeader.PadDrainStatus {
      static final VarHandle DRAIN_STATUS = findVarHandle(BLCHeader.DrainStatusRef.class, "drainStatus", int.class);
      static final int IDLE = 0;
      static final int REQUIRED = 1;
      static final int PROCESSING_TO_IDLE = 2;
      static final int PROCESSING_TO_REQUIRED = 3;
      volatile int drainStatus = 0;

      boolean shouldDrainBuffers(boolean delayable) {
         switch (this.drainStatusOpaque()) {
            case 0:
               return !delayable;
            case 1:
               return true;
            case 2:
            case 3:
               return false;
            default:
               throw new IllegalStateException("Invalid drain status: " + this.drainStatus);
         }
      }

      int drainStatusOpaque() {
         return (int)DRAIN_STATUS.getOpaque((BLCHeader.DrainStatusRef)this);
      }

      int drainStatusAcquire() {
         return (int)DRAIN_STATUS.getAcquire((BLCHeader.DrainStatusRef)this);
      }

      void setDrainStatusOpaque(int drainStatus) {
         DRAIN_STATUS.setOpaque((BLCHeader.DrainStatusRef)this, (int)drainStatus);
      }

      void setDrainStatusRelease(int drainStatus) {
         DRAIN_STATUS.setRelease((BLCHeader.DrainStatusRef)this, (int)drainStatus);
      }

      boolean casDrainStatus(int expect, int update) {
         return DRAIN_STATUS.compareAndSet((BLCHeader.DrainStatusRef)this, (int)expect, (int)update);
      }

      static VarHandle findVarHandle(Class<?> recv, String name, Class<?> type) {
         try {
            return MethodHandles.lookup().findVarHandle(recv, name, type);
         } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
         }
      }
   }

   static class PadDrainStatus {
      byte p000;
      byte p001;
      byte p002;
      byte p003;
      byte p004;
      byte p005;
      byte p006;
      byte p007;
      byte p008;
      byte p009;
      byte p010;
      byte p011;
      byte p012;
      byte p013;
      byte p014;
      byte p015;
      byte p016;
      byte p017;
      byte p018;
      byte p019;
      byte p020;
      byte p021;
      byte p022;
      byte p023;
      byte p024;
      byte p025;
      byte p026;
      byte p027;
      byte p028;
      byte p029;
      byte p030;
      byte p031;
      byte p032;
      byte p033;
      byte p034;
      byte p035;
      byte p036;
      byte p037;
      byte p038;
      byte p039;
      byte p040;
      byte p041;
      byte p042;
      byte p043;
      byte p044;
      byte p045;
      byte p046;
      byte p047;
      byte p048;
      byte p049;
      byte p050;
      byte p051;
      byte p052;
      byte p053;
      byte p054;
      byte p055;
      byte p056;
      byte p057;
      byte p058;
      byte p059;
      byte p060;
      byte p061;
      byte p062;
      byte p063;
      byte p064;
      byte p065;
      byte p066;
      byte p067;
      byte p068;
      byte p069;
      byte p070;
      byte p071;
      byte p072;
      byte p073;
      byte p074;
      byte p075;
      byte p076;
      byte p077;
      byte p078;
      byte p079;
      byte p080;
      byte p081;
      byte p082;
      byte p083;
      byte p084;
      byte p085;
      byte p086;
      byte p087;
      byte p088;
      byte p089;
      byte p090;
      byte p091;
      byte p092;
      byte p093;
      byte p094;
      byte p095;
      byte p096;
      byte p097;
      byte p098;
      byte p099;
      byte p100;
      byte p101;
      byte p102;
      byte p103;
      byte p104;
      byte p105;
      byte p106;
      byte p107;
      byte p108;
      byte p109;
      byte p110;
      byte p111;
      byte p112;
      byte p113;
      byte p114;
      byte p115;
      byte p116;
      byte p117;
      byte p118;
      byte p119;
   }
}
