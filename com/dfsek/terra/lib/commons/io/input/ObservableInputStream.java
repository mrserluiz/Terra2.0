package com.dfsek.terra.lib.commons.io.input;

import com.dfsek.terra.lib.commons.io.IOUtils;
import com.dfsek.terra.lib.commons.io.function.IOConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObservableInputStream extends ProxyInputStream {
   private final List<ObservableInputStream.Observer> observers;

   ObservableInputStream(ObservableInputStream.AbstractBuilder builder) throws IOException {
      super(builder);
      this.observers = builder.observers;
   }

   public ObservableInputStream(InputStream inputStream) {
      this(inputStream, new ArrayList<>());
   }

   private ObservableInputStream(InputStream inputStream, List<ObservableInputStream.Observer> observers) {
      super(inputStream);
      this.observers = observers;
   }

   public ObservableInputStream(InputStream inputStream, ObservableInputStream.Observer... observers) {
      this(inputStream, Arrays.asList(observers));
   }

   public void add(ObservableInputStream.Observer observer) {
      this.observers.add(observer);
   }

   @Override
   public void close() throws IOException {
      IOException ioe = null;

      try {
         super.close();
      } catch (IOException e) {
         ioe = e;
      }

      if (ioe == null) {
         this.noteClosed();
      } else {
         this.noteError(ioe);
      }
   }

   public void consume() throws IOException {
      IOUtils.consume(this);
   }

   private void forEachObserver(IOConsumer<ObservableInputStream.Observer> action) throws IOException {
      IOConsumer.forAll(action, this.observers);
   }

   public List<ObservableInputStream.Observer> getObservers() {
      return new ArrayList<>(this.observers);
   }

   protected void noteClosed() throws IOException {
      this.forEachObserver(ObservableInputStream.Observer::closed);
   }

   protected void noteDataByte(int value) throws IOException {
      this.forEachObserver(observer -> observer.data(value));
   }

   protected void noteDataBytes(byte[] buffer, int offset, int length) throws IOException {
      this.forEachObserver(observer -> observer.data(buffer, offset, length));
   }

   protected void noteError(IOException exception) throws IOException {
      this.forEachObserver(observer -> observer.error(exception));
   }

   protected void noteFinished() throws IOException {
      this.forEachObserver(ObservableInputStream.Observer::finished);
   }

   private void notify(byte[] buffer, int offset, int result, IOException ioe) throws IOException {
      if (ioe != null) {
         this.noteError(ioe);
         throw ioe;
      }

      if (result == -1) {
         this.noteFinished();
      } else if (result > 0) {
         this.noteDataBytes(buffer, offset, result);
      }
   }

   @Override
   public int read() throws IOException {
      int result = 0;
      IOException ioe = null;

      try {
         result = super.read();
      } catch (IOException ex) {
         ioe = ex;
      }

      if (ioe != null) {
         this.noteError(ioe);
         throw ioe;
      }

      if (result == -1) {
         this.noteFinished();
      } else {
         this.noteDataByte(result);
      }

      return result;
   }

   @Override
   public int read(byte[] buffer) throws IOException {
      int result = 0;
      IOException ioe = null;

      try {
         result = super.read(buffer);
      } catch (IOException ex) {
         ioe = ex;
      }

      this.notify(buffer, 0, result, ioe);
      return result;
   }

   @Override
   public int read(byte[] buffer, int offset, int length) throws IOException {
      int result = 0;
      IOException ioe = null;

      try {
         result = super.read(buffer, offset, length);
      } catch (IOException ex) {
         ioe = ex;
      }

      this.notify(buffer, offset, result, ioe);
      return result;
   }

   public void remove(ObservableInputStream.Observer observer) {
      this.observers.remove(observer);
   }

   public void removeAllObservers() {
      this.observers.clear();
   }

   public abstract static class AbstractBuilder<T extends ObservableInputStream.AbstractBuilder<T>>
      extends ProxyInputStream.AbstractBuilder<ObservableInputStream, T> {
      private List<ObservableInputStream.Observer> observers;

      public void setObservers(List<ObservableInputStream.Observer> observers) {
         this.observers = observers;
      }
   }

   public static class Builder extends ObservableInputStream.AbstractBuilder<ObservableInputStream.Builder> {
      public ObservableInputStream get() throws IOException {
         return new ObservableInputStream(this);
      }
   }

   public abstract static class Observer {
      public void closed() throws IOException {
      }

      public void data(byte[] buffer, int offset, int length) throws IOException {
      }

      public void data(int value) throws IOException {
      }

      public void error(IOException exception) throws IOException {
         throw exception;
      }

      public void finished() throws IOException {
      }
   }
}
