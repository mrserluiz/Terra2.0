package net.fabricmc.mappingio.format.intellij;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import net.fabricmc.mappingio.MappedElementKind;
import net.fabricmc.mappingio.MappingFlag;
import net.fabricmc.mappingio.MappingWriter;
import org.jetbrains.annotations.Nullable;

public final class MigrationMapFileWriter implements MappingWriter {
   private static final Set<MappingFlag> flags = EnumSet.of(MappingFlag.NEEDS_ELEMENT_UNIQUENESS);
   private final Writer writer;
   private XMLStreamWriter xmlWriter;
   private boolean wroteName;
   private boolean wroteOrder;
   private String srcName;
   private String dstName;

   public MigrationMapFileWriter(Writer writer) {
      this.writer = writer;
   }

   @Override
   public void close() throws IOException {
      try {
         if (this.xmlWriter != null) {
            if (!this.wroteName) {
               this.xmlWriter.writeCharacters("\n\t");
               this.xmlWriter.writeEmptyElement("name");
               this.xmlWriter.writeAttribute("value", "Unnamed migration map");
            }

            if (!this.wroteOrder) {
               this.xmlWriter.writeCharacters("\n\t");
               this.xmlWriter.writeEmptyElement("order");
               this.xmlWriter.writeAttribute("value", "0");
            }

            this.xmlWriter.writeCharacters("\n");
            this.xmlWriter.writeEndDocument();
            this.xmlWriter.writeCharacters("\n");
            this.xmlWriter.close();
         }
      } catch (XMLStreamException e) {
         throw new IOException(e);
      } finally {
         this.writer.close();
      }
   }

   @Override
   public Set<MappingFlag> getFlags() {
      return flags;
   }

   @Override
   public boolean visitHeader() throws IOException {
      assert this.xmlWriter == null;

      try {
         this.xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(this.writer);
         this.xmlWriter.writeStartDocument("UTF-8", "1.0");
         this.xmlWriter.writeCharacters("\n");
         this.xmlWriter.writeStartElement("migrationMap");
         return true;
      } catch (FactoryConfigurationError | XMLStreamException e) {
         throw new IOException(e);
      }
   }

   @Override
   public void visitNamespaces(String srcNamespace, List<String> dstNamespaces) throws IOException {
   }

   @Override
   public void visitMetadata(String key, @Nullable String value) throws IOException {
      try {
         switch (key) {
            case "name":
               this.wroteName = true;
               break;
            case "migrationmap:order":
               this.wroteOrder = true;
               key = "order";
         }

         this.xmlWriter.writeCharacters("\n\t");
         this.xmlWriter.writeEmptyElement(key);
         this.xmlWriter.writeAttribute("value", value);
      } catch (XMLStreamException e) {
         throw new IOException(e);
      }
   }

   @Override
   public boolean visitClass(String srcName) throws IOException {
      this.srcName = srcName;
      this.dstName = null;
      return true;
   }

   @Override
   public boolean visitField(String srcName, @Nullable String srcDesc) throws IOException {
      return false;
   }

   @Override
   public boolean visitMethod(String srcName, @Nullable String srcDesc) throws IOException {
      return false;
   }

   @Override
   public boolean visitMethodArg(int argPosition, int lvIndex, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public boolean visitMethodVar(int lvtRowIndex, int lvIndex, int startOpIdx, int endOpIdx, @Nullable String srcName) throws IOException {
      return false;
   }

   @Override
   public void visitDstName(MappedElementKind targetKind, int namespace, String name) {
      if (namespace == 0) {
         this.dstName = name;
      }
   }

   @Override
   public boolean visitElementContent(MappedElementKind targetKind) throws IOException {
      if (this.dstName == null) {
         return false;
      }

      try {
         this.xmlWriter.writeCharacters("\n\t");
         this.xmlWriter.writeEmptyElement("entry");
         this.xmlWriter.writeAttribute("oldName", this.srcName.replace('/', '.'));
         this.xmlWriter.writeAttribute("newName", this.dstName.replace('/', '.'));
         this.xmlWriter.writeAttribute("type", "class");
         return false;
      } catch (XMLStreamException e) {
         throw new IOException(e);
      }
   }

   @Override
   public void visitComment(MappedElementKind targetKind, String comment) throws IOException {
   }
}
