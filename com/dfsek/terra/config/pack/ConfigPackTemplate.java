package com.dfsek.terra.config.pack;

import ca.solostudios.strata.version.Version;
import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.terra.api.config.meta.Meta;
import com.dfsek.terra.api.world.chunk.generation.stage.GenerationStage;
import com.dfsek.terra.api.world.chunk.generation.util.provider.ChunkGeneratorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigPackTemplate implements ConfigTemplate {
   @Value("id")
   private String id;
   @Value("variables")
   @Default
   private @Meta Map<String, @Meta Double> variables = new HashMap<>();
   @Value("beta.carving")
   @Default
   private @Meta boolean betaCarvers = false;
   @Value("structures.locatable")
   @Default
   private @Meta Map<@Meta String, @Meta String> locatable = new HashMap<>();
   @Value("blend.terrain.elevation")
   @Default
   private @Meta int elevationBlend = 4;
   @Value("vanilla.mobs")
   @Default
   private @Meta boolean vanillaMobs = true;
   @Value("vanilla.caves")
   @Default
   private @Meta boolean vanillaCaves = false;
   @Value("vanilla.decorations")
   @Default
   private @Meta boolean vanillaDecorations = false;
   @Value("vanilla.structures")
   @Default
   private @Meta boolean vanillaStructures = false;
   @Value("author")
   @Default
   private String author = "Anon Y. Mous";
   @Value("disable.sapling")
   @Default
   private @Meta boolean disableSaplings = false;
   @Value("stages")
   @Default
   private @Meta List<@Meta GenerationStage> stages = Collections.emptyList();
   @Value("version")
   private Version version;
   @Value("disable.carvers")
   @Default
   private @Meta boolean disableCarvers = false;
   @Value("disable.structures")
   @Default
   private @Meta boolean disableStructures = false;
   @Value("disable.ores")
   @Default
   private @Meta boolean disableOres = false;
   @Value("disable.trees")
   @Default
   private @Meta boolean disableTrees = false;
   @Value("disable.flora")
   @Default
   private @Meta boolean disableFlora = false;
   @Value("generator")
   private @Meta ChunkGeneratorProvider generatorProvider;
   @Value("cache.biome.enable")
   @Default
   private boolean biomeCache = false;

   public boolean disableCarvers() {
      return this.disableCarvers;
   }

   public boolean disableFlora() {
      return this.disableFlora;
   }

   public boolean disableOres() {
      return this.disableOres;
   }

   public boolean disableStructures() {
      return this.disableStructures;
   }

   public boolean disableTrees() {
      return this.disableTrees;
   }

   public boolean vanillaMobs() {
      return this.vanillaMobs;
   }

   public boolean vanillaCaves() {
      return this.vanillaCaves;
   }

   public boolean vanillaDecorations() {
      return this.vanillaDecorations;
   }

   public boolean vanillaStructures() {
      return this.vanillaStructures;
   }

   public boolean doBetaCarvers() {
      return this.betaCarvers;
   }

   public ChunkGeneratorProvider getGeneratorProvider() {
      return this.generatorProvider;
   }

   public List<GenerationStage> getStages() {
      return this.stages;
   }

   public Version getVersion() {
      return this.version;
   }

   public boolean isDisableSaplings() {
      return this.disableSaplings;
   }

   public String getID() {
      return this.id;
   }

   public String getAuthor() {
      return this.author;
   }

   public Map<String, Double> getVariables() {
      return this.variables;
   }

   public int getElevationBlend() {
      return this.elevationBlend;
   }

   public Map<String, String> getLocatable() {
      return this.locatable;
   }

   public boolean getBiomeCache() {
      return this.biomeCache;
   }
}
