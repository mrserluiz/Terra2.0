package org.incendo.cloud.parser;

public interface ParserContributor {
   <C> void contribute(ParserRegistry<C> registry);
}
