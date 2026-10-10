package com.dfsek.terra.bukkit.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Localized command UI; pack IDs, command literals and diagnostic causes are never translated. */
public final class CommandMessages {
    public static final List<String> LANGUAGES = List.of("en_US", "es_ES", "id_ID", "it_IT", "fr_FR", "de_DE", "pt_BR",
        "ru_RU", "pl_PL", "vi_VN", "tr_TR", "zh_CN", "ja_JP");
    private static final Map<String, Map<String, String>> CACHE = new ConcurrentHashMap<>();
    private CommandMessages() {}
    public static String text(String language, String key, Object... values) {
        String normalized = language == null ? "pt_BR" : language.replace('-', '_');
        String locale = LANGUAGES.stream().filter(code -> code.equalsIgnoreCase(normalized)).findFirst().orElse("pt_BR");
        String message = CACHE.computeIfAbsent(locale, CommandMessages::load).get(key);
        if(message == null) message = CACHE.computeIfAbsent("en_US", CommandMessages::load).get(key);
        if(message == null) throw new IllegalArgumentException("Missing command translation: " + key);
        // Single-pass substitution: IDs containing braces cannot inject another placeholder.
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < message.length();) {
            boolean replaced = false;
            if(message.charAt(i) == '{') {
                for(int index = 0; index < values.length; index++) {
                    String token = "{" + index + "}";
                    if(message.startsWith(token, i)) {
                        result.append(values[index]); i += token.length(); replaced = true; break;
                    }
                }
            }
            if(!replaced) result.append(message.charAt(i++));
        }
        return result.toString();
    }
    private static Map<String, String> load(String locale) {
        try(var input = CommandMessages.class.getResourceAsStream("/lang/" + locale + ".json")) {
            if(input == null) throw new IllegalStateException("Missing language resource: " + locale);
            try(var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                return Map.copyOf(new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType()));
            }
        } catch(java.io.IOException error) { throw new IllegalStateException(error); }
    }
}
