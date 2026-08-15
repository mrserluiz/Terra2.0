package com.dfsek.terra.lib.commons.io.monitor;

import java.io.File;

public interface FileAlterationListener {
   void onDirectoryChange(File var1);

   void onDirectoryCreate(File var1);

   void onDirectoryDelete(File var1);

   void onFileChange(File var1);

   void onFileCreate(File var1);

   void onFileDelete(File var1);

   void onStart(FileAlterationObserver var1);

   void onStop(FileAlterationObserver var1);
}
