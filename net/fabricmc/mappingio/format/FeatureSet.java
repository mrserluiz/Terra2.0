package net.fabricmc.mappingio.format;

public interface FeatureSet {
   boolean hasNamespaces();

   FeatureSet.MetadataSupport fileMetadata();

   FeatureSet.MetadataSupport elementMetadata();

   FeatureSet.NameSupport packages();

   FeatureSet.ClassSupport classes();

   FeatureSet.MemberSupport fields();

   FeatureSet.MemberSupport methods();

   FeatureSet.LocalSupport args();

   FeatureSet.LocalSupport vars();

   FeatureSet.ElementCommentSupport elementComments();

   boolean hasFileComments();

   default boolean supportsPackages() {
      return this.packages().srcNames() != FeatureSet.FeaturePresence.ABSENT || this.packages().dstNames() != FeatureSet.FeaturePresence.ABSENT;
   }

   default boolean supportsClasses() {
      return this.classes().srcNames() != FeatureSet.FeaturePresence.ABSENT || this.classes().dstNames() != FeatureSet.FeaturePresence.ABSENT;
   }

   default boolean supportsFields() {
      return FeatureSetUtil.isSupported(this.fields());
   }

   default boolean supportsMethods() {
      return FeatureSetUtil.isSupported(this.methods());
   }

   default boolean supportsArgs() {
      return FeatureSetUtil.isSupported(this.args());
   }

   default boolean supportsVars() {
      return FeatureSetUtil.isSupported(this.vars());
   }

   interface ClassSupport extends FeatureSet.NameSupport {
      boolean hasRepackaging();
   }

   interface DescSupport {
      FeatureSet.FeaturePresence srcDescs();

      FeatureSet.FeaturePresence dstDescs();
   }

   enum ElementCommentSupport {
      NAMESPACED,
      SHARED,
      NONE;
   }

   enum FeaturePresence {
      REQUIRED,
      OPTIONAL,
      ABSENT;
   }

   interface LocalSupport extends FeatureSet.NameSupport, FeatureSet.DescSupport {
      FeatureSet.FeaturePresence positions();

      FeatureSet.FeaturePresence lvIndices();

      FeatureSet.FeaturePresence lvtRowIndices();

      FeatureSet.FeaturePresence startOpIndices();

      FeatureSet.FeaturePresence endOpIndices();
   }

   interface MemberSupport extends FeatureSet.NameSupport, FeatureSet.DescSupport {
   }

   enum MetadataSupport {
      NONE,
      FIXED,
      ARBITRARY;
   }

   interface NameSupport {
      FeatureSet.FeaturePresence srcNames();

      FeatureSet.FeaturePresence dstNames();
   }
}
