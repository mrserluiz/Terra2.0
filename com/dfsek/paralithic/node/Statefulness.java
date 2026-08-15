package com.dfsek.paralithic.node;

public enum Statefulness {
   STATELESS(0),
   CONTEXTUAL(1),
   STATEFUL(2);

   private final int rank;

   Statefulness(int rank) {
      this.rank = rank;
   }

   public static Statefulness combine(Statefulness... in) {
      Statefulness run = null;

      for (Statefulness test : in) {
         if (run == null) {
            run = test;
         } else if (test.isMoreStatefulThan(run)) {
            run = test;
         }
      }

      return run;
   }

   public static Statefulness combine(Statefulness state1, Statefulness state2) {
      return state1.isMoreStatefulThan(state2) ? state1 : state2;
   }

   public boolean isMoreStatefulThan(Statefulness test) {
      return this.rank > test.rank;
   }
}
