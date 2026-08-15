package com.dfsek.terra.lib.yaml.snakeyaml.scanner;

import com.dfsek.terra.lib.yaml.snakeyaml.tokens.Token;

public interface Scanner {
   boolean checkToken(Token.ID... var1);

   Token peekToken();

   Token getToken();

   void resetDocumentIndex();
}
