package com.dfsek.terra.lib.yaml.snakeyaml.env;

import com.dfsek.terra.lib.yaml.snakeyaml.LoaderOptions;
import com.dfsek.terra.lib.yaml.snakeyaml.TypeDescription;
import com.dfsek.terra.lib.yaml.snakeyaml.constructor.AbstractConstruct;
import com.dfsek.terra.lib.yaml.snakeyaml.constructor.Constructor;
import com.dfsek.terra.lib.yaml.snakeyaml.error.MissingEnvironmentVariableException;
import com.dfsek.terra.lib.yaml.snakeyaml.nodes.Node;
import com.dfsek.terra.lib.yaml.snakeyaml.nodes.ScalarNode;
import com.dfsek.terra.lib.yaml.snakeyaml.nodes.Tag;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvScalarConstructor extends Constructor {
   public static final Tag ENV_TAG = new Tag("!ENV");
   public static final Pattern ENV_FORMAT = Pattern.compile("^\\$\\{\\s*((?<name>\\w+)((?<separator>:?(-|\\?))(?<value>\\S+)?)?)\\s*\\}$");

   public EnvScalarConstructor() {
      super(new LoaderOptions());
      this.yamlConstructors.put(ENV_TAG, new EnvScalarConstructor.ConstructEnv());
   }

   public EnvScalarConstructor(TypeDescription theRoot, Collection<TypeDescription> moreTDs, LoaderOptions loadingConfig) {
      super(theRoot, moreTDs, loadingConfig);
      this.yamlConstructors.put(ENV_TAG, new EnvScalarConstructor.ConstructEnv());
   }

   public String apply(String name, String separator, String value, String environment) {
      if (environment != null && !environment.isEmpty()) {
         return environment;
      }

      if (separator != null) {
         if (separator.equals("?") && environment == null) {
            throw new MissingEnvironmentVariableException("Missing mandatory variable " + name + ": " + value);
         }

         if (separator.equals(":?")) {
            if (environment == null) {
               throw new MissingEnvironmentVariableException("Missing mandatory variable " + name + ": " + value);
            }

            if (environment.isEmpty()) {
               throw new MissingEnvironmentVariableException("Empty mandatory variable " + name + ": " + value);
            }
         }

         if (separator.startsWith(":")) {
            if (environment == null || environment.isEmpty()) {
               return value;
            }
         } else if (environment == null) {
            return value;
         }
      }

      return "";
   }

   public String getEnv(String key) {
      return System.getenv(key);
   }

   private class ConstructEnv extends AbstractConstruct {
      private ConstructEnv() {
      }

      @Override
      public Object construct(Node node) {
         String val = EnvScalarConstructor.this.constructScalar((ScalarNode)node);
         Matcher matcher = EnvScalarConstructor.ENV_FORMAT.matcher(val);
         matcher.matches();
         String name = matcher.group("name");
         String value = matcher.group("value");
         String separator = matcher.group("separator");
         return EnvScalarConstructor.this.apply(name, separator, value != null ? value : "", EnvScalarConstructor.this.getEnv(name));
      }
   }
}
