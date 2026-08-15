package com.dfsek.terra.lib.google.common.hash;

import com.google.errorprone.annotations.Immutable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@Immutable
public interface HashFunction {
   Hasher newHasher();

   Hasher newHasher(int expectedInputSize);

   HashCode hashInt(int input);

   HashCode hashLong(long input);

   HashCode hashBytes(byte[] input);

   HashCode hashBytes(byte[] input, int off, int len);

   HashCode hashBytes(ByteBuffer input);

   HashCode hashUnencodedChars(CharSequence input);

   HashCode hashString(CharSequence input, Charset charset);

   <T> HashCode hashObject(@ParametricNullness T instance, Funnel<? super T> funnel);

   int bits();
}
