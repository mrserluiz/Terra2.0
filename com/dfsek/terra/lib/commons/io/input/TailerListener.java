package com.dfsek.terra.lib.commons.io.input;

public interface TailerListener {
   void fileNotFound();

   void fileRotated();

   void handle(Exception var1);

   void handle(String var1);

   void init(Tailer var1);
}
