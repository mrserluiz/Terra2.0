package com.dfsek.terra.lib.commons.text.diff;

public class StringsComparator {
   private final String left;
   private final String right;
   private final int[] vDown;
   private final int[] vUp;

   public StringsComparator(String left, String right) {
      this.left = left;
      this.right = right;
      int size = left.length() + right.length() + 2;
      this.vDown = new int[size];
      this.vUp = new int[size];
   }

   private void buildScript(int start1, int end1, int start2, int end2, EditScript<Character> script) {
      StringsComparator.Snake middle = this.getMiddleSnake(start1, end1, start2, end2);
      if (middle == null || middle.getStart() == end1 && middle.getDiag() == end1 - end2 || middle.getEnd() == start1 && middle.getDiag() == start1 - start2) {
         int i = start1;
         int j = start2;

         while (i < end1 || j < end2) {
            if (i < end1 && j < end2 && this.left.charAt(i) == this.right.charAt(j)) {
               script.append(new KeepCommand<>(this.left.charAt(i)));
               i++;
               j++;
            } else if (end1 - start1 > end2 - start2) {
               script.append(new DeleteCommand<>(this.left.charAt(i)));
               i++;
            } else {
               script.append(new InsertCommand<>(this.right.charAt(j)));
               j++;
            }
         }
      } else {
         this.buildScript(start1, middle.getStart(), start2, middle.getStart() - middle.getDiag(), script);

         for (int i = middle.getStart(); i < middle.getEnd(); i++) {
            script.append(new KeepCommand<>(this.left.charAt(i)));
         }

         this.buildScript(middle.getEnd(), end1, middle.getEnd() - middle.getDiag(), end2, script);
      }
   }

   private StringsComparator.Snake buildSnake(int start, int diag, int end1, int end2) {
      int end = start;

      while (end - diag < end2 && end < end1 && this.left.charAt(end) == this.right.charAt(end - diag)) {
         end++;
      }

      return new StringsComparator.Snake(start, end, diag);
   }

   private StringsComparator.Snake getMiddleSnake(int start1, int end1, int start2, int end2) {
      int m = end1 - start1;
      int n = end2 - start2;
      if (m != 0 && n != 0) {
         int delta = m - n;
         int sum = n + m;
         int offset = (sum % 2 == 0 ? sum : sum + 1) / 2;
         this.vDown[1 + offset] = start1;
         this.vUp[1 + offset] = end1 + 1;

         for (int d = 0; d <= offset; d++) {
            for (int k = -d; k <= d; k += 2) {
               int i = k + offset;
               if (k != -d && (k == d || this.vDown[i - 1] >= this.vDown[i + 1])) {
                  this.vDown[i] = this.vDown[i - 1] + 1;
               } else {
                  this.vDown[i] = this.vDown[i + 1];
               }

               int x = this.vDown[i];

               for (int y = x - start1 + start2 - k; x < end1 && y < end2 && this.left.charAt(x) == this.right.charAt(y); y++) {
                  this.vDown[i] = ++x;
               }

               if (delta % 2 != 0 && delta - d <= k && k <= delta + d && this.vUp[i - delta] <= this.vDown[i]) {
                  return this.buildSnake(this.vUp[i - delta], k + start1 - start2, end1, end2);
               }
            }

            for (int k = delta - d; k <= delta + d; k += 2) {
               int i = k + offset - delta;
               if (k != delta - d && (k == delta + d || this.vUp[i + 1] > this.vUp[i - 1])) {
                  this.vUp[i] = this.vUp[i - 1];
               } else {
                  this.vUp[i] = this.vUp[i + 1] - 1;
               }

               int x = this.vUp[i] - 1;

               for (int y = x - start1 + start2 - k; x >= start1 && y >= start2 && this.left.charAt(x) == this.right.charAt(y); y--) {
                  this.vUp[i] = x--;
               }

               if (delta % 2 == 0 && -d <= k && k <= d && this.vUp[i] <= this.vDown[i + delta]) {
                  return this.buildSnake(this.vUp[i], k + start1 - start2, end1, end2);
               }
            }
         }

         throw new IllegalStateException("Internal Error");
      } else {
         return null;
      }
   }

   public EditScript<Character> getScript() {
      EditScript<Character> script = new EditScript<>();
      this.buildScript(0, this.left.length(), 0, this.right.length(), script);
      return script;
   }

   private static final class Snake {
      private final int start;
      private final int end;
      private final int diag;

      Snake(int start, int end, int diag) {
         this.start = start;
         this.end = end;
         this.diag = diag;
      }

      public int getDiag() {
         return this.diag;
      }

      public int getEnd() {
         return this.end;
      }

      public int getStart() {
         return this.start;
      }
   }
}
