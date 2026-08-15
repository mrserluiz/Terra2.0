package com.dfsek.terra.config;

import ca.solostudios.strata.version.Version;
import ca.solostudios.strata.version.VersionRange;
import com.dfsek.paralithic.eval.parser.Parser;
import com.dfsek.tectonic.api.TypeRegistry;
import com.dfsek.terra.api.Platform;
import com.dfsek.terra.api.addon.BaseAddon;
import com.dfsek.terra.api.block.BlockType;
import com.dfsek.terra.api.block.state.BlockState;
import com.dfsek.terra.api.tectonic.LoaderRegistrar;
import com.dfsek.terra.api.util.Range;
import com.dfsek.terra.api.util.collection.MaterialSet;
import com.dfsek.terra.api.util.collection.ProbabilityCollection;
import com.dfsek.terra.config.loaders.ExpressionParserOptionsTemplate;
import com.dfsek.terra.config.loaders.LinkedHashMapLoader;
import com.dfsek.terra.config.loaders.MaterialSetLoader;
import com.dfsek.terra.config.loaders.ProbabilityCollectionLoader;
import com.dfsek.terra.config.loaders.RangeLoader;
import com.dfsek.terra.config.loaders.VersionLoader;
import com.dfsek.terra.config.loaders.VersionRangeLoader;
import java.util.LinkedHashMap;

public class GenericLoaders implements LoaderRegistrar {
   private final Platform platform;

   public GenericLoaders(Platform platform) {
      this.platform = platform;
   }

   @Override
   public void register(TypeRegistry registry) {
      registry.registerLoader(ProbabilityCollection.class, new ProbabilityCollectionLoader())
         .registerLoader(Range.class, new RangeLoader())
         .registerLoader(Version.class, new VersionLoader())
         .registerLoader(MaterialSet.class, new MaterialSetLoader())
         .registerLoader(VersionRange.class, new VersionRangeLoader())
         .registerLoader(LinkedHashMap.class, new LinkedHashMapLoader())
         .registerLoader(Parser.ParseOptions.class, ExpressionParserOptionsTemplate::new);
      if (this.platform != null) {
         registry.registerLoader(BaseAddon.class, this.platform.getAddons())
            .registerLoader(
               BlockType.class, (type, object, configLoader, depthTracker) -> this.platform.getWorldHandle().createBlockState((String)object).getBlockType()
            )
            .registerLoader(BlockState.class, (type, object, configLoader, depthTracker) -> this.platform.getWorldHandle().createBlockState((String)object));
      }
   }
}
