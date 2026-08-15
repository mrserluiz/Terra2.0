package com.dfsek.terra.lib.yaml.snakeyaml.tokens;

import com.dfsek.terra.lib.yaml.snakeyaml.error.Mark;

public final class FlowSequenceEndToken extends Token {
   public FlowSequenceEndToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.FlowSequenceEnd;
   }
}
