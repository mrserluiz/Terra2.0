package com.dfsek.terra.lib.commons.io.build;

import com.dfsek.terra.lib.commons.io.Charsets;
import com.dfsek.terra.lib.commons.io.file.PathUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

public abstract class AbstractStreamBuilder<T, B extends AbstractStreamBuilder<T, B>> extends AbstractOriginSupplier<T, B> {
   private static final int DEFAULT_MAX_VALUE = Integer.MAX_VALUE;
   private static final OpenOption[] DEFAULT_OPEN_OPTIONS = PathUtils.EMPTY_OPEN_OPTION_ARRAY;
   private int bufferSize = 8192;
   private int bufferSizeDefault = 8192;
   private int bufferSizeMax = Integer.MAX_VALUE;
   private Charset charset = Charset.defaultCharset();
   private Charset charsetDefault = Charset.defaultCharset();
   private OpenOption[] openOptions = DEFAULT_OPEN_OPTIONS;
   private final IntUnaryOperator defaultSizeChecker = size -> size > this.bufferSizeMax ? this.throwIae(size, this.bufferSizeMax) : size;
   private IntUnaryOperator bufferSizeChecker = this.defaultSizeChecker;

   private int checkBufferSize(int size) {
      return this.bufferSizeChecker.applyAsInt(size);
   }

   public int getBufferSize() {
      return this.bufferSize;
   }

   public int getBufferSizeDefault() {
      return this.bufferSizeDefault;
   }

   public CharSequence getCharSequence() throws IOException {
      return this.checkOrigin().getCharSequence(this.getCharset());
   }

   public Charset getCharset() {
      return this.charset;
   }

   public Charset getCharsetDefault() {
      return this.charsetDefault;
   }

   public File getFile() {
      return this.checkOrigin().getFile();
   }

   public InputStream getInputStream() throws IOException {
      return this.checkOrigin().getInputStream(this.getOpenOptions());
   }

   public OpenOption[] getOpenOptions() {
      return this.openOptions;
   }

   public OutputStream getOutputStream() throws IOException {
      return this.checkOrigin().getOutputStream(this.getOpenOptions());
   }

   public Path getPath() {
      return this.checkOrigin().getPath();
   }

   public RandomAccessFile getRandomAccessFile() throws IOException {
      return this.checkOrigin().getRandomAccessFile(this.getOpenOptions());
   }

   public Reader getReader() throws IOException {
      return this.checkOrigin().getReader(this.getCharset());
   }

   public Writer getWriter() throws IOException {
      return this.checkOrigin().getWriter(this.getCharset(), this.getOpenOptions());
   }

   public B setBufferSize(int bufferSize) {
      this.bufferSize = this.checkBufferSize(bufferSize > 0 ? bufferSize : this.bufferSizeDefault);
      return this.asThis();
   }

   public B setBufferSize(Integer bufferSize) {
      this.setBufferSize(bufferSize != null ? bufferSize : this.bufferSizeDefault);
      return this.asThis();
   }

   public B setBufferSizeChecker(IntUnaryOperator bufferSizeChecker) {
      this.bufferSizeChecker = bufferSizeChecker != null ? bufferSizeChecker : this.defaultSizeChecker;
      return this.asThis();
   }

   protected B setBufferSizeDefault(int bufferSizeDefault) {
      this.bufferSizeDefault = bufferSizeDefault;
      return this.asThis();
   }

   public B setBufferSizeMax(int bufferSizeMax) {
      this.bufferSizeMax = bufferSizeMax > 0 ? bufferSizeMax : Integer.MAX_VALUE;
      return this.asThis();
   }

   public B setCharset(Charset charset) {
      this.charset = Charsets.toCharset(charset, this.charsetDefault);
      return this.asThis();
   }

   public B setCharset(String charset) {
      return this.setCharset(Charsets.toCharset(charset, this.charsetDefault));
   }

   protected B setCharsetDefault(Charset defaultCharset) {
      this.charsetDefault = defaultCharset;
      return this.asThis();
   }

   public B setOpenOptions(OpenOption... openOptions) {
      this.openOptions = openOptions != null ? openOptions : DEFAULT_OPEN_OPTIONS;
      return this.asThis();
   }

   private int throwIae(int size, int max) {
      throw new IllegalArgumentException(String.format("Request %,d exceeds maximum %,d", size, max));
   }
}
