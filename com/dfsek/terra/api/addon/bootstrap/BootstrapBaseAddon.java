package com.dfsek.terra.api.addon.bootstrap;

import com.dfsek.terra.api.addon.BaseAddon;
import java.nio.file.Path;

public interface BootstrapBaseAddon<T extends BaseAddon> extends BaseAddon {
   Iterable<T> loadAddons(Path var1, BootstrapAddonClassLoader var2);
}
