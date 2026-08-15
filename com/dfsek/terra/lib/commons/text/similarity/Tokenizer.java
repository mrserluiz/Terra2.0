package com.dfsek.terra.lib.commons.text.similarity;

import java.util.function.Function;

interface Tokenizer<T, R> extends Function<T, R[]> {
}
