package com.dfsek.terra.config.pack;

import com.dfsek.paralithic.eval.parser.Parser;
import com.dfsek.tectonic.api.config.template.ConfigTemplate;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;

public class ConfigPackExpressionOptionsTemplate implements ConfigTemplate {
   @Value("expressions.options")
   @Default
   private Parser.ParseOptions parseOptions = new Parser.ParseOptions();

   public Parser.ParseOptions getParseOptions() {
      return this.parseOptions;
   }
}
