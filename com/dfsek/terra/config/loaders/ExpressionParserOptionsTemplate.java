package com.dfsek.terra.config.loaders;

import com.dfsek.paralithic.eval.parser.Parser;
import com.dfsek.tectonic.api.config.template.annotations.Default;
import com.dfsek.tectonic.api.config.template.annotations.Value;
import com.dfsek.tectonic.api.config.template.object.ObjectTemplate;

public class ExpressionParserOptionsTemplate implements ObjectTemplate<Parser.ParseOptions> {
   private static final Parser.ParseOptions DEFAULT_PARSE_OPTIONS = new Parser.ParseOptions();
   @Value("use-let-expressions")
   @Default
   private boolean useLetExpressions = DEFAULT_PARSE_OPTIONS.useLetExpressions();

   public Parser.ParseOptions get() {
      return new Parser.ParseOptions(this.useLetExpressions);
   }
}
