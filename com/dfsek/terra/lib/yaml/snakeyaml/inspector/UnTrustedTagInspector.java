package com.dfsek.terra.lib.yaml.snakeyaml.inspector;

import com.dfsek.terra.lib.yaml.snakeyaml.nodes.Tag;

public final class UnTrustedTagInspector implements TagInspector {
   @Override
   public boolean isGlobalTagAllowed(Tag tag) {
      return false;
   }
}
