package org.terra2.pack;

import java.util.*;

/** Server-specific validation is not evidence of successful world execution. */
public interface NativePackBackend {
    record Validation(String backend, String fingerprint, Map<String, Integer> decoded, List<String> errors) {
        public Validation { decoded = Map.copyOf(decoded); errors = List.copyOf(errors); }
        public boolean valid() { return errors.isEmpty(); }
    }
    Validation validate(ResourceBundle source);
}
