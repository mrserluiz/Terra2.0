package com.dfsek.terra.lib.commons.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class CloseableURLConnection extends URLConnection implements AutoCloseable {
   private final URLConnection urlConnection;

   static CloseableURLConnection open(URI uri) throws IOException {
      return open(Objects.requireNonNull(uri, "uri").toURL());
   }

   static CloseableURLConnection open(URL url) throws IOException {
      return new CloseableURLConnection(url.openConnection());
   }

   CloseableURLConnection(URLConnection urlConnection) {
      super(Objects.requireNonNull(urlConnection, "urlConnection").getURL());
      this.urlConnection = urlConnection;
   }

   @Override
   public void addRequestProperty(String key, String value) {
      this.urlConnection.addRequestProperty(key, value);
   }

   @Override
   public void close() {
      IOUtils.close(this.urlConnection);
   }

   @Override
   public void connect() throws IOException {
      this.urlConnection.connect();
   }

   @Override
   public boolean equals(Object obj) {
      return this.urlConnection.equals(obj);
   }

   @Override
   public boolean getAllowUserInteraction() {
      return this.urlConnection.getAllowUserInteraction();
   }

   @Override
   public int getConnectTimeout() {
      return this.urlConnection.getConnectTimeout();
   }

   @Override
   public Object getContent() throws IOException {
      return this.urlConnection.getContent();
   }

   @Override
   public Object getContent(Class[] classes) throws IOException {
      return this.urlConnection.getContent(classes);
   }

   @Override
   public String getContentEncoding() {
      return this.urlConnection.getContentEncoding();
   }

   @Override
   public int getContentLength() {
      return this.urlConnection.getContentLength();
   }

   @Override
   public long getContentLengthLong() {
      return this.urlConnection.getContentLengthLong();
   }

   @Override
   public String getContentType() {
      return this.urlConnection.getContentType();
   }

   @Override
   public long getDate() {
      return this.urlConnection.getDate();
   }

   @Override
   public boolean getDefaultUseCaches() {
      return this.urlConnection.getDefaultUseCaches();
   }

   @Override
   public boolean getDoInput() {
      return this.urlConnection.getDoInput();
   }

   @Override
   public boolean getDoOutput() {
      return this.urlConnection.getDoOutput();
   }

   @Override
   public long getExpiration() {
      return this.urlConnection.getExpiration();
   }

   @Override
   public String getHeaderField(int n) {
      return this.urlConnection.getHeaderField(n);
   }

   @Override
   public String getHeaderField(String name) {
      return this.urlConnection.getHeaderField(name);
   }

   @Override
   public long getHeaderFieldDate(String name, long Default) {
      return this.urlConnection.getHeaderFieldDate(name, Default);
   }

   @Override
   public int getHeaderFieldInt(String name, int Default) {
      return this.urlConnection.getHeaderFieldInt(name, Default);
   }

   @Override
   public String getHeaderFieldKey(int n) {
      return this.urlConnection.getHeaderFieldKey(n);
   }

   @Override
   public long getHeaderFieldLong(String name, long Default) {
      return this.urlConnection.getHeaderFieldLong(name, Default);
   }

   @Override
   public Map<String, List<String>> getHeaderFields() {
      return this.urlConnection.getHeaderFields();
   }

   @Override
   public long getIfModifiedSince() {
      return this.urlConnection.getIfModifiedSince();
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return this.urlConnection.getInputStream();
   }

   @Override
   public long getLastModified() {
      return this.urlConnection.getLastModified();
   }

   @Override
   public OutputStream getOutputStream() throws IOException {
      return this.urlConnection.getOutputStream();
   }

   @Override
   public Permission getPermission() throws IOException {
      return this.urlConnection.getPermission();
   }

   @Override
   public int getReadTimeout() {
      return this.urlConnection.getReadTimeout();
   }

   @Override
   public Map<String, List<String>> getRequestProperties() {
      return this.urlConnection.getRequestProperties();
   }

   @Override
   public String getRequestProperty(String key) {
      return this.urlConnection.getRequestProperty(key);
   }

   @Override
   public URL getURL() {
      return this.urlConnection.getURL();
   }

   @Override
   public boolean getUseCaches() {
      return this.urlConnection.getUseCaches();
   }

   @Override
   public int hashCode() {
      return this.urlConnection.hashCode();
   }

   @Override
   public void setAllowUserInteraction(boolean allowUserInteraction) {
      this.urlConnection.setAllowUserInteraction(allowUserInteraction);
   }

   @Override
   public void setConnectTimeout(int timeout) {
      this.urlConnection.setConnectTimeout(timeout);
   }

   @Override
   public void setDefaultUseCaches(boolean defaultUseCaches) {
      this.urlConnection.setDefaultUseCaches(defaultUseCaches);
   }

   @Override
   public void setDoInput(boolean doInput) {
      this.urlConnection.setDoInput(doInput);
   }

   @Override
   public void setDoOutput(boolean doOutput) {
      this.urlConnection.setDoOutput(doOutput);
   }

   @Override
   public void setIfModifiedSince(long ifModifiedSince) {
      this.urlConnection.setIfModifiedSince(ifModifiedSince);
   }

   @Override
   public void setReadTimeout(int timeout) {
      this.urlConnection.setReadTimeout(timeout);
   }

   @Override
   public void setRequestProperty(String key, String value) {
      this.urlConnection.setRequestProperty(key, value);
   }

   @Override
   public void setUseCaches(boolean useCaches) {
      this.urlConnection.setUseCaches(useCaches);
   }

   @Override
   public String toString() {
      return this.urlConnection.toString();
   }
}
