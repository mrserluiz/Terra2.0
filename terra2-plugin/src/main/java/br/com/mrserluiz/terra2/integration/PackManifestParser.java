package br.com.mrserluiz.terra2.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/** Minimal reader for the stable top-level fields of Terra's pack.yml. */
final class PackManifestParser {
    PackDescriptor parse(Reader source, Path packSource) throws IOException {
        String id = null;
        String version = null;
        Set<String> addons = new LinkedHashSet<>();
        boolean inAddons = false;

        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String withoutComment = stripComment(line);
                if (withoutComment.isBlank()) continue;

                int indent = leadingSpaces(withoutComment);
                String trimmed = withoutComment.trim();
                if (indent == 0) {
                    inAddons = trimmed.equals("addons:");
                    if (trimmed.startsWith("id:")) id = scalar(trimmed.substring(3));
                    if (trimmed.startsWith("version:")) version = scalar(trimmed.substring(8));
                    continue;
                }

                if (inAddons && indent > 0 && trimmed.contains(":")) {
                    String addon = scalar(trimmed.substring(0, trimmed.indexOf(':')));
                    if (!addon.isBlank()) addons.add(addon);
                }
            }
        }

        return new PackDescriptor(id, version, addons, packSource);
    }

    private static String stripComment(String line) {
        boolean quoted = false;
        char quote = 0;
        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if ((current == '\'' || current == '"') && (i == 0 || line.charAt(i - 1) != '\\')) {
                if (!quoted) { quoted = true; quote = current; }
                else if (quote == current) quoted = false;
            }
            if (current == '#' && !quoted) return line.substring(0, i);
        }
        return line;
    }

    private static int leadingSpaces(String value) {
        int count = 0;
        while (count < value.length() && value.charAt(count) == ' ') count++;
        return count;
    }

    private static String scalar(String value) {
        String result = value.trim();
        if (result.length() >= 2) {
            char first = result.charAt(0);
            char last = result.charAt(result.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                result = result.substring(1, result.length() - 1);
            }
        }
        return result.trim();
    }
}
